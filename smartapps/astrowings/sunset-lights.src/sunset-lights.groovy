/**
 *  Sunset Lights
 *
 *  Copyright 2016 Phil Maynard
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
 *  Developer retains all right, title, copyright, and interest, including all copyright, patent rights, trade secret 
 *  in the Background technology. May be subject to consulting fees under the Agreement between the Developer and the Customer. 
 *  Developer grants a non exclusive perpetual license to use the Background technology in the Software developed for and delivered 
 *  to Customer under this Agreement. However, the Customer shall make no commercial use of the Background technology without
 *  Developer's written consent.
 */ 
definition(
    name: "Sunset Lights",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn on selected lights at sunset (w/ optional offset) and turn them off at a specified time.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light25-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light25-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light25-icn@3x.png")


//   -----------------------------------
//   ***   SETTING THE PREFERENCES   ***

preferences {
	page(name: "page1", title: "Turn on these lights at sunset", nextPage: "page2", uninstall: true) {
        section() {
        	paragraph "This app turns on selected lights at sunset and turns them off at a specified time."
        }
        section("Choose the lights to turn on") {
            input "switches", "capability.switch", title: "Lights", multiple: true, required: true
            }
        section("Set the amount of time after sunset when the lights will turn on") {
            input "offset", "number", title: "Minutes (optional)", required: false
            }
        }
	page(name: "page2", title: "Turn the lights off automatically", nextPage: "page3") {
    	section("Turn the lights off at this time (optional, lights will turn off 15 minutes before sunrise if no time is entered)") {
        	input "timeOff", "time", title: "Time to turn lights off?", required: false
            }
    	section("Set a different time to turn off the lights on each day (optional, lights will turn off at the default time if not set)") {
        	input "sundayOff", "time", title: "Sunday", required: false
            input "mondayOff", "time", title: "Monday", required: false
            input "tuesdayOff", "time", title: "Tuesday", required: false
            input "wednesdayOff", "time", title: "Wednesday", required: false
            input "thursdayOff", "time", title: "Thursday", required: false
            input "fridayOff", "time", title: "Friday", required: false
            input "saturdayOff", "time", title: "Saturday", required: false
            }
		}
	page(name: "page3", title: "Add random factor", install: true) {
    	section("Specify a window around the scheduled time when the lights will turn off " +
        	"(e.g. a 30 minute window would have the lights turn off sometime between " +
            "15 minutes before and 15 minutes after the scheduled time.)") {
            input "randWindow", "number", title: "Random window (minutes)?", required: false
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
    def sunsetTimeHandlerMsg = "triggered sunsetTimeHandler; next sunset will be ${evt.value}"
    log.debug sunsetTimeHandlerMsg
	
    //at sunset, schedule the next day's TurnOn
	scheduleTurnOn(evt.value)
}

def sunriseTimeHandler(evt) {
    def sunriseTimeHandlerMsg = "triggered sunriseTimeHandler; next sunrise will be ${evt.value}"
    log.debug sunriseTimeHandlerMsg

    //at sunrise, schedule the next day's TurnOff
    scheduleTurnOff(evt.value)
}    

def locationPositionChange(evt) {
	log.trace "locationChange()"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def scheduleTurnOn(sunsetString) {
	//log.debug "sunsetString: $sunsetString"
    def sunsetTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)
    log.debug "sunsetTime: ${sunsetTime}"

    //calculate the offset
    def offsetTurnOn = (offset != null && offset != "") ? offset * 60 * 1000 : 0
	def schedTurnOnTime = new Date(sunsetTime.time + offsetTurnOn)
    
    //store the scheduled ON time (Unix format) in State so we can use it later to compare it to the scheduled OFF time
    state.schedON = schedTurnOnTime.time

	//schedule this to run once (it will trigger again at next sunset)
	log.debug "scheduling lights ON for: ${schedTurnOnTime} (${offsetTurnOn / 60000} minutes after sunsetTime)"
    runOnce(schedTurnOnTime, turnOn, [overwrite: false])
}

def scheduleTurnOff(sunriseString) {
	//get the sunrise turn-off time
    def sunriseTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
    log.debug "sunriseTime: $sunriseTime"
    //calculate the offset 30 minutes before sunrise
    def sunriseTurnOffTime = new Date(sunriseTime.time - (30 * 60 * 1000))

	//select the turn-off time to use (1st priority = weekday-specific, 2nd = default time, 3rd = based on sunrise)
	def schedTurnOffTime = sunriseTurnOffTime
    def DOW_TurnOffTime = weekdayTurnOffTime
    def def_TurnOffTime = defaultTurnOffTime
    if (DOW_TurnOffTime) {
    	log.debug "using the weekdayTurnOffTime"
        schedTurnOffTime = DOW_TurnOffTime
    } else if (def_TurnOffTime) {
    	log.debug "using the defaultTurnOffTime"
    	schedTurnOffTime = def_TurnOffTime
    } else {
    	log.debug "using the sunriseTurnOffTime"
    }

	//check that the scheduled turn off time is in the future (for example, if the lights are
    //scheduled to turn on at 19:23 based on the sunset time, but the user had them set to turn
    //off at 19:00, the turn-off will fire before the lights are turned on. In that case, the
    //lights would still turn on at 19:23, but they wouldn't turn off until the next day at 19:00.
	def unixTurnOnTime = state.schedON			//retrieving the (Unix) turn-on time from State
	def unixTurnOffTime = schedTurnOffTime.time //converting turn-off time into Unix format for comparison
	if (unixTurnOffTime < unixTurnOnTime) {
    	def schedTurnOnTime = new Date(unixTurnOnTime)
    	log.debug "unscheduling the lights because the OFF time (${schedTurnOffTime}) would occur before the ON time (${schedTurnOnTime})"
        unschedule(turnOn)
	} else {
        //schedule this to run once (it will trigger again at next sunset)
		log.debug "scheduling lights OFF for: ${schedTurnOffTime}"
        runOnce(schedTurnOffTime, turnOff, [overwrite: false])
    }
}

def turnOn() {
	log.debug "turning on lights"
    switches.on()
}

def turnOff() {
    log.debug "turning off lights"
    switches.off()
}


//   ----------------
//   ***   UTILS  ***

private getDefaultTurnOffTime() {
    if (timeOff) {
    	//convert preset time to today's date
        def def_TurnOffTime = timeTodayAfter(new Date(), timeOff, location.timeZone)
        
        //apply random factor to turnoff time
        if (randWindow) {
	    	def random = new Random()
			def randDefOffset = random.nextInt(randWindow)
            def rdm_DefTime = new Date(def_TurnOffTime.time - (randWindow * 30000) + (randDefOffset * 60000))
            log.debug "rdm_DefTime: $rdm_DefTime"
            return rdm_DefTime
        } else {
        	log.debug "def_TurnOffTime: $def_TurnOffTime"
            return def_TurnOffTime
        }
    } else {
        log.debug "Default turnoff time not specified"
        return false
	}
}

private getWeekdayTurnOffTime() {
//calculate weekday-specific offtime
    
    //find out current day of week (this executes at sunrise,
    //so when the sun rises on Tuesday, it will schedule the
    //lights' turn-off time for Tuesday night

	def nowDOW = new Date().format("E")

    //find out the preset (if entered) turn-off time for the current weekday
    def DOW_Off = null
    if (sundayOff != null && sundayOff != "" && nowDOW == "Sun") {
        DOW_Off = sundayOff
    } else if (mondayOff != null && mondayOff != "" && nowDOW == "Mon") {
        DOW_Off = mondayOff
    } else if (tuesdayOff != null && tuesdayOff != "" && nowDOW == "Tue") {
        DOW_Off = tuesdayOff
    } else if (wednesdayOff != null && wednesdayOff != "" && nowDOW == "Wed") {
        DOW_Off = wednesdayOff
    } else if (thursdayOff != null && thursdayOff != "" && nowDOW == "Thu") {
        DOW_Off = thursdayOff
    } else if (fridayOff != null && fridayOff != "" && nowDOW == "Fri") {
        DOW_Off = fridayOff
    } else if (saturdayOff != null && saturdayOff != "" && nowDOW == "Sat") {
        DOW_Off = saturdayOff
    }

	if (DOW_Off) {
    	//convert preset time to today's date
    	def DOW_TurnOffTime = timeTodayAfter(new Date(), DOW_Off, location.timeZone)
    	//log.debug "DOW turnoff time: $DOWTurnOffTime"
        
        //apply random factor to turnoff time
		if (randWindow) {
        	def random = new Random()
            def randDOWOffset = random.nextInt(randWindow)
            def rdm_DOWTime = new Date(DOW_TurnOffTime.time - (randWindow * 30000) + (randDOWOffset * 60000))
            log.debug "rdm_DOWTime: $rdm_DOWTime"
            return rdm_DOWTime
        } else {
        	log.debug "DOWTurnOffTime: $DOWTurnOffTime"
            return DOW_TurnOffTime
        }
    } else {
    	log.debug "DOW turnoff time not specified"
        return false
    }
}

