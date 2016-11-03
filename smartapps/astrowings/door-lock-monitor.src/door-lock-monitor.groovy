/**
 *  Door Lock Monitor
 *
 *  Copyright © 2016 Phil Maynard
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *	VERSION HISTORY                                    */
 	 def versionNum() {	return "version 1.20" }       /*
 
 *   v1.20 (02-Nov-2016): implement multi-level debug logging function
 *   v1.10 (01-Nov-2016): standardize pages layout
 *	 v1.03 (01-Nov-2016): standardize section headers
 *   v1.02 (26-Oct-2016): added trace for each event handler
 *   v1.01 (26-Oct-2016): added 'About' section in preferences
 *   v1.00 (2016 date unknown): working version, no version tracking up to this point
 *
*/
definition(
    name: "Door Lock Monitor",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Monitor a door lock and report anomalies",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home3-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

//TODO: make parent app
preferences {
	page(name: "pageMain")
    page(name: "pageSettings")
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***



//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    //TODO: add the option to enable based on presence
	//TODO: add options for notification conditions
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp sends a push notification if a lock gets unlocked or if the mode changes while the lock is unlocked."
        }
        section("Monitor this door lock") {
            input "theLock", "capability.lock", required: true, title: "Which lock?"
        }
		section() {
			if (theLock) {
            	href "pageSettings", title: "App settings", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", required: false
            }
		}
    }
}

def pageSettings() {
	dynamicPage(name: "pageSettings", install: false, uninstall: false) {
		section("About") {
        	paragraph "Copyright ©2016 Phil Maynard\n${versionNum()}", title: app.name
            //TODO: link to license
		}
   		section() {
			mode title: "Set for specific mode(s)"
            label title: "Assign a name", defaultValue: "${app.name} - ${theLock.label}", required: false
            href "pageUninstall", title: "Uninstall", description: "Uninstall this SmartApp", state: null, required: true
		}
        section("Debugging Options", hideable: true, hidden: true) {
            input "debugging", "bool", title: "Enable debugging", defaultValue: false, required: false, submitOnChange: true
            if (debugging) {
                input "log#info", "bool", title: "Log info messages", defaultValue: true, required: false
                input "log#trace", "bool", title: "Log trace messages", defaultValue: true, required: false
                input "log#debug", "bool", title: "Log debug messages", defaultValue: true, required: false
                input "log#warn", "bool", title: "Log warning messages", defaultValue: true, required: false
                input "log#error", "bool", title: "Log error messages", defaultValue: true, required: false
            }
        }
    }
}

def pageUninstall() {
	dynamicPage(name: "pageUninstall", title: "Uninstall", install: false, uninstall: true) {
		section() {
        	paragraph parent ? "CAUTION: You are about to disable monitoring of the '${theLock.label}'. This action is irreversible. If you want to proceed, tap on the 'Remove' button below." : "CAUTION: You are about to completely remove the SmartApp '${app.name}'. This action is irreversible. If you want to proceed, tap on the 'Remove' button below.",
                required: true, state: null
        }
	}
}


//   ----------------------------
//   ***   APP INSTALLATION   ***

def installed() {
	debug "installed with settings: ${settings}", "trace"
    initialize()
}

def updated() {
    debug "updated with settings ${settings}", "trace"
	unsubscribe()
    //unschedule()
    initialize()
}

def uninstalled() {
    state.debugLevel = 0
    debug "application uninstalled", "trace"
}

def initialize() {
    state.debugLevel = 0
    debug "initializing", "trace", 1
    subscribeToEvents()
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    subscribe(theLock, "lock.unlocked", unlockHandler)
    subscribe(location, modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if the hub location changes
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def unlockHandler(evt) {
    debug "unlockHandler event: ${evt.descriptionText}", "trace", 1
    def unlockText = evt.descriptionText
	debug "sendPush : $unlockText", "warn"
	sendPush(unlockText)
    debug "unlockHandler complete", "trace", -1
}

def modeChangeHandler(evt) {
    debug "modeChangeHandler event: ${evt.descriptionText}", "trace", 1
    if (theLock.currentLock == "unlocked") {
    	def unlockedMsg = "The mode changed to $location.currentMode and the $theLock.label is $theLock.currentLock"
        debug "sendPush : $unlockedMsg", "warn"
        sendPush(unlockedMsg)
    }
    debug "modeChangeHandler complete", "trace", -1
}

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------------
//   ***   APP FUNCTIONS   ***



//   ------------------------
//   ***   COMMON UTILS   ***

def debug(message, lvl = null, shift = null, err = null) {
	def debugging = settings.debugging
	if (!debugging) {
		return
	}
	lvl = lvl ?: "debug"
	if (!settings["log#$lvl"]) {
		return
	}
	
    def maxLevel = 4
	def level = state.debugLevel ?: 0
	def levelDelta = 0
	def prefix = "║"
	def pad = "░"
	
    //shift is:
	//	 0 - initialize level, level set to 1
	//	 1 - start of routine, level up
	//	-1 - end of routine, level down
	//	 anything else - nothing happens
	
    switch (shift) {
		case 0:
			level = 0
			prefix = ""
			break
		case 1:
			level += 1
			prefix = "╚"
			pad = "═"
			break
		case -1:
			levelDelta = -(level > 0 ? 1 : 0)
			pad = "═"
			prefix = "╔"
			break
	}

	if (level > 0) {
		prefix = prefix.padLeft(level, "║").padRight(maxLevel, pad)
	}

	level += levelDelta
	state.debugLevel = level

	if (debugging) {
		prefix += " "
	} else {
		prefix = ""
	}

    if (lvl == "info") {
        log.info ": :$prefix$message", err
	} else if (lvl == "trace") {
        log.trace "::$prefix$message", err
	} else if (lvl == "warn") {
		log.warn "::$prefix$message", err
	} else if (lvl == "error") {
		log.error "::$prefix$message", err
	} else {
		log.debug "$prefix$message", err
	}
}