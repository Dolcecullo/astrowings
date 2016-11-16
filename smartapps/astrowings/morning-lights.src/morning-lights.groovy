/**
 *  Morning Lights
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
 	 private versionNum() {	return "version 2.32" }
     private versionDate() { return "09-Nov-2016" }		/*
 *
 *    vx.xx (xx-Nov-2016) - code improvement: store images on GitHub, use getAppImg() to display app images
 *                        - added option to disable icons
 *                        - added option to disable multi-level logging
 *                        - configured default values for app settings
 *						  - moved 'About' to its own page
 *						  - added link to readme file
 *						  - bug fix: removed log level increase for sunrise handler event because it was causing the log
 *							level to keep increasing without ever applying the '-1' at the end to restore the log level
 *						  - list current schedule/random settings in associated links
 *    v2.31 (04-Nov-2016) - update href state & images
 *	  v2.30 (03-Nov-2016) - new feature: add option to specify turn-off time
 *                        - code improvement: use constants instead of hard-coding
 *    v2.21 (02-Nov-2016) - add link for Apache license
 *    v2.20 (02-Nov-2016) - implement multi-level debug logging function
 *    v2.10 (01-Nov-2016) - code improvement: standardize pages layout
 *	  v2.01 (01-Nov-2016) - code improvement: standardize section headers
 *    v2.00 (29-Oct-2016) - inital version base code adapted from 'Sunset Lights - v2'
 *
*/
definition(
    name: "Morning Lights",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn on selected lights in the morning and turn them off automatically at sunrise.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light25-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light25-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light25-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
    page(name: "pageSchedule")
    page(name: "pageRandom")
    page(name: "pageSettings")
    page(name: "pageLogOptions")
    page(name: "pageAbout")
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

		 //	  name (C_XXX)			value					description
private		C_MIN_TIME_ON()			{ return 10 }			//value to use when scheduling turnOn to make sure lights will remain on for at least this long (minutes) before the scheduled turn-off time
private		appImgPath()			{ return "https://raw.githubusercontent.com/astrowings/SmartThings/master/images/" }
private		readmeLink()			{ return "https://github.com/astrowings/SmartThings/blob/master/smartapps/astrowings/morning-lights.src/readme.md" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp turns on selected lights at a specified time and turns turns them off at sunrise " +
            	"if no turn-off time is set. Different turn-on times can be configured for each day of the week, and they can be " +
                "randomized within a specified window to simulate manual operation."
        }
        section() {
            input "theLights", "capability.switch", title: "Which lights?", description: "Choose the lights to turn on", multiple: true, required: true, submitOnChange: true
            if (theLights) {
            	def startTimeOk = weekdayOn || saturdayOn || sundayOn || defaultOn
                href "pageSchedule", title: "Set scheduling Options", description: schedOptionsDesc, image: getAppImg("office7-icn.png"), required: true, state: startTimeOk ? "complete" : null
                href "pageRandom", title: "Configure random scheduling", description: randomOptionsDesc, image: getAppImg("dice-xxl.png"), required: true, state: "complete"
        	}
        }
		section() {
			if (theLights) {
	            href "pageSettings", title: "App settings", description: "", image: getAppImg("configure_icon.png"), required: false
            }
            href "pageAbout", title: "About", description: "", image: getAppImg("info-icn.png"), required: false
		}
    }
}

def pageSchedule() {
    dynamicPage(name: "pageSchedule", install: false, uninstall: false) {
        section(){
        	paragraph title: "Scheduling Options", "Use the options on this page to set the scheduling preferences."
        }
        section("Set a different time to turn on the lights on each day " +
                "(optional - lights will turn on at the default time if not set)") {
            input "weekdayOn", "time", title: "Mon-Fri", required: false
            input "saturdayOn", "time", title: "Saturday", required: false
            input "sundayOn", "time", title: "Sunday", required: false
        }
        section("Turn the lights on at this time if no weekday time is set " +
                "(optional - this setting used only if no weekday time is specified; " +
                "lights activation is disabled otherwise)") {
            input "defaultOn", "time", title: "Default time ON?", required: false
        }
        //TODO: option to turn lights off when mode changes to Away
        //TODO: use illuminance-capable device instead of sunrise/sunset to detect darkness
        section("Turn the lights off at this time " +
                "(optional - app will use the earliest of sunrise and this time)") { //TODO: reword this
            input "timeOff", "time", title: "Time OFF?", required: false
        }
	}
}

def pageRandom() {
    dynamicPage(name: "pageRandom", install: false, uninstall: false) {
        section(){
        	paragraph title: "Random Scheduling",
            	"Use the options on this page to add a random factor to " +
                "the lights' switching so the timing varies slightly " +
                "from one day to another (it looks more 'human' that way)."
        }
    	section("Specify a window around the scheduled time when the lights will turn on/off " +
        	"(e.g. a 30-minute window would have the lights switch sometime between " +
            "15 minutes before and 15 minutes after the scheduled time.)") {
            input "randOn", "number", title: "Random ON window (minutes)?", required: false, defaultValue: 8
            input "randOff", "number", title: "Random OFF window (minutes)?", required: false, defaultValue: 15
        }
        section("The settings above are used to randomize preset times such that lights will " +
        	"turn on/off at slightly different times from one day to another, but if multiples lights " +
            "are selected, they will still switch status at the same time. Use the options below " +
            "to insert a random delay between the switching of each individual light. " +
            "This option can be used independently of the ones above.") {
            input "onDelay", "bool", title: "Delay switch-on?", required: false, submitOnChange: true, defaultValue: true
            input "offDelay", "bool", title: "Delay switch-off?", required: false, submitOnChange: true, defaultValue: true
            if (onDelay || offDelay) {
            	input "delaySeconds", "number", title: "Switching delay", description: "Choose 1-60 seconds", required: true, defaultValue: 5, range: "1..60"
            }
        }
	}
}

def pageSettings() {
	dynamicPage(name: "pageSettings", install: false, uninstall: false) {
   		//TODO: add option for sunrise offset
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


//   ---------------------------------
//   ***   PAGES SUPPORT METHODS   ***

def getSchedOptionsDesc() {
    def strWeekdayOn = weekdayOn?.substring(11,16)
    def strSaturdayOn = saturdayOn?.substring(11,16)
    def strSundayOn = sundayOn?.substring(11,16)
    def strDefaultOn = defaultOn?.substring(11,16)
    def startTimeOk = weekdayOn || saturdayOn || sundayOn || defaultOn
    def strTimeOff = timeOff?.substring(11,16)
    def strDesc = ""
    strDesc += 				  " • Turn-on time:"
    strDesc += defaultOn	? " ${strDefaultOn}\n" : "\n"
    strDesc += weekdayOn	? "   └ weekdays: ${strWeekdayOn}\n" : ""
    strDesc += saturdayOn	? "   └ saturday: ${strSaturdayOn}\n" : ""
    strDesc += sundayOn		? "   └ sunday: ${strSundayOn}\n" : ""
    strDesc += timeOff		? " • Turn-off time: ${strTimeOff}" : " • Turn off at sunrise"
    return startTimeOk ? strDesc : "Turn-on time not set; automation disabled."
}

def getRandomOptionsDesc() {
    def delayType = (onDelay && offDelay) ? "on & off" : (onDelay ? "on" : "off")
    def strDesc = ""
    strDesc += (randOn || randOff)	? " • Random window:\n" : ""
    strDesc += randOn				? "   └ turn on:  +/-${randOn/2} minutes\n" : ""
    strDesc += randOn				? "   └ turn off: +/-${randOff/2} minutes\n" : ""
    strDesc += delaySeconds			? " • Light-light delay: ${delaySeconds} seconds\n    (when switching ${delayType})" : ""
    return (randOn || randOff || delaySeconds) ? strDesc : "Tap to configure random settings..."
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
	schedTurnOff(location.currentValue("sunriseTime"))
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    subscribe(location, "sunriseTime", sunriseTimeHandler)	//triggers at sunrise, evt.value is the sunrise String (time for next day's sunrise)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    //TODO: subscribe to lights on/off events IF commanded by this app (and log events)
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def sunriseTimeHandler(evt) {
    debug "sunriseTimeHandler event: ${evt.descriptionText}", "trace"
    def sunriseTimeHandlerMsg = "triggered sunriseTimeHandler; next sunrise will be ${evt.value}"
    debug "sunriseTimeHandlerMsg : $sunriseTimeHandlerMsg"
    schedTurnOff(evt.value)
    debug "sunriseTimeHandler complete", "trace"
}    

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def schedTurnOff(sunriseString) {
    debug "executing schedTurnOff(sunriseString: ${sunriseString})", "trace", 1
	
    def datTurnOff = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
    debug "sunrise date : ${datTurnOff}"

    if (timeOff) {
    	def userOff = timeTodayAfter("12:00", timeOff, location.timeZone)
        datTurnOff = datTurnOff < userOff ? datTurnOff : userOff //select the earliest of the two turn-off times
    }
    
    //apply random factor
    if (randOff) {
        def random = new Random()
        def randOffset = random.nextInt(randOff)
        datTurnOff = new Date(datTurnOff.time - (randOff * 30000) + (randOffset * 60000))
	}
    
    // This method gets called at sunrise to schedule next day's turn-off. However it's possible that
    // today's turn-off could be scheduled after sunrise (therefore after this method gets called),
    // so we use [overwrite: false] to prevent today's scheduled turn-off from being overwriten.
	debug "scheduling lights OFF for: ${datTurnOff}", "info"
    runOnce(datTurnOff, turnOff, [overwrite: false])
    schedTurnOn(datTurnOff)
    debug "schedTurnOff() complete", "trace", -1
}

def schedTurnOn(datTurnOff) {
	//fires at sunrise to schedule next day's turn-on
    debug "executing schedTurnOn(datTurnOff: ${datTurnOff})", "trace", 1
    
    def DOW_TurnOn = DOWTurnOnTime
    def default_TurnOn = defaultTurnOnTime
    def datTurnOn
    
	if (!DOW_TurnOn && !default_TurnOn) {
    	debug "user didn't specify turn-on time; scheduling cancelled", "warn"
    } else {
        //select which turn-on time to use (1st priority: weekday-specific, 2nd: default, else: no turn-on)
        def useTime
        if (DOW_TurnOn) {
            datTurnOn = DOW_TurnOn
            useTime = "using the weekday turn-on time"
        } else if (default_TurnOn) {
            datTurnOn = default_TurnOn
            useTime = "using the default turn-on time"
    	}
        
        //check that turn-on is scheduled earlier than turn-off by at least 'minTimeOn' minutes
	    def minTimeOn = C_MIN_TIME_ON()
        def safeOff = datTurnOff.time - (minTimeOn * 60 * 1000) //subtract 'minTimeOn' from scheduled turn-off time to ensure lights will stay on for at least 'minTimeOn' minutes
        if (datTurnOn.time < safeOff) {
            debug "scheduling lights ON for: ${datTurnOn} (${useTime})", "info"
            runOnce(datTurnOn, turnOn)
        } else {
        	debug "scheduling cancelled because tomorrow's turn-on time (${datTurnOn}) " +
            	"would be later than (or less than ${minTimeOn} minutes before) " +
                "the scheduled turn-off time (${datTurnOff}).", "info"
        }
    }
    debug "schedTurnOn() complete", "trace", -1
}

def turnOn() {
    debug "executing turnOn()", "trace", 1
    def newDelay = 0L
    def delayMS = (onDelay && delaySeconds) ? delaySeconds * 1000 : 5 //ensure delayMS != 0
    def random = new Random()
    theLights.each { theLight ->
        if (theLight.currentSwitch != "on") {
            debug "turning on the ${theLight.label} in ${convertToHMS(newDelay)}", "info"
            theLight.on(delay: newDelay)
            newDelay += random.nextInt(delayMS) //calculate random delay before turning on next light
        } else {
            debug "the ${theLight.label} is already on; doing nothing", "info"
        }
    }
    debug "turnOn() complete", "trace", -1
}

def turnOff() {
    debug "executing turnOff()", "trace", 1
    def newDelay = 0L
    def delayMS = (offDelay && delaySeconds) ? delaySeconds * 1000 : 5 //ensure delayMS != 0
    def random = new Random()
    theLights.each { theLight ->
        if (theLight.currentSwitch != "off") {
            debug "turning off the ${theLight.label} in ${convertToHMS(newDelay)}", "info"
            theLight.off(delay: newDelay)
            newDelay += random.nextInt(delayMS) //calculate random delay before turning off next light
        } else {
            debug "the ${theLight.label} is already off; doing nothing", "info"
        }
    }
    debug "turnOff() complete", "trace", -1
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getDefaultTurnOnTime() {
    //calculate default turn-on time
    //this gets called at sunrise, so when the sun rises on Tuesday, it will
    //schedule the lights' turn-on time for Wednesday morning
	debug "start evaluating defaultTurnOnTime", "trace", 1
    
    if (defaultOn) {
    	//convert preset time to next morning's date
        def timeOn = timeTodayAfter("12:00", defaultOn, location.timeZone)
        
        //apply random factor to turn-on time
        if (randOn) {
	    	def random = new Random()
			def randOffset = random.nextInt(randOn)
            timeOn = new Date(timeOn.time - (randOn * 30000) + (randOffset * 60000))
            debug "randomized default turn-on time: ${timeOn}"
        } else {
        	debug "default turn-on time: ${timeOn}"
        }
        debug "finished evaluating defaultTurnOnTime", "trace", -1
        return timeOn
    } else {
        debug "default turn-on time not specified"
        debug "finished evaluating defaultTurnOnTime", "trace", -1
        return false
	}
}

def getDOWTurnOnTime() {
    //calculate weekday-specific turn-on time
    //this gets called at sunrise, so when the sun rises on Tuesday, it will
    //schedule the lights' turn-on time for Wednesday morning
	debug "start evaluating DOWTurnOnTime", "trace", 1

    def tmrDOW = (new Date() + 1).format("E") //find out tomorrow's day of week

    //find out the preset (if entered) turn-on time for tomorrow
    def DOWtimeOn
    if (saturdayOn && tmrDOW == "Sat") {
        DOWtimeOn = saturdayOn
    } else if (sundayOn && tmrDOW == "Sun") {
        DOWtimeOn = sundayOn
    } else if (weekdayOn && tmrDOW == "Mon") {
        DOWtimeOn = weekdayOn
    } else if (weekdayOn && tmrDOW == "Tue") {
        DOWtimeOn = weekdayOn
    } else if (weekdayOn && tmrDOW == "Wed") {
        DOWtimeOn = weekdayOn
    } else if (weekdayOn && tmrDOW == "Thu") {
        DOWtimeOn = weekdayOn
    } else if (weekdayOn && tmrDOW == "Fri") {
        DOWtimeOn = weekdayOn
    }

	if (DOWtimeOn) {
    	//convert preset time to tomorrow's date
    	def tmrOn = timeTodayAfter("12:00", DOWtimeOn, location.timeZone)
        
        //apply random factor to turn-on time
		if (randOn) {
            def random = new Random()
            def randOffset = random.nextInt(randOn)
            tmrOn = new Date(tmrOn.time - (randOn * 30000) + (randOffset * 60000))
            debug "randomized DOW turn-on time: $tmrOn"
        } else {
        	debug "DOW turn-on time: $tmrOn"
        }
        debug "finished evaluating DOWTurnOnTime", "trace", -1
        return tmrOn
    } else {
    	debug "DOW turn-on time not specified"
        debug "finished evaluating DOWTurnOnTime", "trace", -1
        return false
    }
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