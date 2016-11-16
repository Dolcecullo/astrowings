/**
 *  Garage Door Monitor
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
 *    v2.00 (15-Nov-2016) - code improvement: store images on GitHub, use getAppImg() to display app images
 *                        - added option to disable icons
 *                        - added option to disable multi-level logging
 *                        - configured default values for app settings
 *						  - moved 'About' to its own page
 *						  - added link to readme file
 *    v1.32 (05-Nov-2016) - code improvement: update 'convertToHM()'
 *                        - bug fix: fixed calculation for state.numWarning
 *    v1.31 (04-Nov-2016) - update href state & images
 *    v1.30 (04-Nov-2016) - add option to send periodic reminders
 *	  v1.21 (03-Nov-2016) - add link for Apache license
 *    v1.20 (02-Nov-2016) - implement multi-level debug logging function
 *    v1.10 (01-Nov-2016) - code improvement: standardize pages layout
 *	  v1.03 (01-Nov-2016) - code improvement: standardize section headers
 *    v1.02 (26-Oct-2016) - code improvement: added trace for each event handler
 *    v1.01 (26-Oct-2016) - added 'About' section in preferences
 *    v1.00               - initial release, no version tracking up to this point
 *
*/
definition(
    name: "Garage Door Monitor",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Notify if garage door is left open when leaving the house, left open for too long, or if it opens while away.",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
    page(name: "pageSettings")
    page(name: "pageLogOptions")
    page(name: "pageAbout")
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private		appImgPath()			{ return "https://raw.githubusercontent.com/astrowings/SmartThings/master/images/" }
private		readmeLink()			{ return "https://github.com/astrowings/SmartThings/blob/master/smartapps/astrowings/garage-door-monitor.src/readme.md" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp sends a notification (SMS optional) to notify " +
	        	"that a door is left open when leaving the house, left open for too long, or if it opens while away."
        }
        section("When I leave") {
            input "myself", "capability.presenceSensor", title: "Who?", multiple: false, required: true
        }
        section("Or when all these persons leave") {
            input "everyone", "capability.presenceSensor", title: "Who?", multiple: true, required: false
        }
        section("Send a notification if this door is left open") {
            input "thedoor", "capability.contactSensor", title: "Which door?", multiple: false, required: true
        }
        section("Notify me if the door opens while I'm away") {
            input "warnOpening", "bool", title: "Yes/No?", required: false, defaultValue: true
        }
        section("Let me know anytime it's left open for too long") {
            input "maxOpenMinutes", "number", title: "How long? (minutes)", defaultValue: 15, required: false, submitOnChange: true
            if (maxOpenMinutes) {
                input "remindMinutes", "number", title: "Remind me every x minutes", description: "Optional", defaultValue: 50, required: false
            }
        }
        section("Also send SMS alerts?"){
        input "phone", "phone", title: "Phone number (For SMS - Optional)", required: false
        }
		section() {
            href "pageSettings", title: "App settings", description: "", image: getAppImg("configure_icon.png"), required: false
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
    state.numWarning = 0
    state.debugLevel = 0
    debug "initializing", "trace", 1
    subscribeToEvents()
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    subscribe(myself, "presence.not present", iLeaveHandler)
    subscribe(everyone, "presence.not present", allLeaveHandler)
    subscribe(thedoor, "contact", doorHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def iLeaveHandler(evt) {
    debug "iLeaveHandler event: ${evt.descriptionText}", "trace", 1
    if (thedoor.currentContact != "closed") {
    	def message = "${evt.device} has left the house and the ${thedoor.device} is ${thedoor.currentContact}."
        debug "sendPush : ${message}", "warn"
        sendPush(message)
        sendText(message)
    } else {
    	debug "the ${thedoor.device} is ${thedoor.currentContact}; doing nothing"
    }
    debug "iLeaveHandler complete", "trace", -1
}

def allLeaveHandler(evt) {
    debug "allLeaveHandler event: ${evt.descriptionText}", "trace", 1
    if (thedoor.currentContact == "open") {
        if (everyoneIsAway) {
            def message = "Everyone has left the house and the ${thedoor.device} is ${thedoor.currentContact}."
            debug "sendPush : ${message}", "warn"
            sendPush(message)
            sendText(message)
		} else {
            debug "The ${thedoor.device} is ${thedoor.currentContact} but not everyone is away; doing nothing", "info"
        }
    }
    debug "allLeaveHandler complete", "trace", -1
}

def doorHandler(evt) {
    debug "doorHandler event: ${evt.descriptionText}", "trace", 1
    if (evt.value == "open" && warnOpening && imAway) {
    	def msg = "The ${thedoor.device} was opened."
        debug "sendPush : ${msg}", "warn"
        sendPush(msg)
        sendText(msg)
	} 
    if (evt.value == "open" && maxOpenMinutes) {
    	debug "The ${thedoor.device} was opened; scheduling a check in ${maxOpenMinutes} minutes to see if it's still open.", "info"
    	state.timeOpen = now()
        runIn(60 * maxOpenMinutes, checkOpen)
    }
    debug "doorHandler complete", "trace", -1
}

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def checkOpen() {
    debug "executing checkOpen()", "trace", 1
    def updated_numWarning = state.numWarning
    if (thedoor.currentContact == "open") {
        updated_numWarning ++
        debug "updated_numWarning : ${updated_numWarning}"
        sendNotification()
    } else {
    	debug "The ${thedoor.device} is no longer open.", "info"
    }
    state.numWarning = updated_numWarning
    debug "checkOpen() complete", "trace", -1
}

def sendNotification() {
	debug "executing sendNotification()", "trace", 1
    int elapsedOpen = now() - state.timeOpen
    debug "state.numWarning : ${state.numWarning}"
    debug "now() - state.timeOpen = ${now()} - ${state.timeOpen} = ${elapsedOpen}"
    //def datOpen = new Date(state.timeOpen)
    def msg = "The ${thedoor.device} has been opened for ${convertToHM(elapsedOpen)}"
    debug "sendPush : ${msg}", "warn"
    sendPush(msg)
    sendText(msg)
    setReminder()
    debug "sendNotification() complete", "trace", -1
}

def sendText(msg) {
    debug "executing sendText(msg: ${msg})", "trace", 1
	if (phone) {
		debug "sending SMS", "info"
		sendSms(phone, msg)
	} else {
    	debug "SMS number not configured", "info"
    }
    debug "sendText() complete", "trace", -1
}

def setReminder() {
	debug "executing sendReminder()", "trace", 1
    if (remindMinutes) {
    	def remindSeconds = 60 * remindMinutes
        debug "scheduling a reminder check in ${remindMinutes} minutes", "info"
        runIn(remindSeconds, checkOpen)
    } else {
    	debug "reminder option not set", "info"
    }
    debug "sendReminder() complete", "trace", -1
}

//   -------------------------
//   ***   APP FUNCTIONS   ***

def getEveryoneIsAway() {
    def result = true
    for (person in everyone) {
        if (person.currentPresence == "present") {
            result = false
            break
        }
    }
    debug ">> everyoneIsAway : ${result}"
    return result
}

def getImAway() {
	def result = !(myself.currentPresence == "present")
    debug ">> imAway : ${result}"
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

def convertToHM(ms) {
	//def df = new DecimalFormat("00")
    int hours = Math.floor(ms/1000/60/60)
    double dblMin = ((ms/1000/60) - (hours * 60))
    int minutes = dblMin.round()
	def strHr = hours == 1 ? "hour" : "hours"
    def strMin = minutes == 1 ? "minute" : "minutes"
    def result = (hours == 0) ? "${minutes} ${strMin}" : "${hours} ${strHr} and ${minutes} ${strMin}"
    return result
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