/**
 * Reolink AI Sensor
 *
 * Version: 0.2.0
 * Author: Micah Duke
 */

metadata {

    definition(
        name: "Reolink AI Sensor",
        namespace: "MicahDuke",
        author: "Micah Duke"
    ) {

        capability "Motion Sensor"
        capability "Sensor"

        attribute "eventType", "string"

    }

}


preferences {

    input(
        name:"debugLogging",
        type:"bool",
        title:"Enable debug logging",
        defaultValue:false
    )

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



def updateEvent(value){

    sendEvent(
        name:"motion",
        value:value
    )


    if(debugLogging){

        log.info "${device.label}: ${value}"

    }

}
