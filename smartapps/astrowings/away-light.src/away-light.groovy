/**
 *  Away Light
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
 	 def versionNum() {	return "version 1.11" }       /*
 
 *	 v1.11 (01-Nov-2016): standardize section headers
 *	 v1.10 (27-Oct-2016): change layout of preferences pages, default value for app name
 *   v1.02 (26-Oct-2016): added trace for each event handler
 *   v1.01 (26-Oct-2016): added 'About' section in preferences
 *   v1.00 (2016 date unknown): working version, no version tracking up to this point
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
	page(name: "prefs")
	page(name: "options")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private C_1() { return "this is constant1" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def prefs() {
	dynamicPage(name: "prefs", uninstall: true, install: true) {
    	section("About"){
        	paragraph title: "This SmartApp turns a light on/off simulate presence while away.",
            	"version 1.1"
        }
        section("Select the light") {
            input "theLight", "capability.switch", title: "Which light?", multiple: false, required: true, submitOnChange: true
        }
        if (theLight) {
            section("Restrict automation to certain times (optional)") {
                input "startTime", "time", title: "Start time?", required: false
                input "endTime", "time", title: "End time?", required: false
            }
            section("Set additional scheduling options") {
                href(page: "options", title: "Additional Options")
            }
            section("Only run in selected modes (automation disabled if none selected)") {
                input "theModes", "mode", title: "Select the mode(s)", multiple: true, required: false
            }
            section() {
                label title: "Assign a name", required: false, defaultValue: "Away Light - ${theLight.label}"
            }
		}
    }
}

def options() {
	dynamicPage(name: "options") {
        section(){
        	paragraph title: "Additional Options",
            	"Set additional scheduling options for the ${theLight.label ?: light}"
        }
        section("Enable only for certain days of the week? (optional - will run every day if nothing selected)") {
        	input "theDays", "enum", title: "On which days?", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
        }
    	section("Only when it's dark out (between sunset and sunrise)") {
        	input "bDark", "bool", title: "Yes/No?", required: false, submitOnChange: true
            if (bDark) {
            	input "sunsetOffset", "number", title: "Sunset time offset (minutes)?", description: "Disable lights until x minutes after sunset", required: false
            }
        }
        section("Set light on/off duration - use these settings to have the light turn on and off at the specified interval within the specified time window") {
            input "onFor", "number", title: "Stay on for (minutes)?", required: false //If set, the light will turn off after the amount of time specified (or at specified end time, whichever comes first)
            input "offFor", "number", title: "Leave off for (minutes)?", required: false //If set, the light will turn back on after the amount of time specified (unless the specified end time has passed)
        }
        section("Random factor - if set, randomize on/off times within the selected window") {
        	input "randWind", "number", title: "Random window (minutes)?", required: false
        }
	}
}


//   ----------------------------
//   ***   APP INSTALLATION   ***

def installed() {
	log.info "installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.info "updated with settings $settings"
	unsubscribe()
    unschedule()
    initialize()
}

def uninstalled() {
	if (state.appOn) {
    	theLight.off()
        state.appOn = false
        }
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    state.debugLevel = 0
    state.appOn = false
    theLight.off()
    subscribeToEvents()
	schedTurnOn()
}

def subscribeToEvents() {
    subscribe(location, "mode", modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def modeChangeHandler(evt) {
    log.trace "modeChangeHandler>${evt.descriptionText}"
    if(modeOk) {
        log.debug "mode changed to ${location.currentMode}; calling schedTurnOn()"
        schedTurnOn()
    } else {
        log.debug "mode changed to ${location.currentMode}; cancelling scheduled tasks"
        unschedule()
    }
}

def locationPositionChange(evt) {
	log.trace "locationPositionChange>${evt.descriptionText}"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def schedTurnOn(offForDelay) {
    //determine turn-on time and schedule the turnOn() that will verify the remaining conditions before turning the light on
	log.debug "executing schedTurnOn(offForDelay: ${offForDelay})"
	
    def random = new Random()
    
    if (offForDelay) {
        //method was called from turnOff() to turn the light back on after the "offFor" delay
        if (randWind) {
            def rdmDelay = random.nextInt(randWind)
            offForDelay = offForDelay - (randWind * 30000) + (rdmDelay * 60000)
            offForDelay = Math.max(0, offForDelay) //make sure that we don't end up with a negative number
		}
        def onDate = new Date(now() + offForDelay)
        log.debug "calculated ON time for turning the light back on after the 'off for' delay of ${convertToHMS(offForDelay)} :: ${onDate}"
        runOnce(onDate, turnOn)
	} else {   
        def onDate = schedOnDate()
        def nowDate = new Date()
        
        //set a delay of up to 2 min to be applied when requested to turn on now
        def maxDelay = 2 * 60 * 1000
        def delayOnNow = random.nextInt(maxDelay)
        
        if (!onDate) {
            //no turn-on time set, call method to turn light on now; whether or not it actually turns on will depend on dow/mode
            log.debug "no turn-on time specified; calling to turn on the light in ${convertToHMS(delayOnNow)}"
            turnOn(delayOnNow)
        } else {
            if (onDate < nowDate) {
                log.debug "scheduled turn-on time of ${onDate} has already passed; calling to turn on the light in ${convertToHMS(delayOnNow)}"
                turnOn(delayOnNow)
            } else {
                log.debug "scheduling the light to turn on at ${onDate}"
                runOnce(onDate, turnOn)
            }
        }
    }
}

def turnOn(delay) {
	//check conditions and turn on the light
    log.debug "executing turnOn(delay: ${delay})"

    def tz = location.timeZone
    def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
	def strDOW = nowDOW
    def DOWOk = !theDays || theDays?.contains(strDOW)
    def darkOk = !bDark || itsDarkOut

    log.debug "checking conditions before turning the light on"
	if (modeOk && DOWOk && darkOk) {
        def nowDate = new Date(now() + (randWind * 30000)) //add 1/2 random window to current time to enable the light to come on around the sunset time
        def offDate = schedOffDate()
        def timeOk = offDate > nowDate
        if (timeOk) {    	
            delay = delay ?: 0
            log.debug "we're good to go; turning the light on in ${convertToHMS(delay)}"
            state.appOn = true
            theLight.on(delay: delay)
            schedTurnOff(delay, offDate)
        } else {
            log.debug "the light's turn-off time has already passed; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, schedTurnOn)
        }
    } else {
        if (!modeOk) {
    		log.debug "light activation is not enabled in current mode; check again at mode change"
    	} else if (!DOWOk) {
            log.debug "light activation is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, schedTurnOn)
        } else if (!darkOk) {
        	def sunTime = getSunriseAndSunset(sunsetOffset: sunsetOffset)
            def sunsetDate = sunTime.sunset
            log.debug "light activation is not enabled during daytime; check again at sunset (${sunsetDate})"
            //add random factor
			if (randWind) {
            	def random = new Random()
                def rdmOffset = random.nextInt(randWind)
                sunsetDate = new Date(sunsetDate.time - (randWind * 30000) + (rdmOffset * 60000))
            }
            runOnce(sunsetDate, schedTurnOn)
        }
	}
}	

def schedTurnOff(onDelay, offDate) {
    //determine turn-off time and schedule the turnOff()
	log.debug "executing schedTurnOff(onDelay: ${onDelay}, offDate: ${offDate})"

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
        log.debug "calculated OFF time for turning the light off after the 'on for' delay of ${convertToHMS(lightOnFor)} :: ${endOnFor}"
        offDate = offDate && (offDate < endOnFor) ? offDate : endOnFor
    }
    
    if (offDate) {
        if (offDate > nowDate) {
            log.debug "scheduling turn-off of the light to occur at ${offDate}"
            runOnce(offDate, turnOff)
        } else {
        	def maxDelay = 2 * 60 * 1000 //set a delay of up to 2 min to be applied when requested to turn off now
            def delayOffNow = random.nextInt(maxDelay)
            log.debug "the calculated turn-off time has already passed; calling for the light to turn off in ${convertToHMS(delayOffNow)}"
            turnOff(delayOffNow)
        }
    } else {
        log.debug "no turn-off time specified"
    }
}

def turnOff(delay) {
    if (state.appOn == true) {
        delay = delay ?: 0
        log.debug "turning off the light in ${convertToHMS(delay)}"
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
            log.debug "the light isn't scheduled to turn back on today; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, schedTurnOn)
        }
    } else {
		log.debug "the light wasn't turned on by this app; doing nothing"
    }
}

def terminate() {
	//For each configured light that was turned on by this app, turn the light off after a random delay.
    //Called when it's detected that the conditions are no longer valid
    log.debug "received command to turn the light off after random delay up to 2 minutes"
    def random = new Random()
    def maxDelay = 2 * 60 * 1000
   	if (state.appOn) {
        def delay = random.nextInt(maxDelay)
        log.debug "turning off the light in ${convertToHMS(delay)}"
        theLight.off(delay: delay)
        state.appOn = false
    }
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getModeOk() {
	def result = theModes?.contains(location.mode)
	//log.debug "modeOk :: $result"
	return result
}

def getItsDarkOut() {
    def sunTime = getSunriseAndSunset(sunsetOffset: sunsetOffset)
    def nowDate = new Date(now() + 2000) // be safe and set current time for 2 minutes later
    def result = false
	if(sunTime.sunrise < nowDate && sunTime.sunset > nowDate){
    	//log.debug "it's daytime"
        result = false
    } else {
    	//log.debug "it's nighttime"
        result = true
    }
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
    //log.debug "strDOW :: $strDOW"
    return strDOW
}

def schedOnDate() {
    // ***  CALCULATE TURN-ON TIME  ***
    //figure out the next 'on' time based on user settings
    def tz = location.timeZone
	def random = new Random()
    def onDate = startTime ? timeToday(startTime, tz) : null
	
    log.debug "user-configured turn-on time :: ${onDate}"
    
    if (randWind && onDate) {
        //apply random factor to onDate
        def rdmOffset = random.nextInt(randWind)
        onDate = new Date(onDate.time - (randWind * 30000) + (rdmOffset * 60000))
        log.debug "random-adjusted turn-on time :: ${onDate}"
    } else {
        //log.debug "no random factor configured in preferences"
    }
    
    return onDate
}

def schedOffDate() {
    // ***  CALCULATE TURN-OFF TIME  ***
    //figure out the light's 'off' time based on user settings
    def tz = location.timeZone
	def random = new Random()
    def offDate = endTime ? timeToday(endTime, tz) : null
    
    //get the earliest of user-preset start time and sunrise time
    if (bDark) {
        def sunriseString = location.currentValue("sunriseTime") //get the next sunrise time string
        def sunriseDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
		log.debug "comparing end time (${offDate}) to sunrise time (${sunriseDate})"
		offDate = offDate && (offDate < sunriseDate) ? offDate : sunriseDate
    }
    
    log.debug "calculated turn-off time :: ${offDate}"
    
    if (randWind && offDate) {
        //apply random factor to offDate
        def rdmOffset = random.nextInt(randWind)
        def offTime = offDate.time - (randWind * 30000) + (rdmOffset * 60000)
        offDate = new Date(offTime)
        log.debug "random-adjusted turn-off time :: ${offDate}"
    } else {
        //log.debug "no random factor configured in preferences"
    }
    
    return offDate
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