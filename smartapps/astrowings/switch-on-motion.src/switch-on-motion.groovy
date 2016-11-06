/**
 *  Switch on Motion
 *
 *  Copyright © 2016 Phil Maynard
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0                                       */
 	       def urlApache() { return "http://www.apache.org/licenses/LICENSE-2.0" }      /*
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *	VERSION HISTORY                                    */
 	 def versionNum() {	return "version 1.22" }       /*
 *
 *    v1.23 (06-Nov-2016): added parent definition
 *    v1.22 (04-Nov-2016): update href state & images
 *    v1.21 (02-Nov-2016): add link for Apache license
 *    v1.20 (02-Nov-2016): implement multi-level debug logging function
 *    v1.10 (01-Nov-2016): standardize pages layout
 *	  v1.03 (01-Nov-2016): standardize section headers
 *    v1.02 (26-Oct-2016): added trace for each event handler
 *    v1.01 (26-Oct-2016): added 'About' section in preferences
 *    v1.00 (2016 date unknown): working version, no version tracking up to this point
 *
*/
definition(
    parent: "astrowings:Switches on Motion",
    name: "Switch on Motion",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn light on/off based on motion",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home30-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
    page(name: "pageSettings")
    //next page not required for child app
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***



//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp turns a light on/off based on detected motion"
        }
        section("When motion is detected on this sensor:") {
            input "theMotion", "capability.motionSensor", required: true, title: "Where?"
        }
        section("Turn on this light:") {
            input "theSwitch", "capability.switch", required: true, title: "Which light?"
        }
    	section("Enable only when it's dark out") {
        	input "whenDark", "bool", title: "Yes/No?", required: false, defaultValue: true, submitOnChange: true
        }
        /*
        if (whenDark) {
            section("This SmartApp uses luminance as a criteria to trigger actions; select the illuminance-capable " +
                    "device to use (if none selected, sunset/sunrise times will be used instead.",
                    hideWhenEmpty: true, required: true, state: (theLuminance ? "complete" : null)) {
                //TODO: test using virtual luminance device based on sunrise/sunset
                //TODO: enable use of device everywhere there's a reference to darkness setting (i.e. sunset/sunrise)
                input "theLuminance", "capability.illuminance", title: "Which illuminance device?", multiple: false, required: false, submitOnChange: true
            }
        }
        */
        section("Turn the light off when there's been no movement for this long:") {
            input "minutes", "number", required: true, title: "How long? (minutes)"
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
            href name: "hrefLicense", title: "License", description: "Apache License", url: urlApache()
		}
        if (!theLuminance) {
            section("This SmartApp uses the sunset/sunrise time to evaluate luminance as a criteria to trigger actions. " +
                    "If required, you can adjust the amount time before/after sunset when the app considers that it's dark outside " +
                    "(e.g. use '-20' to adjust the sunset time 20 minutes earlier than actual).") {
                input "sunsetOffset", "number", title: "Sunset offset time", description: "How many minutes (+/- 60)?", range: "-60..60", required: false
            }
   		}
   		section() {
			label title: "Assign a name", defaultValue: "${app.name}", required: false
            //next line not required for child app
            //href "pageUninstall", title: "", description: "Uninstall this SmartApp", image: "https://cdn0.iconfinder.com/data/icons/social-messaging-ui-color-shapes/128/trash-circle-red-512.png", state: null, required: true
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
	turnOff()
    state.debugLevel = 0
    debug "application uninstalled", "trace"
}

def initialize() {
    state.debugLevel = 0
    debug "initializing", "trace", 1
    subscribeToEvents()
    state.enable = true
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    subscribe(theMotion, "motion.active", motionDetectedHandler)
    subscribe(theMotion, "motion.inactive", motionStoppedHandler)
    subscribe(theSwitch, "switch.on", switchOnHandler)
    subscribe(theSwitch, "switch.off", switchOffHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    //TODO: subscribe to lights on/off events IF commanded by this app (and log events)
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def motionDetectedHandler(evt) {
    debug "motionDetectedHandler event: ${evt.descriptionText}", "trace", 1
    if (allOk) {
        theSwitch.on()
    }
    def testShare = parent.testShare()
    debug "1: ${testShare.key1}"
    debug "motionDetectedHandler complete", "trace", -1
}

def motionStoppedHandler(evt) {
    debug "motionStoppedHandler event: ${evt.descriptionText}", "trace", 1
    runIn(minutes * 60, turnOff)
    debug "motionStoppedHandler complete", "trace", -1
}

def switchOnHandler(evt) {
    debug "switchOnHandler event: ${evt.descriptionText}", "trace", 1
    debug "switch turned on; enabling automation", "info"
    state.enable = true
    debug "switchOnHandler complete", "trace", -1
}

def switchOffHandler(evt) {
    debug "switchOffHandler event: ${evt.descriptionText}", "trace", 1
    if (evt.isPhysical()) {
    	//disable automation when switch is activated manually (i.e. prevent light from turning back on when movement is detected)
        debug "switch physically turned off; disabling automation", "info"
        state.enable = false
    } else {
    	debug "switch turned off by app", "info"
    }
    debug "switchOffHandler complete", "trace", -1
}

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def turnOff() {
    debug "executing turnOff()", "trace", 1
   	theSwitch.off()
    debug "turnOff() complete", "trace", -1
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getAllOk() {
	def result = darkOk && state.enable == true
    debug ">> allOk : $result"
    return result
}

def getDarkOk() {
	def result = !whenDark || itsDarkOut
	debug ">> darkOk : $result"
	return result
}


//   ------------------------
//   ***   COMMON UTILS   ***

def getItsDarkOut() { //implement use of illuminance capability
    def sunTime = getSunriseAndSunset(sunsetOffset: sunsetOffset)
    def nowDate = new Date(now() + 2000) // be safe and set current time for 2 minutes later
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