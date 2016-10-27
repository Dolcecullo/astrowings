/**
 *  Switch on Motion
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
 *   v1.02 (26-Oct-2016): added trace for each event handler
 *   v1.01 (26-Oct-2016): added 'About' section in preferences
 *   v1 (2016 date unknown): working version, no version tracking up to this point
 *
*/
definition(
    name: "Switch on Motion",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn light on/off based on motion",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home30-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@3x.png")


//   -----------------------------------
//   ***   SETTING THE PREFERENCES   ***

preferences {
	section("About") {
    	paragraph "This SmartApp turns a light on/off based on detected motion"
        paragraph "version 1.02"
    }
    section("When motion is detected on this sensor:") {
        input "theMotion", "capability.motionSensor", required: true, title: "Where?"
    }
    section("Turn on this light:") {
        input "theSwitch", "capability.switch", required: true, title: "Which light?"
    }
    section("Set the conditions") {
        input "whenDark", "bool", title: "Only after sunset?", required: false, defaultValue: true
        //input "theModes", "mode", title: "Only in certain modes?", required: false, multiple: true
    }
    section("Turn the light off when there's been no movement for this long:") {
        input "minutes", "number", required: true, title: "How long? (minutes)"
    }
}


//   ----------------------------
//   ***   APP INSTALLATION   ***

def installed() {
	log.info "installed with settings: $settings"
    initialize()
}

def updated() {
    log.info "updated with settings $settings"
	unsubscribe()
    unschedule()
    initialize()
}

def uninstalled() {
	lightOff()
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    state.enable = true
    subscribe(theMotion, "motion.active", motionDetectedHandler)
    subscribe(theMotion, "motion.inactive", motionStoppedHandler)
    subscribe(theSwitch, "switch.on", switchOnHandler)
    subscribe(theSwitch, "switch.off", switchOffHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def motionDetectedHandler(evt) {
    log.trace "motionDetectedHandler>${evt.descriptionText}"
    if (allOk) {
        theSwitch.on()
    }
}

def motionStoppedHandler(evt) {
	log.trace "motionStoppedHandler>${evt.descriptionText}"
    runIn(minutes * 60, lightOff)
}

def switchOnHandler(evt) {
	log.trace "switchOnHandler>${evt.descriptionText}"
    log.info "switch turned on; enabling automation"
    state.enable = true
}

def switchOffHandler(evt) {
	log.trace "switchOffHandler>${evt.descriptionText}"
    if (evt.isPhysical()) {
    	//disable automation when switch is activated manually (i.e. prevent light from turning back on when movement is detected)
        log.info "switch physically turned off; disabling automation"
        state.enable = false
    } else {
    	log.info "switch turned off by app"
    }
}

def locationPositionChange(evt) {
	log.trace "locationChange>${evt.descriptionText}"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def lightOff() {
   	theSwitch.off()
}


//   ----------------
//   ***   UTILS  ***

def getAllOk() {
	def result = darkOk && state.enable == true // && modeOk
    //log.debug "allOk :: $result"
    return result
}

/*def getModeOk() {
	def result = !theModes || theModes.contains(location.mode)
	log.debug "modeOk :: $result"
	return result
}*/

def getDarkOk() {
	def result = !whenDark || itsDarkOut
	//log.debug "darkOk :: $result"
	return result
}

def getItsDarkOut() {
    def sunTime = getSunriseAndSunset(sunsetOffset: "-00:30")
    def currentDTG = new Date()
    def result = false

	if(sunTime.sunrise < currentDTG && sunTime.sunset > currentDTG){
    	//log.debug "it's daytime"
        result = false
    } else {
    	//log.debug "it's nighttime"
        result = true
    }
    return result
}