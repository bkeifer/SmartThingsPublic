/**
 *  Log to Graphite
 *
 *  Copyright 2014 Brian Keifer
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
 *  Based on "Xively Logger" by Patrick Stuart (patrick@patrickstuart.com)
 *
 */
definition(
    name: "Log to Graphite",
    namespace: "bkeifer",
    author: "Brian Keifer",
    description: "Log various things to a Graphite instance",
    category: "Convenience",
    iconUrl: "http://grafana.valinor.net/img/fav32.png",
    iconX2Url: "http://grafana.valinor.net/img/fav32.png",
    iconX3Url: "http://grafana.valinor.net/img/fav32.png")


preferences {

	section("Log devices...") {
        input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true
        input "humidities", "capability.relativeHumidityMeasurement", title: "Humidities", required:false, multiple: true
        input "contacts", "capability.contactSensor", title: "Contacts", required: false, multiple: true
        input "illuminances", "capability.illuminanceMeasurement", title: "Illuminances", required: false, multiple: true
        input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
        input "switches", "capability.switch", title: "Switches", required: false, multiple: true
        input "batteries", "capability.battery", title: "Batteries", required:false, multiple: true
        input "thermostats", "capability.thermostat", title: "Thermostat Setpoints", required: false, multiple: true
        input "energymeters", "capability.energyMeter", title: "Energy Meters", required: false, multiple: true
    }
    section ("Graphite Server") {
    	input "graphite_host", "text", title: "Graphite Hostname/IP"
        input "graphite_port", "number", title: "Graphite Port"
    }
	//section() {
	//	paragraph "${generateURL("stamp").join()}"
    //			href url:"${generateURL("link").join()}", style:"embedded", required:false, title:"URL", description:"Tap to view, then click \"Done\""
	//}
}

mappings {
  path("/stamp") {
    action: [
      GET: "html",
    ]
  }
  path("/reschedule") {
    action: [
      GET: "reschedule",
    ]
  }
}

/*def generateURL(path) {
        try {
            createAccessToken()
            log.debug "Creating new Access Token: $state.accessToken"
        } catch (ex) {
            log.error "Did you forget to enable OAuth in SmartApp IDE settings for ActiON Dashboard?"
            log.error ex
        }
	
    ["https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/$path", "?access_token=${state.accessToken}"]
}*/


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    log.debug "App ID: ${app.id}"
	unsubscribe()
	initialize()
}

def initialize() {

	state.clear()
    unschedule(checkSensors)
    createSchedule()
    subscribe(app, appTouch)
}


def createSchedule() {
    unschedule()
    schedule("0 * * * * ?", "checkSensors")
}


def appTouch(evt) {
	log.debug "Manually triggered: $evt"
    unschedule(checkSensors)
    createSchedule()
    checkSensors()
    log.debug(generateURL("stamp"))

}


def generateURL(path) {
	log.debug "resetOauth: $resetOauth"
	if (resetOauth) {
		log.debug "Reseting Access Token"
		state.accessToken = null
	}

	if (!resetOauth && !state.accessToken || resetOauth && !state.accessToken) {
		try {
			createAccessToken()
			log.debug "Creating new Access Token: $state.accessToken"
		} catch (ex) {
			log.error "Did you forget to enable OAuth in SmartApp IDE settings for ActiON Dashboard?"
			log.error ex
		}
	}

	["https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/$path", "?access_token=${state.accessToken}"]
}


def checkSensors() {

    def logitems = []

    for (t in settings.temperatures) {
    	try {
	        logitems.add([t.displayName, "temperature", Double.parseDouble(t.latestValue("temperature").toString())] )
	        state[t.displayName + ".temp"] = t.latestValue("temperature")
	        log.debug("[temp]     " + t.displayName + ": " + t.latestValue("temperature"))
        } finally {
        	continue
        }
    }
    for (t in settings.humidities) {
    	logitems.add([t.displayName, "humidity", Double.parseDouble(t.latestValue("humidity").toString())] )
	    state[t.displayName + ".humidity"] = t.latestValue("humidity")
		log.debug("[humidity] " + t.displayName + ": " + t.latestValue("humidity"))
    }
    for (t in settings.batteries) {
    	try {
			log.debug("[battery]  " + t.displayName + ": " + t.latestValue("battery"))
	        logitems.add([t.displayName, "battery", Double.parseDouble(t.latestValue("battery").toString())] )
	        state[t.displayName + ".battery"] = t.latestValue("battery")
        } finally {
        	continue
        }
	}

    for (t in settings.contacts) {
		try {
			logitems.add([t.displayName, "contact", t.latestValue("contact")] )
	        state[t.displayName + ".contact"] = t.latestValue("contact")
        } finally {
        	continue
        }
    }

    for (t in settings.motions) {
		try {
			logitems.add([t.displayName, "motion", t.latestValue("motion")] )
	        state[t.displayName + ".motion"] = t.latestValue("motion")
        } finally {
        	continue
        }
	}

    for (t in settings.illuminances) {
    	try {
	        def x = new BigDecimal(t.latestValue("illuminance") ) // instanceof Double)
	        logitems.add([t.displayName, "illuminance", x] )
	        state[t.displayName + ".illuminance"] = x
			log.debug("[luminance] " + t.displayName + ": " + t.latestValue("illuminance"))
        } finally {
        	continue
        }
	}

    for (t in settings.switches) {
		try {
			logitems.add([t.displayName, "switch", (t.latestValue("switch") == "on" ? 1 : 0)] )
	        state[t.displayName + ".switch"] = (t.latestValue("switch") == "on" ? 1 : 0)
	        log.debug("[switch] " + t.displayName + ": " + (t.latestValue("switch") == "on" ? 1 : 0))
        } finally {
        	continue
        }
	}

    for (t in settings.thermostats) {
    	try {
	        logitems.add([t.displayName + ".heatingSetpoint", "thermostat", t.latestValue("heatingSetpoint")] )
	        state[t.displayName + ".heatingSetpoint"] = t.latestValue("heatingSetpoint")
	        log.debug("[thermostat] " + t.displayName + ".heatingSetpoint: " + t.latestValue("heatingSetpoint"))
        
	        logitems.add([t.displayName + ".coolingSetpoint", "thermostat", t.latestValue("coolingSetpoint")] )
	        state[t.displayName + ".coolingSetpoint"] = t.latestValue("heatingSetpoint")
	        log.debug("[thermostat] " + t.displayName + ".coolingSetpoint: " + t.latestValue("coolingSetpoint"))
        
	        def currentStateHeat
	        def currentStateCool
            log.debug(t.latestValue("thermostatOperatingState"))
			switch(t.latestValue("thermostatOperatingState")) {
	        	case("idle"):
	            	currentStateHeat = 0
	            	currentStateCool = 0
	                break
                
	            case("heating"):
	            	currentStateHeat = 1
	            	currentStateCool = 0
	                break
                
	            case("cooling"):
	            	currentStateHeat = 0
	                currentStateCool = 1
	                break
	        }
        
			logitems.add([t.displayName + ".heating", "thermostat", currentStateHeat] )
	        state[t.displayName + ".heating"] = currentStateHeat
	        log.debug("[thermostat] " + t.displayName + ".heating: " + currentStateHeat)

			logitems.add([t.displayName + ".cooling", "thermostat", currentStateCool] )
	        state[t.displayName + ".cooling"] = currentStateCool
	        log.debug("[thermostat] " + t.displayName + ".cooling: " + currentStateCool)
        } finally {
        	continue
        }
	}

    for (t in settings.energymeters) {
    	try {
	        logitems.add([t.displayName + ".power", "energy", t.latestValue("power")])
	        state[t.displayName + ".Watts"] = t.latestValue("power")
	        log.debug("[energy] " + t.displayName + ": " + t.latestValue("power"))

	        logitems.add([t.displayName + ".amps", "energy", t.latestValue("amps")])
	        state[t.displayName + ".Amps"] = t.latestValue("amps")
	        log.debug("[energy] " + t.displayName + ": " + t.latestValue("amps"))

			logitems.add([t.displayName + ".volts", "energy", t.latestValue("volts")])
	        state[t.displayName + ".Volts"] = t.latestValue("volts")
	        log.debug("[energy] " + t.displayName + ": " + t.latestValue("volts"))
        } finally {
        	continue
        }
	}

	logField2(logitems)
    state.timestamp = now()

}


private logField2(logItems) {
    def fieldvalues = ""
    def timeNow = now()
    timeNow = (timeNow/1000).toInteger()

    logItems.eachWithIndex() { item, i ->
		def path = item[0].replace(" ","")
		def value = item[2]

		def json = "{\"metric\":\"${path}\",\"value\":\"${value}\",\"measure_time\":\"${timeNow}\"}"
		log.debug json

		def params = [
        	uri: "http://${graphite_host}:${graphite_port}/publish/${item[1]}",
            body: json
        ]
        try {
        	httpPostJson(params)// {response -> parseHttpResponse(response)}
        }
		catch ( groovyx.net.http.HttpResponseException ex ) {
        	log.debug "Unexpected response error: ${ex.statusCode}"
        }
	}
}


def html() {
    def result
    log.trace("now: ${now()}")
    log.trace("stamp: ${atomicState.timestamp}")
    log.trace("diff: ${now() - state.timestamp}")
    if (now() - state.timestamp < 1200000) {
        result = "FIRING"
    } else {
        result = "FAIL"
    }
    render contentType: "text/html", data: "<!DOCTYPE html><html><head></head><body>${result}<br><hr><br>App: ${app.name}<br>Last timestamp: ${new Date(state.timestamp)}</body></html>"
}


def reschedule() {
  createSchedule()
  log.trace("Rescheduled via web API call!")
  render contentType: "text/html", data: "<!DOCTYPE html><html><head></head><body>Rescheduled ${app.name}</body></html>"
}