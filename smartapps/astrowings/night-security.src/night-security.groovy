/**
 *  Night Security
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
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private C_1() { return "this is constant1" }


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
            href(page: "pageSensors", title: "Sensor Selection", description: sensorDesc)
            href(page: "pageSchedule", title: "Scheduling Options", description: "Set the conditions for the monitoring window")
            href(page: "pageNotify", title: "Notification Method", description: "Configure the notification method")
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
        		href(page: "pageFlash", title: "Flasher settings")
            }
        }
        section("Turn a light/switch on") {
        	input "lightYesNo", "bool", title: "Yes/No?", required: false, submitOnChange: true
	        if (lightYesNo) {
            	href(page: "pageSwitch", title: "Switch settings")
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
                input "flashOnFor", "number", title: "How many seconds ON? (default = 1)", required: false
                input "flashOffFor", "number", title: "How many seconds OFF? (default = 1)", required: false
            }
            section("Set the number of flash cycles") {
                input "flashCycles", "number", title: "How many cycles? (default = 3)", required: false //TODO: change default to 5 (use constant)
            }
            section("Leave light(s) on after the flashing duration") {
                input "flashLeaveOn", "bool", title: "Yes/No?", submitOnChange: true, required: false, defaultValue: false
                if (flashLeaveOn) {
                    input "flashLeaveDuration", "number", title: "For how long (minutes)?", required: false
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
	log.info "installed with settings $settings"
	initialize()
}

def updated() {
    log.info "updated with settings $settings"
	unsubscribe()
    unschedule()
    initialize()
}

def uninstalled() {
    flashLights?.off()
    turnOnLights?.off()
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    state.debugLevel = 0
    state.alarmTime = null
    flashLights?.off()
    turnOnLights?.off()
    subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(location, "mode", modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
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
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def intrusionHandler(evt) {
	log.trace "intrusionHandler>${evt.descriptionText}"
    if (monitorOn) {
    	def triggerDevice = evt.device
        def triggerTime = evt.date
        state.alarmTime = now()
    	log.warn "An intrusion was detected ($triggerDevice at $triggerTime); triggering the alarm"
        alarmHandler(triggerDevice, triggerTime)
    } else {
    	log.info "An intrusion was detected but the monitoring conditions are not met; doing nothing" //TODO: why are the conditions not met? return monitorOn as map of conditions
    }
}

def modeChangeHandler(evt) {
	log.trace "modeChangeHandler>${evt.descriptionText}"
    if (!modeOk && alarmOn) {
    	if (state.alarmFlash == "on") {
        	deactivateFlash()
        }
        if (state.alarmLights == "on") {
        	deactivateLights()
        }
	}
}

def locationPositionChange(evt) {
	log.trace "locationPositionChange>${evt.descriptionText}"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def alarmHandler(triggerDevice, triggerTime) {
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
}

def activateFlash() {
	def doFlash = true
	def onFor = flashOnFor ? flashOnFor * 1000 : 1000
	def offFor = flashOffFor ? flashOffFor * 1000 : 1000
	def numFlashes = flashCycles ?: 3
	def sequenceTime = (numFlashes) * (onFor + offFor) + offFor

	if (state.flashLastActivated) {
		def elapsed = now() - state.flashLastActivated
		doFlash = elapsed > sequenceTime
	}

	if (doFlash) {
    	log.debug "starting lights flash"
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
        	log.debug "setting turn-on delay to leave flasher lights on after flash"
        	flashLights.on(delay: sequenceTime)
            if (flashLeaveDuration) {
            	log.debug "setting turn-off delay to turn flasher lights off after $flashLeaveDuration minutes"
            	def flashOffDelay = flashLeaveDuration * 60 * 1000
            	flashLights.off(delay: sequenceTime + flashOffDelay)
            }
            if (flashOffSun) {
				def strSunriseTime = location.currentValue("sunriseTime")
        		def datSunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", strSunriseTime)
                log.debug "scheduling flasher lights to turn off at sunrise ($datSunriseTime)"
                runOnce(datSunriseTime, deactivateFlash)
            }
        } else {
        	state.alarmFlash = "off"
        }
	}
}

def deactivateFlash() {
	flashLights.off()
    state.alarmFlash = "off"
}

def activateLights(evt) {
	log.debug "turning lights on"
    state.alarmLights = "on"
    turnOnLights.on()
	
    //schedule turn-off time (pick earliest from different turn-off options)
    def listOffTimes = []
    if (turnOnDurationYN) { //check for preset duration
    	def unxDurationEnd = now() + (60 * 1000 * turnOnMinutes)
        def datDurationEnd = new Date(unxDurationEnd)
        log.debug "end of preset duration :: $datDurationEnd"
        listOffTimes << datDurationEnd
	}
    if (turnOffTimeYN) { //check for preset off time
    	def datTurnOffTime = timeToday(turnOffTime, location.timeZone)
        log.debug "preset turn-off time :: $datTurnOffTime"
        listOffTimes << datTurnOffTime
    }
    if (turnOnSun) {
        def strSunriseTime = location.currentValue("sunriseTime")
        def datSunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", strSunriseTime)
        log.debug "sunrise time :: $datSunriseTime"
        listOffTimes << datSunriseTime
    }
    if (listOffTimes.size() > 0) {
    	def LightsOnEnd = listOffTimes.min()
        log.debug "the min time from $listOffTimes is $LightsOnEnd"
        log.info "scheduling deactivateLights at $LightsOnEnd"
        runOnce(LightsOnEnd, deactivateLights)
    }
}

def deactivateLights() {
	log.info "turning lights off"
    turnOnLights.off()
    state.alarmLights = "off"
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getSensorDesc() {
	if (theContacts || theMotions || theSmoke || theCO || theWater) {
    	def numSensors =
        	(theContacts?.size() ?: 0) +
            (theMotions?.size() ?: 0) +
            (theSmoke?.size() ?: 0) +
            (theCO?.size() ?: 0) +
            (theWater?.size() ?: 0)
    	//log.debug "${(theContacts?.size() ?: 0)}, ${(theMotions?.size() ?: 0)}, ${(theSmoke?.size() ?: 0)}, ${(theCO?.size() ?: 0)}, ${(theWater?.size() ?: 0)}"
        //log.debug "number of sensors: $numSensors"
        return "$numSensors sensors selected"
    } else {
    	return "Select the sensors to monitor"
    }
}

def getItsDarkOut() {
    def sunTime = getSunriseAndSunset(sunsetOffset: 15)
    def currentDTG = new Date()
    def result = false
	//log.debug "currentDTG: $currentDTG"
	//log.debug "sunTime.sunrise: $sunTime.sunrise"
	//log.debug "sunTime.sunset: $sunTime.sunset (with 15 min offset)"
    //log.debug "unx_sunTime.sunset: ${sunTime.sunset.time}"
    //log.debug "dat_unx_sunTime.sunset: ${new Date(sunTime.sunset.time)}"

	if(sunTime.sunrise < currentDTG && sunTime.sunset > currentDTG){
    	//log.debug "it's daytime"
        result = false
    } else {
    	//log.debug "it's nighttime"
        result = true
    }
    return result
}

def getMonitorOn() {
	def result = modeOk && darkOk && someoneHome && daysOk && timeOk && coolDownOk
    log.debug "MonitorOn :: $result"
    return result
}

def getModeOk() {
	def result = !theModes || theModes.contains(location.mode)
	log.debug "modeOk :: $result"
	return result
}

def getDarkOk() {
	def result = !theSun || itsDarkOut
	log.debug "darkOk :: $result"
	return result
}

def getSomeoneHome() {
	def result = (thePresence) ? false : true
    if(thePresence?.findAll {it?.currentPresence == "present"}) {
		result = true
	}
	log.debug "someoneHome :: $result"
	return result
}

def getDaysOk() {
	def result = true
	if (theDays) {
        def strDOW = nowDOW
        //log.debug "strDOW :: $strDOW"
		result = theDays.contains(strDOW)
	}
	log.debug "daysOk :: $result"
	return result
}

def getNowDOW() {
	//method to obtain current weekday adjusted for local time
    def javaDate = new java.text.SimpleDateFormat("EEEE, dd MMM yyyy @ HH:mm:ss")
    def javaDOW = new java.text.SimpleDateFormat("EEEE")
    if (location.timeZone) {
    	//log.debug "location.timeZone = true"
        javaDOW.setTimeZone(location.timeZone)
    } else {
        //log.debug "location.timeZone = false"
        //javaDate.setTimeZone(TimeZone.getTimeZone("America/Edmonton"))
    }
    def strDOW = javaDOW.format(new Date())
    log.debug "strDOW :: $strDOW"
    return strDOW
}

def getTimeOk() {
	def result = true
    if (theTimes) {
    	def start = timeToday(startTime, location.timeZone)
        def stop = timeToday(endTime, location.timeZone)
        //log.debug "today's time window is from $start to $stop (current ime is ${new Date()})"
		result = timeOfDayIsBetween(start, stop, new Date(), location.timeZone)
	}
	log.debug "timeOk :: $result"
	return result
}

def getCoolDownOk() {
	def result = true
    if (state.alarmTime) {
    	def delay = coolDown * 60 * 1000
        def elapsed = now() - state.alarmTime
		result = elapsed > delay
    }
    log.debug "coolDownOk :: $result"
    return result
}

def getAlarmOn() {
	def alarmFlash = state.alarmFlash
    def alarmLights = state.alarmLights
    def result = (alarmFlash == "on" || alarmLights == "on")
    log.debug "The alarm is currently ${result ? 'active' : 'inactive'}"
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