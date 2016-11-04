/**
 *  Weather Station Controller
 *
 *  Copyright © 2016 Phil Maynard
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0                                       */
 	       def urlApache() { return "http://www.apache.org/licenses/LICENSE-2.0" }      /*
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
 	 def versionNum() {	return "version 1.71" }       /*
 *
 *   v1.72 (03-Nov-2016): use constant instead of hard-coding for minimum refresh rate
 *   v1.71 (02-Nov-2016): add link for Apache license
 *   v1.70 (02-Nov-2016): implement multi-level debug logging function
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

		 //	  name (C_XXX)			value					description
private		C_MIN_REFRESH()			{ return 5 }			//minimum refresh rate (minutes)


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
            input name: "updateRate", type: "number", title: "Update frequency (min. ${C_MIN_REFRESH()} minutes)", description: "How often do you want to update the weather information?", required: true, range: "${C_MIN_REFRESH()}..*", defaultValue: 15
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
            href name: "hrefLicense", title: "License", description: "Apache License", url: urlApache()
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
    unschedule()
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
    doRefresh()
    debug "initialization complete", "trace", -1
}

def subscribeToEvents() {
    debug "subscribing to events", "trace", 1
    subscribe(location, modeChangeHandler)
    subscribe(location, "sunset", sunsetHandler)
    subscribe(location, "sunrise", sunriseHandler)
    debug "subscriptions complete", "trace", -1
}


//   --------------------------
//   ***   EVENT HANDLERS   ***

def modeChangeHandler(evt) {
    debug "modeChangeHandler event: ${evt.descriptionText}", "trace", 1
    debug "kick-starting the refresh schedule due to mode change", "info"
    doRefresh()
    debug "modeChangeHandler complete", "trace", -1
}

def sunsetHandler(evt) {
    debug "sunsetHandler event: ${evt.descriptionText}", "trace", 1
    debug "kick-starting the refresh schedule due to sunset event", "info"
    doRefresh()
    debug "sunsetHandler complete", "trace", -1
}

def sunriseHandler(evt) {
    debug "sunriseHandler event: ${evt.descriptionText}", "trace", 1
    debug "kick-starting the refresh schedule due to sunrise event", "info"
    doRefresh()
    debug "sunriseHandler complete", "trace", -1
}


//   -------------------
//   ***   METHODS   ***

def doRefresh() {
    debug "executing doRefresh()", "trace", 1
    debug "user refresh rate setting (updateRate): $updateRate minutes"
	def minRate = C_MIN_REFRESH()
    def userRate = updateRate
    def adjRate = userRate > minRate ? userRate : minRate
    debug "adjusted refresh rate: ${adjRate}"
    runIn(adjRate * 60, doRefresh)
    weatherDevices.refresh()
    debug "doRefresh() complete", "trace", -1
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