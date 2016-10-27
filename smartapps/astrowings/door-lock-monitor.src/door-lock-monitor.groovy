/**
 *  Door Lock Monitor
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
	section("About") {
    	paragraph "This SmartApp sends a push notification if a lock gets unlocked or if the mode changes while the lock is unlocked."
        paragraph "version 1.02"
    }
    section("Monitor this door lock") {
        input "theLock", "capability.lock", required: true, title: "Which lock?"
    }
}
//TODO: add the option to enable based on presence


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
	log.trace "unlockHandler>${evt.descriptionText}"
    def unlockText = evt.descriptionText
	log.debug unlockText
	sendPush(unlockText)
}

def modeChangeHandler(evt) {
	log.trace "modeChangeHandler>${evt.descriptionText}"
    if (theLock.currentLock == "unlocked") {
    	def unlockedMsg = "The mode changed to $location.currentMode and the $theLock.label is $theLock.currentLock"
        log.debug unlockedMsg
        sendPush(unlockedMsg)
    }
}

def locationPositionChange(evt) {
	log.trace "locationPositionChange>${evt.descriptionText}"
	initialize()
}