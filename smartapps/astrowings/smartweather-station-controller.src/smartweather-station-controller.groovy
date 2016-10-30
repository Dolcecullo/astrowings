/**
 *  Weather Station Controller
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
 *  Adapted from post found at
 *    https://community.smartthings.com/t/release-smart-weather-station-tile-updater-fix-broken-modes/8403
 *  
 * 
 *  VERSION HISTORY
 *
 *  Version 1.51
 *   2016-10-30 - set updateInterval minimum value of 5 minutes in scheduledEvent()
 *				  make device type compatible with SmartWeather Station Tile 2.0
 *   2016-02-12 - Changed scheduling API's (hopefully more resilient), added an option for users to specify update interval
 *   2016-01-20 - Kick-start timers on sunrise and sunset also
 *   2015-10-04 - Kick-start timers on each mode change to prevent them from dying
 *   2015-07-12 - Simplified app, udpates every 5 minutes now (hopefully more reliable)
 *   2015-07-17 - Improved reliability when mode changes
 *	 2015-06-06 - Bugfix for timers not scheduling, keep only one timer
 *				  Added support to update multiple devices
 *				  Added support for frequency of updates            
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
 */

definition(
    name: "SmartWeather Station Controller",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Updates SmartWeather Station Tile devices at specified intervals.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn@3x.png"
)


//   -----------------------------------
//   ***   SETTING THE PREFERENCES   ***

preferences {
    section("About") {
    	paragraph title: "This SmartApp updates SmartWeather Station Tile devices at specified intervals.",
        	"version 1.5"
    }
    section ("Weather Devices") {
        input name: "weatherDevices", type: "device.smartWeatherStationTile2", title: "Select device(s)", description: "Select the Weather Tiles to update", required: true, multiple: true
        input name: "updateFreq", type: "number", title: "Update frequency (min. 5 minutes)", description: "How often do you want to update the weather information?", required: true, defaultValue: 15
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
    log.info "uninstalled"
}

def initialize() {
	log.info "initializing"
    subscribe(location, modeChangeHandler)
    subscribe(location, "sunset", sunsetHandler)
    subscribe(location, "sunrise", sunriseHandler)
    doRefresh()
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def modeChangeHandler(evt) {
    log.trace "modeChangeHandler>${evt.descriptionText}"
    log.info "kick-starting the refresh schedule due to mode change"
    doRefresh()
}

def sunsetHandler(evt) {
    log.trace "sunsetHandler>${evt.descriptionText}"
    log.info "kick-starting the refresh schedule due to sunset event"
    doRefresh()
}

def sunriseHandler(evt) {
    log.trace "sunriseHandler>${evt.descriptionText}"
    log.info "kick-starting the refresh schedule due to sunrise event"
    doRefresh()
}


//   -------------------
//   ***   METHODS   ***

def doRefresh() {
    log.trace "doRefresh() - refresh frequency setting: $updateFreq minutes"
    def adjustedFreq = updateFreq
    if (adjustedFreq < 5) {
        //log.debug "refresh frequency adjusted to the minimum value of 5 minutes"
        adjustedFreq = 5
    }
    runIn(adjustedFreq * 60, doRefresh)
    weatherDevices.refresh()
}