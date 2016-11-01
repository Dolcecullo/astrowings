/**
 *  Light up front door
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
    name: "Light-up front door",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Turn on a light when someone arrives home and it's dark out.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light9-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	section("About") {
    	paragraph title: "This SmartApp turns on a light when someone arrives home and it's dark out. (e.g. to turn on a porch light)",
        	"version 1.02"
    }
    section("When any of these people arrive") {
        input "people", "capability.presenceSensor", title: "Who?", multiple: true
    }
    section("Turn on this light") {
        input "theLight", "capability.switch", title: "Which light?"
    }
    section("And leave it on for...") {
        input "leaveOn", "number", title: "How long? (minutes)"
    }
}
    

//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private C_1() { return "this is constant1" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***



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
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    state.debugLevel = 0
	subscribe(people, "presence.present", presenceHandler)
    subscribe(location, "position", locationPositionChange) //update settings if hub location changes
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def presenceHandler(evt) {
    log.trace "presenceHandler>${evt.descriptionText}"
    log.debug "$evt.displayName has arrived"
    //turn on the light only if it's currently off
    if (itsDarkOut && (theLight.currentSwitch != "on")) {
        log.debug "do turn on $theLight.displayName"
        turnOn()
    } else {
        log.debug "do nothing"
    }
}

def locationPositionChange(evt) {
	log.trace "locationPositionChange>${evt.descriptionText}"
	initialize()
}


//   -------------------
//   ***   METHODS   ***

def turnOn() {
    log.debug "turning on $theLight.displayName"
    theLight.on()
    log.debug "scheduling $theLight.displayName to turn off in $leaveOn minutes"
    runIn(60 * leaveOn, turnOff)
}

def turnOff() {
    log.debug "turning off $theLight.displayName"
    theLight.off()
}


//   -------------------------
//   ***   APP FUNCTIONS   ***

def getItsDarkOut() {
    def sunTime = getSunriseAndSunset(sunsetOffset: 15)
    def currentDTG = new Date()
    def result = false
	//log.debug "sunTime.sunrise: $sunTime.sunrise"
	//log.debug "sunTime.sunset: $sunTime.sunset (with 15 min offset)"
    //log.debug "unx_sunTime.sunset: ${sunTime.sunset.time}"
    //log.debug "dat_unx_sunTime.sunset: ${new Date(sunTime.sunset.time)}"

	if(sunTime.sunrise < currentDTG && sunTime.sunset > currentDTG){
    	log.debug "it's daytime"
        result = false
    } else {
    	log.debug "it's nighttime"
        result = true
    }
    return result
}


//   ------------------------
//   ***   COMMON UTILS   ***
