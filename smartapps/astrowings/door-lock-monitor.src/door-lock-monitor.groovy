/**
 *  Door Lock Monitor
 *
 *  Copyright © 2016 Phil Maynard
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0												*/
 	       private urlApache() { return "http://www.apache.org/licenses/LICENSE-2.0" }			/*
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *	VERSION HISTORY										*/
 	 private versionNum() {	return "version 2.00" }
     private versionDate() { return "15-Nov-2016" }		/*
 *
 *    v2.00 (15-Nov-2016) - bug fix: fix 'Notification Options' page not appearing due to incorrect paragraph definition format
 *                        - code improvement: store images on GitHub, use getAppImg() to display app images
 *                        - added option to disable icons
 *                        - added option to disable multi-level logging
 *                        - configured default values for app settings
 *						  - moved 'About' to its own page
 *						  - added link to readme file
 *						  - list current notification settings in link to notifications page
 *    v1.32 (04-Nov-2016) - update href state & images
 *	  v1.31 (03-Nov-2016) - add options for notification conditions
 *                        - add link for Apache license
 *    v1.20 (02-Nov-2016) - implement multi-level debug logging function
 *    v1.10 (01-Nov-2016) - code improvement: standardize pages layout
 *	  v1.03 (01-Nov-2016) - code improvement: standardize section headers
 *    v1.02 (26-Oct-2016) - code improvement: added trace for each event handler
 *    v1.01 (26-Oct-2016) - added 'About' section in preferences
 *    v1.00               - initial release, no version tracking up to this point
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

//TODO: make parent app or enable multiple doors
preferences {
	page(name: "pageMain")
    page(name: "pageNotify")
    page(name: "pageSettings")
    page(name: "pageLogOptions")
    page(name: "pageAbout")
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private		appImgPath()			{ return "https://raw.githubusercontent.com/astrowings/SmartThings/master/images/" }
private		readmeLink()			{ return "https://github.com/astrowings/SmartThings/blob/master/smartapps/astrowings/door-lock-monitor.src/readme.md" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    //TODO: add the option to enable based on presence
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp sends a push notification if a lock gets unlocked or if the mode changes while the lock is unlocked."
        }
        section("Monitor this door lock") {
            input "theLock", "capability.lock", required: true, title: "Which lock?"
            if (theLock) {
            	href "pageNotify", title: "Notification Options", description: notifyOptionsDesc, image: getAppImg("notify-icn.png"), required: true, state: (pushUnlock || pushMode) ? "complete" : null
            }
        }
		section() {
			if (theLock) {
            	href "pageSettings", title: "App settings", description: "", image: getAppImg("configure_icon.png"), required: false
            }
            href "pageAbout", title: "About", description: "", image: getAppImg("info-icn.png"), required: false
        }
    }
}

def pageNotify() {
	dynamicPage(name: "pageNotify", install: false, uninstall: false) {
        section() {
        	paragraph "Send a push notification when...", title: "Notification Options"
            input "pushUnlock", "bool", title: "Door gets unlocked", defaultValue: true, required: false, submitOnChange: true
            input "pushMode", "bool", title: "Door is left unlocked at mode change", defaultValue: true, required: false, submitOnChange: true //TODO: test if this works on change from Home to Night when app is set to work only during Away and Night
        }
    }
}

def pageSettings() {
	dynamicPage(name: "pageSettings", install: false, uninstall: false) {
   		section() {
			mode title: "Set for specific mode(s)"
			label title: "Assign a name", defaultValue: "${app.name}", required: false
            href "pageUninstall", title: "", description: "Uninstall this SmartApp", image: getAppImg("trash-circle-red-512.png"), state: null, required: true
		}
        section("Debugging Options", hideable: true, hidden: true) {
            input "noAppIcons", "bool", title: "Disable App Icons", description: "Do not display icons in the configuration pages", image: getAppImg("disable_icon.png"), defaultValue: false, required: false, submitOnChange: true
            href "pageLogOptions", title: "IDE Logging Options", description: "Adjust how logs are displayed in the SmartThings IDE", image: getAppImg("office8-icn.png"), required: true, state: "complete"
        }
    }
}

def pageAbout() {
	dynamicPage(name: "pageAbout", title: "About this SmartApp", install: false, uninstall: false) { //with 'install: false', clicking 'Done' goes back to previous page
		section() {
        	href url: readmeLink(), title: app.name, description: "Copyright ©2016 Phil Maynard\n${versionNum()}", image: getAppImg("readme-icn.png")
            href url: urlApache(), title: "License", description: "View Apache license", image: getAppImg("license-icn.png")
		}
    }
}

def pageLogOptions() {
	dynamicPage(name: "pageLogOptions", title: "IDE Logging Options", install: false, uninstall: false) {
        section() {
	        input "debugging", "bool", title: "Enable debugging", description: "Display the logs in the IDE", defaultValue: false, required: false, submitOnChange: true 
        }
        if (debugging) {
            section("Select log types to display") {
                input "log#info", "bool", title: "Log info messages", defaultValue: true, required: false 
                input "log#trace", "bool", title: "Log trace messages", defaultValue: true, required: false 
                input "log#debug", "bool", title: "Log debug messages", defaultValue: true, required: false 
                input "log#warn", "bool", title: "Log warning messages", defaultValue: true, required: false 
                input "log#error", "bool", title: "Log error messages", defaultValue: true, required: false 
			}
            section() {
                input "setMultiLevelLog", "bool", title: "Enable Multi-level Logging", defaultValue: true, required: false,
                    description: "Multi-level logging prefixes log entries with special characters to visually " +
                        "represent the hierarchy of events and facilitate the interpretation of logs in the IDE"
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


//   ---------------------------------
//   ***   PAGES SUPPORT METHODS   ***

def getNotifyOptionsDesc() {
    def strDesc = ""
    strDesc += (!pushUnlock && !pushMode)	? "Notifications are not set" : "You will receive a notification when...\n"
    strDesc += pushUnlock					? " • the door gets unlocked\n" : ""
    strDesc += pushMode						? " • the door is left unlocked at mode change" : ""
    return strDesc
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
    def warnUnlock = evt.descriptionText
	debug "warnUnlock : $warnUnlock", "warn"
	if (pushUnlock) {
    	sendPush(warnUnlock)
    }
    debug "unlockHandler complete", "trace", -1
}

def modeChangeHandler(evt) {
    debug "modeChangeHandler event: ${evt.descriptionText}", "trace", 1
    if (theLock.currentLock == "unlocked") {
    	def warnMode = "The mode changed to $location.currentMode and the $theLock.label is $theLock.currentLock"
        debug "warnMode : $warnMode", "warn"
        if (pushMode) {
        	sendPush(warnMode)
        }
    }
    debug "modeChangeHandler complete", "trace", -1
}

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------
//   ***   METHODS   ***



//   -------------------------
//   ***   APP FUNCTIONS   ***



//   ------------------------
//   ***   COMMON UTILS   ***

def getAppImg(imgName, forceIcon = null) {
	def imgPath = appImgPath()
    return (!noAppIcons || forceIcon) ? "$imgPath/$imgName" : ""
}

def debug(message, lvl = null, shift = null, err = null) {
	
    def debugging = settings.debugging
	if (!debugging) {
		return
	}
    
    lvl = lvl ?: "debug"
	if (!settings["log#$lvl"]) {
		return
	}
	
    def multiEnable = (settings.setMultiLevelLog == false ? false : true) //set to true by default
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

	if (multiEnable) {
		prefix += " "
	} else {
		prefix = ""
	}

    if (lvl == "info") {
    	def leftPad = (multiEnable ? ": :" : "")
        log.info "$leftPad$prefix$message", err
	} else if (lvl == "trace") {
    	def leftPad = (multiEnable ? "::" : "")
        log.trace "$leftPad$prefix$message", err
	} else if (lvl == "warn") {
    	def leftPad = (multiEnable ? "::" : "")
		log.warn "$leftPad$prefix$message", err
	} else if (lvl == "error") {
    	def leftPad = (multiEnable ? "::" : "")
		log.error "$leftPad$prefix$message", err
	} else {
		log.debug "$prefix$message", err
	}
}