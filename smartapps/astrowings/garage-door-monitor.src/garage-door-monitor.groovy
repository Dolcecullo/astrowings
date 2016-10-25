/**
 *  Garage Door Monitor
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
    name: "Garage Door Monitor",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Notify if garage door is left open when leaving the house, left open for too long, or if it opens while away.",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation12-icn@3x.png")


preferences {
	section() {
    	paragraph "Notify if garage door is left open when leaving the house, left open for too long, or if it opens while away."
    }
	section("When I leave") {
		input "myself", "capability.presenceSensor", title: "Who?", multiple: false, required: true
	}
    section("Or when all these persons leave") {
    	input "everyone", "capability.presenceSensor", title: "Who?", multiple: true, required: false
    }
	section("Send a notification if this door is left open") {
		input "thedoor", "capability.contactSensor", title: "Which door?", multiple: false, required: true
	}
	section("Also notify me if the door opens while I'm away") {
		input "warnOpening", "bool", title: "Yes/No?", required: false, defaultValue: false
	}
	section("And let me know anytime it's left open for too long") {
		input "maxOpenMinutes", "number", title: "Minutes?", multiple: false, required: false
	}
    section("Send SMS alerts?"){
    input "phone", "phone", title: "Phone number (For SMS - Optional)", required: false
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
    log.info "uninstalled"
}

def initialize() {
    log.info "Initializing"
    subscribe(myself, "presence.not present", iLeaveHandler)
    subscribe(everyone, "presence.not present", allLeaveHandler)
    subscribe(thedoor, "contact", doorHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def iLeaveHandler(evt) {
    if (thedoor.currentContact == "open") {
    	def message = "${evt.device} has left the house and the ${thedoor.device} is ${thedoor.currentContact}."
        log.warn message
        sendText(message)
        sendPush(message)
    }
}

def allLeaveHandler(evt) {
    if (thedoor.currentContact == "open") {
        if (everyoneIsAway) {
            def message = "Everyone has left the house and the ${thedoor.device} is ${thedoor.currentContact}."
            log.warn message
            sendText(message)
            sendPush(message)
		} else {
            log.debug "The ${thedoor.device} is ${thedoor.currentContact} but not everyone is away; doing nothing"
        }
    }
}

def doorHandler(evt) {
	if (evt.value == "open" && warnOpening && imAway) {
    	def message = "The ${thedoor.device} was opened."
        log.warn message
        sendText(message)
        sendPush(message)
	} else if (evt.value == "open" && maxOpenMinutes) {
    	log.info "The ${thedoor.device} was opened; scheduling a check in $maxOpenMinutes minutes to see if it's still open."
    	runIn(60 * maxOpenMinutes, checkOpen)
    }
}

def locationPositionChange(evt) {
	log.trace "locationChange()"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def checkOpen() {
    if (thedoor.currentContact == "open") {
    	def message = "The ${thedoor.device} has been opened for $maxOpenMinutes minutes."
        log.warn message
		sendText(message)
        sendPush(message)
    } else {
    	log.info "The ${thedoor.device} is no longer open."
    }
}

private sendText(msg) {
	if (phone) {
		log.debug "sending SMS"
		sendSms(phone, msg)
	}
}


//   ----------------
//   ***   UTILS  ***

private getEveryoneIsAway() {
    def result = true
    for (person in everyone) {
        if (person.currentPresence == "present") {
            result = false
            break
        }
    }
    log.debug "everyoneIsAway: $result"
    return result
}

private getImAway() {
	def result = !(myself.currentPresence == "present")
    log.debug "imAway :: $result"
    return result
}
