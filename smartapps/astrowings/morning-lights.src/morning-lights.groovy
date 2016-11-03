/**
 *  Morning Lights
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
 	 def versionNum() {	return "version 2.21" }       /*
 *
 *   v2.21 (02-Nov-2016): add link for Apache license
 *   v2.20 (02-Nov-2016): implement multi-level debug logging function
 *   v2.10 (01-Nov-2016): standardize pages layout
 *	 v2.01 (01-Nov-2016): standardize section headers
 *   v2.00 (29-Oct-2016): inital version base code adapted from 'Sunset Lights - v2'
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
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***



//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp turns on selected lights at a specified time and turns them off at sunrise. " +
            	"Different turn-on times can be configured for each day of the week, and they can be " +
                "randomized within a specified window to simulate manual activation."
        }
        section() {
            input "theLights", "capability.switch", title: "Which lights?", description: "Choose the lights to turn on", multiple: true, required: true, submitOnChange: true
            if (theLights) {
                href "pageSchedule", title: "Set scheduling options", required: false
                href "pageRandom", title: "Configure random scheduling", required: false
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
            input "defaultOn", "time", title: "Default time?", required: false
        }
        //TODO: add option to specify turn-off time
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
            input "randOn", "number", title: "Random ON window (minutes)?", required: false //TODO: valid range
            input "randOff", "number", title: "Random OFF window (minutes)?", required: false //TODO: valid range
        }
        section("The settings above are used to randomize preset times such that lights will " +
        	"turn on/off at slightly different times from one day to another, but if multiples lights " +
            "are selected, they will still switch status at the same time. Use the options below " +
            "to insert a random delay between the switching of each individual light. " +
            "This option can be used independently of the ones above.") {
            input "onDelay", "bool", title: "Delay switch-on?", required: false
            input "offDelay", "bool", title: "Delay switch-off?", required: false
            input "delaySeconds", "number", title: "Delay switching by up to (seconds)?", required: true, defaultValue: 10 //TODO: specify valid range, not required (move default value to method), description default value, use constant for default value
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
    debug "sunriseTimeHandler event: ${evt.descriptionText}", "trace", 1
    def sunriseTimeHandlerMsg = "triggered sunriseTimeHandler; next sunrise will be ${evt.value}"
    debug "sunriseTimeHandlerMsg : $sunriseTimeHandlerMsg"
    schedTurnOff(evt.value)
    debug "sunriseTimeHandler complete", "trace", -1
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

    //apply random factor
    if (randOff) {
        def random = new Random()
        def randOffset = random.nextInt(randOff)
        datTurnOff = new Date(datTurnOff.time - (randOff * 30000) + (randOffset * 60000))
	}
    
	debug "scheduling lights OFF for: ${datTurnOff}", "info"
    // This method gets called at sunrise to schedule next day's turn-off. Because of the random factor,
    // today's turn-off could actually be scheduled after sunrise (therefore after this method gets called),
    // so we use [overwrite: false] to prevent today's scheduled turn-off from being overwriten.
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
        
        //check that turn-on is scheduled earlier than turn-off by at least 10 minutes
        def safeOff = datTurnOff.time - (10 * 60 * 1000) //subtract 10 minutes from scheduled turn-off time
        if (datTurnOn.time < safeOff) {
            debug "scheduling lights ON for: ${datTurnOn} (${useTime})", "info"
            runOnce(datTurnOn, turnOn)
        } else {
        	debug "scheduling cancelled because tomorrow's turn-on time (${datTurnOn}) " +
            	"would be later than (or less than 10 minutes before) " +
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
            info "turning off the ${theLight.label} in ${convertToHMS(newDelay)}", "info"
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
        return timeOn
    } else {
        debug "default turn-on time not specified"
        return false
	}
	debug "finished evaluating defaultTurnOnTime", "trace", -1
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
        return tmrOn
    } else {
    	debug "DOW turn-on time not specified"
        return false
    }
	debug "finished evaluating DOWTurnOnTime", "trace", -1
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