/**
 *  Away Light
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
 	 def versionNum() {	return "version 1.41" }       /*
 *
 *    v1.41 (04-Nov-2016): update href state & images
 *	  v1.40 (03-Nov-2016): add option to configure sunset offset
 *    v1.31 (02-Nov-2016): add link for Apache license
 *    v1.30 (02-Nov-2016): implement multi-level debug logging function
 *	  v1.20 (01-Nov-2016): standardize pages layout
 *	  v1.11 (01-Nov-2016): standardize section headers
 *	  v1.10 (27-Oct-2016): change layout of preferences pages, default value for app name
 *    v1.02 (26-Oct-2016): added trace for each event handler
 *    v1.01 (26-Oct-2016): added 'About' section in preferences
 *    v1.00 (2016 date unknown): working version, no version tracking up to this point
 *
*/
definition(
    name: "Away Light",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn a light on/off to simulate presence while away",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
	page(name: "pageSchedule")
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
        	paragraph "", title: "This SmartApp turns a light on/off simulate presence while away."
        }
        section() {
            input "theLight", "capability.switch", title: "Which light?", multiple: false, required: true, submitOnChange: true
            if (theLight) {
                href "pageSchedule", title: "Set scheduling Options", image: "http://cdn.device-icons.smartthings.com/Office/office7-icn.png", required: false
        	}
        }
		section() {
			if (theLight) {
            	href "pageSettings", title: "App settings", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", required: false
            }
		}
    }
}

def pageSchedule() {
	dynamicPage(name: "pageSchedule", install: false, uninstall: false) {
        section(){
        	paragraph title: "Scheduling Options",
            	"Use the options on this page to set the scheduling options for the ${theLight.label}"
        }
        section("Restrict automation to certain times (optional)") {
            input "startTime", "time", title: "Start time?", required: false
            input "endTime", "time", title: "End time?", required: false
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
        section("Set light on/off duration - use these settings to have the light turn on and off within the activation period") {
            input "onFor", "number", title: "Stay on for (minutes)?", required: false //If set, the light will turn off after the amount of time specified (or at specified end time, whichever comes first)
            input "offFor", "number", title: "Leave off for (minutes)?", required: false //If set, the light will turn back on after the amount of time specified (unless the specified end time has passed)
        }
        section("Random factor - if set, randomize on/off times within the selected window") {
        	input "randWind", "number", title: "Random window (minutes)?", required: false
        }
        section("Enable only for certain days of the week? (optional - will run every day if nothing selected)") {
        	input "theDays", "enum", title: "On which days?", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
        }
        section("Only run in selected modes (automation disabled if none selected)") {
            input "theModes", "mode", title: "Select the mode(s)", multiple: true, required: false
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
			label title: "Assign a name", defaultValue: "${app.name} - ${theLight.label}", required: false
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
        	paragraph parent ? "CAUTION: You are about to unschedule the '${theLight.label}'. This action is irreversible. If you want to proceed, tap on the 'Remove' button below." : "CAUTION: You are about to completely remove the SmartApp '${app.name}' and all of its schedules. This action is irreversible. If you want to proceed, tap on the 'Remove' button below.",
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
	if (state.appOn) {
    	theLight.off()
        state.appOn = false
        }
    state.debugLevel = 0
    debug "application uninstalled", "trace"
}

def initialize() {
    state.debugLevel = 0
    debug "initializing", "trace", 1
    state.appOn = false
    theLight.off()
    subscribeToEvents()
	schedTurnOn()
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    //TODO: subscribe to lights on/off events IF commanded by this app (and log events)
    subscribe(location, "mode", modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def modeChangeHandler(evt) {
    debug "modeChangeHandler event: ${evt.descriptionText}", "trace", 1
    if(modeOk) {
        debug "mode changed to ${location.currentMode}; calling schedTurnOn()"
        schedTurnOn()
    } else {
        debug "mode changed to ${location.currentMode}; cancelling scheduled tasks"
        unschedule()
    }
    debug "modeChangeHandler complete", "trace", -1
}

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def schedTurnOn(offForDelay) {
    //determine turn-on time and schedule the turnOn() that will verify the remaining conditions before turning the light on
	debug "executing schedTurnOn(offForDelay: ${offForDelay})", "trace", 1
	
    def random = new Random()
    
    if (offForDelay) {
        //method was called from turnOff() to turn the light back on after the "offFor" delay
        if (randWind) {
            def rdmDelay = random.nextInt(randWind)
            offForDelay = offForDelay - (randWind * 30000) + (rdmDelay * 60000)
            offForDelay = Math.max(0, offForDelay) //make sure that we don't end up with a negative number
		}
        def onDate = new Date(now() + offForDelay)
        debug "calculated ON time for turning the light back on after the 'off for' delay of ${convertToHMS(offForDelay)} : ${onDate}"
        runOnce(onDate, turnOn)
	} else {   
        def onDate = schedOnDate()
        def nowDate = new Date()
        
        //set a delay of up to 2 min to be applied when requested to turn on now
        def maxDelay = 2 * 60 * 1000
        def delayOnNow = random.nextInt(maxDelay)
        
        if (!onDate) {
            //no turn-on time set, call method to turn light on now; whether or not it actually turns on will depend on dow/mode
            debug "no turn-on time specified; calling to turn the light on in ${convertToHMS(delayOnNow)}"
            turnOn(delayOnNow)
        } else {
            if (onDate < nowDate) {
                debug "scheduled turn-on time of ${onDate} has already passed; calling to turn the light on in ${convertToHMS(delayOnNow)}"
                turnOn(delayOnNow)
            } else {
                debug "scheduling the light to turn on at ${onDate}"
                runOnce(onDate, turnOn)
            }
        }
    }
    debug "schedTurnOn() complete", "trace", -1
}

def turnOn(delay) {
	//check conditions and turn on the light
    debug "executing turnOn(delay: ${delay})", "trace", 1

    def tz = location.timeZone
    def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
	def strDOW = nowDOW
    def DOWOk = !theDays || theDays?.contains(strDOW)
    def darkOk = !whenDark || itsDarkOut

	if (modeOk && DOWOk && darkOk) {
        def nowDate = new Date(now() + (randWind * 30000)) //add 1/2 random window to current time to enable the light to come on around the sunset time
        def offDate = schedOffDate()
        def timeOk = offDate > nowDate
        if (timeOk) {    	
            delay = delay ?: 0
            debug "we're good to go; turning the light on in ${convertToHMS(delay)}"
            state.appOn = true
            theLight.on(delay: delay)
            schedTurnOff(delay, offDate)
        } else {
            debug "the light's turn-off time has already passed; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, schedTurnOn)
        }
    } else {
        if (!modeOk) {
    		debug "light activation is not enabled in current mode; check again at mode change"
    	} else if (!DOWOk) {
            debug "light activation is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, schedTurnOn)
        } else if (!darkOk) {
        	def sunTime = getSunriseAndSunset(sunsetOffset: sunsetOffset)
            def sunsetDate = sunTime.sunset
            //add random factor
			if (randWind) {
            	def random = new Random()
                def rdmOffset = random.nextInt(randWind)
                sunsetDate = new Date(sunsetDate.time - (randWind * 30000) + (rdmOffset * 60000))
            }
            debug "light activation is not enabled during daytime; check again at sunset (${sunsetDate})"
            runOnce(sunsetDate, schedTurnOn)
        }
	}
    debug "turnOn() complete", "trace", -1
}	

def schedTurnOff(onDelay, offDate) {
    //determine turn-off time and schedule the turnOff()
	debug "executing schedTurnOff(onDelay: ${onDelay}, offDate: ${offDate})", "trace", 1

    def nowDate = new Date()
    def random = new Random()
    
    //re-calculate the turn-off time if a light-on duration was specified
    if (onFor) {
    	def lightOnFor = onFor * 60 * 1000
        if (randWind) {
            def rdmDelay = random.nextInt(randWind)
            lightOnFor = lightOnFor - (randWind * 30000) + (rdmDelay * 60000)
            lightOnFor = Math.max(0, lightOnFor) //make sure that we don't end up with a negative number
		}
        def endOnFor = new Date(now() + lightOnFor + onDelay)
        debug "calculated OFF time for turning the light off after the 'on for' delay of ${convertToHMS(lightOnFor)} : ${endOnFor}"
        offDate = offDate && (offDate < endOnFor) ? offDate : endOnFor
    }
    
    if (offDate) {
        if (offDate > nowDate) {
            debug "scheduling turn-off of the light to occur at ${offDate}"
            runOnce(offDate, turnOff)
        } else {
        	def maxDelay = 2 * 60 * 1000 //set a delay of up to 2 min to be applied when requested to turn off now
            def delayOffNow = random.nextInt(maxDelay)
            debug "the calculated turn-off time has already passed; calling for the light to turn off in ${convertToHMS(delayOffNow)}"
            turnOff(delayOffNow)
        }
    } else {
        debug "no turn-off time specified"
    }
    debug "schedTurnOff() complete", "trace", -1
}

def turnOff(delay) {
	debug "executing turnOff(delay: ${delay})", "trace", 1
    if (state.appOn == true) {
        delay = delay ?: 0
        debug "turning off the light in ${convertToHMS(delay)}"
        theLight.off(delay: delay)
        state.appOn = false
        if (offFor) {
            def offForDelay = offFor * 60 * 1000
            if (randWind) {
                def random = new Random()
                def rdmOffset = random.nextInt(randWind)
                offForDelay = offForDelay - (randWind * 30000) + (rdmOffset * 60000)
                offForDelay = Math.max(0, offForDelay) //make sure that we don't end up with a negative number
			}
            schedTurnOn(offForDelay)
        } else {
        	def tz = location.timeZone
            def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
            debug "the light isn't scheduled to turn back on today; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, schedTurnOn)
        }
    } else {
		debug "the light wasn't turned on by this app; doing nothing"
    }
    debug "turnOff() complete", "trace", -1
}

def terminate() {
	//For each configured light that was turned on by this app, turn the light off after a random delay.
    //Called when it's detected that the conditions are no longer valid
	debug "executing terminate()", "trace", 1
    def random = new Random()
    def maxDelay = 2 * 60 * 1000
   	if (state.appOn) {
        def delay = random.nextInt(maxDelay)
        debug "turning off the light in ${convertToHMS(delay)}"
        theLight.off(delay: delay)
        state.appOn = false
    }
    debug "terminate() complete", "trace", -1
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getModeOk() {
	def result = theModes?.contains(location.mode)
	debug ">> modeOk : $result"
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

def schedOnDate() {
    // ***  CALCULATE TURN-ON TIME  ***
    //figure out the next 'on' time based on user settings
	debug "start evaluating schedOnDate", "trace", 1
    
    def tz = location.timeZone
	def random = new Random()
    def onDate = startTime ? timeToday(startTime, tz) : null
	
    debug "user-configured turn-on time : ${onDate}"
    
    if (randWind && onDate) {
        //apply random factor to onDate
        def rdmOffset = random.nextInt(randWind)
        onDate = new Date(onDate.time - (randWind * 30000) + (rdmOffset * 60000))
        debug "random-adjusted turn-on time : ${onDate}"
    } else {
        debug "no random factor configured in preferences"
    }
    
    return onDate
	debug "finished evaluating schedOnDate", "trace", -1
}

def schedOffDate() {
    // ***  CALCULATE TURN-OFF TIME  ***
    //figure out the light's 'off' time based on user settings
	debug "start evaluating schedOffDate", "trace", 1
    
    def tz = location.timeZone
	def random = new Random()
    def offDate = endTime ? timeToday(endTime, tz) : null
    
    //get the earliest of user-preset start time and sunrise time
    if (whenDark) {
        def sunriseString = location.currentValue("sunriseTime") //get the next sunrise time string
        def sunriseDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
		debug "comparing end time (${offDate}) to sunrise time (${sunriseDate})"
		offDate = offDate && (offDate < sunriseDate) ? offDate : sunriseDate
    }
    
    debug "calculated turn-off time : ${offDate}"
    
    if (randWind && offDate) {
        //apply random factor to offDate
        def rdmOffset = random.nextInt(randWind)
        def offTime = offDate.time - (randWind * 30000) + (rdmOffset * 60000)
        offDate = new Date(offTime)
        debug "random-adjusted turn-off time : ${offDate}"
    } else {
        debug "no random factor configured in preferences"
    }
    
    return offDate
	debug "finished evaluating schedOffDate", "trace", -1
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