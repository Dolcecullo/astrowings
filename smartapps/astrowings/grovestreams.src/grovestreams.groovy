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
 *	VERSION HISTORY                                       */
 	 def versionNum() {	return "version 1.01" }          /*
 
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
    section("About") {
        paragraph title: "This SmartApp logs events from selected sensors to the GroveStreams data analytics platform",
            "version 1"
    }
    section("Log devices...") {
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
    section ("GroveStreams Feed PUT API key...") {
        input "apiKey", "text", title: "API key"
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
    initialize()
}
 
def uninstalled() {
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    state.debugLevel = 0
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

    log.debug "Logging to GroveStreams ${compId}, ${streamId} = ${value}"

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
                log.debug "GroveStreams logging failed, status = ${response.status}"
            }
        }
    
    } catch (groovyx.net.http.ResponseParseException e) {
        // ignore error 200, bogus exception
        if (e.statusCode != 200) {
            log.error "Grovestreams: ${e}"
        }
    } catch (Exception e) {
        log.error "Grovestreams: ${e}"
    }

}


//   -------------------------
//   ***   APP FUNCTIONS   ***



//   ------------------------
//   ***   COMMON UTILS   ***
