/**
 *  Everyone's Presence
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
    name: "Everyone's Presence",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Emulates a single presence sensor for all physical sensors: will set to 'not present' when nobody is home / 'present' if at least one person is home",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home4-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home4-icn@3x.png")


//   -----------------------------------
//   ***   SETTING THE PREFERENCES   ***

preferences {
	section("About") {
    	paragraph title: "Emulates a single presence sensor for all physical sensors: " +
        	"will set to 'not present' when nobody is home, or 'present' if at least one person is home",
        	"version 1.02"
    }
	section("Physical Presence Sensors") {
		input "presenceSensors", "capability.presenceSensor", multiple: true, required: true,
        	title: "Physical Presence Sensors",
            description: "select the real presence sensors that will be used to determine the state of the simulated presence"
	}
    section("Simulated Presence Sensor") {
    	input "simulatedPresence", "device.simulatedPresenceSensor", title: "Simulated Presence Sensor", multiple: false, required: true
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
    //unschedule()
    initialize()
}

def uninstalled() {
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
	setPresence()
	subscribe(presenceSensors, "presence", presenceHandler)
    subscribe(simulatedPresence, "presence", simHandler)
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def presenceHandler(evt) {
	log.trace "presenceHandler>${evt.descriptionText}"
    setPresence()
}

def simHandler(evt) {
	log.info "simHandler>${evt.displayName} set to ${evt.value}"
}


//   -------------------
//   ***   METHODS   ***

def setPresence(){
	def presentCount = 0
    presenceSensors.each {
    	if (it.currentValue("presence") == "present") {
        	presentCount++
        }
    }
    
    log.debug("presentCount: ${presentCount}, simulatedPresence: ${simulatedPresence.currentValue("presence")}")
    
    if (presentCount > 0) {
    	if (simulatedPresence.currentValue("presence") != "present") {
    		simulatedPresence.arrived()
            log.debug("Arrived")
        }
    } else {
    	if (simulatedPresence.currentValue("presence") != "not present") {
    		simulatedPresence.departed()
            log.debug("Departed")
        }
    }
}