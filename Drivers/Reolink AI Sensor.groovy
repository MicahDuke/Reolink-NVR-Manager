/**
 * Reolink AI Sensor
 *
 * Hubitat driver for Reolink NVR events
 *
 * Version: 0.1.0
 * Author: Micah Duke
 *
 * MIT License
 */

metadata {

    definition(
        name: "Reolink AI Sensor",
        namespace: "MicahDuke",
        author: "Micah Duke"
    ) {

        capability "Motion Sensor"
        capability "Sensor"

    }


    preferences {

        input(
            name:"debugLogging",
            type:"bool",
            title:"Enable debug logging",
            defaultValue:false
        )

    }

}


def installed(){

    sendEvent(
        name:"motion",
        value:"inactive"
    )

}


def updateMotion(value){

    sendEvent(
        name:"motion",
        value:value
    )


    if(debugLogging){

        log.info "${device.label}: ${value}"

    }

}
