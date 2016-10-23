/**
 *  Away Lights
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
 */

/** TODO:
	Currently, lights will never turn-on before sunrise because that's also before sunset, and
    the max turn-on time (app / light / sunset) would be that of the sunset. To rectify, we need
    to ignore the sunset time if the max of scheduled turn-on times (app & light) is before sunrise
 */

definition(
    name: "Away Lights",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn selected lights on/off on a random schedule to simulate presence while away",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light17-icn@3x.png")


//   -----------------------------------
//   ***   SETTING THE PREFERENCES   ***

preferences {
	page(name: "topMenu")
	page(name: "setLight1")
    page(name: "setLight2")
    page(name: "setLight3")
    page(name: "setLight4")
    page(name: "appSettings")
}

def topMenu() {
	dynamicPage(name: "topMenu", uninstall: true, install: true) {
    	section(){
        	paragraph "This app turns lights on/off on a random schedule to simulate presence while away. " +
            	"Use the menu on this page to define app-wide conditions and to configure each light individually."
        }
        section("Set application conditions") {
            href(page: "appSettings", title: "App Settings")
        }
        section("Configure lights") {
            def desc1 = light1?.label ?: "Configure the scheduling options for light #1"
            def desc2 = light2?.label ?: "Configure the scheduling options for light #2"
            def desc3 = light3?.label ?: "Configure the scheduling options for light #3"
            def desc4 = light4?.label ?: "Configure the scheduling options for light #4"
            href(page: "setLight1", title: "Light #1", description: desc1)
            href(page: "setLight2", title: "Light #2", description: desc2)
            href(page: "setLight3", title: "Light #3", description: desc3)
            href(page: "setLight4", title: "Light #4", description: desc4)
		}
        section() {
        	label title: "Assign a name", required: false
        }
	}
}

def appSettings() {
	dynamicPage(name: "appSettings") {
        section(){
        	paragraph title: "Automation Schedule",
            	"Set the conditions for enabling the automation schedule."
        }
    	section("Enable only when the mode is set to...") {
        	input "theModes", "mode", title: "Select the mode(s)", multiple: true, required: false
        }
    	section("Only when it's dark out (between sunset and sunrise)") {
        	input "appDark", "bool", title: "Yes/No?", required: false, submitOnChange: true
            if (appDark) {
            	input "sunsetOffset", "number", title: "Sunset time offset (minutes)?", description: "Disable lights until x minutes after sunset", required: false
            }
        }
        section("On certain days of the week") {
        	input "appDays", "enum", title: "On which days?", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
        }
        section("Between certain times") {
            input "appStart", "time", title: "Start time?", required: false
            input "appEnd", "time", title: "End time?", required: false
        }
	}
}

def setLight1() {
	dynamicPage(name: "setLight1") {
        section(){
        	paragraph title: "Light #1",
            	"Select the preferences for light #1"
        }
        section("Pick the light that you wish to have turned on/off automatically") {
            input "light1", "capability.switch", title: "Which light?", multiple: false, required: false, submitOnChange: true
        }
        if (light1) {
        	section("Optional scheduling preferences for the $light1.displayName") {
            	input "light1OnFor", "number", title: "Stay on for (minutes)?", description: "Restrict light-on duration", required: false
                input "light1OffFor", "number", title: "Leave off for (minutes)?", description: "Turn light back on after x minutes", required: false
                input "light1Rdm", "number", title: "Random window (minutes)?", description: "Randomize on/off times", required: false
            }
            section("If desired, override application settings for the automation of the $light1.displayName") {
				input "light1Days", "enum", title: "Only on these days", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
                input "light1Start", "time", title: "Start time", required: false
                input "light1End", "time", title: "End time", required: false
                input "light1Daytime", "bool", title: "Allow during daytime", description: "Overrides the global setting that restricts schedule based on sunrise/sunset times (if enabled)", required: false
            }
        }
	}
}

def setLight2() {
	dynamicPage(name: "setLight2") {
        section(){
        	paragraph title: "Light #2",
            	"Select the preferences for light #2"
        }
        section("Pick the light that you wish to have turned on/off automatically") {
            input "light2", "capability.switch", title: "Which light?", multiple: false, required: false, submitOnChange: true
        }
        if (light2) {
        	section("Optional scheduling preferences for the $light2.displayName") {
            	input "light2OnFor", "number", title: "Stay on for (minutes)?", description: "Restrict light-on duration", required: false
                input "light2OffFor", "number", title: "Leave off for (minutes)?", description: "Turn light back on after x minutes", required: false
                input "light2Rdm", "number", title: "Random window (minutes)?", description: "Randomize on/off times", required: false
            }
            section("If desired, override application settings for the automation of the $light2.displayName") {
				input "light2Days", "enum", title: "Only on these days", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
                input "light2Start", "time", title: "Start time", required: false
                input "light2End", "time", title: "End time", required: false
                input "light2Daytime", "bool", title: "Allow during daytime", description: "Overrides the global setting that restricts schedule based on sunrise/sunset times (if enabled)", required: false
            }
        }
	}
}

def setLight3() {
	dynamicPage(name: "setLight3") {
        section(){
        	paragraph title: "Light #3",
            	"Select the preferences for light #3"
        }
        section("Pick the light that you wish to have turned on/off automatically") {
            input "light3", "capability.switch", title: "Which light?", multiple: false, required: false, submitOnChange: true
        }
        if (light3) {
        	section("Optional scheduling preferences for the $light3.displayName") {
            	input "light3OnFor", "number", title: "Stay on for (minutes)?", description: "Restrict light-on duration", required: false
                input "light3OffFor", "number", title: "Leave off for (minutes)?", description: "Turn light back on after x minutes", required: false
                input "light3Rdm", "number", title: "Random window (minutes)?", description: "Randomize on/off times", required: false
            }
            section("If desired, override application settings for the automation of the $light3.displayName") {
				input "light3Days", "enum", title: "Only on these days", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
                input "light3Start", "time", title: "Start time", required: false
                input "light3End", "time", title: "End time", required: false
                input "light3Daytime", "bool", title: "Allow during daytime", description: "Overrides the global setting that restricts schedule based on sunrise/sunset times (if enabled)", required: false
            }
        }
	}
}

def setLight4() {
	dynamicPage(name: "setLight4") {
        section(){
        	paragraph title: "Light #4",
            	"Select the preferences for light #4"
        }
        section("Pick the light that you wish to have turned on/off automatically") {
            input "light4", "capability.switch", title: "Which light?", multiple: false, required: false, submitOnChange: true
        }
        if (light4) {
        	section("Optional scheduling preferences for the $light4.displayName") {
            	input "light4OnFor", "number", title: "Stay on for (minutes)?", description: "Restrict light-on duration", required: false
                input "light4OffFor", "number", title: "Leave off for (minutes)?", description: "Turn light back on after x minutes", required: false
                input "light4Rdm", "number", title: "Random window (minutes)?", description: "Randomize on/off times", required: false
            }
            section("If desired, override application settings for the automation of the $light4.displayName") {
				input "light4Days", "enum", title: "Only on these days", options: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"], required: false, multiple: true
                input "light4Start", "time", title: "Start time", required: false
                input "light4End", "time", title: "End time", required: false
                input "light4Daytime", "bool", title: "Allow during daytime", description: "Overrides the global setting that restricts schedule based on sunrise/sunset times (if enabled)", required: false
            }
        }
	}
}


//   ----------------------------
//   ***   APP INSTALLATION   ***

def installed() {
	log.info "Installed with settings: ${settings}"
    initialize()
}

def updated() {
	log.info "Updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    initialize()
}

def uninstalled() {
	unschedule()
	turnAllOffNow()
    log.debug "uninstalled"
}

def initialize() {
	log.info "Initializing"
    subscribeToEvents()
    initStatus()
    log.debug "state.lightStatus :: $state.lightStatus"
    //def stateCharSize = state.toString().length()
    //log.debug "size of state :: $stateCharSize"
	turnAllOffNow()
	setAllOn()
}

def subscribeToEvents() {
	subscribe(location, "mode", modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
}

def initStatus() {
    //define a map that will get used to access individual light preferences when iterating through lights
    
    def lightStatus = [
    	[num: 1, label: light1?.label, days: light1Days, rdm: light1Rdm, onFor: light1OnFor, offFor: light1OffFor, daytime: light1Daytime, lightStart: light1Start, lightEnd: light1End, appOn: false],
    	[num: 2, label: light2?.label, days: light2Days, rdm: light2Rdm, onFor: light2OnFor, offFor: light2OffFor, daytime: light2Daytime, lightStart: light2Start, lightEnd: light2End, appOn: false],
    	[num: 3, label: light3?.label, days: light3Days, rdm: light3Rdm, onFor: light3OnFor, offFor: light3OffFor, daytime: light3Daytime, lightStart: light3Start, lightEnd: light3End, appOn: false],
    	[num: 4, label: light4?.label, days: light4Days, rdm: light4Rdm, onFor: light4OnFor, offFor: light4OffFor, daytime: light4Daytime, lightStart: light4Start, lightEnd: light4End, appOn: false]
    ]
	
    lightStatus.each {
    	if (it.label) {
        	log.debug "light#$it.num :: label=$it.label"
        }
    }
    
    state.lightStatus = lightStatus
}

//   --------------------------
//   ***   EVENT HANDLERS   ***

def modeChangeHandler(evt) {
    if (theModes) {
        if(modeOk) {
        	log.debug "mode changed to ${location.currentMode}; calling setAllOn()"
    		setAllOn()
    	} else {
			log.debug "mode changed to ${location.currentMode}; cancelling all scheduled tasks and calling turnAllOff()"
            unschedule()
            turnAllOff()
        }
    }
}

def locationPositionChange(evt) {
	log.trace "locationChange()"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def setAllOn() {
	//run setTurnOn for each configured light
	log.debug "executing setAllOn()"
    
    state.lightStatus.each {
    	if (it.label) {
        	setTurnOn(it.num, 0)
        }
    }
}

def setTurnOn(lightNum, onAgainDelay) {
    //determine turn-on time for each light and schedule the turnOn#() that will verify the remaining conditions before ultimately turning each light on
	log.debug "executing setTurnOn(lightNum: ${lightNum}, onAgainDelay: ${onAgainDelay})"
    
    def idx = lightNum - 1 //(-1 because the map index starts at 0)
    def light = state.lightStatus[idx]
    def tz = location.timeZone
    def onDate = schedOnDate(lightNum)
	
    if (onAgainDelay) {
        //method was called from turnOff#() to turn the light back on after the "lightOffFor" delay
        onDate = new Date(now() + onAgainDelay)
        log.debug "calculated ON time for turning the light #${lightNum} back on after the 'off for' delay of ${convertToHMS(onAgainDelay)} :: ${onDate}"
	} else {   
        if (!onDate) {
            //no turn-on time set, call method to turn light on now; whether or not it actually turns on will depend on dow/mode
            log.debug "no turn-on time specified for light #${lightNum}; requesting to turn on now"
            onDate = new Date(now() - 5000) //set the light turn-on time to 5 sec ago
        } else {
            if (onDate.time < now()) {
                log.debug "scheduled turn-on time of $onDate has already passed for light #${lightNum}; requesting to turn on now"
            } else {
                log.debug "scheduling light #${lightNum} to turn on at $onDate"
            }
        }
    }
    schedTurnOn(lightNum, onDate)
}

def schedTurnOn(lightNum, onDate) {
	//use provided arguments to schedule the light to turn on
    log.debug "executing schedTurnOn(lightNum: ${lightNum}, onDate: ${onDate})"
	
    def nowDate = new Date()
    def random = new Random()
    def maxDelay = 2 * 60 * 1000 //set a delay of up to 2 min to be applied when requested to turn on now
    
    switch(lightNum) {
    	case 1:
        	if (onDate < nowDate) {
            	def delayOnNow = random.nextInt(maxDelay)
                log.debug "calling to turn on the light #${lightNum} in ${convertToHMS(delayOnNow)}"
                turnOn1(delayOnNow)
            } else {
            	runOnce(onDate, turnOn1)
            }
            break
    	case 2:
        	if (onDate < nowDate) {
            	def delayOnNow = random.nextInt(maxDelay)
                log.debug "calling to turn on the light #${lightNum} in ${convertToHMS(delayOnNow)}"
                turnOn2(delayOnNow)
            } else {
            	runOnce(onDate, turnOn2)
            }
            break
    	case 3:
        	if (onDate < nowDate) {
            	def delayOnNow = random.nextInt(maxDelay)
                log.debug "calling to turn on the light #${lightNum} in ${convertToHMS(delayOnNow)}"
                turnOn3(delayOnNow)
            } else {
            	runOnce(onDate, turnOn3)
            }
            break
    	case 4:
        	if (onDate < nowDate) {
            	def delayOnNow = random.nextInt(maxDelay)
                log.debug "calling to turn on the light #${lightNum} in ${convertToHMS(delayOnNow)}"
                turnOn4(delayOnNow)
            } else {
            	runOnce(onDate, turnOn4)
            }
            break
	}
}

def turnOn1(delay) {
	//check conditions and turn on light#1
    log.debug "executing turnOn1(delay: ${delay})"

    def idx = 0
    def lightStatus = state.lightStatus[idx]
    def tz = location.timeZone
    def offDate = schedOffDate(1)
    def nowDate = new Date(now() + 2 * 60 * 1000) //set current time to +2 min so if offDate > nowDate, the light will stay on for at least 2 min
    def timeOk = offDate > nowDate
    def strDOW = nowDOW
    def lightDOWOk = (!lightStatus.days || lightStatus.days?.contains(strDOW))
    def appDOWOk = lightStatus.days ? lightDOWOk : appDaysOk //if set, the light-specific DOW setting overrides the app setting

    log.debug "checking conditions to turn on the light #1 (${lightStatus.label})"
	if (modeOk && lightDOWOk && appDOWOk && timeOk && light1.currentSwitch != "on") {
	    delay = delay ?: 0
        log.debug "we're good to go; turning on the light #1 in ${convertToHMS(delay)}"
        state.lightStatus[idx].appOn = true
        light1.on(delay: delay)
        setTurnOff(1, delay)
    } else {
        def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
        runOnce(tomorrowTime, setAllOn)
    	
        if (!modeOk) {
    		log.debug "lights activation not enabled in current mode; check again at mode change"
    	}
        if (!lightDOWOk) {
            log.debug "light #1 is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
        }
        if (!appDOWOk) {
            log.debug "app is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
        }
        if (!timeOk) {
            log.debug "light #1's turn-off time has already passed; check again tomorrow (${tomorrowTime})"
		}
	}
}	

def turnOn2(delay) {
	//check conditions and turn on light#2
    log.debug "executing turnOn2(delay: ${delay})"

    def idx = 1
    def lightStatus = state.lightStatus[idx]
    def tz = location.timeZone
    def offDate = schedOffDate(2)
    def nowDate = new Date(now() + 2 * 60 * 1000) //set current time to +2 min so if offDate > nowDate, the light will stay on for at least 2 min
    def timeOk = offDate > nowDate
    def strDOW = nowDOW
    def lightDOWOk = (!lightStatus.days || lightStatus.days?.contains(strDOW))
    def appDOWOk = lightStatus.days ? lightDOWOk : appDaysOk //if set, the light-specific DOW setting overrides the app setting

    log.debug "checking conditions to turn on the light #2 (${lightStatus.label})"
	if (modeOk && lightDOWOk && appDOWOk && timeOk && light2.currentSwitch != "on") {
        delay = delay ?: 0
        log.debug "we're good to go; turning on the light #2 in ${convertToHMS(delay)}"
        state.lightStatus[idx].appOn = true
        light2.on(delay: delay)
        setTurnOff(2, delay)
    } else {
        def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
        runOnce(tomorrowTime, setAllOn)
    	
        if (!modeOk) {
    		log.debug "lights activation not enabled in current mode; check again at mode change"
    	}
        if (!lightDOWOk) {
            log.debug "light #2 is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
        }
        if (!appDOWOk) {
            log.debug "app is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
        }
        if (!timeOk) {
            log.debug "light #2's turn-off time has already passed; check again tomorrow (${tomorrowTime})"
		}
	}
}	

def turnOn3(delay) {
	//check conditions and turn on light#3
    log.debug "executing turnOn3(delay: ${delay})"

    def idx = 2
    def lightStatus = state.lightStatus[idx]
    def tz = location.timeZone
    def offDate = schedOffDate(3)
    def nowDate = new Date(now() + 2 * 60 * 1000) //set current time to +2 min so if offDate > nowDate, the light will stay on for at least 2 min
    def timeOk = offDate > nowDate
    def strDOW = nowDOW
    def lightDOWOk = (!lightStatus.days || lightStatus.days?.contains(strDOW))
    def appDOWOk = lightStatus.days ? lightDOWOk : appDaysOk //if set, the light-specific DOW setting overrides the app setting

    log.debug "checking conditions to turn on the light #3 (${lightStatus.label})"
	if (modeOk && lightDOWOk && appDOWOk && timeOk && light3.currentSwitch != "on") {
        delay = delay ?: 0
        log.debug "we're good to go; turning on the light #3 in ${convertToHMS(delay)}"
        state.lightStatus[idx].appOn = true
        light3.on(delay: delay)
        setTurnOff(3, delay)
    } else {
        def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
        runOnce(tomorrowTime, setAllOn)
    	
        if (!modeOk) {
    		log.debug "lights activation not enabled in current mode; check again at mode change"
    	}
        if (!lightDOWOk) {
            log.debug "light #3 is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
        }
        if (!appDOWOk) {
            log.debug "app is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
        }
        if (!timeOk) {
            log.debug "light #3's turn-off time has already passed; check again tomorrow (${tomorrowTime})"
		}
	}
}	

def turnOn4(delay) {
	//check conditions and turn on light#4
    log.debug "executing turnOn4(delay: ${delay})"

    def idx = 3
    def lightStatus = state.lightStatus[idx]
    def tz = location.timeZone
    def offDate = schedOffDate(4)
    def nowDate = new Date(now() + 2 * 60 * 1000) //set current time to +2 min so if offDate > nowDate, the light will stay on for at least 2 min
    def timeOk = offDate > nowDate
    def strDOW = nowDOW
    def lightDOWOk = (!lightStatus.days || lightStatus.days?.contains(strDOW))
    def appDOWOk = lightStatus.days ? lightDOWOk : appDaysOk //if set, the light-specific DOW setting overrides the app setting

    log.debug "checking conditions to turn on the light #4 (${lightStatus.label})"
	if (modeOk && lightDOWOk && appDOWOk && timeOk && light4.currentSwitch != "on") {
        delay = delay ?: 0
        log.debug "we're good to go; turning on the light #4 in ${convertToHMS(delay)}"
        state.lightStatus[idx].appOn = true
        light4.on(delay: delay)
        setTurnOff(4, delay)
    } else {
        def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
        runOnce(tomorrowTime, setAllOn)
    	
        if (!modeOk) {
    		log.debug "lights activation not enabled in current mode; check again at mode change"
    	}
        if (!lightDOWOk) {
            log.debug "light #4 is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
        }
        if (!appDOWOk) {
            log.debug "app is not enabled on ${strDOW}; check again tomorrow (${tomorrowTime})"
        }
        if (!timeOk) {
            log.debug "light #4's turn-off time has already passed; check again tomorrow (${tomorrowTime})"
		}
	}
}	

def setTurnOff(lightNum, onDelay) {
    // ***  CALL TO SCHEDULE THE TURN-OFF  ***
	def idx = lightNum - 1 //-1 because the map index starts at 0
    def light = state.lightStatus[idx]
    def nowTime = now()
    def offDate = schedOffDate(lightNum)
    
    def times = [:]
    times.offTime = offDate ? offDate.time : null
    if (light.onFor) { //calculate the adjusted off time if a light-on duration was specified
    	def lightOnFor = light.onFor * 60 * 1000
		if (light.rdm) {
            def random = new Random()
            def rdmDelay = random.nextInt(light.rdm)
			lightOnFor = lightOnFor - (light.rdm * 30000) + (rdmDelay * 60000)
            lightOnFor = Math.max(0, lightOnFor) //make sure that we don't end up with a negative number
		}
		times.lightOnForEnd = nowTime + lightOnFor + onDelay
    }
    
    log.debug "light #${lightNum} updated OFF times :: " +
        "lightOnForEnd: ${times.lightOnForEnd ? new Date(times.lightOnForEnd) : null}, " +
        "offDate: ${times.offTime ? new Date(times.offTime) : null}"
	
    if (times) {
        def offTime = times.min{it.value}.value //take the earliest of the calculated and adjusted off times
        offDate = new Date(offTime) //convert time back into date format (had to be unix format for min function)
        log.debug "light #${lightNum} earliest turn-off time (after adjusting for onFor delay) :: ${offDate} (${times.min{it.value}.key})"
        if (offTime > nowTime) {
            log.debug "scheduling turn-off of the light #${lightNum} to occur at ${offDate}"
        } else {
            log.debug "the calculated turn-off time has already passed; calling for the light #${lightNum} to turn off now"
        }
        schedTurnOff(lightNum, offDate)
    } else {
        log.debug "no turn-off time specified for the light #${lightNum}"
    }
}

def schedTurnOff(lightNum, offDate) {
	//use provided arguments to schedule the light to turn off
	def nowDate = new Date()
    def random = new Random()
    def maxDelay = 2 * 60 * 1000 //set a delay of up to 2 min to be applied when requested to turn off now
    switch(lightNum) {
    	case 1:
        	if (offDate < nowDate) {
            	def delayOffNow = random.nextInt(maxDelay)
                turnOff1(delayOffNow)
            } else {
                runOnce(offDate, turnOff1)
            }
            break
    	case 2:
        	if (offDate < nowDate) {
            	def delayOffNow = random.nextInt(maxDelay)
                turnOff2(delayOffNow)
            } else {
                runOnce(offDate, turnOff2)
            }
            break
    	case 3:
        	if (offDate < nowDate) {
            	def delayOffNow = random.nextInt(maxDelay)
                turnOff3(delayOffNow)
            } else {
                runOnce(offDate, turnOff3)
            }
            break
    	case 4:
        	if (offDate < nowDate) {
            	def delayOffNow = random.nextInt(maxDelay)
                turnOff4(delayOffNow)
            } else {
                runOnce(offDate, turnOff4)
            }
            break
	}
}

def turnOff1(delay) {
    def lightNum = 1
    def theLight = light1
    
    def idx = lightNum - 1
    def lightStatus = state.lightStatus[idx]
    
    if (lightStatus.appOn == true) {
        delay = delay ?: 0
        log.debug "turning off the light #${lightNum} in ${convertToHMS(delay)}"
        theLight.off(delay: delay)
        state.lightStatus[idx].appOn = false
        if (lightStatus.offFor) {
            def onAgainDelay = lightStatus.offFor * 60 * 1000
            if (lightStatus.rdm) {
                def random = new Random()
                def rdmOffset = random.nextInt(lightStatus.rdm)
                onAgainDelay = (lightStatus.offFor * 60 * 1000) - (lightStatus.rdm * 30000) + (rdmOffset * 60000)
                onAgainDelay = Math.max(0, onAgainDelay) //make sure that we don't end up with a negative number
			}
            setTurnOn(lightNum, onAgainDelay)
        } else {
        	def tz = location.timeZone
            def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
            log.debug "the light #${lightNum} isn't scheduled to turn back on today; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, setAllOn)
        }
    } else {
		log.debug "the light #${lightNum} wasn't turned on by this app; doing nothing"
    }
}

def turnOff2(delay) {
    def lightNum = 2
    def theLight = light2
    
    def idx = lightNum - 1
    def lightStatus = state.lightStatus[idx]
    
    if (lightStatus.appOn == true) {
        delay = delay ?: 0
        log.debug "turning off the light #${lightNum} in ${convertToHMS(delay)}"
        theLight.off(delay: delay)
        state.lightStatus[idx].appOn = false
        if (lightStatus.offFor) {
            def onAgainDelay = lightStatus.offFor * 60 * 1000
            if (lightStatus.rdm) {
                def random = new Random()
                def rdmOffset = random.nextInt(lightStatus.rdm)
                onAgainDelay = (lightStatus.offFor * 60 * 1000) - (lightStatus.rdm * 30000) + (rdmOffset * 60000)
                onAgainDelay = Math.max(0, onAgainDelay) //make sure that we don't end up with a negative number
			}
            setTurnOn(lightNum, onAgainDelay)
        } else {
        	def tz = location.timeZone
            def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
            log.debug "the light #${lightNum} isn't scheduled to turn back on today; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, setAllOn)
        }
    } else {
		log.debug "the light #${lightNum} wasn't turned on by this app; doing nothing"
    }
}

def turnOff3(delay) {
    def lightNum = 3
    def theLight = light3
    
    def idx = lightNum - 1
    def lightStatus = state.lightStatus[idx]
    
    if (lightStatus.appOn == true) {
        delay = delay ?: 0
        log.debug "turning off the light #${lightNum} in ${convertToHMS(delay)}"
        theLight.off(delay: delay)
        state.lightStatus[idx].appOn = false
        if (lightStatus.offFor) {
            def onAgainDelay = lightStatus.offFor * 60 * 1000
            if (lightStatus.rdm) {
                def random = new Random()
                def rdmOffset = random.nextInt(lightStatus.rdm)
                onAgainDelay = (lightStatus.offFor * 60 * 1000) - (lightStatus.rdm * 30000) + (rdmOffset * 60000)
                onAgainDelay = Math.max(0, onAgainDelay) //make sure that we don't end up with a negative number
			}
            setTurnOn(lightNum, onAgainDelay)
        } else {
        	def tz = location.timeZone
            def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
            log.debug "the light #${lightNum} isn't scheduled to turn back on today; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, setAllOn)
        }
    } else {
		log.debug "the light #${lightNum} wasn't turned on by this app; doing nothing"
    }
}

def turnOff4(delay) {
    def lightNum = 4
    def theLight = light4
    
    def idx = lightNum - 1
    def lightStatus = state.lightStatus[idx]
    
    if (lightStatus.appOn == true) {
        delay = delay ?: 0
        log.debug "turning off the light #${lightNum} in ${convertToHMS(delay)}"
        theLight.off(delay: delay)
        state.lightStatus[idx].appOn = false
        if (lightStatus.offFor) {
            def onAgainDelay = lightStatus.offFor * 60 * 1000
            if (lightStatus.rdm) {
                def random = new Random()
                def rdmOffset = random.nextInt(lightStatus.rdm)
                onAgainDelay = (lightStatus.offFor * 60 * 1000) - (lightStatus.rdm * 30000) + (rdmOffset * 60000)
                onAgainDelay = Math.max(0, onAgainDelay) //make sure that we don't end up with a negative number
			}
            setTurnOn(lightNum, onAgainDelay)
        } else {
        	def tz = location.timeZone
            def tomorrowTime = timeTodayAfter("23:59", "04:00", tz)
            log.debug "the light #${lightNum} isn't scheduled to turn back on today; check again tomorrow (${tomorrowTime})"
            runOnce(tomorrowTime, setAllOn)
        }
    } else {
		log.debug "the light #${lightNum} wasn't turned on by this app; doing nothing"
    }
}

def turnAllOff() {
	//For each configured light that was turned on by this app, turn the light off after a random delay.
    //Triggered when it's detected that the conditions are no longer valid
    //(e.g. incorrect mode, time outside window, weekday, etc.)
    log.debug "received command to turn all lights off after random delay up to 2 minutes"
    def lightStatus = state.lightStatus
    def theLights = [light1, light2, light3, light4]
    def random = new Random()
    def maxDelay = 2 * 60 * 1000
    for (int i = 0; i < 4; i++) {
    	if (lightStatus[i].appOn) {					//only if the light was initially off (i.e. do not turn off lights that were manually turned on)
        	state.lightStatus[i].appOn = false			//unmark as being turned on by app so that it won't turn off automatically if someone manually turns it on
        	def delay = random.nextInt(maxDelay)	//set random delay(ms) to turn light off within the next 2 minutes
            log.debug "turning off the ${theLights[i].label} in ${convertToHMS(delay)}"
            theLights[i].off(delay: delay)
        }
    }
}

def turnAllOffNow() {
	//turn all the lights off now (only used when initializing/uninstalling the app)
    log.debug "received command to turn all lights off now"
    def theLights = [light1, light2, light3, light4]
    theLights.each { theLight ->
        if (theLight) {
            log.debug "turning off the ${theLight.label}"
            theLight.off()
        }
    }
}


//   ----------------
//   ***   UTILS  ***

private getModeOk() {
	def result = !theModes || theModes.contains(location.mode)
	log.debug "modeOk :: $result"
	return result
}

private getDarkOk() {
	def result = !appDark || itsDarkOut
	log.debug "darkOk :: $result"
	return result
}

private getAppDaysOk() {
	def result = true
	if (appDays) {
        def strDOW = nowDOW
		result = appDays.contains(strDOW)
	}
	log.debug "appDaysOk :: $result"
	return result
}

private getItsDarkOut() {
    def sunTime = getSunriseAndSunset(sunsetOffset: sunsetOffset)
    def timeNow = now() + (2*1000) // be safe and set current time for 2 minutes later
    def result = false
	if(sunTime.sunrise.time < timeNow && sunTime.sunset.time > timeNow){
    	log.debug "it's daytime"
        result = false
    } else {
    	log.debug "it's nighttime"
        result = true
    }
    return result
}

private getNowDOW() {
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
    log.debug "strDOW :: $strDOW"
    return strDOW
}

private convertToHMS(ms) {
    int hours = Math.floor(ms/1000/60/60)
    int minutes = Math.floor((ms/1000/60) - (hours * 60))
    int seconds = Math.floor((ms/1000) - (hours * 60 * 60) - (minutes * 60))
    double millisec = ms-(hours*60*60*1000)-(minutes*60*1000)-(seconds*1000)
    int tenths = (millisec/100).round(0)
    return "${hours}h${minutes}m${seconds}.${tenths}s"
}

private schedOnDate(lightNum) {
    // ***  CALCULATE TURN-ON TIME FOR lightNum(1-4)  ***
    //figure out the next 'on' time for selected light based on the combination of app-wide and light-specific conditions
	def idx = lightNum - 1 //(-1 because the map index starts at 0)
    def light = state.lightStatus[idx]
    def tz = location.timeZone
	def random = new Random()
    def sunTime = getSunriseAndSunset(sunsetOffset: sunsetOffset)
    def sunsetTime = sunTime.sunset
    
    //set the 'times' map to be used for the comparison
    def times = [appStart: null, lightStart: null, sunStart: null]
    times.appStart = appStart ? timeToday(appStart, tz).time : null						//if it exists, store the appStart time in 'times' map for comparison
    times.lightStart = light.lightStart ? timeToday(light.lightStart, tz).time : null	//if it exists, store the lightStart time in the 'times' map for comparison
    times.sunStart = (appDark && !light.daytime) ? sunsetTime.time : null				//if the app setting for "during darkness" is enabled and the light setting to override it is disabled, we need to take the sunset time into consideration; store the sunset time in the times map for comparison.
    
    log.debug "light #${lightNum} calculated ON times :: " +
    	"appStart: ${times.appStart ? new Date(times.appStart) : null}, " +
        "lightStart: ${times.lightStart ? new Date(times.lightStart) : null}, " +
        "sunStart: ${times.sunStart ? new Date(times.sunStart) : null}"
    
    //override appStart if lightStart was set
    times.appStart = times.lightStart ?: times.appStart
    
    if (times.appStart || times.lightStart || times.sunStart) {					//check if at least one time is set
        def onTime = times.max{it.value}.value									//find the max time value (light will not turn on before then)
        def onDate = new Date(onTime)											//convert time back into date format (had to be unix format for max function)
        log.debug "light #${lightNum} latest turn-on time :: ${onDate} (${times.max{it.value}.key})"
        if (light.rdm) {
            //apply random factor to schedDate
            def rdmOffset = random.nextInt(light.rdm)
            onDate = new Date(onTime - (light.rdm * 30000) + (rdmOffset * 60000))
            log.debug "light #${lightNum} random-adjusted turn-on time :: $onDate"
        } else {
            //log.debug "no random factor configured in preferences"
        }
        return onDate
    } else {
    	return null
    }
}

private schedOffDate(lightNum) {
    // ***  CALCULATE TURN-OFF TIME FOR lightNum(1-4)  ***
    //figure out the next 'off' time for selected light based on the combination of app-wide and light-specific conditions
	def idx = lightNum - 1 //(-1 because the map index starts at 0)
    def light = state.lightStatus[idx]
    def tz = location.timeZone
	def random = new Random()
    def sunriseString = location.currentValue("sunriseTime")
    def sunriseDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunriseString)
    
    def times = [:]
    if (appEnd) {
    	times.appEnd = timeTodayAfter("04:00", appEnd, tz).time
    }
    if (light.lightEnd) {
    	times.lightEnd = timeTodayAfter("04:00", light.lightEnd, tz).time
    }
    if (appDark && !light.daytime) {
    	times.sunEnd = sunriseDate.time
    }
    
    log.debug "light #${lightNum} calculated OFF times :: " +
        "appEnd: ${times.appEnd ? new Date(times.appEnd) : null}, " +
        "lightEnd: ${times.lightEnd ? new Date(times.lightEnd) : null}, " +
        "sunEnd: ${times.sunEnd ? new Date(times.sunEnd) : null}"

	//override appEnd if lightEnd was set
    times.appEnd = times.lightEnd ?: times.appEnd

    if (times) {
        def offTime = times.min{it.value}.value
        def offDate = new Date(offTime) //convert time back into date format (had to be unix format for min function)
        log.debug "light #${lightNum} earliest turn-off time :: ${offDate} (${times.min{it.value}.key})"
        if (light.rdm) {
            //apply random factor to offDate
            def rdmOffset = random.nextInt(light.rdm)
            offTime = offTime - (light.rdm * 30000) + (rdmOffset * 60000)
            offDate = new Date(offTime)
            log.debug "light #${lightNum} random-adjusted turn-off time :: $offDate"
        } else {
            //log.debug "no random factor configured in preferences"
        }
        return offDate
    } else {
        return null
    }
}