/**
 *  Light up front door
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
    name: "Light-up front door",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn on a light when someone arrives home and it's dark out.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

//TODO: make parent
preferences {
	page(name: "pageMain")
    page(name: "pageSettings")
    page(name: "pageUninstall")
}
    

//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private C_1() { return "this is constant1" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp turns on a light when someone arrives home and it's dark out. (e.g. to turn on a porch light)"
        }
        section("When any of these people arrive") {
            input "people", "capability.presenceSensor", title: "Who?", multiple: true, required: true
        }
        section("Turn on this light") {
            input "theLight", "capability.switch", title: "Which light?", required: true
        }
        section("And leave it on for...") {
            input "leaveOn", "number", title: "How long? (minutes)", required: true //TODO: restrict allowable range
        }
		section() {
            href "pageSettings", title: "App settings", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", required: false
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
			label title: "Assign a name", defaultValue: "${app.name}", required: false
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
        	paragraph "CAUTION: You are about to completely remove the SmartApp '${app.name}'. This action is irreversible. If you want to proceed, tap on the 'Remove' button below.",
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
    unschedule()
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
	subscribe(people, "presence.present", presenceHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    //TODO: subscribe to lights on/off events IF commanded by this app (and log events)
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def presenceHandler(evt) {
    debug "presenceHandler event: ${evt.descriptionText}", "trace", 1
    debug "$evt.displayName has arrived", "info"

	if (itsDarkOut && (theLight.currentSwitch != "on")) {
        debug "call to turn on the $theLight.displayName"
        turnOn()
    } else {
        debug "conditions not met; do nothing"
    }
    debug "presenceHandler complete", "trace", -1
}

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def turnOn() {
    debug "executing turnOn()", "trace", 1
    theLight.on()
    debug "scheduling $theLight.displayName to turn off in $leaveOn minutes", "info"
    runIn(60 * leaveOn, turnOff)
    debug "turnOn() complete", "trace", -1
}

def turnOff() {
    debug "executing turnOff()", "trace", 1
    theLight.off()
    debug "turnOff() complete", "trace", -1
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getItsDarkOut() {
    def sunTime = getSunriseAndSunset(sunsetOffset: 15) //TODO: convert '15' to variable (user setting) or  constant
    def nowDate = new Date()
    def result = false
    def desc = ""
	
    if(sunTime.sunrise < nowDate && sunTime.sunset > nowDate){
    	desc = "it's daytime"
        result = false
    } else {
    	desc = "it's nighttime"
        result = true
    }
    debug ">> itsDarkOut : $result ($desc)"
    return result
}


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