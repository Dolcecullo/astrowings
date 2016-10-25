/**
 *  Door Lock Monitor
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
    name: "Door Lock Monitor",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Monitor a door lock and report anomalies",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home3-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home3-icn@3x.png")


//   -----------------------------------
//   ***   SETTING THE PREFERENCES   ***

preferences {
	section() {
    	paragraph "This app sends a push notification if a lock gets unlocked or if the mode changes while the lock is unlocked."
    }
    section("Monitor this door lock") {
        input "theLock", "capability.lock", required: true, title: "Which lock?"
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
    subscribe(theLock, "lock.unlocked", unlockHandler)
    subscribe(location, modeChangeHandler)
    subscribe(location, "position", locationPositionChange) //update settings if the hub location changes

}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def unlockHandler(evt) {
	def unlockText = evt.descriptionText
	log.debug unlockText
	sendPush(unlockText)
}

def modeChangeHandler(evt) {
	if (theLock.currentLock == "unlocked") {
    	def unlockedMsg = "The mode changed to $location.currentMode and the $theLock.label is $theLock.currentLock"
        log.debug unlockedMsg
        sendPush(unlockedMsg)
    }
}

def locationPositionChange(evt) {
	log.trace "locationChange()"
	initialize()
}
