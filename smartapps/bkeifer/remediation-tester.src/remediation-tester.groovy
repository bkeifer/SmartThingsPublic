/**
 *  Remediation Tester
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
    name: "Remediation Tester",
    namespace: "bkeifer",
    author: "Brian Keifer",
    description: "Testing remediation via Icinga",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
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
    unschedule(checkSensors)
    createSchedule()
    subscribe(app, appTouch)
}

def updateState() {
	log.debug("State updated!")
    state.timestamp = now()
}

def createSchedule() {
    unschedule()
    updateState()
    schedule("0 * * * * ?", "updateState")
}


def appTouch(evt) {
	log.debug "Setting state.timestamp backwards 5 minutes and running unschedule()."
    unschedule(updateState)
    state.timestamp = now() - 300000
    logURLs()

}


def logURLs() {
	if (!state.accessToken) {
		try {
			createAccessToken()
			log.debug "Token: $state.accessToken"
		} catch (e) {
			log.debug("Error.  Is OAuth enabled?")
		}
	}
    def baseURL = "https://graph.api.smartthings.com/api/smartapps/installations"
	log.debug "Stamp URL:  ${baseURL}/${app.id}/stamp?access_token=${state.accessToken}"
	log.debug "Reset URL:  ${baseURL}/${app.id}/reschedule?access_token=${state.accessToken}"
}


def html() {
    def result
    log.trace("now: ${now()}")
    log.trace("stamp: ${atomicState.timestamp}")
    log.trace("diff: ${now() - state.timestamp}")
    if (now() - state.timestamp < 300000) {
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