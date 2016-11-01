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
 *	VERSION HISTORY                                    */
 	 def versionNum() {	return "version 1.60" }       /*
 
 *   v1.60 (01-Nov-2016): standardize pages layout
 *	 v1.52 (01-Nov-2016): standardize section headers
 *   v1.51 (30-Oct-2016): copied code from RBoy
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

preferences {
}

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false) {
    	section(){
        	paragraph "", title: "This SmartApp updates SmartWeather Station Tile devices at specified intervals."
        }
        section ("Weather Devices") {
            input name: "weatherDevices", type: "device.smartWeatherStationTile2", title: "Select device(s)", description: "Select the Weather Tiles to update", required: true, multiple: true
            input name: "updateFreq", type: "number", title: "Update frequency (min. 5 minutes)", description: "How often do you want to update the weather information?", required: true, defaultValue: 15
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

//   -------------------------
//   ***   APP FUNCTIONS   ***



//   ------------------------
//   ***   COMMON UTILS   ***

def debug(message, shift = null, lvl = null, err = null) {
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
		log.info "◦◦$prefix$message", err
	} else if (lvl == "trace") {
		log.trace "◦$prefix$message", err
	} else if (lvl == "warn") {
		log.warn "◦$prefix$message", err
	} else if (lvl == "error") {
		log.error "◦$prefix$message", err
	} else {
		log.debug "$prefix$message", err
	}
}