/**
 *  Lights when door unlocks
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
    name: "Lights when door unlocks",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn a light on when a door is unlocked from outside.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@3x.png")


//   -----------------------------------
//   ***   SETTING THE PREFERENCES   ***

preferences {
	section() {
    	paragraph "This app turns on a light when a door is unlocked using the keypad."
    }
    section("When this door is unlocked using the keypad") {
        input "theLock", "capability.lock", required: true, title: "Which lock?"
    }
    section("Turn on this light") {
        input "theSwitch", "capability.switch", required: true, title: "Which light?"
        input "leaveOn", "number", title: "For how long (minutes)?"
    }
    section("Set the conditions") {
        input "whenDark", "bool", title: "Only after sunset?", required: false, defaultValue: true
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
	switchOff()
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    subscribe(theLock, "lock.unlocked", unlockHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def locationPositionChange(evt) {
	log.trace "locationChange()"
	initialize()
}

def unlockHandler(evt) {
	if (allOk) {
    	def unlockText = evt.descriptionText
        if (unlockText.contains("was unlocked with code")) {
            log.debug "${unlockText}; turning the light on"
            switchOn()
            log.debug "scheduling the light to turn off in ${leaveOn} minutes"
            runIn(leaveOn * 60, switchOff)
        } else {
        	log.debug "door wasn't unlocked using the keypad; doing nothing"
        }
    } else {
    	//log.debug "conditions not met; doing nothing" //TODO: why?
    }
}


//   -------------------
//   ***   METHODS   ***

def switchOn() {
	theSwitch.on()
}

def switchOff() {
	log.debug "turning the light off"
	theSwitch.off()
}


//   ----------------
//   ***   UTILS  ***

private getAllOk() {
	def result = theSwitch.currentSwitch == "off" && darkOk
    log.debug "allOk :: ${result}"
    return result
}

/*private getModeOk() {
	def result = !theModes || theModes.contains(location.mode)
	log.debug "modeOk :: $result"
	return result
}*/

private getDarkOk() {
	def result = !whenDark || itsDarkOut
	//log.debug "darkOk :: $result"
	return result
}

private getItsDarkOut() {
    def sunTime = getSunriseAndSunset(sunsetOffset: "00:30")
    def currentDTG = new Date()
    def result = false

	if(sunTime.sunrise < currentDTG && sunTime.sunset > currentDTG){
    	log.debug "it's daytime"
        result = false
    } else {
    	log.debug "it's nighttime"
        result = true
    }
    return result
}
