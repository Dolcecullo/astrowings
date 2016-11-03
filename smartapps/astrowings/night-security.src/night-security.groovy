/**
 *  Night Security
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
 	 def versionNum() {	return "version 1.21" }       /*
 
 *   v1.21 (02-Nov-2016): add link for Apache license
 *   v1.20 (02-Nov-2016): implement multi-level debug logging function
 *   v1.10 (01-Nov-2016): standardize pages layout
 *	 v1.03 (01-Nov-2016): standardize section headers
 *   v1.02 (26-Oct-2016): added trace for each event handler
 *   v1.01 (26-Oct-2016): added 'About' section in preferences
 *   v1.00 (2016 date unknown): working version, no version tracking up to this point
 *
*/
definition(
    name: "Night Security",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Monitors a set of sensors during specified modes and alerts the user when an intrusion is detected.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather4-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather4-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather4-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
	page(name: "pageSensors", hideWhenEmpty: true)
    page(name: "pageSchedule")
    page(name: "pageNotify")
    page(name: "pageFlash")
    page(name: "pageSwitch")
    page(name: "pageSettings")
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***



//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
	dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp sends an alert when any of the selected sensors are triggered. " +
            	"It sends push notifications, SMS alerts, turns lights on, and flashes lights to alert the user of an intrusion. " +
                "Can be used to monitor if someone (child, elderly) is attempting to leave the house."
        }
        section("Configuration") {
            href(page: "pageSensors", title: "Sensor Selection", description: sensorDesc) //TODO: state
            href(page: "pageSchedule", title: "Scheduling Options", description: "Set the conditions for the monitoring window") //TODO: state
            href(page: "pageNotify", title: "Notification Method", description: "Configure the notification method") //TODO: state
		}
		section() {
            href "pageSettings", title: "App settings", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", required: false
		}
	}
}

def pageSensors() {
	dynamicPage(name: "pageSensors", install: false, uninstall: false) {
        section(){
        	paragraph title: "Sensor Selection",
            	"Select the various sensors you want to monitor (i.e. triggers)."
        }
        section() {
            input "theContacts", "capability.contactSensor", title: "Open/Close Sensors", multiple: true, required: false, submitOnChange: true
            input "theMotions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false, submitOnChange: true
        	input "theSmoke", "capability.smokeDetector", title: "Smoke Detectors", multiple: true, required: false, submitOnChange: true
        	input "theCO", "capability.carbonMonoxideDetector", title: "Carbon Monoxide Detectors", multiple: true, required: false, submitOnChange: true
        	input "theWater", "capability.waterSensor", title: "Water Leak Sensors", multiple: true, required: false, submitOnChange: true
        }
	}
}

def pageSchedule() {
	dynamicPage(name: "pageSchedule", install: false, uninstall: false) {
        section(){
        	paragraph title: "Monitoring Schedule",
            	"Set the conditions for the monitoring window. These settings are the 'and' type " +
            	"(e.g. selecting \"Dark out\" and \"Days of week: Monday\" would only alert for " +
                "intrusions/triggers that occur during darkness on Monday, not anytime on Monday or " +
                "anytime it's dark)"
        }
    	section("When the mode is set to... (any mode if none selected)") {
        	input "theModes", "mode", title: "Select the mode(s)", multiple: true, required: false
        }
    	section("When it's dark out (between sunset and sunrise)") {
        	input "theSun", "bool", title: "Yes/No?", required: false
        }
        section("When someone is home") {
        	input "thePresence", "capability.presenceSensor", title: "Who?", multiple: true, required: false
        }
        section("On certain days of the week (any day if none selected)") {
        	input "theDays", "enum", title: "On which days?", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
        }
        section("Between certain times") {
        	input "theTimes", "bool", title: "Yes/No?", required: false, submitOnChange: true
	        if (theTimes) {
            	input "startTime", "time", title: "Start time?", required: true, submitOnChange: true
                input "endTime", "time", title: "End time?", required: true, submitOnChange: true
            }
        }
	}
}

def pageNotify() {
	dynamicPage(name: "pageNotify", install: false, uninstall: false) {
        section(){
        	paragraph title: "Notification Method",
            	"Configure the method used to notify of a trigger condition."
        }
        section("Send a push notification") {
        	input "pushYesNo", "bool", title: "Yes/No?", required: false
        }
        section("Send a SMS notification") {
        	input "smsYesNo", "bool", title: "Yes/No?", required: false, submitOnChange: true
            if (smsYesNo) {
            	input "smsNumber", "phone", title: "To which number?", required: true
            }
        }
        section("Flash a light") {
            input "flashYesNo", "bool", title: "Yes/No?", required: false, submitOnChange: true
	        if (flashYesNo) {
        		href(page: "pageFlash", title: "Flasher settings") //TODO: state
            }
        }
        section("Turn a light/switch on") {
        	input "lightYesNo", "bool", title: "Yes/No?", required: false, submitOnChange: true
	        if (lightYesNo) {
            	href(page: "pageSwitch", title: "Switch settings") //TODO: state
            }
        }
        section("Set the cooldown period") {
        	input "coolDown", "number", title: "Do not trigger another alarm within? (minutes)", required: false
        }
    }
}

def pageFlash() {
	dynamicPage(name: "pageFlash", install: false, uninstall: false) {
        section(){
        	paragraph title: "Configure flasher settings",
            	"Configure the frequency and duration to have a light flash when an alarm condition is detected."
        }
        section("Choose the light(s)") {
        	input "flashLights", "capability.switch", title: "Which?", multiple: true, submitOnChange: true
        }
        if (flashLights) {
            section("Set the flash interval") {
                input "flashOnFor", "number", title: "How many seconds ON? (default = 1)", required: false //TODO: use constant for default, specify valid range
                input "flashOffFor", "number", title: "How many seconds OFF? (default = 1)", required: false //TODO: use constant for default, specify valid range
            }
            section("Set the number of flash cycles") {
                input "flashCycles", "number", title: "How many cycles? (default = 3)", required: false //TODO: change default to 5 (use constant), specify valid range
            }
            section("Leave light(s) on after the flashing duration") {
                input "flashLeaveOn", "bool", title: "Yes/No?", submitOnChange: true, required: false, defaultValue: false
                if (flashLeaveOn) {
                    input "flashLeaveDuration", "number", title: "For how long (minutes)?", description: "Leave on until mode change if not set", required: false
                    input "flashOffSun", "bool", title: "Turn off at sunrise?", defaultValue: true, required: false //TODO: remove this option
                }
            }
        }
    }
}

def pageSwitch() {
	dynamicPage(name: "pageSwitch", install: false, uninstall: false) {
        section(){
        	paragraph title: "Configure switch on/off settings",
            	"Configure the settings on this page to turn a light (or switch) on when an alarm condition is detected."
        }
        section("Choose the light(s)") {
        	input "turnOnLights", "capability.switch", title: "Which?", multiple: true, submitOnChange: true
        }
        if (turnOnLights) {
            section("Set the light(s) to turn off automatically") {
                input "turnOnDurationYN", "bool", title: "After a predetermined duration?", submitOnChange: true, required: false, defaultValue: false
                if (turnOnDurationYN) {
                    input "turnOnMinutes", "number", title: "How long (minutes)?", required: true
                }
                input "turnOffTimeYN", "bool", title: "At a specific time?", submitOnChange: true, required: false, defaultValue: false
                if (turnOffTimeYN) {
                    input "turnOffTime", "time", title: "What time?", required: true
                }
                input "turnOnSun", "bool", title: "At sunrise?", defaultValue: true, required: false //TODO: remove this option
            }
        }
    }
}

def pageSettings() {
	dynamicPage(name: "pageSettings", install: false, uninstall: false) {
		section("About") {
        	paragraph "Copyright ©2016 Phil Maynard\n${versionNum()}", title: app.name
            href name: "hrefLicense", title: "License", description: "Apache License", url: urlApache()
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
    flashLights?.off()
    turnOnLights?.off()
    state.debugLevel = 0
    debug "application uninstalled", "trace"
}

def initialize() {
    state.debugLevel = 0
    debug "initializing", "trace", 1
    state.alarmTime = null
    flashLights?.off()
    turnOnLights?.off()
    subscribeToEvents()
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
	subscribe(location, "mode", modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    //TODO: subscribe to lights on/off events IF commanded by this app (and log events)
	if (theContacts) {
		subscribe(theContacts, "contact.open", intrusionHandler)
    }
    if (theMotions) {
    	subscribe(theMotions, "motion.active", intrusionHandler)
    }
    if (theSmoke) {
    	subscribe(theSmoke, "smoke.detected", intrusionHandler)
    }
    if (theCO) {
    	subscribe(theCO, "carbonMonoxide.detected", intrusionHandler)
    }
    if (theWater) {
    	subscribe(theWater, "water.wet", intrusionHandler)
    }
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def intrusionHandler(evt) {
    debug "intrusionHandler event: ${evt.descriptionText}", "trace", 1
    if (monitorOn) {
    	def triggerDevice = evt.device
        def triggerTime = evt.date
        state.alarmTime = now()
    	debug "an intrusion was detected ($triggerDevice at $triggerTime); triggering the alarm", "warn"
        alarmHandler(triggerDevice, triggerTime)
    } else {
    	debug "an intrusion was detected but the monitoring conditions are not met; doing nothing", "info" //TODO: why are the conditions not met? return monitorOn as map of conditions
    }
    debug "intrusionHandler complete", "trace", -1
}

def modeChangeHandler(evt) {
    debug "modeChangeHandler event: ${evt.descriptionText}", "trace", 1
    if (!modeOk && alarmOn) {
    	if (state.alarmFlash == "on") {
        	deactivateFlash()
        }
        if (state.alarmLights == "on") {
        	deactivateLights()
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

def alarmHandler(triggerDevice, triggerTime) {
    debug "executing alarmHandler(triggerDevice: ${triggerDevice}, triggerTime: ${triggerTime})", "trace", 1
	def alarmMsg = "Intrusion detected by $triggerDevice at $triggerTime"
	if (pushYesNo) {
    	sendPush(alarmMsg)
    }
    if (smsYesNo) {
    	sendSmsMessage(smsNumber, alarmMsg)
    }
    if (flashYesNo) {
    	activateFlash()
    }
    if (lightYesNo) {
    	activateLights()
    }
    debug "alarmHandler() complete", "trace", -1
}

def activateFlash() {
    debug "executing activateFlash()", "trace", 1
	def doFlash = true
	def onFor = flashOnFor ? flashOnFor * 1000 : 1000 //TODO: use constant for default
	def offFor = flashOffFor ? flashOffFor * 1000 : 1000 //TODO: use constant for default
	def numFlashes = flashCycles ?: 3 //TODO: use constant for default
	def sequenceTime = (numFlashes) * (onFor + offFor) + offFor

	if (state.flashLastActivated) {
		def elapsed = now() - state.flashLastActivated
		doFlash = elapsed > sequenceTime
	}

	if (doFlash) {
    	debug "starting lights flash", "info"
		state.flashLastActivated = now()
        state.alarmFlash = "on"
		def initialActionOn = flashLights.collect{it.currentSwitch != "on"}
		def delay = 0L
		numFlashes.times {
			flashLights.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				} else {
					s.off(delay: delay)
				}
			}
			delay += onFor
			flashLights.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				} else {
					s.on(delay: delay)
				}
			}
			delay += offFor
		}
        if (flashLeaveOn) {
        	debug "setting turn-on delay to leave flasher lights on after flash", "info"
        	flashLights.on(delay: sequenceTime)
            if (flashLeaveDuration) {
            	debug "setting turn-off delay to turn flasher lights off after $flashLeaveDuration minutes", "info"
            	def flashOffDelay = flashLeaveDuration * 60 * 1000
            	flashLights.off(delay: sequenceTime + flashOffDelay)
            }
            if (flashOffSun) {
				def strSunriseTime = location.currentValue("sunriseTime")
        		def datSunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", strSunriseTime)
                debug "scheduling flasher lights to turn off at sunrise ($datSunriseTime)", "info"
                runOnce(datSunriseTime, deactivateFlash)
            }
        } else {
        	state.alarmFlash = "off"
        }
	}
    debug "activateFlash() complete", "trace", -1
}

def deactivateFlash() {
    debug "executing deactivateFlash()", "trace", 1
	flashLights.off()
    state.alarmFlash = "off"
    debug "deactivateFlash() complete", "trace", -1
}

def activateLights() {
    debug "executing activateLights()", "trace", 1
    state.alarmLights = "on"
    turnOnLights.on()
	
    //schedule turn-off time (pick earliest from different turn-off options)
    def listOffTimes = []
    if (turnOnDurationYN) { //check for preset duration
    	def unxDurationEnd = now() + (60 * 1000 * turnOnMinutes)
        def datDurationEnd = new Date(unxDurationEnd)
        debug "end of preset duration : $datDurationEnd"
        listOffTimes << datDurationEnd
	}
    if (turnOffTimeYN) { //check for preset off time
    	def datTurnOffTime = timeToday(turnOffTime, location.timeZone)
        debug "preset turn-off time : $datTurnOffTime"
        listOffTimes << datTurnOffTime
    }
    if (turnOnSun) {
        def strSunriseTime = location.currentValue("sunriseTime")
        def datSunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", strSunriseTime)
        debug "sunrise time : $datSunriseTime"
        listOffTimes << datSunriseTime
    }
    if (listOffTimes.size() > 0) {
    	def LightsOnEnd = listOffTimes.min()
        debug "the min time from $listOffTimes is $LightsOnEnd"
        debug "scheduling deactivateLights at $LightsOnEnd", "info"
        runOnce(LightsOnEnd, deactivateLights)
    }
    debug "activateLights() complete", "trace", -1
}

def deactivateLights() {
    debug "executing deactivateLights()", "trace", 1
    turnOnLights.off()
    state.alarmLights = "off"
    debug "deactivateLights() complete", "trace", -1
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getSensorDesc() {
		if (theContacts || theMotions || theSmoke || theCO || theWater) {
    	def result = ""
        def numSensors =
        	(theContacts?.size() ?: 0) +
            (theMotions?.size() ?: 0) +
            (theSmoke?.size() ?: 0) +
            (theCO?.size() ?: 0) +
            (theWater?.size() ?: 0)
    	//debug "${(theContacts?.size() ?: 0)}, ${(theMotions?.size() ?: 0)}, ${(theSmoke?.size() ?: 0)}, ${(theCO?.size() ?: 0)}, ${(theWater?.size() ?: 0)}"
        //debug "number of sensors: $numSensors"
        result = "$numSensors sensors selected"
    } else {
    	result = "Select the sensors to monitor"
    }
        return result
        debug ">> sensorDesc : $result"
}

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

def getMonitorOn() {
	def result = modeOk && darkOk && someoneHome && daysOk && timeOk && coolDownOk
    debug ">> monitorOn : $result"
    return result
}

def getModeOk() {
	def result = !theModes || theModes.contains(location.mode)
	debug ">> modeOk : $result"
	return result
}

def getDarkOk() {
	def result = !theSun || itsDarkOut
	debug ">> darkOk : $result"
	return result
}

def getSomeoneHome() {
	def result = (thePresence) ? false : true
    if(thePresence?.findAll {it?.currentPresence == "present"}) {
		result = true
	}
	debug ">> someoneHome : $result"
	return result
}

def getDaysOk() {
	def result = true
	if (theDays) {
        def strDOW = nowDOW
		result = theDays.contains(strDOW)
	}
	debug ">> daysOk : $result"
	return result
}

def getNowDOW() {
	//method to obtain current weekday adjusted for local time
    def javaDate = new java.text.SimpleDateFormat("EEEE, dd MMM yyyy @ HH:mm:ss")
    def javaDOW = new java.text.SimpleDateFormat("EEEE")
    if (location.timeZone) {
    	//debug "location.timeZone = true"
        javaDOW.setTimeZone(location.timeZone)
    } else {
        //debug "location.timeZone = false"
        //javaDate.setTimeZone(TimeZone.getTimeZone("America/Edmonton"))
    }
    def strDOW = javaDOW.format(new Date())
    debug ">> nowDOW : $strDOW"
    return strDOW
}

def getTimeOk() {
	def result = true
    if (theTimes) {
    	def start = timeToday(startTime, location.timeZone)
        def stop = timeToday(endTime, location.timeZone)
        //debug "today's time window is from $start to $stop (current ime is ${new Date()})"
		result = timeOfDayIsBetween(start, stop, new Date(), location.timeZone)
	}
	debug ">> timeOk : $result"
	return result
}

def getCoolDownOk() {
	def result = true
    if (state.alarmTime) {
    	def delay = coolDown * 60 * 1000
        def elapsed = now() - state.alarmTime
		result = elapsed > delay
    }
    debug ">> coolDownOk : $result"
    return result
}

def getAlarmOn() {
	def alarmFlash = state.alarmFlash
    def alarmLights = state.alarmLights
    def result = (alarmFlash == "on" || alarmLights == "on")
    debug ">> alarmOn : $result"
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