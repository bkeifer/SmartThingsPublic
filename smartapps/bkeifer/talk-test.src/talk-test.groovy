/**
 *  Talk Test
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
    name: "Talk Test",
    namespace: "bkeifer",
    author: "Brian Keifer",
    description: "Send text to DLNA renderer",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Title") {
		input "speech", "capability.speechSynthesis", title: "speech", required: true, multiple: false 
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
    subscribe(app, appTouch)

}

def appTouch(evt) {
	log.debug("Talking!")
	speech.speak('testing multiple words')
    //music.playTrack("http://valinor.net/st/Test.mp3")
}


// TODO: implement event handlers
//http://valinor.net/st/AllClear.mp3
//http://valinor.net/st/AllDoorsSecure.mp3
//http://valinor.net/st/AllMoisturesDry.mp3
//http://valinor.net/st/ForesterGarageDoor.mp3
//http://valinor.net/st/FrontDoor.mp3
//http://valinor.net/st/GarageEntryDoor.mp3
//http://valinor.net/st/GarageMoisture.mp3
//http://valinor.net/st/GreatRoomSlidingGlassDoor.mp3
//http://valinor.net/st/LaundryAreaMoisture.mp3
//http://valinor.net/st/LowerPatioDoor.mp3
//http://valinor.net/st/MBRWaterHeaterMoisture.mp3
//http://valinor.net/st/MultipleDoors.mp3
//http://valinor.net/st/SumpPumpMoisture.mp3
//http://valinor.net/st/SunRoomFrenchDoor.mp3
//http://valinor.net/st/WaterMeterMoisture.mp3
//http://valinor.net/st/WRXGarageDoor.mp3