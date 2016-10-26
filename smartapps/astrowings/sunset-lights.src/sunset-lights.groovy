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
        section("About") {
        	paragraph "This SmartApp turns on selected lights at sunset and turns them off at a specified time."
            paragraph "version 1"
        }
        section("Choose the lights to turn on") {
            input "theLights", "capability.switch", title: "Lights", multiple: true, required: true
            }
        section("Set the amount of time after sunset when the lights will turn on") {
            input "offset", "number", title: "Minutes (optional)", required: false
            }
        }
	page(name: "page2", title: "Turn the lights off automatically", nextPage: "page3") {
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
	page(name: "page3", title: "Add random factor", install: true) {
    	section("Specify a window around the scheduled time when the lights will turn off " +
        	"(e.g. a 30-minute window would have the lights turn off sometime between " +
            "15 minutes before and 15 minutes after the scheduled time.)") {
            input "randOff", "number", title: "Random window (minutes)?", required: false
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
    def datSunset = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)
    log.debug "sunset date: ${datSunset}"

    //calculate the offset
    def offsetTurnOn = offset ? offset * 60 * 1000 : 0 //convert offset to ms
	def datTurnOn = new Date(datSunset.time + offsetTurnOn)
    
    //store the scheduled ON time (Unix format) in State so we can use it later to compare it to the scheduled OFF time
    state.turnOn = datTurnOn.time

	//schedule this to run once (it will trigger again at next sunset)
	log.debug "scheduling lights ON for: ${datTurnOn} (${offsetTurnOn / 60000} minutes after sunset)"
    runOnce(datTurnOn, turnOn, [overwrite: false])
}

def scheduleTurnOff(sunriseString) {
	//get the sunrise turn-off time
    def datSunrise = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
    log.debug "sunrise date: $datSunrise"
    def datSunriseTurnOff = new Date(datSunrise.time - (15 * 60 * 1000)) //calculate the offset 15 minutes before sunrise

	//select the turn-off time to use (1st priority = weekday-specific, 2nd = default time, 3rd = based on sunrise)
	def datTurnOff = datSunriseTurnOff
    def DOW_TurnOff = weekdayTurnOffTime
    def default_TurnOff = defaultTurnOffTime
    if (DOW_TurnOff) {
    	log.debug "using the weekday turn-off time"
        datTurnOff = DOW_TurnOff
    } else if (default_TurnOff) {
    	log.debug "using the default turn-off time"
    	datTurnOff = default_TurnOff
    } else {
    	log.debug "using the sunrise turn-off time"
    }

	//check that the scheduled turn off time is in the future (for example, if the lights are
    //scheduled to turn on at 19:23 based on the sunset time, but the user had them set to turn
    //off at 19:00, the turn-off will fire before the lights are turned on. In that case, the
    //lights would still turn on at 19:23, but they wouldn't turn off until the next day at 19:00.
	def datTurnOn = new Date(state.turnOn)	//retrieving the (Unix) turn-on time from State
	if (datTurnOff < datTurnOn) {
    	log.debug "unscheduling the lights because the OFF time (${datTurnOff}) would occur before the ON time (${datTurnOn})"
        unschedule(turnOn)
	} else {
        //schedule this to run once (it will trigger again at next sunrise)
		log.debug "scheduling lights OFF for: ${datTurnOff}"
        runOnce(datTurnOff, turnOff, [overwrite: false])
    }
}

def turnOn() {
	log.debug "turning on lights"
    //TODO: add a delay (up to 2 minutes) for each light so they don't all turn on at the same time
    theLights.on()
}

def turnOff() {
    log.debug "turning off lights"
    //TODO: add a delay (up to 2 minutes) for each light so they don't all turn off at the same time
    theLights.off()
}


//   ----------------
//   ***   UTILS  ***

private getDefaultTurnOffTime() {
    if (timeOff) {
    	//convert preset time to today's date
        //TODO: deal with times after midnight
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

private getWeekdayTurnOffTime() {
//calculate weekday-specific offtime
//this executes at sunrise, so when the sun rises on Tuesday, it will
//schedule the lights' turn-off time for Tuesday night

//TODO: deal with times after midnight

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
