/**
 * Reolink Motion Sensor Driver
 *
 * Version: 2.2.0
 * Author: Micah Duke
 *
 * Used by Reolink NVR Manager
 */

metadata {
    definition(
        name: "Reolink Motion Sensor",
        namespace: "local",
        author: "Micah Duke"
    ) {
        capability "Motion Sensor"
        capability "Sensor"

        attribute "person", "number"
        attribute "vehicle", "number"
        attribute "motionType", "string"
    }

    preferences {
        input(
            name: "debugLogging",
            type: "bool",
            title: "Enable debug logging",
            defaultValue: false
        )
    }
}

def installed() {
    sendEvent(name: "motion", value: "inactive")
    sendEvent(name: "motionType", value: "None")
}

def updateEvent(type) {
    debugLog("updateEvent(${type})")
    sendEvent(name: "motionType", value: type)
}

def updateMotion(state) {

    debugLog("updateMotion(${state})")

    if(device.currentValue("motion") != state) {

        log.info "UPDATE MOTION CALLED: ${device.displayName} = ${state}"

        sendEvent(
            name: "motion",
            value: state
        )
    }
}

private void debugLog(msg) {
    if (settings?.debugLogging) {
        log.debug msg
    }
}
