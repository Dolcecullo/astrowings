/**
 *  Away Light
 *
 *  Copyright © 2016 Phil Maynard
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0												*/
 	       def urlApache() { return "http://www.apache.org/licenses/LICENSE-2.0" }			/*
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *   --------------------------------
 *   ***   VERSION HISTORY  ***
 *
 *	  v2.31 (09-Aug-2018) - standardize debug log types
 *						  - change category to 'convenience'
 *						  - standardize layout of app data and constant definitions
 *						  - convert hard-coded value for 'onNowRandom' into constant
 *    v2.30 (18-Oct-2017) - add call to terminate() method to turn light off when mode changes to one that isn't enabled
 *    v2.21 (09-Jan-2017) - add schedule to run schedTurnOn() daily
 *    v2.20 (29-Dec-2016) - add user-configurable activation delay after mode changes
 *	  v2.10 (14-Nov-2016) - create reinit() method to allow parent to re-initialize all child apps
 *						  - bug fix: specify int data type to strip decimals when using the result of a division
 * 							to obtain a date, which returns the following error if trying to convert a decimal:
 *							Could not find matching constructor for: java.util.Date(java.math.BigDecimal)
 *    v2.00 (14-Nov-2016) - code improvement: store images on GitHub, use getAppImg() to display app images
 *                        - added option to disable icons
 *                        - added option to disable multi-level logging
 *                        - configured default values for app settings
 *						  - moved 'About' to its own page
 *						  - added link to readme file
 *						  - bug fix: removed log level increase for modeChangeHandler() event because it was causing the log
 *							level to keep increasing without ever applying the '-1' at the end to restore the log level
 *						  - added parent definition to convert into child app
 *						  - list current configuration settings in link to configuration page
 *						  - moved sunset offset setting to parent app
 *						  - moved debugging options settings to parent app
 *    v1.31 (04-Nov-2016) - update href state & images
 *	  v1.30 (03-Nov-2016) - add option to configure sunset offset
 *    v1.21 (02-Nov-2016) - add link for Apache license
 *    v1.20 (02-Nov-2016) - implement multi-level debug logging function
 *	  v1.14 (01-Nov-2016) - code improvement: standardize pages layout
 *	  v1.13 (01-Nov-2016) - code improvement: standardize section headers
 *	  v1.12 (27-Oct-2016) - change layout of preferences pages, default value for app name
 *    v1.11 (26-Oct-2016) - code improvement: added trace for each event handler
 *    v1.10 (26-Oct-2016) - added 'About' section in preferences
 *    v1.00               - initial release, no version tracking up to this point
 *
*/
definition(
    parent: "astrowings:Away Lights",
    name: "Away Light",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn a light on/off to simulate presence while away",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn@3x.png")


//   --------------------------------
//   ***   APP DATA  ***

def		versionNum()			{ return "version 2.31" }
def		versionDate()			{ return "07-Aug-2018" }     
def		gitAppName()			{ return "away-light" }
def		gitOwner()				{ return "astrowings" }
def		gitRepo()				{ return "SmartThings" }
def		gitBranch()				{ return "master" }
def		gitAppFolder()			{ return "smartapps/${gitOwner()}/${gitAppName()}.src" }
def		appImgPath()			{ return "https://raw.githubusercontent.com/${gitOwner()}/${gitRepo()}/${gitBranch()}/images/" }
def		readmeLink()			{ return "https://github.com/${gitOwner()}/SmartThings/blob/master/${gitAppFolder()}/readme.md" } //TODO: convert to httpGet?
def		changeLog()				{ return getWebData([uri: "https://raw.githubusercontent.com/${gitOwner()}/${gitRepo()}/${gitBranch()}/${gitAppFolder()}/changelog.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

	 	//name					value					description
def		C_ON_NOW_RANDOM()		{ return 2 } 			//set the max value for the random delay to be applied when requesting to turn on now (minutes)


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
	page(name: "pageSchedule")
}


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
	dynamicPage(name: "pageMain", install: true, uninstall: false) {
        section() {
            input "theLight", "capability.switch", title: "Which light?", multiple: false, required: true, submitOnChange: true
            if (theLight) {
                href "pageSchedule", title: !theModes ? "Set scheduling options" : "Scheduling options:", description: schedOptionsDesc, image: getAppImg("office7-icn.png"), required: true, state: theModes ? "complete" : null
        	}
		}
		section() {
			if (theLight) {
            	label title: "Assign a name for this automation", defaultValue: "${theLight.label}", required: false
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
        section("Set light on/off duration - use these settings to have the light turn on and off within the activation period") {
            input "onFor", "number", title: "Stay on for (minutes)?", required: false, defaultValue: 25 //If set, the light will turn off after the amount of time specified (or at specified end time, whichever comes first)
            input "offFor", "number", title: "Leave off for (minutes)?", required: false, defaultValue: 40 //If set, the light will turn back on after the amount of time specified (unless the specified end time has passed)
        }
        section("Random factor - if set, randomize on/off times within the selected window") {
        	input "randomMinutes", "number", title: "Random window (minutes)?", required: false, defaultValue: 20
        }
        section("Enable only for certain days of the week? (optional - will run every day if nothing selected)") {
        	input "theDays", "enum", title: "On which days?", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
        }
        section("Only run in selected modes (automation disabled if none selected)") {
            input "theModes", "mode", title: "Select the mode(s)", multiple: true, required: false, submitOnChange: true
            if (theModes) {
                input "activationDelay", "number", title: "Activation delay", required: false, defaultValue: 2, range: "0..60"
            }
        }
    	section("Enable even during daytime") {
        	input "daytime", "bool", title: "Yes/No?", required: false, defaultValue: false
        }
        /*
        if (!daytime) {
            section("This SmartApp uses illuminance as a criteria to trigger actions; select the illuminance-capable " +
                    "device to use (if none selected, sunset/sunrise times will be used instead.",
                    hideWhenEmpty: true, required: true, state: (theLuminance ? "complete" : null)) {
                //TODO: implement use of illuminance device
                input "theLuminance", "capability.illuminanceMeasurement", title: "Which illuminance device?", multiple: false, required: false, submitOnChange: true
            }
        }
        */
	}
}


//   ---------------------------------
//   ***   PAGES SUPPORT METHODS   ***

def getSchedOptionsDesc() {
    def strStartTime = startTime?.substring(11,16)
    def strEndTime = endTime?.substring(11,16)
    def strDesc = ""
    strDesc += (strStartTime || strEndTime)		? " • Time:\n" : ""
    strDesc += (strStartTime && !strEndTime)	? "   └ from: ${strStartTime}\n" : ""
    strDesc += (!strStartTime && strEndTime)	? "   └ until: ${strEndTime}\n" : ""
    strDesc += (strStartTime && strEndTime)		? "   └ between ${strStartTime} and ${strEndTime}\n" : ""
    strDesc += (onFor || offFor)				? " • Duration:\n" : ""
    strDesc += (onFor && !offFor)				? "   └ stay on for: ${onFor} minutes\n" : ""
    strDesc += (onFor && offFor)				? "   └ ${onFor} minutes on / ${offFor} off\n" : ""
    strDesc += randomMinutes					? " • ${randomMinutes}-min random window\n" : ""
    strDesc += theDays							? " • Only on selected days\n   └ ${theDays}\n" : " • Every day\n"
    strDesc += theModes							? " • While in modes:\n   └ ${theModes}\n   └ delay: ${activationDelay} minutes\n" : ""
    strDesc += daytime	 						? " • Enabled during daytime\n" : ""
    return theModes ? strDesc : "No modes selected; automation disabled"
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
    debug "initializing", "trace", 1
    state.debugLevel = 0
    state.appOn = false
    theLight.off()
    subscribeToEvents()
    debug "initialization complete", "trace", -1
	schedTurnOn()
    schedule("0 0 4 1/1 * ?", schedTurnOn) //run schedule at 04:00 daily
}

def reinit() {
    debug "refreshed with settings ${settings}", "trace"
    state.debugLevel = 0
    initialize()
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    subscribe(location, "mode", modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def modeChangeHandler(evt) {
    debug "modeChangeHandler event: ${evt.descriptionText}", "trace"
    if(modeOk) {
        int delay = activationDelay ? activationDelay * 60 : 5
        debug "mode changed to ${location.currentMode}; calling schedTurnOn() in ${delay} seconds", "info"
        runIn(delay,schedTurnOn)
    } else {
        debug "mode changed to ${location.currentMode}; cancelling scheduled tasks", "info"
        unschedule()
        terminate()
    }
    debug "modeChangeHandler complete", "trace"
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
        if (randomMinutes) {
            int rangeUser = randomMinutes * 60000 //convert user random window from minutes to ms
            int rangeMax = 2 * offForDelay //the maximum random window is 2 * offForDelay
            int range = rangeUser < rangeMax ? rangeUser : rangeMax //limit the random window to 2 * offForDelay
            int rdmOffset = random.nextInt(range)
            offForDelay = (int)(offForDelay - range/2 + rdmOffset)
		}
        def onDate = new Date(now() + offForDelay)
        debug "calculated ON time for turning the light back on after the 'off for' delay of ${convertToHMS(offForDelay)} : ${onDate}", "info"
        runOnce(onDate, turnOn)
	} else {   
        def onDate = schedOnDate()
        def nowDate = new Date()
        
        //set a random delay of up to 'C_ON_NOW_RANDOM()' min to be applied if requesting to turn on now
        def onNowRandom = C_ON_NOW_RANDOM()
        def onNowRandomMS = onNowRandom * 60 * 1000
        def onNowDelay = random.nextInt(onNowRandomMS)
        
        if (!onDate) {
            //no turn-on time set, call method to turn light on now; whether or not it actually turns on will depend on dow/mode
            debug "no turn-on time specified; calling to turn the light on in ${convertToHMS(onNowDelay)}", "info"
            turnOn(onNowDelay)
        } else {
            if (onDate < nowDate) {
                debug "scheduled turn-on time of ${onDate} has already passed; calling to turn the light on in ${convertToHMS(onNowDelay)}", "info"
                turnOn(onNowDelay)
            } else {
                debug "scheduling the light to turn on at ${onDate}", "info"
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
    def darkOk = daytime || itsDarkOut

	if (modeOk && DOWOk && darkOk) {
        def nowDate = new Date(now() + (randomMinutes * 30000)) //add 1/2 random window to current time to enable the light to come on around the sunset time
        def offDate = schedOffDate()
        def timeOk = offDate > nowDate
        if (timeOk) {    	
            delay = delay ?: 0
            debug "we're good to go; turning the light on in ${convertToHMS(delay)}", "info"
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
        	def sunTime = getSunriseAndSunset(sunsetOffset: parent.sunsetOffset)
            def sunsetDate = sunTime.sunset
            //add random factor
			if (randomMinutes) {
            	def random = new Random()
                def rdmOffset = random.nextInt(randomMinutes)
                sunsetDate = new Date(sunsetDate.time - (randomMinutes * 30000) + (rdmOffset * 60000))
            }
            debug "light activation is not enabled during daytime; check again at sunset (${sunsetDate})" //TODO: if using illuminance, subscribe to the sensor and check again when dark
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
    	int lightOnFor = onFor * 60 * 1000
        if (randomMinutes) {
            int rangeUser = randomMinutes * 60000 //convert user random window from minutes to ms
            int rangeMax = 2 * lightOnFor //the maximum random window is 2 * lightOnFor
            int range = rangeUser < rangeMax ? rangeUser : rangeMax //limit the random window to 2 * lightOnFor
            int rdmOffset = random.nextInt(range)
            lightOnFor = lightOnFor - range/2 + rdmOffset
		}
        def endOnFor = new Date(now() + lightOnFor + onDelay)
        debug "calculated OFF time for turning the light off after the 'on for' delay of ${convertToHMS(lightOnFor)} : ${endOnFor}", "info"
        offDate = offDate && (offDate < endOnFor) ? offDate : endOnFor
    }
    
    if (offDate) {
        if (offDate > nowDate) {
            debug "scheduling turn-off of the light to occur at ${offDate}", "info"
            runOnce(offDate, turnOff)
        } else {
        	def maxDelay = 2 * 60 * 1000 //set a delay of up to 2 min to be applied when requested to turn off now
            def delayOffNow = random.nextInt(maxDelay)
            debug "the calculated turn-off time has already passed; calling for the light to turn off in ${convertToHMS(delayOffNow)}", "info"
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
        debug "turning off the light in ${convertToHMS(delay)}", "info"
        theLight.off(delay: delay)
        state.appOn = false
        if (offFor) {
            int offForDelay = offFor * 60 * 1000
            if (randomMinutes) {
                def random = new Random()
                int rangeUser = randomMinutes * 60000 //convert user random window from minutes to ms
                int rangeMax = 2 * offForDelay //the maximum random window is 2 * offForDelay
                int range = rangeUser < rangeMax ? rangeUser : rangeMax //limit the random window to 2 * offForDelay
                int rdmOffset = random.nextInt(range)
                offForDelay = offForDelay - range/2 + rdmOffset
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
        debug "turning off the light in ${convertToHMS(delay)}", "info"
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
    
    if (randomMinutes && onDate) {
        //apply random factor to onDate
        def rdmOffset = random.nextInt(randomMinutes)
        onDate = new Date(onDate.time - (randomMinutes * 30000) + (rdmOffset * 60000))
        debug "random-adjusted turn-on time : ${onDate}"
    } else {
        debug "no random factor configured in preferences"
    }
    
	debug "finished evaluating schedOnDate", "trace", -1
    return onDate
}

def schedOffDate() {
    // ***  CALCULATE TURN-OFF TIME  ***
    //figure out the light's 'off' time based on user settings
	debug "start evaluating schedOffDate", "trace", 1
    
    def tz = location.timeZone
	def random = new Random()
    def offDate = endTime ? timeToday(endTime, tz) : null
    
    //get the earliest of user-preset start time and sunrise time
    if (!daytime) {
        def sunriseString = location.currentValue("sunriseTime") //get the next sunrise time string
        def sunriseDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
		debug "comparing end time (${offDate}) to sunrise time (${sunriseDate})"
		offDate = offDate && (offDate < sunriseDate) ? offDate : sunriseDate
    }
    
    debug "calculated turn-off time : ${offDate}"
    
    if (randomMinutes && offDate) {
        //apply random factor to offDate
        def rdmOffset = random.nextInt(randomMinutes)
        def offTime = offDate.time - (randomMinutes * 30000) + (rdmOffset * 60000)
        offDate = new Date(offTime)
        debug "random-adjusted turn-off time : ${offDate}"
    } else {
        debug "no random factor configured in preferences"
    }
    
	debug "finished evaluating schedOffDate", "trace", -1
    return offDate
}


//   ------------------------
//   ***   COMMON UTILS   ***

def getItsDarkOut() { //implement use of illuminance capability
    def sunTime = getSunriseAndSunset(sunsetOffset: parent.sunsetOffset)
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

def getAppImg(imgName, forceIcon = null) {
	def imgPath = appImgPath()
    return (!parent.noAppIcons || forceIcon) ? "$imgPath/$imgName" : ""
}

def getWebData(params, desc, text=true) {
	try {
		debug "trying getWebData for ${desc}"
		httpGet(params) { resp ->
			if(resp.data) {
				if(text) {
					return resp?.data?.text.toString()
				} else { return resp?.data }
			}
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			debug "${desc} file not found", "warn"
		} else {
			debug "getWebData(params: $params, desc: $desc, text: $text) Exception:", "error"
		}
		return "an error occured while trying to retrieve ${desc} data"
	}
}

def debug(message, lvl = null, shift = null, err = null) {
	
    def debugging = parent.debugging
	if (!debugging) {
		return
	}
    
    lvl = lvl ?: "debug"
	if (!parent["log#$lvl"]) {
		return
	}
	
    def multiEnable = (parent.setMultiLevelLog == false ? false : true) //set to true by default
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