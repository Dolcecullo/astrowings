/**
 *  Everyone's Presence
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
	section("Select Presence Sensor Group") {
		input "presenceSensors", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: true
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
	subscribe(presenceSensors, "presence", "presenceHandler")
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def presenceHandler(evt) {
	setPresence()
}


//   -------------------
//   ***   METHODS   ***

def setPresence(){
	def presentCounter = 0
    presenceSensors.each {
    	if (it.currentValue("presence") == "present") {
        	presentCounter++
        }
    }
    
    log.debug("presentCounter: ${presentCounter}, simulatedPresence: ${simulatedPresence.currentValue("presence")}")
    
    if (presentCounter > 0) {
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
