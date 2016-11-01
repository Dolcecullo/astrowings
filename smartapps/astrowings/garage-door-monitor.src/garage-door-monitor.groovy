/**
 *  Garage Door Monitor
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
 	 def versionNum() {	return "version 1.03" }       /*
 
 *	 v1.03 (01-Nov-2016): standardize section headers
 *   v1.02 (26-Oct-2016): added trace for each event handler
 *   v1.01 (26-Oct-2016): added 'About' section in preferences
 *   v1.00 (2016 date unknown): working version, no version tracking up to this point
 *
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


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	section("About") {
    	paragraph title: "This SmartApp sends a notification (or, optionally, a SMS) to notify" +
        	"that a door is left open when leaving the house, left open for too long, or if it opens while away.",
        	"version 1.02"
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
	section("Notify me if the door opens while I'm away") {
		input "warnOpening", "bool", title: "Yes/No?", required: false, defaultValue: false
	}
	section("Let me know anytime it's left open for too long") {
		input "maxOpenMinutes", "number", title: "How long? (minutes)", multiple: false, required: false
	}
    section("Send SMS alerts?"){
    input "phone", "phone", title: "Phone number (For SMS - Optional)", required: false
    }
}
//TODO: add option to send periodic reminders


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private C_1() { return "this is constant1" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***



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
    state.debugLevel = 0
    subscribe(myself, "presence.not present", iLeaveHandler)
    subscribe(everyone, "presence.not present", allLeaveHandler)
    subscribe(thedoor, "contact", doorHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def iLeaveHandler(evt) {
    log.trace "iLeaveHandler>${evt.descriptionText}"
    if (thedoor.currentContact == "open") {
    	def message = "${evt.device} has left the house and the ${thedoor.device} is ${thedoor.currentContact}."
        log.warn message
        sendText(message)
        sendPush(message)
    }
}

def allLeaveHandler(evt) {
    log.trace "allLeaveHandler>${evt.descriptionText}"
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
	log.trace "doorHandler>${evt.descriptionText}"
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
	log.trace "locationPositionChange>${evt.descriptionText}"
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

def sendText(msg) {
	if (phone) {
		log.debug "sending SMS"
		sendSms(phone, msg)
	}
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getEveryoneIsAway() {
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

def getImAway() {
	def result = !(myself.currentPresence == "present")
    log.debug "imAway :: $result"
    return result
}


//   ------------------------
//   ***   COMMON UTILS   ***

