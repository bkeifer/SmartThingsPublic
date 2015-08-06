/**
 *  Don't Kill The Fish
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
    name: "Don't Kill The Fish",
    namespace: "bkeifer",
    author: "Brian Keifer",
    description: "DKTF",
    category: "My Apps",
    iconUrl: "http://i.imgur.com/V2vEmmO.jpg",
    iconX2Url: "http://i.imgur.com/V2vEmmO.jpg",
    iconX3Url: "http://i.imgur.com/V2vEmmO.jpg",
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
    log.debug "Initializing"
    createSchedule()
    subscribe(app, appTouch)
}

def updateState() {
	log.debug("State updated!")
    state.timestamp = now()
}

def createSchedule() {
    unschedule()
    log.debug("Schedule created.")
    updateState()
    runEvery5Minutes("updateState")
}


def appTouch(evt) {
	log.debug "Setting state.timestamp backwards 5 minutes and running unschedule()."
    unschedule(updateState)
    state.timestamp = now() - 300000
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


def html() {
    def result
    log.trace("now: ${now()}")
    log.trace("stamp: ${atomicState.timestamp}")
    log.trace("diff: ${now() - state.timestamp}")
    if (now() - state.timestamp < 1200000) {
        result = "FIRING<br><img src=\"http://i.imgur.com/V2vEmmO.jpg\">"
    } else {
        result = "FAIL<br><img src=\"http://i.imgur.com/lIF0JbH.jpg\">"
    }
    render contentType: "text/html", data: "<!DOCTYPE html><html><head></head><body>${result}<br><hr><br>App: ${app.name}<br>Last timestamp: ${new Date(state.timestamp)}</body></html>"
}


def reschedule() {
  createSchedule()
  log.trace("Rescheduled via web API call!")
  render contentType: "text/html", data: "<!DOCTYPE html><html><head></head><body>Rescheduled ${app.name}</body></html>"
}