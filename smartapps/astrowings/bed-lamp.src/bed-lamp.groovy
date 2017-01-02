/**
 *  Bed Lamp
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
 	 private versionNum() {	return "version 1.01" }
     private versionDate() { return "01-Jan-2017" }		/*
 *
 *    v1.01 (01-Jan-2017) - added call to timeCheck() during initialization
 *                        - moved 'thePeople' input to pageSettings
 *    v1.00 (31-Dec-2016) - initial release
 *    v0.10 (27-Nov-2016) - developing
 *
*/
definition(
    name: "Bed Lamp",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Automatically turn on selected lights after dark and turn them off when the mode changes to Night.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light2-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light2-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light2-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
    page(name: "pageSchedule")
    page(name: "pageSettings")
    page(name: "pageLogOptions")
    page(name: "pageAbout")
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private		appImgPath()			{ return "https://raw.githubusercontent.com/astrowings/SmartThings/master/images/" }
private		readmeLink()			{ return "https://github.com/astrowings/SmartThings/blob/master/smartapps/astrowings/bed-lamp.src/readme.md" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp turns on selected lights after dark (or at a preset time) and turns them off when the mode changes to Night."
        }
        section() {
            input "theLights", "capability.switch", title: "Which lights?", description: "Choose the lights to turn on", multiple: true, required: true, submitOnChange: true
        }
		section() {
			if (theLights) {
	            href "pageSettings", title: "App settings", description: "", image: getAppImg("configure_icon.png"), required: false
            }
            href "pageAbout", title: "About", description: "", image: getAppImg("info-icn.png"), required: false
		}
    }
}

def pageSettings() {
	dynamicPage(name: "pageSettings", install: false, uninstall: false) {
        section() {
			label title: "Assign a name", defaultValue: "${app.name}", required: false
            href "pageUninstall", title: "", description: "Uninstall this SmartApp", image: getAppImg("trash-circle-red-512.png"), state: null, required: true
		}
        section("Enter a time if you wish to override the illuminance and turn lights on at a specific time, regardless of whether it's dark out.") {
        	input "presetOnTime", "time", title: "Turn-on time?", required: false
        }
        if (!theLuminance) {
            section("If desired, you can adjust the amount of time after sunset when the app will turn the lights on.") {
                input "sunsetOffset", "number", title: "Sunset offset time", description: "How many minutes?", range: "0..180", required: false
            }
   		}
        section("Optionally, you can choose to enable this SmartApp only when selected persons are home; if none selected, it will run whenever the mode is set to Home.") {
            input "thePeople", "capability.presenceSensor", title: "Who?", description: "Only when these persons are home", multiple: true, required: false
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
        	paragraph "CAUTION: You are about to completely remove the SmartApp '${app.name}'. This action is irreversible. If you want to proceed, tap on the 'Remove' button below.",
                required: true, state: null
        }
	}
}


//   ---------------------------------
//   ***   PAGES SUPPORT METHODS   ***



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
    if (presetOnTime) { //if the user set an ON time, schedule the switch
    	schedule(presetOnTime, turnOn)
    }
    timeCheck()
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    if (!presetOnTime) { //if the user set an ON time, the sunset is not required
    	subscribe(location, "sunsetTime", sunsetTimeHandler)	//triggers at sunset, evt.value is the sunset String (time for next day's sunset)
    }
    subscribe(location, "mode", modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    //TODO: subscribe to lights on/off events IF commanded by this app (and log events)
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def sunsetTimeHandler(evt) {
    debug "sunriseTimeHandler event: ${evt.descriptionText}", "trace"
    timeCheck()
    debug "sunriseTimeHandler complete", "trace"
}    

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}

def modeChangeHandler(evt) {
	debug "modeChangeHandler event: ${evt.descriptionText}", "trace"
    if (modeOk) {
    	timeCheck()
    } else {
    	turnOff()
    }
    debug "modeChangeHandler complete", "trace"
}

//   -------------------
//   ***   METHODS   ***

def timeCheck() {
    debug "executing timeCheck()", "trace", 1
	def nowDate = new Date()
    debug "nowDate: ${nowDate}"
    def onTime = getTurnOnTime()
    debug "onTime: ${onTime}"
    if (onTime > nowDate) {
    	debug "onTime > nowDate; scheduling turnOn for ${onTime}"
    	schedule(onTime, turnOn)
    } else {
    	debug "nowDate >= onTime; calling turnOn()"
        turnOn()
    }
    debug "timeCheck() complete", "trace", -1
}

def turnOn() {
    debug "executing turnOn()", "trace", 1
    if (modeOk && presenceOk) {
    	debug "conditions met; turning lights on"
        theLights.on()
    } else {
    	debug "conditions not met; wait for next call"
    }
    debug "turnOn() complete", "trace", -1
}

def turnOff() {
    debug "executing turnOff()", "trace", 1
    theLights.off()
    debug "turnOff() complete", "trace", -1
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getModeOk() {
    def nowMode = location.mode
    def result = (nowMode == "Home") ? true : false
    return result
}

def getPresenceOk() {
	def result = true
    if (thePeople) {
        for (person in thePeople) {
            if (person.currentPresence == "not present") {
            	result = false
                break
            }
		}
    }
    return result
}

def getTurnOnTime() {
    def tz = location.timeZone
    def result = new Date()
    if (presetOnTime) {
        result = timeToday(presetOnTime, tz)
    } else {
    	def offset = sunsetOffset ?: 0
        def sunTime = getSunriseAndSunset(sunsetOffset: offset)
    	result = sunTime.sunset
    }
    return result
}


//   ------------------------
//   ***   COMMON UTILS   ***

def convertToHMS(ms) {
    int hours = Math.floor(ms/1000/60/60)
    int minutes = Math.floor((ms/1000/60) - (hours * 60))
    int seconds = Math.floor((ms/1000) - (hours * 60 * 60) - (minutes * 60))
    double millisec = ms-(hours*60*60*1000)-(minutes*60*1000)-(seconds*1000)
    int tenths = (millisec/100).round(0)
    return "${hours}h${minutes}m${seconds}.${tenths}s"
}

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