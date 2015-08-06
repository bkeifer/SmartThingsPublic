/**
 *  Metathermometer
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
    name: "Metathermometer",
    namespace: "bkeifer",
    author: "Brian Keifer",
    description: "A virtual thermometer that averages multiple other temperature measurements",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Preferences") {
        input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:true, multiple: true
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
    subscribe(temperatures, "temperature", temperatureHandler)

    state.temperature = [:]
    state.average = 0
    for (t in settings.temperatures) {
		state.temperature[t.id] = t.currentState("temperature").value
	}
    log.debug(state.temperature)
    calculateAverage()
}

def temperatureHandler(evt) {
    log.debug "This event name is ${evt.name}"
    log.debug "The value of this event is ${evt.value}"
    log.debug "This event happened at ${evt.date}"
    log.debug "The value of this event is different from its previous value: ${evt.isStateChange()}"
	log.debug("Before: ${state.temperature[t.id]}")
	if (evt.isStateChange()) {
    	state.temperature[evt.deviceId] = evt.value
        calculateAverage()
    }
	log.debug("After: ${state.temperature[t.id]}")
}

def calculateAverage() {
	def average = 0
	def total = 0
    def count = state.temperature.size()
    log.debug("Count: ${count}")
    def previousTemp = state.average.toInteger()
	log.debug("Prev: ${previousTemp}")
	state.temperature.each{ device, temp -> total += temp.toInteger() }
	log.debug("Total: ${total}")
    log.debug("Avg: ${total/count}")
}