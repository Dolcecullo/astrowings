/**
 *  Thermostat Notify
 *
 *  Copyright © 2016 Phil Maynard
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0												*/
 	       def urlApache() { return "http://www.apache.org/licenses/LICENSE-2.0" }				/*
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *   --------------------------------
 *   ***   VERSION HISTORY  ***
 *	
 *	  v1.01 (09-Aug-2018) - standardize debug log types and make 'debug' logs disabled by default
 *						  - standardize layout of app data and constant definitions
 *    v1.00 (15-Apr-2017) - initial release
 *    v0.10 (26-Mar-2017) - developing
 *
*/
definition(
    name: "Thermostat Notify",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Send notifications based on selected thermostat events.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Electronics/electronics1-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Electronics/electronics1-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Electronics/electronics1-icn@3x.png"
)


//   --------------------------------
//   ***   APP DATA  ***

def		versionNum()			{ return "version 1.01" }
def		versionDate()			{ return "09-Aug-2018" }     
def		gitAppName()			{ return "thermostat-notify" }
def		gitOwner()				{ return "astrowings" }
def		gitRepo()				{ return "SmartThings" }
def		gitBranch()				{ return "master" }
def		gitAppFolder()			{ return "smartapps/${gitOwner()}/${gitAppName()}.src" }
def		appImgPath()			{ return "https://raw.githubusercontent.com/${gitOwner()}/${gitRepo()}/${gitBranch()}/images/" }
def		readmeLink()			{ return "https://github.com/${gitOwner()}/SmartThings/blob/master/${gitAppFolder()}/readme.md" } //TODO: convert to httpGet?


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

	 	//name					value					description


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
    page(name: "pageSettings")
    page(name: "pageLogOptions")
    page(name: "pageAbout")
    page(name: "pageUninstall")
}


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp sends notifications based on selected thermostat events."
        }
        section() {
            input "theThermostat", "capability.Thermostat", title: "Which thermostat?", description: "Monitor changes on this thermostat", required: false, submitOnChange: true
        }
        section("Select monitoring functions") {
			if (theThermostat) {
            	input "chkMode", "bool", title: "Monitor changes in thermostat operation mode (off, eco, heat, cool)?", defaultValue: true
                input "chkTemp", "bool", title: "Monitor changes in temperature setpoint?"
			}        
        }
		section("Select notification method") {
			if (theThermostat) {
                input "sendPush", "bool", title: "Send push notification on selected events", defaultValue: true, required: false
                input "sendSMS", "bool", title: "Send SMS notification on selected events", defaultValue: false, required: false, submitOnChange: true
                if (sendSMS) {
                	input "numberSMS", "phone", title: "Phone number", required: true
                }
                href "pageSettings", title: "App settings", description: "", image: getAppImg("configure_icon.png"), required: false
            }
            href "pageAbout", title: "About", description: "", image: getAppImg("info-icn.png"), required: false
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
        	href url: readmeLink(), title: app.name, description: "Copyright ©2017 Phil Maynard\n${versionNum()}", image: getAppImg("readme-icn.png")
            href url: urlApache(), title: "License", description: "View Apache license", image: getAppImg("license-icn.png")
		}
    }
}

def pageLogOptions() {
	dynamicPage(name: "pageLogOptions", title: "IDE Logging Options", install: false, uninstall: false) {
        section() {
	        input "debugging", "bool", title: "Enable debugging", description: "Display the logs in the IDE", defaultValue: true, required: false, submitOnChange: true
        }
        if (debugging) {
            section("Select log types to display") {
                input "log#info", "bool", title: "Log info messages", defaultValue: true, required: false
                input "log#trace", "bool", title: "Log trace messages", defaultValue: true, required: false
                input "log#debug", "bool", title: "Log debug messages", defaultValue: false, required: false
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
	subscribe(theThermostat, "nestThermostatMode", handleThermostatChange)
    subscribe(theThermostat, "thermostatSetpoint", handleThermostatChange)
    //subscribe(location, "mode", modeChangeHandler) //TODO: is there anything to do on mode change?
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def handleThermostatChange(evt) {
	debug "thermostat event triggered: ${evt.descriptionText}", "trace", 1
    debug "thermostat: ${theThermostat.label}"
	debug "mode: ${theThermostat.currentNestThermostatMode}"
    debug "setpoint: ${theThermostat.currentThermostatSetpoint}"
    debug "temp: ${theThermostat.currentTemperature}"
    
    //take appropriate action based on type of event detected
    if (evt.descriptionText.contains("Nest HVAC mode is")) {
    	debug "nestThermostatMode event detected"
        notifyMode()
    } else if (evt.descriptionText.contains("thermostatSetpoint")) {
    	debug "thermostatSetpoint event detected"
        notifySetpoint()
    } else {
    	debug "other event detected"
    }
    
    debug "thermostat event handler complete", "trace", -1
	}

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}

def modeChangeHandler(evt) {
	debug "modeChangeHandler event: ${evt.descriptionText}", "trace"
    //TODO: is there anything to do on mode change?
    debug "modeChangeHandler complete", "trace"
}


//   -------------------
//   ***   METHODS   ***

def notifyMode() {
	debug "executing notifyMode()", "trace", 1
	if (chkMode) {
        def msg = "Nest thermostat mode changed to '${theThermostat.currentNestThermostatMode}'"
        sendNotification(msg)
    } else {
    	debug "notifications not enabled for thermostat mode changes"
    }
    debug "notifyMode() complete", "trace", -1
}

def notifySetpoint() {
	debug "executing notifySetpoint()", "trace", 1
	if (chkTemp) {
    	def msg = "Nest thermostat setpoint changed to ${theThermostat.currentThermostatSetpoint}°"
        sendNotification(msg)
    } else {
    	debug "notifications not enabled for thermostat setpoint changes"
    }
    debug "notifySetpoint() complete", "trace", -1
}

def sendNotification(msg) {
	debug "executing sendNotification()", "trace", 1
    if (sendPush) {
    	sendPush(msg)
    }
    if (sendSMS) {
    	sendSms(phone, msg)
    }
    debug "sendNotification() complete", "trace", -1
}


//   -------------------------
//   ***   APP FUNCTIONS   ***


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