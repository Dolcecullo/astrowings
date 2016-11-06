/**
 *  Sunset Lights
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
 	 def versionNum() {	return "version 2.23" }       /*
 *
 *    v2.23 (04-Nov-2016): update href state & images
 *    v2.22 (03-Nov-2016): use constants instead of hard-coding
 *    v2.21 (02-Nov-2016): add link for Apache license
 *    v2.20 (02-Nov-2016): implement multi-level debug logging function
 *    v2.10 (01-Nov-2016): standardize pages layout
 *	  v2.01 (01-Nov-2016): standardize section headers
 *	  v2.00 (28-Oct-2016): add option to insert random delay between the switching of individual lights,
 *                         change method to evaluate which turn-off time to use
 *                         move off-time comparison to turnOn()
 *                         add option to apply random factor to ON time
 *    v1.02 (26-Oct-2016): added trace for each event handler
 *    v1.01 (26-Oct-2016): added 'About' section in preferences
 *    v1.00 (2016 date unknown): working version, no version tracking up to this point
 *
*/
definition(
    name: "Sunset Lights",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn on selected lights at sunset and turn them off at a specified time.",
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
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

		 //	  name (C_XXX)			value					description
private		C_SUNRISE_OFFSET()		{ return -30 }			//offset used for sunrise time calculation (minutes)
private		C_MIN_TIME_ON()			{ return 15 }			//value to use when scheduling turnOn to make sure lights will remain on for at least this long (minutes) before the scheduled turn-off time


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp turns on selected lights at sunset and turns them off at a specified time." +
            	"Different turn-off times can be configured for each day of the week, and they can be " +
                "randomized within a specified window to simulate manual activation."
        }
        section() {
            input "theLights", "capability.switch", title: "Which lights?", description: "Choose the lights to turn on", multiple: true, required: true, submitOnChange: true
            if (theLights) {
                href "pageSchedule", title: "Set scheduling Options", image: "http://cdn.device-icons.smartthings.com/Office/office7-icn.png", required: false
                href "pageRandom", title: "Configure random scheduling", image: "http://www.iconsdb.com/icons/preview/gray/dice-xxl.png", required: true, state: "complete"
        	}
        }
		section() {
			if (theLights) {
            	href "pageSettings", title: "App settings", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", required: false
            }
		}
    }
}

def pageSchedule() {
    dynamicPage(name: "pageSchedule", install: false, uninstall: false) {
        def sunriseOffset = C_SUNRISE_OFFSET()
        def sunriseOffset_minutes = sunriseOffset.abs()
        def sunriseOffset_BeforeAfter = sunriseOffset < 0 ? "before" : "after"
        section(){
        	paragraph title: "Scheduling Options", "Use the options on this page to set the scheduling preferences."
        }
        section("Set the amount of time before/after sunset when the lights will turn on " +
        		"(e.g. use '-20' to enable lights 20 minutes before sunset).") {
                input "sunsetOffset", "number", title: "Sunset offset time", description: "How many minutes (+/- 60)?", range: "-60..60", required: false
        }
    	section("Turn the lights off at this time " +
        		"(optional - lights will turn off ${sunriseOffset_minutes} minutes ${sunriseOffset_BeforeAfter} next sunrise if no time is entered)") {
        	input "timeOff", "time", title: "Time to turn lights off?", required: false
        }
    	section("Set a different time to turn off the lights on each day (optional - lights will turn off at the default time if not set)") {
        	input "sundayOff", "time", title: "Sunday", required: false
            input "mondayOff", "time", title: "Monday", required: false
            input "tuesdayOff", "time", title: "Tuesday", required: false
            input "wednesdayOff", "time", title: "Wednesday", required: false
            input "thursdayOff", "time", title: "Thursday", required: false
            input "fridayOff", "time", title: "Friday", required: false
            input "saturdayOff", "time", title: "Saturday", required: false
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
            input "randOn", "number", title: "Random ON window (minutes)?", required: false
            input "randOff", "number", title: "Random OFF window (minutes)?", required: false
        }
        section("The settings above are used to randomize preset times such that lights will " +
        	"turn on/off at slightly different times from one day to another, but if multiples lights " +
            "are selected, they will still switch status at the same time. Use the options below " +
            "to insert a random delay between the switching of each individual light. " +
            "This option can be used independently of the ones above.") {
            input "onDelay", "bool", title: "Delay switch-on?", required: false, submitOnChange: true
            input "offDelay", "bool", title: "Delay switch-off?", required: false, submitOnChange: true
            if (onDelay || offDelay) {
            	input "delaySeconds", "number", title: "Switching delay", description: "Choose 1-60 seconds", required: true, defaultValue: 5, range: "1..60"
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
            href "pageUninstall", title: "", description: "Uninstall this SmartApp", image: "https://cdn0.iconfinder.com/data/icons/social-messaging-ui-color-shapes/128/trash-circle-red-512.png", state: null, required: true
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
	scheduleTurnOn(location.currentValue("sunsetTime"))
    scheduleTurnOff(location.currentValue("sunriseTime"))
    //TODO: subscribe to lights on/off events IF commanded by this app (and log events)
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    subscribe(location, "sunsetTime", sunsetTimeHandler)	//triggers at sunset, evt.value is the sunset String (time for next day's sunset)
    subscribe(location, "sunriseTime", sunriseTimeHandler)	//triggers at sunrise, evt.value is the sunrise String (time for next day's sunrise)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    //TODO: subscribe to lights on/off events IF commanded by this app (and log events)
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def sunsetTimeHandler(evt) {
    debug "sunsetTimeHandler event: ${evt.descriptionText}", "trace", 1
    debug "next sunset will be ${evt.value}"
	scheduleTurnOn(evt.value)
    debug "sunsetTimeHandler complete", "trace", -1
}

def sunriseTimeHandler(evt) {
    debug "sunriseTimeHandler event: ${evt.descriptionText}", "trace", 1
    debug "next sunrise will be ${evt.value}"
    scheduleTurnOff(evt.value)
    debug "sunriseTimeHandler complete", "trace", -1
}    

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def scheduleTurnOn(sunsetString) {
    debug "executing scheduleTurnOn(sunsetString: ${sunsetString})", "trace", 1
	
    def datSunset = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)
    debug "sunset date: ${datSunset}"

    //calculate the offset
    def offsetTurnOn = sunsetOffset ? sunsetOffset * 60 * 1000 : 0 //convert offset to ms
	def datTurnOn = new Date(datSunset.time + offsetTurnOn)

    //apply random factor
    if (randOn) {
        def random = new Random()
        def randOffset = random.nextInt(randOn)
        datTurnOn = new Date(datTurnOn.time - (randOn * 30000) + (randOffset * 60000))
	}
    
	//schedule this to run once (it will trigger again at next sunset)
	debug "scheduling lights ON for: ${datTurnOn}", "info"
    runOnce(datTurnOn, turnOn, [overwrite: false])
    debug "scheduleTurnOn() complete", "trace", -1
}

def scheduleTurnOff(sunriseString) {
    debug "executing scheduleTurnOff(sunriseString: ${sunriseString})", "trace", 1
    def DOW_TurnOff = weekdayTurnOffTime
    def default_TurnOff = defaultTurnOffTime
    def datTurnOff

    //select which turn-off time to use (1st priority: weekday-specific, 2nd: default, 3rd: sunrise)
    if (DOW_TurnOff) {
    	debug "using the weekday turn-off time", "info"
        datTurnOff = DOW_TurnOff
    } else if (default_TurnOff) {
    	debug "using the default turn-off time", "info"
    	datTurnOff = default_TurnOff
    } else {
    	debug "user didn't specify turn-off time; using sunrise time", "info"
        def datSunrise = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
        debug "sunrise date : $datSunrise"
        def sunriseOffset = C_SUNRISE_OFFSET()
        datTurnOff = new Date(datSunrise.time + (15 * 60 * 1000)) //apply sunrise offset
    }
    state.turnOff = datTurnOff.time //store the scheduled OFF time in State so we can use it later to compare it to the ON time
	debug "scheduling lights OFF for: ${datTurnOff}", "info"
    runOnce(datTurnOff, turnOff, [overwrite: false])
    debug "scheduleTurnOff() complete", "trace", -1
}

def turnOn() {
    //check that the scheduled turn-off time is in the future (for example, if the lights are
    //scheduled to turn on at 20:23 based on the sunset time, but the user had them set to turn
    //off at 20:00, the turn-off will fire before the lights are turned on. In that case, the
    //lights would still turn on at 20:23, but they wouldn't turn off until the next day at 20:00.
    debug "executing turnOn()", "trace", 1
	
    def minTimeOn = C_MIN_TIME_ON()
    def nowTime = now() + (minTimeOn * 60 * 1000) //making sure lights will stay on for at least 'minTimeOn'
    def offTime = state.turnOff //retrieving the turn-off time from State
    if (offTime < nowTime) {
		debug "scheduled turn-off time has already passed; turn-on cancelled", "info"
	} else {
        debug "turning lights on", "info"
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

def getDefaultTurnOffTime() {
	debug "start evaluating defaultTurnOffTime", "trace", 1
    if (timeOff) {
    	//convert preset time to today's date
        def default_TurnOffTime = timeTodayAfter(new Date(), timeOff, location.timeZone)
        
        //apply random factor to turnoff time
        if (randOff) {
	    	def random = new Random()
			def randOffset = random.nextInt(randOff)
            default_TurnOffTime = new Date(default_TurnOffTime.time - (randOff * 30000) + (randOffset * 60000))
            debug "randomized default turn-off time: $default_TurnOffTime"
        } else {
        	debug "default turn-off time: $default_TurnOffTime"
        }
        return default_TurnOffTime
    } else {
        debug "default turn-off time not specified"
        return false
	}
	debug "finished evaluating defaultTurnOffTime", "trace", -1
}

def getWeekdayTurnOffTime() {
    //calculate weekday-specific offtime
    //this executes at sunrise, so when the sun rises on Tuesday, it will
    //schedule the lights' turn-off time for Tuesday night
	debug "start evaluating weekdayTurnOffTime", "trace", 1

	def nowDOW = new Date().format("E") //find out current day of week

    //find out the preset (if entered) turn-off time for the current weekday
    def DOW_Off
    if (sundayOff && nowDOW == "Sun") {
        DOW_Off = sundayOff
    } else if (mondayOff && nowDOW == "Mon") {
        DOW_Off = mondayOff
    } else if (tuesdayOff && nowDOW == "Tue") {
        DOW_Off = tuesdayOff
    } else if (wednesdayOff && nowDOW == "Wed") {
        DOW_Off = wednesdayOff
    } else if (thursdayOff && nowDOW == "Thu") {
        DOW_Off = thursdayOff
    } else if (fridayOff && nowDOW == "Fri") {
        DOW_Off = fridayOff
    } else if (saturdayOff && nowDOW == "Sat") {
        DOW_Off = saturdayOff
    }

	if (DOW_Off) {
    	//convert preset time to today's date
    	def DOW_TurnOffTime = timeTodayAfter(new Date(), DOW_Off, location.timeZone)
        
        //apply random factor to turnoff time
		if (randOff) {
        	def random = new Random()
            def randOffset = random.nextInt(randOff)
            DOW_TurnOffTime = new Date(DOW_TurnOffTime.time - (randOff * 30000) + (randOffset * 60000))
            debug "randomized DOW turn-off time: $DOW_TurnOffTime"
        } else {
        	debug "DOW turn-off time: $DOW_TurnOffTime"
        }
        return DOW_TurnOffTime
    } else {
    	debug "DOW turn-off time not specified"
        return false
    }
	debug "finished evaluating weekdayTurnOffTime", "trace", -1
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