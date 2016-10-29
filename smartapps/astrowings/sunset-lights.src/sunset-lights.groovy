/**
 *  Sunset Lights
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
 *  VERSION HISTORY
 *
 *	 v2 (28-Oct-2016): add option to insert random delay between the switching of individual lights,
 *                     change method to evaluate which turn-off time to use
 *                     move off-time comparison to turnOn()
 *                     add option to apply random factor to ON time
 *   v1.02 (26-Oct-2016): added trace for each event handler
 *   v1.01 (26-Oct-2016): added 'About' section in preferences
 *   v1 (2016 date unknown): working version, no version tracking up to this point
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


//   -----------------------------------
//   ***   SETTING THE PREFERENCES   ***

preferences {
	page(name: "page1", title: "Sunset Lights - Turn ON", nextPage: "page2", uninstall: true) {
        section("About") {
        	paragraph "This SmartApp turns on selected lights at sunset and turns them off at a specified time." +
            	"Different turn-off times can be configured for each day of the week, and they can be " +
                "randomized within a specified window to simulate manual activation. " +
                "Use it to automatically control exterior lights."
            paragraph "version 2"
        }
        section("Choose the lights to turn on") {
            input "theLights", "capability.switch", title: "Lights", multiple: true, required: true
        }
        section("Set the amount of time after sunset when the lights will turn on") {
            input "offset", "number", title: "Minutes (optional)", required: false
        }
    }
	page(name: "page2", title: "Sunset Lights - Turn OFF", nextPage: "page3") {
    	section("Turn the lights off at this time (optional; lights will turn off 15 minutes before sunrise if no time is entered)") {
        	input "timeOff", "time", title: "Time to turn lights off?", required: false
        }
    	section("Set a different time to turn off the lights on each day (optional; lights will turn off at the default time if not set)") {
        	input "sundayOff", "time", title: "Sunday", required: false
            input "mondayOff", "time", title: "Monday", required: false
            input "tuesdayOff", "time", title: "Tuesday", required: false
            input "wednesdayOff", "time", title: "Wednesday", required: false
            input "thursdayOff", "time", title: "Thursday", required: false
            input "fridayOff", "time", title: "Friday", required: false
            input "saturdayOff", "time", title: "Saturday", required: false
        }
	}
	page(name: "page3", title: "Random Factor", install: true) {
    	section("Optionally, specify a window around the scheduled time when the lights will turn on/off " +
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
            input "onDelay", "bool", title: "Delay switch-on?", required: false
            input "offDelay", "bool", title: "Delay switch-off?", required: false
            input "delaySeconds", "number", title: "Delay switching by up to (seconds)?", required: true, defaultValue: 15
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
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    subscribe(location, "sunsetTime", sunsetTimeHandler)	//triggers at sunset, evt.value is the sunset String (time for next day's sunset)
    subscribe(location, "sunriseTime", sunriseTimeHandler)	//triggers at sunrise, evt.value is the sunrise String (time for next day's sunrise)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes

	//schedule it to run today too
	scheduleTurnOn(location.currentValue("sunsetTime"))
    scheduleTurnOff(location.currentValue("sunriseTime"))
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def sunsetTimeHandler(evt) {
    log.trace "sunsetTimeHandler>${evt.descriptionText}"
    def sunsetTimeHandlerMsg = "triggered sunsetTimeHandler; next sunset will be ${evt.value}"
    log.debug sunsetTimeHandlerMsg
	scheduleTurnOn(evt.value)
}

def sunriseTimeHandler(evt) {
    log.trace "sunriseTimeHandler>${evt.descriptionText}"
    def sunriseTimeHandlerMsg = "triggered sunriseTimeHandler; next sunrise will be ${evt.value}"
    log.debug sunriseTimeHandlerMsg
    scheduleTurnOff(evt.value)
}    

def locationPositionChange(evt) {
	log.trace "locationChange>${evt.descriptionText}"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def scheduleTurnOn(sunsetString) {
	log.trace "scheduleTurnOn(sunsetString: ${sunsetString})"
	
    def datSunset = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)
    log.debug "sunset date: ${datSunset}"

    //calculate the offset
    def offsetTurnOn = offset ? offset * 60 * 1000 : 0 //convert offset to ms
	def datTurnOn = new Date(datSunset.time + offsetTurnOn)

    //apply random factor
    if (randOn) {
        def random = new Random()
        def randOffset = random.nextInt(randOn)
        datTurnOn = new Date(datTurnOn.time - (randOn * 30000) + (randOffset * 60000))
	}
    
	//schedule this to run once (it will trigger again at next sunset)
	log.info "scheduling lights ON for: ${datTurnOn}"
    runOnce(datTurnOn, turnOn, [overwrite: false])
}

def scheduleTurnOff(sunriseString) {
    def DOW_TurnOff = weekdayTurnOffTime
    def default_TurnOff = defaultTurnOffTime
    def datTurnOff

    //select which turn-off time to use (1st priority: weekday-specific, 2nd: default, 3rd: sunrise)
    if (DOW_TurnOff) {
    	log.info "using the weekday turn-off time"
        datTurnOff = DOW_TurnOff
    } else if (default_TurnOff) {
    	log.info "using the default turn-off time"
    	datTurnOff = default_TurnOff
    } else {
    	log.info "user didn't specify turn-off time; using sunrise time"
        def datSunrise = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
        log.debug "sunrise: $datSunrise"
        datTurnOff = new Date(datSunrise.time - (15 * 60 * 1000)) //set turn-off time to 15 minutes before sunrise
    }
    state.turnOff = datTurnOff.time //store the scheduled OFF time in State so we can use it later to compare it to the ON time
	log.info "scheduling lights OFF for: ${datTurnOff}"
    runOnce(datTurnOff, turnOff, [overwrite: false])
}

def turnOn() {
    //check that the scheduled turn-off time is in the future (for example, if the lights are
    //scheduled to turn on at 20:23 based on the sunset time, but the user had them set to turn
    //off at 20:00, the turn-off will fire before the lights are turned on. In that case, the
    //lights would still turn on at 20:23, but they wouldn't turn off until the next day at 20:00.
	def nowTime = now() + (15 * 60 * 1000) //making sure lights will stay on for at least 15 min
    def offTime = state.turnOff //retrieving the turn-off time from State
    if (offTime < nowTime) {
		log.info "scheduled turn-off time has already passed; turn-on cancelled"
	} else {
        log.info "turning lights on"
        def newDelay = 0L
        def delayMS = (onDelay && delaySeconds) ? delaySeconds * 1000 : 5 //ensure positive number for delayMS
        def random = new Random()
        theLights.each { theLight ->
            if (theLight.currentSwitch != "on") {
				log.info "turning on the ${theLight.label} in ${convertToHMS(newDelay)}"
                theLight.on(delay: newDelay)
                newDelay += random.nextInt(delayMS) //calculate random delay before turning on next light
            } else {
            	log.info "the ${theLight.label} is already on; doing nothing"
            }
        }
    }
}

def turnOff() {
    log.info "turning lights off"
    def newDelay = 0L
    def delayMS = (offDelay && delaySeconds) ? delaySeconds * 1000 : 5 //ensure positive number for delayMS
    def random = new Random()
    theLights.each { theLight ->
        if (theLight.currentSwitch != "off") {
            log.info "turning off the ${theLight.label} in ${convertToHMS(newDelay)}"
            theLight.off(delay: newDelay)
            newDelay += random.nextInt(delayMS) //calculate random delay before turning off next light
        } else {
            log.info "the ${theLight.label} is already off; doing nothing"
        }
    }
}


//   ----------------
//   ***   UTILS  ***

def convertToHMS(ms) {
    int hours = Math.floor(ms/1000/60/60)
    int minutes = Math.floor((ms/1000/60) - (hours * 60))
    int seconds = Math.floor((ms/1000) - (hours * 60 * 60) - (minutes * 60))
    double millisec = ms-(hours*60*60*1000)-(minutes*60*1000)-(seconds*1000)
    int tenths = (millisec/100).round(0)
    return "${hours}h${minutes}m${seconds}.${tenths}s"
}

def getDefaultTurnOffTime() {
    if (timeOff) {
    	//convert preset time to today's date
        def default_TurnOffTime = timeTodayAfter(new Date(), timeOff, location.timeZone)
        
        //apply random factor to turnoff time
        if (randOff) {
	    	def random = new Random()
			def randOffset = random.nextInt(randOff)
            default_TurnOffTime = new Date(default_TurnOffTime.time - (randOff * 30000) + (randOffset * 60000))
            log.debug "randomized default turn-off time: $default_TurnOffTime"
        } else {
        	log.debug "default turn-off time: $default_TurnOffTime"
        }
        return default_TurnOffTime
    } else {
        log.debug "default turn-off time not specified"
        return false
	}
}

def getWeekdayTurnOffTime() {
//calculate weekday-specific offtime
//this executes at sunrise, so when the sun rises on Tuesday, it will
//schedule the lights' turn-off time for Tuesday night

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
            log.debug "randomized DOW turn-off time: $DOW_TurnOffTime"
        } else {
        	log.debug "DOW turn-off time: $DOW_TurnOffTime"
        }
        return DOW_TurnOffTime
    } else {
    	log.debug "DOW turn-off time not specified"
        return false
    }
}