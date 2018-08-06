/**
 *  Yard Lights
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
 	 private versionNum() {	return "version 0.10" }
     private versionDate() { return "30-Jul-2018" }     /*
 *
 *    v1.00 (TBD)         - initial release
 *    v0.10 (30-Jul-2018) - developing
*/
definition(
    name: "Yard Lights",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn on/off selected lights and dim based on various conditions.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
    page(name: "pageOn")
    page(name: "pageOff")
    page(name: "pageDim")
    page(name: "pageSettings")
    page(name: "pageLogOptions")
    page(name: "pageAbout")
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

		 //	  name (C_XXX)			value					description
private     C_ON_DELAY_S()          { return 5 }            //random window used to calculate delay between each light's turn-on (minutes) | TODO: convert to user preference
private     C_OFF_DELAY_S()         { return 5 }            //random window used to calculate delay between each light's turn-off (minutes) | TODO: convert to user preference
private		C_SUNRISE_OFFSET()		{ return -30 }			//offset used for sunrise time calculation (minutes)
private		C_MIN_TIME_ON()			{ return 15 }			//value to use when scheduling turnOn to make sure lights will remain on for at least this long (minutes) before the scheduled turn-off time
private		C_MIN_DIM_DURATION()	{ return 5 }			//minimum duration for temporary dim event (seconds)
private		appImgPath()			{ return "https://raw.githubusercontent.com/astrowings/SmartThings/master/images/" }
private		readmeLink()			{ return "https://github.com/astrowings/SmartThings/blob/master/smartapps/astrowings/sunset-lights.src/readme.md" } //TODO: update link


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section("I wrote this SmartApp to control the lights around my shed in the backyard; I wanted " +
        	    "the lights to turn on and off automatically, similarly to what my 'Sunset Lights' " +
                "application does, but I also wanted to be able to program certain conditions to adjust " +
                "the brightness so as not to inconvenience neighbours, while still getting good lighting when needed.\n\n" +
                "This SmartApp turns on selected lights at sunset and turns them off at a specified time. " +
            	"Different turn-off times can be configured for each day of the week, and they can be " +
                "randomized within a specified window to simulate manual operation. This SmartApp also supports " +
                "dimmers and can be configured to adjust the brightness of selected lights based on various conditions."){
        }
        section() {
            input "theDimmers", "capability.switchLevel", title: "Which dimmers?", description: "Choose the lights to control", multiple: true, required: true, submitOnChange: true
            if (theDimmers) {
                href "pageOn", title: "Set turn-on conditions", description: onConfigDesc, image: getAppImg("office6-icn.png"), required: true, state: "complete"
                href "pageOff", title: "Set turn-off conditions", description: offConfigDesc, image: getAppImg("office6-icn.png"), required: true, state: "complete"
                href "pageDim", title: "Configure brightness settings", description: dimConfigDesc, image: getAppImg("light11-icn.png"), required: true, state: "complete"
        	}
        }
		section() {
			if (theDimmers) {
	            href "pageSettings", title: "App settings", description: "", image: getAppImg("configure_icon.png"), required: false
            }
            href "pageAbout", title: "About", description: "", image: getAppImg("info-icn.png"), required: false
		}
    }
}

def pageOn() {
    dynamicPage(name: "pageOn", install: false, uninstall: false) {
        section(){
        	paragraph title: "Turn-on Conditions", "Use the options on this page to determine when the selected light(s) will turn on."
        }
        //TODO: use illuminance-capable device instead of sunrise/sunset to detect darkness
        section("Set the initial brightness setting for when the lights get turned on (optional - defaults to 100% if not set).") {
        	input "onDim", "number", title: "Initial brightness", description: "Brightness level (0 - 100)?", range: "0..100", required: false, defaultValue: 100
        }
        section("Set the amount of time before/after sunset when the lights will turn on " +
        		"(e.g. use '-20' to enable lights 20 minutes before sunset; " +
                "optional - lights will turn on at sunset if not set") {
            input "onSunsetOffset", "number", title: "Sunset offset", description: "How many minutes (+/- 60)?", range: "-60..60", required: false
        }
    	section("Turn the lights on at a given time. This setting optional and, if set, overrides the sunset setting above.") {
        	input "onDefaultTime", "time", title: "ON time", description: "Time to turn lights ON?", required: false
        }
        section("You can also specify a different time to turn the lights on for each day of the week. " +
	            "Again, this is optional and, if set, overrides all settings above for that particular day.") {
        	input "onSunday", "time", title: "Sunday", description: "Lights ON time?", required: false
            input "onMonday", "time", title: "Monday", description: "Lights ON time?", required: false
            input "onTuesday", "time", title: "Tuesday", description: "Lights ON time?", required: false
            input "onWednesday", "time", title: "Wednesday", description: "Lights ON time?", required: false
            input "onThursday", "time", title: "Thursday", description: "Lights ON time?", required: false
            input "onFriday", "time", title: "Friday", description: "Lights ON time?", required: false
            input "onSaturday", "time", title: "Saturday", description: "Lights ON time?", required: false
        }
    	section("Finally, you can add a random factor so that the timing varies slightly from one day to another " +
    	        "(it looks more 'human' that way).\nSpecify a window around the scheduled time when the lights will turn on " +
      			"(e.g. a 30-minute window would have the lights switch on sometime between " +
           		"15 minutes before and 15 minutes after the scheduled time.)") {
            input "onRand", "number", title: "Random ON window (minutes)?", description: "Set random window", required: false, defaultValue: 30
        }
	}
}

def pageOff() {
    dynamicPage(name: "pageOff", install: false, uninstall: false) {
        def sunriseOffset = C_SUNRISE_OFFSET()
        def sunriseOffset_minutes = sunriseOffset.abs()
        def sunriseOffset_BeforeAfter = sunriseOffset < 0 ? "before" : "after"
        section(){
        	paragraph title: "Turn-off Conditions", "Use the options on this page to choose when the selected light(s) will turn off."
        }
        //TODO: use illuminance-capable device instead of sunrise/sunset to detect darkness
    	section("Turn the light(s) off at a given time " +
        		"(optional - light(s) will turn off ${sunriseOffset_minutes} minutes ${sunriseOffset_BeforeAfter} next sunrise if no time is entered).") {
        	input "offDefaultTime", "time", title: "OFF time", description: "Time to turn lights OFF?", required: false
        }
    	section("Set a different time to turn off the lights on each day (optional - lights will turn off at the default time if not set).") {
        	input "offSunday", "time", title: "Sunday", description: "Lights OFF time?", required: false
            input "offMonday", "time", title: "Monday", description: "Lights OFF time?", required: false
            input "offTuesday", "time", title: "Tuesday", description: "Lights OFF time?", required: false
            input "offWednesday", "time", title: "Wednesday", description: "Lights OFF time?", required: false
            input "offThursday", "time", title: "Thursday", description: "Lights OFF time?", required: false
            input "offFriday", "time", title: "Friday", description: "Lights OFF time?", required: false
            input "offSaturday", "time", title: "Saturday", description: "Lights OFF time?", required: false
        }
    	section("Finally, you can add a random factor so that the timing varies slightly from one day to another " +
    	        "(it looks more 'human' that way).\nSpecify a window around the scheduled time when the lights will turn off " +
      			"(e.g. a 30-minute window would have the lights switch off sometime between " +
           		"15 minutes before and 15 minutes after the scheduled time.)") {
            input "offRand", "number", title: "Random OFF window (minutes)?", description: "Set random window", required: false, defaultValue: 30
        }
	}
}

def pageDim() { //TODO: check that all prefs are being applied in the methods
	dynamicPage(name: "pageDim", install: false, uninstall: false) {
    	section(){
        	paragraph title: "Brightness Adjustments", "Use the options on this page to set the conditions that will affect brightness level. " +
            	"The various settings are listed in increasing priority order (i.e. the brightness setting based on motion will be applied even if outside the time window)."
        }
        section("Adjust brightness during a specified time window. If only 'From' time is set, brightness setting will apply " +
                "until scheduled turn-off time. Conversely, if only the 'To' time is set, brightness setting will apply from " +
                "turn-on time until the 'To' time, at which point it will revert to the default turn-on brightness. You can " +
                "also chose to apply a random on/off window to these settings (e.g. a 10-minute window would have the brightness " +
                "adjustment occur sometime between 5 minutes before and 5 minutes after the scheduled time.)") {
        	input "timeDimTimeLvl", "number", title: "Brightness during time window", description: "Brightness level (0 - 100)?", range: "0..100", required: false, defaultValue: 100
            input "timeDimFrom", "time", title: "From", description: "Starting when?", required: false
            input "timeDimTo", "time", title: "To", description: "Until when?", required: false
            input "timeDimRand", "number", title: "Random window (minutes)?", description: "Set random window", required: false, defaultValue: 10
        }
        section("Adjust brightness when a door is open.") {
        	input "doorDimLvl", "number", title: "Brightness when door open", description: "Brightness level (0 - 100)?", range: "0..100", required: false, defaultValue: 100
            input "doorDimSensors", "capability.contactSensor", title: "Open/Close Sensors", description: "Select which door(s)", multiple: true, required: false
            input "doorDimDelayAfterClose", "number", title: "Reset brightness x seconds after doors close", description: "Seconds (0 - 300)?", range: "0..300", required: false, defaultValue: 5
            input "doorDimDelayFixed", "number", title: "Reset brightness after fixed delay (overrides previous setting)", description: "Seconds (0 - 300)?", range: "0..300", required: false
        }
        section("Adjust brightness when motion is detected.") {
        	input "motionDimLvl", "number", title: "Brightness when motion detected", description: "Brightness level (0 - 100)?", range: "0..100", required: false, defaultValue: 100
            input "motionDimSensors", "capability.motionSensor", title: "Motion Sensors", description: "Select which sensor(s)", multiple: true, required: false
            input "motionDimDelayAfterStop", "number", title: "Reset brightness x seconds after motion stops", description: "Seconds (0 - 300)?", range: "0..300", required: false, defaultValue: 5
            input "motionDimDelayFixed", "number", title: "Reset brightness after fixed delay (overrides previous setting)", description: "Seconds (0 - 300)?", range: "0..300", required: false
        }
    }
}

def pageSettings() {
	dynamicPage(name: "pageSettings", install: false, uninstall: false) {
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
        	//TODO: uncomment line below once link has been verified
            //href url: readmeLink(), title: app.name, description: "Copyright ©2016 Phil Maynard\n${versionNum()}", image: getAppImg("readme-icn.png")
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

def getOnConfigDesc() {
    //TODO: define strDesc
    def strDesc = "Turn-on config summary"
    return strDesc
}

def getOffConfigDesc() {
    //TODO: define strDesc
    def strDesc = "Turn-off config summary"
    return strDesc
}

def getDimConfigDesc() {
    //TODO: define strDesc
    def strDesc = "Brightness config summary"
    return strDesc
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
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
	subscribe(app, appTouch)
	if (doorDimSensors && doorDimLvl && (doorDimDelayAfterClose || doorDimDelayFixed)) {
		subscribe(doorDimSensors, "contact", doorHandler)   //adjust brightness when door opens
    }
    if (motionDimSensors && motionDimLvl && (motionDimDelayAfterStop || motionDimDelayFixed)) {
    	subscribe(motionDimSensors, "motion", motionHandler)//adjust brightness when motion is detected
    }
    subscribe(location, "sunsetTime", sunsetTimeHandler)	//triggers at sunset, evt.value is the sunset String (time for next day's sunset)
    subscribe(location, "sunriseTime", sunriseTimeHandler)	//triggers at sunrise, evt.value is the sunrise String (time for next day's sunrise)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def appTouch(evt) {
	debug "appTouch event: ${evt.descriptionText}", "trace"
    initialize()
    debug "appTouch complete", "trace"
}

def doorHandler(evt) {
	debug "doorHandler event: ${evt.descriptionText}", "trace"
    if (evt.value == "open") {
    	doorOpen()
    } else if ((evt.value == "closed") && doorDimDelayAfterClose) {
    	doorClosed()
    }
    debug "doorHandler complete", "trace"
}

def motionHandler(evt) {
	debug "motionHandler event: ${evt.descriptionText}", "trace"
    if (evt.value == "active") {
    	motionActive()
    } else if ((evt.value == "inactive") && motionDimDelayAfterStop) {
    	motionInactive()
    }
    debug "motionHandler complete", "trace"
}

def sunsetTimeHandler(evt) {
    debug "sunsetTimeHandler event: ${evt.descriptionText}", "trace"
    debug "next sunset will be ${evt.value}"
	scheduleTurnOn(evt.value)
    debug "sunsetTimeHandler complete", "trace"
}

def sunriseTimeHandler(evt) {
    debug "sunriseTimeHandler event: ${evt.descriptionText}", "trace"
    debug "next sunrise will be ${evt.value}"
    scheduleTurnOff(evt.value)
    debug "sunriseTimeHandler complete", "trace"
}    

def locationPositionChange(evt) {
    debug "locationPositionChange(${evt.descriptionText})", "warn"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def scheduleTurnOn(sunsetString) {
//schedule next day's turn-on
    debug "executing scheduleTurnOn(sunsetString: ${sunsetString})", "trace", 1
    def datOnDOW = weekdayTurnOnTime
    def datOnDefault = defaultTurnOnTime
    def datOn

    //select which turn-on time to use (1st priority: weekday-specific, 2nd: default, 3rd: sunset)
    if (datOnDOW) {
    	debug "using the weekday turn-on time: ${datOnDOW}", "info"
        datOn = datOnDOW
    } else if (datOnDefault) {
    	debug "using the default turn-on time: ${datOnDefault}", "info"
    	datOn = datOnDefault
    } else {
    	debug "user didn't specify turn-on time; using sunset time", "info"
        def datSunset = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)
        //calculate the offset
        def offsetTurnOn = onSunsetOffset ? onSunsetOffset * 60 * 1000 : 0 //convert offset to ms
        datOn = new Date(datSunset.time + offsetTurnOn)
	}
    
    //apply random factor
    if (onRand) {
        debug "Applying random factor to the turn-on time", "info"
        def random = new Random()
        def randOffset = random.nextInt(onRand)
        datOn = new Date(datOn.time - (onRand * 30000) + (randOffset * 60000)) //subtract half the random window (converted to ms) then add the random factor (converted to ms)
	}
	
	debug "scheduling lights ON for: ${datOn}", "info"
    runOnce(datOn, turnOn, [overwrite: false]) //schedule this to run once (it will trigger again at next sunset)
    debug "scheduleTurnOn() complete", "trace", -1
}

def scheduleTurnOff(sunriseString) {
//schedule next day's turn-off
    debug "executing scheduleTurnOff(sunriseString: ${sunriseString})", "trace", 1
    def datOffDOW = weekdayTurnOffTime
    def datOffDefault = defaultTurnOffTime
    def datOff

    //select which turn-off time to use (1st priority: weekday-specific, 2nd: default, 3rd: sunrise)
    if (datOffDOW) {
    	debug "using the weekday turn-off time", "info"
        datOff = datOffDOW
    } else if (datOffDefault) {
    	debug "using the default turn-off time", "info"
    	datOff = datOffDefault
    } else {
    	debug "user didn't specify turn-off time; using sunrise time", "info"
        def datSunrise = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
        //calculate the offset
        def sunriseOffset = C_SUNRISE_OFFSET()
		def sunriseOffsetMS = sunriseOffset ? sunriseOffset * 60 * 1000 : 0 //convert offset to ms
        datOff = new Date(datSunrise.time + sunriseOffsetMS)
    }
    
    //apply random factor
    if (offRand) {
        debug "Applying random factor to the turn-off time", "info"
        def random = new Random()
        def randOffset = random.nextInt(offRand)
        datOff = new Date(datOff.time - (offRand * 30000) + (randOffset * 60000)) //subtract half the random window (converted to ms) then add the random factor (converted to ms)
	}
    
    state.schedOffTime = datOff.time //store the scheduled OFF time in State so we can use it later to compare it to the ON time
	debug "scheduling lights OFF for: ${datOff}", "info"
    runOnce(datOff, turnOff, [overwrite: false]) //schedule this to run once (it will trigger again at next sunrise)
    debug "scheduleTurnOff() complete", "trace", -1
}

def turnOn() {
    //check that the scheduled turn-off time is in the future (for example, if the lights are
    //scheduled to turn on at 20:23 based on the sunset time, but the user had them set to turn
    //off at 20:00, the turn-off will fire before the lights are turned on. In that case, the
    //lights would still turn on at 20:23, but they wouldn't turn off until the next day at 20:00.
    debug "executing turnOn()", "trace", 1
	
    def onDelayS = C_ON_DELAY_S()
    def minTimeOn = C_MIN_TIME_ON()
    def onTime = now() + (minTimeOn * 60 * 1000) //making sure lights will stay on for at least 'minTimeOn'
    def offTime = state.schedOffTime //retrieving the turn-off time from State
    if (offTime < onTime) {
		debug "scheduled turn-off time has already passed; turn-on cancelled", "info"
	} else {
        debug "turning lights on", "info"
        def nextDelayMS = 0L //set-up the nextDelayMS variable that will be used to calculate a new time to turn on each light in the group
        def onDelayMS = onDelayS ? onDelayS * 1000 : 5 //ensure delayMS != 0
        def random = new Random()
        theDimmers.each { theDimmer ->
            if (theDimmer.currentSwitch != "on") {
				debug "turning on the ${theDimmer.label} at ${onDim}% brightness in ${convertToHMS(nextDelayMS)}", "info"
                theDimmer.setLevel(onDim, delay: nextDelayMS)
                nextDelay += random.nextInt(onDelayMS) //calculate random delay before turning on next light
            } else {
            	debug "the ${theDimmer.label} is already on; doing nothing", "info"
            }
        }
       	if (timeDimLvl && (timeDimFrom || timeDimTo)) {
        	schedTimeDim() //schedule temporary brightness adjustment if configured
        }
    }
    debug "turnOn() complete", "trace", -1
}

def turnOff() {
    debug "executing turnOff()", "trace", 1
    def offDelayS = C_OFF_DELAY_S()
    def nextDelayMS = 0L //set-up the nextDelayMS variable that will be used to calculate a new time to turn off each light in the group
    def offDelayMS = offDelayS ? offDelayS * 1000 : 5 //ensure delayMS != 0
    def random = new Random()
    theDimmers.each { theDimmer ->
        if (theDimmer.currentSwitch != "off") {
            debug "turning off the ${theDimmer.label} in ${convertToHMS(nextDelayMS)}", "info"
            theDimmer.off(delay: nextDelayMS)
            nextDelayMS += random.nextInt(offDelayMS) //calculate random delay before turning off next light
        } else {
            debug "the ${theDimmer.label} is already off; doing nothing", "info"
        }
    }
    debug "turnOff() complete", "trace", -1
}

def schedTimeDim() {
	//called from turnOn()
    //to schedule the user-defined temporary brightness setting

    debug "executing schedDimStart()", "trace", 1
    
    def minDimDuration = C_MIN_DIM_DURATION()
    def nowTime = now() + (minDimDuration * 60 * 1000) //making sure lights will stay on for at least 'minDimDuration'
    def datNow = new Date(nowTime)
    def datTimeDimFrom = timeDimFrom ? timeToday(timeDimFrom, location.timeZone) : null
    def datTimeDimTo = timeDimTo ? timeToday(timeDimTo, location.timeZone) : null
    
    if (datTimeDimFrom && timeDimRand) {
    	//apply random factor to 'timeDimFrom'
        debug "Applying random factor to the 'dim from' time", "info"
        def random = new Random()
        def randOffset = random.nextInt(timeDimRand)
        datTimeDimFrom = new Date(datTimeDimFrom.time - (timeDimRand * 30000) + (randOffset * 60000)) //subtract half the random window (converted to ms) then add the random factor (converted to ms)
    }
    if (datTimeDimTo && timeDimRand) {
    	//apply random factor to 'timeDimTo'
        debug "Applying random factor to the 'dim to' time", "info"
        def random = new Random()
        def randOffset = random.nextInt(timeDimRand)
        datTimeDimTo = new Date(datTimeDimTo.time - (timeDimRand * 30000) + (randOffset * 60000)) //subtract half the random window (converted to ms) then add the random factor (converted to ms)
    }
    
    def timeOk = datNow < datTimeDimTo //check that the scheduled end of the user-defined temporary brightness window hasn't passed yet
    if (!timeOk) {
    	debug "the scheduled end of the user-defined temporary brightness window has passed", "info"
        debug "schedDimStart() complete", "trace", -1
        return
    }
        
    if (!timeDimFrom) {
        debug "dim start time not specified; applying temporary brightness setting now", "info"
        timeDimGo()
    } else if (datTimeDimFrom < datNow) {
        debug "dim start time already passed; applying temporary brightness setting now", "info"
        timeDimGo()
    } else {
    	debug "scheduling temporary brightness setting to occur at ${datTimeDimFrom}", "info"
        runOnce(datTimeDimFrom, timeDimGo)
    }
    
    if (timeDimTo) {
        debug "scheduling temporary brightness setting to end at ${datTimeDimTo}", "info"
        runOnce(datTimeDimTo, dimDefault)
    }
    
    debug "schedDimStart() complete", "trace", -1
}

def timeDimGo() {
    debug "executing timeDimGo()", "trace", 1
    theDimmers.each { theDimmer ->
	    if (theDimmer.currentSwitch != "off") {
        	debug "temporarily setting ${theDimmer.label} to ${timeDimTimeLvl}%", "info"
            theDimmer.setLevel(timeDimTimeLvl)
        }
    }
    debug "timeDimGo() complete", "trace", -1
}

def dimDefault() {
    debug "executing dimDefault()", "trace", 1
    theDimmers.each { theDimmer ->
	    if (theDimmer.currentSwitch != "off") {
        	debug "end of temporary brightness adjustment;re-setting ${theDimmer.label} to ${onDim}%", "info"
            theDimmer.setLevel(onDim)
        }
    }
    debug "dimDefault() complete", "trace", -1
}

def motionActive() {
    //called from motionHandler when motion is active
    //to apply temporary brightness setting
    debug "executing motionActive()", "trace", 1

    theDimmers.each { theDimmer ->
	    if (theDimmer.currentSwitch != "off") {
        	debug "temporarily setting ${theDimmer.label} to ${motionDimLvl}% because motion was detected", "info" //TODO: specify which sensor detected the motion
            theDimmer.setLevel(motionDimLvl)
        }
    }
	
    if (motionDimDelayFixed) {
    	debug "calling to reset brightness to default setting in ${motionDimDelayFixed} seconds", "info"
        runIn(motionDimDelayFixed, dimDefault)
    }
    
    debug "motionActive() complete", "trace", -1
}

def motionInactive() {
    //called from motionHandler when motion stops
    //to reset brightness to default setting after 'motionDimDelayAfterClose'
    debug "executing motionInactive()", "trace", 1
    
    def allInactive = true
    for (sensor in motionDimSensors) {
    	if (sensor.motion == "active") {
        	allInactive = false
            debug "the ${sensor.label} is still active; check again when motion stops", "info"
            break
        }
    }
    
    if (allInactive) {
    	debug "calling to reset brightness to default setting in ${motionDimDelayAfterClose} seconds", "info"
        runIn(motionDimDelayAfterClose, dimDefault)
    }
    
    debug "motionInactive() complete", "trace", -1
}

def doorOpen() {
    //called from doorHandler when a contact is open
    //to apply temporary brightness setting
    debug "executing doorOpen()", "trace", 1
    
    theDimmers.each { theDimmer ->
	    if (theDimmer.currentSwitch != "off") {
        	debug "temporarily setting ${theDimmer.label} to ${doorDimLvl}% because a door was open", "info" //TODO:replace "a door" with the actual contact name
            theDimmer.setLevel(doorDimLvl)
        }
    }
    
    if (doorDimDelayFixed) {
    	debug "calling to reset brightness to default setting in ${doorDimDelayFixed} seconds", "info"
        runIn(doorDimDelayFixed, dimDefault)
    }
    
    debug "doorOpen() complete", "trace", -1
}

def doorClosed() {
    //called from doorHandler when a contact is closed
    //to reset brightness to default setting after 'doorDimDelayAfterClose'
    debug "executing doorClosed()", "trace", 1
    
    def allClosed = true
    for (door in doorDimSensors) {
    	if (door.contact == "open") {
        	allClosed = false
            debug "the ${door.label} is still open; check again next time a door closes", "info"
            break
        }
    }
    
    if (allClosed) {
    	debug "calling to reset brightness to default setting in ${doorDimDelayAfterClose} seconds", "info"
        runIn(doorDimDelayAfterClose, dimDefault)
    }
    
    debug "doorClosed() complete", "trace", -1
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getDefaultTurnOnTime() {
	debug "start evaluating defaultTurnOnTime", "trace", 1
    if (onDefaultTime) {
        def onDate = timeTodayAfter("23:59", onDefaultTime, location.timeZone) //convert preset time to tomorrow's date
       	debug "default turn-on time: $onDate"
        return onDate
    } else {
        debug "default turn-on time not specified"
        return false
	}
    debug "finished evaluating defaultTurnOnTime", "trace", -1
}

def getDefaultTurnOffTime() {
	debug "start evaluating defaultTurnOffTime", "trace", 1
    if (offDefaultTime) {
        def offDate = timeTodayAfter(new Date(), offDefaultTime, location.timeZone) //convert preset time to today's date
        debug "default turn-off time: $offDate"
        return offDate
    } else {
        debug "default turn-off time not specified"
        return false
	}
    debug "finished evaluating defaultTurnOffTime", "trace", -1
}

def getWeekdayTurnOnTime() {
    //calculate weekday-specific on-time
    //this executes at sunset (called from scheduleTurnOn),
    //so when the sun sets on Tuesday, it will
    //schedule the lights' turn-on time for Wednesday
	debug "start evaluating weekdayTurnOnTime", "trace", 1

	def nowDOW = new Date().format("E") //find out current day of week

    //find out the preset (if entered) turn-on time for next day
    def onDOWtime
    if (onSunday && nowDOW == "Sat") {
        onDOWtime = onSunday
    } else if (onMonday && nowDOW == "Sun") {
        onDOWtime = onMonday
    } else if (onTuesday && nowDOW == "Mon") {
        onDOWtime = onTuesday
    } else if (onWednesday && nowDOW == "Tue") {
        onDOWtime = onWednesday
    } else if (onThursday && nowDOW == "Wed") {
        onDOWtime = onThursday
    } else if (onFriday && nowDOW == "Thu") {
        onDOWtime = onFriday
    } else if (onSaturday && nowDOW == "Fri") {
        onDOWtime = onSaturday
    }

	if (onDOWtime) {
    	def onDOWdate = timeTodayAfter("23:59", onDOWtime, location.timeZone) //set for tomorrow
      	debug "DOW turn-on time: $onDOWdate"
        return onDOWdate
    } else {
    	debug "DOW turn-on time not specified"
        return false
    }
    debug "finished evaluating weekdayTurnOnTime", "trace", -1
}

def getWeekdayTurnOffTime() {
    //calculate weekday-specific offtime
    //this executes at sunrise (called from scheduleTurnOff),
    //so when the sun rises on Tuesday, it will
    //schedule the lights' turn-off time for Tuesday night
	debug "start evaluating weekdayTurnOffTime", "trace", 1

	def nowDOW = new Date().format("E") //find out current day of week

    //find out the preset (if entered) turn-off time for the current weekday
    def offDOWtime
    if (offSunday && nowDOW == "Sun") {
        offDOWtime = offSunday
    } else if (offMonday && nowDOW == "Mon") {
        offDOWtime = offMonday
    } else if (offTuesday && nowDOW == "Tue") {
        offDOWtime = offTuesday
    } else if (offWednesday && nowDOW == "Wed") {
        offDOWtime = offWednesday
    } else if (offThursday && nowDOW == "Thu") {
        offDOWtime = offThursday
    } else if (offFriday && nowDOW == "Fri") {
        offDOWtime = offFriday
    } else if (offSaturday && nowDOW == "Sat") {
        offDOWtime = offSaturday
    }

	if (offDOWtime) {
    	def offDOWdate = timeTodayAfter(new Date(), offDOWtime, location.timeZone)
       	debug "DOW turn-off time: $offDOWdate"
        return offDOWdate
    } else {
    	debug "DOW turn-off time not specified"
        return false
    }
    debug "finished evaluating weekdayTurnOffTime", "trace", -1
}


//   ------------------------
//   ***   COMMON UTILS   ***

int randomTime(int baseDate, int rangeMinutes) {
   int min = baseDate.time - (rangeMinutes * 30000)
   int range = (rangeMinutes * 60000) + 1
   return new Date((int)(Math.random() * range) + min)
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