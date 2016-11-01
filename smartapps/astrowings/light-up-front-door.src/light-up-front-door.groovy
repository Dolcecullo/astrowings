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
 	 def versionNum() {	return "version 1.10" }       /*
 
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
	log.info "installed with settings: $settings"
    initialize()
}

def updated() {
    log.info "updated with settings $settings"
	unsubscribe()
    unschedule()
    initialize()
}

def uninstalled() {
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    state.debugLevel = 0
	subscribe(people, "presence.present", presenceHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def presenceHandler(evt) {
    log.trace "presenceHandler>${evt.descriptionText}"
    log.debug "$evt.displayName has arrived"
    //turn on the light only if it's currently off
    if (itsDarkOut && (theLight.currentSwitch != "on")) {
        log.debug "do turn on $theLight.displayName"
        turnOn()
    } else {
        log.debug "do nothing"
    }
}

def locationPositionChange(evt) {
	log.trace "locationPositionChange>${evt.descriptionText}"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def turnOn() {
    log.debug "turning on $theLight.displayName"
    theLight.on()
    log.debug "scheduling $theLight.displayName to turn off in $leaveOn minutes"
    runIn(60 * leaveOn, turnOff)
}

def turnOff() {
    log.debug "turning off $theLight.displayName"
    theLight.off()
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getItsDarkOut() {
    def sunTime = getSunriseAndSunset(sunsetOffset: 15)
    def currentDTG = new Date()
    def result = false
	//log.debug "sunTime.sunrise: $sunTime.sunrise"
	//log.debug "sunTime.sunset: $sunTime.sunset (with 15 min offset)"
    //log.debug "unx_sunTime.sunset: ${sunTime.sunset.time}"
    //log.debug "dat_unx_sunTime.sunset: ${new Date(sunTime.sunset.time)}"

	if(sunTime.sunrise < currentDTG && sunTime.sunset > currentDTG){
    	log.debug "it's daytime"
        result = false
    } else {
    	log.debug "it's nighttime"
        result = true
    }
    return result
}


//   ------------------------
//   ***   COMMON UTILS   ***

def debug(message, shift = null, lvl = null, err = null) {
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
		log.info "◦◦$prefix$message", err
	} else if (lvl == "trace") {
		log.trace "◦$prefix$message", err
	} else if (lvl == "warn") {
		log.warn "◦$prefix$message", err
	} else if (lvl == "error") {
		log.error "◦$prefix$message", err
	} else {
		log.debug "$prefix$message", err
	}
}