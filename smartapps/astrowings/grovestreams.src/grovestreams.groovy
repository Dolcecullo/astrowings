/**
 *  Sunset Lights
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
 *  Adapted from example written by Jason Steele with usage instructions and code found at
 *    https://www.grovestreams.com/developers/getting_started_smartthings.html
 *  
 * 
 *	VERSION HISTORY                                    */
 	 def versionNum() {	return "version 1.20" }       /*
 
 *   v1.20 (02-Nov-2016): implement multi-level debug logging function
 *   v1.10 (01-Nov-2016): standardize pages layout
 *	 v1.01 (01-Nov-2016): standardize section headers
 *   v1.00 (30-Oct-2016): copied code from example (https://www.grovestreams.com/developers/getting_started_smartthings.html)
 *
*/
definition(
    name: "GroveStreams",
    namespace: "astrowings",
    author: "Jason Steele",
    description: "Log to GroveStreams",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Office/office8-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Office/office8-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Office/office8-icn@3x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
    page(name: "pageSettings")
    page(name: "pageUninstall")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private C_1() { return "this is constant1" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp logs events from selected sensors to the GroveStreams data analytics platform"
        }
        section("Log devices...") { //TODO: move section to its own page
            input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true
            input "humidities", "capability.relativeHumidityMeasurement", title: "Humidities", required: false, multiple: true
            input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true
            input "accelerations", "capability.accelerationSensor", title: "Accelerations", required: false, multiple: true
            input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
            input "presence", "capability.presenceSensor", title: "Presence", required: false, multiple: true
            input "switches", "capability.switch", title: "Switches", required: false, multiple: true
            input "waterSensors", "capability.waterSensor", title: "Water sensors", required: false, multiple: true
            input "batteries", "capability.battery", title: "Batteries", required:false, multiple: true
            input "powers", "capability.powerMeter", title: "Power Meters", required:false, multiple: true
            input "energies", "capability.energyMeter", title: "Energy Meters", required:false, multiple: true
        }
		section() {
            href "pageSettings", title: "App settings", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", required: false
		}
    }
}

def pageSettings() {
	dynamicPage(name: "pageSettings", install: false, uninstall: false) {
		section("About") {
        	paragraph "Copyright ©2016 Phil Maynard\n${versionNum()}", title: app.name
            //TODO: link to license
		}
        section ("GroveStreams Feed PUT API key") {
            input "apiKey", "text", title: "Enter API key"
        }
   		section() {
			label title: "Assign a name", defaultValue: "${app.name}", required: false
            href "pageUninstall", title: "Uninstall", description: "Uninstall this SmartApp", state: null, required: true
		}
        section("Debugging Options", hideable: true, hidden: true) {
            input "debugging", "bool", title: "Enable debugging", defaultValue: false, required: false, submitOnChange: true
            if (debugging) {
                input "log#info", "bool", title: "Log info messages", defaultValue: true, required: false
                input "log#trace", "bool", title: "Log trace messages", defaultValue: true, required: false
                input "log#debug", "bool", title: "Log debug messages", defaultValue: true, required: false
                input "log#warn", "bool", title: "Log warning messages", defaultValue: true, required: false
                input "log#error", "bool", title: "Log error messages", defaultValue: true, required: false
            }
        }
    }
}

def pageUninstall() {
	dynamicPage(name: "pageUninstall", title: "Uninstall", install: false, uninstall: true) {
		section() {
        	paragraph "CAUTION: You are about to completely remove the SmartApp '${app.name}'. This action is irreversible. If you want to proceed, tap on the 'Remove' button below.",
                required: true, state: null
        }
	}
}


//   ----------------------------
//   ***   APP INSTALLATION   ***

def installed() {
	debug "installed with settings: ${settings}", "trace"
    initialize()
}
 
def updated() {
    debug "updated with settings ${settings}", "trace"
	unsubscribe()
    initialize()
}
 
def uninstalled() {
    state.debugLevel = 0
    debug "application uninstalled", "trace"
}

def initialize() {
    state.debugLevel = 0
    debug "initializing", "trace", 1
    subscribeToEvents()
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    subscribe(temperatures, "temperature", handleTemperatureEvent)
    subscribe(waterSensors, "water", handleWaterEvent)
    subscribe(humidities, "humidity", handleHumidityEvent)
    subscribe(contacts, "contact", handleContactEvent)
    subscribe(accelerations, "acceleration", handleAccelerationEvent)
    subscribe(motions, "motion", handleMotionEvent)
    subscribe(presence, "presence", handlePresenceEvent)
    subscribe(switches, "switch", handleSwitchEvent)
    subscribe(batteries, "battery", handleBatteryEvent)
    subscribe(powers, "power", handlePowerEvent)
    subscribe(energies, "energy", handleEnergyEvent)
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def handleTemperatureEvent(evt) {
    sendValue(evt) { it.toString() }
}
 
def handleWaterEvent(evt) {
    sendValue(evt) { it == "wet" ? "true" : "false" }
}
 
def handleHumidityEvent(evt) {
    sendValue(evt) { it.toString() }
}
 
def handleContactEvent(evt) {
    sendValue(evt) { it == "open" ? "true" : "false" }
}
 
def handleAccelerationEvent(evt) {
    sendValue(evt) { it == "active" ? "true" : "false" }
}
 
def handleMotionEvent(evt) {
    sendValue(evt) { it == "active" ? "true" : "false" }
}
 
def handlePresenceEvent(evt) {
    sendValue(evt) { it == "present" ? "true" : "false" }
}
 
def handleSwitchEvent(evt) {
    sendValue(evt) { it == "on" ? "true" : "false" }
}
 
def handleBatteryEvent(evt) {
    sendValue(evt) { it.toString() }
}
 
def handlePowerEvent(evt) {
    sendValue(evt) { it.toString() }
}
 
def handleEnergyEvent(evt) {
    sendValue(evt) { it.toString() }
}
 

//   -------------------
//   ***   METHODS   ***

private sendValue(evt, Closure convert) {
    def compId = URLEncoder.encode(evt.displayName.trim())
    def streamId = evt.name
    def value = convert(evt.value)

    debug "logging to GroveStreams ${compId}, ${streamId} = ${value}", "info"

    def url = "https://grovestreams.com/api/feed?api_key=${apiKey}&compId=${compId}&${streamId}=${value}"

    //Make the actual device the origin of the message to avoid exceeding 12 calls within 2 minutes rule:
    //http://forum.grovestreams.com/topic/155/10-second-feed-put-limit-algorithm-change/
    def header = ["X-Forwarded-For": evt.deviceId]

    try {
        def putParams = [
            uri: url,
            header: header,
            body: []]

        httpPut(putParams) { response ->
            if (response.status != 200 ) {
                debug "GroveStreams logging failed, status = ${response.status}", "error"
            }
        }
    
    } catch (groovyx.net.http.ResponseParseException e) {
        // ignore error 200, bogus exception
        if (e.statusCode != 200) {
            debug "Grovestreams exception: ${e}", "error"
        }
    } catch (Exception e) {
        debug "Grovestreams exception:: ${e}", "error"
    }

}


//   -------------------------
//   ***   APP FUNCTIONS   ***



//   ------------------------
//   ***   COMMON UTILS   ***

def debug(message, lvl = null, shift = null, err = null) {
	def debugging = settings.debugging
	if (!debugging) {
		return
	}
	lvl = lvl ?: "debug"
	if (!settings["log#$lvl"]) {
		return
	}
	
    def maxLevel = 4
	def level = state.debugLevel ?: 0
	def levelDelta = 0
	def prefix = "║"
	def pad = "░"
	
    //shift is:
	//	 0 - initialize level, level set to 1
	//	 1 - start of routine, level up
	//	-1 - end of routine, level down
	//	 anything else - nothing happens
	
    switch (shift) {
		case 0:
			level = 0
			prefix = ""
			break
		case 1:
			level += 1
			prefix = "╚"
			pad = "═"
			break
		case -1:
			levelDelta = -(level > 0 ? 1 : 0)
			pad = "═"
			prefix = "╔"
			break
	}

	if (level > 0) {
		prefix = prefix.padLeft(level, "║").padRight(maxLevel, pad)
	}

	level += levelDelta
	state.debugLevel = level

	if (debugging) {
		prefix += " "
	} else {
		prefix = ""
	}

    if (lvl == "info") {
        log.info ": :$prefix$message", err
	} else if (lvl == "trace") {
        log.trace "::$prefix$message", err
	} else if (lvl == "warn") {
		log.warn "::$prefix$message", err
	} else if (lvl == "error") {
		log.error "::$prefix$message", err
	} else {
		log.debug "$prefix$message", err
	}
}