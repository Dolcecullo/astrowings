/**
 *  Test
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
 	 def versionNum() {	return "version #" }          /*
 
 *   31-Oct-2016 : v# - most recent release changes
 *   28-Oct-2016 : v# - previous release changes
 *
*/
definition(
    name: "Test",
    namespace: "astrowings",
    author: "Phil Maynard",
    description: "Test",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


//   ---------------------------
//   ***   APP PREFERENCES   ***

preferences {
	page(name: "pageMain")
    page(name: "pageSettings")
    page(name: "pageRemove")
}


//   --------------------------------
//   ***   CONSTANTS DEFINITIONS  ***

private		C_1()				{ return "this is constant1" }
private		getC_2()			{ return "this is constant2" }
private		getSOME_CONSTANT()	{ return "this is some constant" }


//   -----------------------------
//   ***   PAGES DEFINITIONS   ***

//TODO: implement href state (exemple: https://github.com/SmartThingsCommunity/Code/blob/master/smartapps/preferences/page-params-by-href.groovy)
def pageMain() {
	dynamicPage(name: "pageMain", title: "Main", install: true, uninstall: false) {
        section() {
        	paragraph title: "This is the intro section (paragraph title)", "paragraph content"
        }
        section("Inputs") {
            input "theSwitch", "capability.switch",
            	title: "Title",
                description: "description",
                multiple: false,
                required: true,
                submitOnChange: false
        }
		section() {
			href "pageSettings", title: "Settings", image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png", required: false
		}
    }
}
    
def pageSettings() {
	dynamicPage(name: "pageSettings", title: "Settings", install: false, uninstall: false) { //with 'install: false', clicking 'Done' goes back to previous page
		section("About") {
			paragraph versionNum(), title: "Version"
            paragraph stateCap(), title: "Memory Usage"
			label name: "name", title: "Name", state: (name ? "complete" : null), defaultValue: app.name, required: false
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
   		section("Remove Test") {
			href "pageRemove", title: "href Title", description: "Remove Test", required: false
		}
    }
}

def pageRemove() {
	dynamicPage(name: "pageRemove", title: "Remove", install: true, uninstall: true) { //with 'install: true', clicking 'Done' installs the app
		section("Test") {
        	paragraph "testing", title: "warning"
        }
	}
}


//   ----------------------------
//   ***   APP INSTALLATION   ***

def installed() {
    state.debugLevel = 0
    debug "begin installed()","trace",1
    debug "installed with settings: $settings","info"
	initialize()
    debug "end installed()","trace",-1
}

def updated() {
    state.debugLevel = 0
    debug "begin updated()","trace",1
    debug "updated with settings: $settings","info"
	unsubscribe()
    //unschedule()
    initialize()
    debug "end updated()","trace",-1
}

def uninstalled() {
    debug "uninstalled","trace"
}

def initialize() {
	debug "begin initialize()","trace",1
    debug "initializing","info"
	debugTest()
    subscribeToEvents()
    debug "end initialize()","trace",-1
}

def subscribeToEvents() {
	debug "begin subscribeToEvents()","trace",1
	//subscribe(theSwitch, "switch", eventProperties)
    //subscribe(theSwitch, "switch", eventTest)
    subscribe(theSwitch, "switch", debugTest)
    debug "end subscribeToEvents()","trace",-1
}

//   --------------------------
//   ***   EVENT HANDLERS   ***

void eventProperties(evt) {
    log.trace "eventProperties>data:${evt.data}"
    log.trace "eventProperties>description:${evt.description}"
    log.trace "eventProperties>descriptionText:${evt.descriptionText}"
    log.trace "eventProperties>device:${evt.device}"
    log.trace "eventProperties>displayName:${evt.displayName}"
    log.trace "eventProperties>deviceId:${evt.deviceId}"
    log.trace "eventProperties>name:${evt.name}"
    log.trace "eventProperties>source:${evt.source}"
    log.trace "eventProperties>stringValue:${evt.stringValue}"
    log.trace "eventProperties>unit:${evt.unit}"
    log.trace "eventProperties>value:${evt.value}"
    log.trace "eventProperties>isDigital:${evt.isDigital()}"
    log.trace "eventProperties>isPhysical:${evt.isPhysical()}"
    log.trace "eventProperties>isStateChange:${evt.isStateChange()}"
}

def eventTest(evt) {
	debug "switch event"
}

//   -------------------
//   ***   METHODS   ***

def debugTest() {
	debug "begin debugTest()","trace",1
    debug "constant1 : ${C_1()}"//			-> this is constant1 
    debug "constant2a: ${C_2}"//			-> null
    debug "constant2b: ${c_2}"//			-> this is constant2 
    debug "constant3 : ${SOME_CONSTANT}"//	-> this is some constant
   	debug "a random number between 4 and 16 could be: ${randomWithRange(4, 16)}"
    debug "end debugTest()","trace",-1
}

//   ------------------------
//   ***   COMMON UTILS   ***

/*
 ** see below for idiot-proof version of this **
 
        int randomWithRange(int min, int max)
        {
           int range = (max - min) + 1;     
           return (int)(Math.random() * range) + min;
        }
        
 *
 */

int randomWithRange(int min, int max)
{
   int range = Math.abs(max - min) + 1;     
   return (int)(Math.random() * range) + (min <= max ? min : max);
}

def stateCap(showBytes = true) {
	def bytes = state.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}

def cpu() {
	if (state.lastExecutionTime == null) {
		return "N/A"
	} else {
		def cpu = Math.round(state.lastExecutionTime / 20000)
		if (cpu > 100) {
			cpu = 100
		}
		return "$cpu%"
	}
}


//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
//   *******************   TEST ZONE  ********************   //
//   Put new code here before moving up into main sections   //
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//

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