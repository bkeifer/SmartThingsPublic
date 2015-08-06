/**
 *  Event Logger
 *
 *  Copyright 2015 Brian Keifer
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
    name: "LogStash Event Logger",
    namespace: "bkeifer",
    author: "Brian Keifer",
    description: "Log SmartThings events to a LogStash server",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Log these presence sensors:") {
        input "presences", "capability.presenceSensor", multiple: true, required: false
    }
 	section("Log these switches:") {
    	input "switches", "capability.switch", multiple: true, required: false
    }
 	section("Log these switch levels:") {
    	input "levels", "capability.switchLevel", multiple: true, required: false
    }
	section("Log these motion sensors:") {
    	input "motions", "capability.motionSensor", multiple: true, required: false
    }
	section("Log these temperature sensors:") {
    	input "temperatures", "capability.temperatureMeasurement", multiple: true, required: false
    }
    section("Log these humidity sensors:") {
    	input "humidities", "capability.relativeHumidityMeasurement", multiple: true, required: false
    }
    section("Log these contact sensors:") {
    	input "contacts", "capability.contactSensor", multiple: true, required: false
    }
    section("Log these alarms:") {
		input "alarms", "capability.alarm", multiple: true, required: false
	}
    section("Log these indicators:") {
    	input "indicators", "capability.indicator", multiple: true, required: false
    }
	section("Log these acceleration sensors:") {
    	input "accelerations", "capability.acceleration", multiple: true, required: false
    }
    section("Log these CO detectors:") {
    	input "codetectors", "capability.carbonMonoxideDetector", multiple: true, required: false
    }
    section("Log these smoke detectors:") {
    	input "smokedetectors", "capability.smokeDetector", multiple: true, required: false
    }
    section("Log these water detectors:") {
    	input "waterdetectors", "capability.waterSensor", multiple: true, required: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
	doSubscriptions()
}

def doSubscriptions() {
	subscribe(alarms,			"alarm",					genericHandler)
    subscribe(codetectors,		"carbonMonoxideDetector",	genericHandler)
	subscribe(contacts,			"contact",      			genericHandler)
    subscribe(indicators,		"indicator",    			genericHandler)
    subscribe(modes,			"locationMode", 			genericHandler)
    subscribe(motions,			"motion",       			genericHandler)
   	subscribe(presences,		"presence",     			genericHandler)
    subscribe(relays,			"relaySwitch",  			genericHandler)
	subscribe(smokedetectors,	"smokeDetector",			genericHandler)
	subscribe(switches,			"switch",       			genericHandler)
    subscribe(levels,			"level",					genericHandler)
	subscribe(temperatures,		"temperature",  			genericHandler)
	subscribe(waterdetectors,	"water",					genericHandler)
    subscribe(location,			"location",					genericHandler)
}

def genericHandler(evt) {
	log.debug("------------------------------")
	log.debug("date: ${evt.date}")
	log.debug("name: ${evt.name}")
    log.debug("displayName: ${evt.displayName}")
    log.debug("device: ${evt.device}")
    log.debug("deviceId: ${evt.deviceId}")
    log.debug("value: ${evt.value}")
    log.debug("isStateChange: ${evt.isStateChange()}")
	log.debug("id: ${evt.id}")
    log.debug("description: ${evt.description}")
    log.debug("descriptionText: ${evt.descriptionText}")
    log.debug("installedSmartAppId: ${evt.installedSmartAppId}")
    log.debug("isoDate: ${evt.isoDate}")
    log.debug("isDigital: ${evt.isDigital()}")
    log.debug("isPhysical: ${evt.isPhysical()}")
    log.debug("location: ${evt.location}")
    log.debug("locationId: ${evt.locationId}")
    log.debug("source: ${evt.source}")
    log.debug("unit: ${evt.unit}")
    def json = "{"
    json += "\"date\":\"${evt.date}\","
    json += "\"name\":\"${evt.name}\","
    json += "\"displayName\":\"${evt.displayName}\","
    json += "\"device\":\"${evt.device}\","
    json += "\"deviceId\":\"${evt.deviceId}\","
    json += "\"value\":\"${evt.value}\","
    json += "\"isStateChange\":\"${evt.isStateChange()}\","
    json += "\"id\":\"${evt.id}\","
    json += "\"description\":\"${evt.description}\","
    json += "\"descriptionText\":\"${evt.descriptionText}\","
    json += "\"installedSmartAppId\":\"${evt.installedSmartAppId}\","
    json += "\"isoDate\":\"${evt.isoDate}\","
    json += "\"isDigital\":\"${evt.isDigital()}\","
    json += "\"isPhysical\":\"${evt.isPhysical()}\","
    json += "\"location\":\"${evt.location}\","
    json += "\"locationId\":\"${evt.locationId}\","
    json += "\"unit\":\"${evt.unit}\","
    json += "\"source\":\"${evt.source}\""
    json += "}"
    log.debug("JSON: ${json}")
    
    def params = [
    	uri: "http://graphite.valinor.net:5279",
        body: json
    ]
    try {
        httpPostJson(params)
    } catch ( groovyx.net.http.HttpResponseException ex ) {
       	log.debug "Unexpected response error: ${ex.statusCode}"
    }
}


//private logField2(logItems) {
//    def fieldvalues = ""
//    def timeNow = now()
//    timeNow = (timeNow/1000).toInteger()
//
//    logItems.eachWithIndex() { item, i ->
//		def path = item[0].replace(" ","")
//		def value = item[2]
//
//		def json = "{\"metric\":\"${path}\",\"value\":\"${value}\",\"measure_time\":\"${timeNow}\"}"
//		log.debug json
//
//		def params = [
//        	uri: "http://${graphite_host}:${graphite_port}/publish/${item[1]}",
//            body: json
//        ]
//        try {
//        	httpPostJson(params)// {response -> parseHttpResponse(response)}
//        }
//		catch ( groovyx.net.http.HttpResponseException ex ) {
//        	log.debug "Unexpected response error: ${ex.statusCode}"
//        }
//	}
//}

def presenceHandler(evt) {
	genericHandler(evt)
}

def switchHandler(evt) {
	genericHandler(evt)
}

def contactHandler(evt) {
	genericHandler(evt)
}

def temperatureHandler(evt) {
	genericHandler(evt)
}

def motionHandler(evt) {
	genericHandler(evt)
}

def modeHandler(evt) {
	genericHandler(evt)
}

def relayHandler(evt) {
	genericHandler(evt)
}