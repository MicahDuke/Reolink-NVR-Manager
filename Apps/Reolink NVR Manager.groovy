/**
 * Reolink NVR Manager v0.2.2
 * RLN8-410 Integration
 * Author: Micah Duke
 */

definition(
    name: "Reolink NVR Manager",
    namespace: "local.reolink",
    author: "Micah Duke",
    description: "Reolink NVR Motion Integration",
    category: "Convenience",
    singleInstance: true,
    iconUrl: "https://raw.githubusercontent.com/hubitat/HubitatPublic/master/resources/images/icon.png",
    iconX2Url: "https://raw.githubusercontent.com/hubitat/HubitatPublic/master/resources/images/icon.png"
)

preferences {
    page(name: "mainPage")
    page(name: "loginPage")
}

def mainPage() {
    dynamicPage(
        name: "mainPage",
        title: "Reolink NVR Setup",
        install: true,
        uninstall: true
    ) {
        section("NVR Settings") {
            input(
                "nvrIP",
                "text",
                title: "NVR IP Address",
                defaultValue: "192.168.0.32",
                required: true
            )

            input(
                "username",
                "text",
                title: "Username",
                required: true
            )

            input(
                "password",
                "password",
                title: "Password",
                required: true
            )

            input(
                "pollTime",
                "number",
                title: "Polling seconds",
                defaultValue: 10,
                required: true
            )
        }

section("Actions") {

    href(
        name: "loginTest",
        title: "Login and Create Devices",
        description: "Connect to Reolink",
        page: "loginPage"
    )

    input(
        name: "restartPolling",
        type: "button",
        title: "Restart Polling"
    )

    input(
        name: "testNames",
        type: "button",
        title: "Test Camera Names"
    )
}

        section("Status") {
            paragraph "RLN8-410 Reolink Integration"
        }
    }
}

def appButtonHandler(btn) {

    switch(btn) {

        case "loginButton":
            log.info "Manual Reolink Login Started"
            loginReolink()
            break

        case "restartPolling":
            log.info "Restarting Reolink Polling"

            unschedule()

            startPolling()
            break

        case "testNames":
            log.info "Testing Camera Names"
            testCameraNames()
            break
    }
}

def loginPage() {
    dynamicPage(
        name: "loginPage",
        title: "Reolink Login",
        install: false,
        uninstall: false
    ) {
        section("Actions") {

            input(
                name: "loginButton",
                type: "button",
                title: "Login & Create Devices"
            )

        }

        section("Status") {
            paragraph "Press the button above, then check Live Logs."
        }
    }
}

def loginReolink() {

    def body = """[
        {
            "cmd": "Login",
            "param": {
                "User": {
                    "userName": "${username}",
                    "password": "${password}"
                }
            }
        }
    ]"""

    httpPost([
        uri: "http://${nvrIP}/api.cgi?cmd=Login",
        headers: [
            "Content-Type": "application/json"
        ],
        body: body
    ]) { response ->

        def jsonText = response.getData().toString()

        log.info "Login Response:"
        log.info jsonText

        def slurper = new groovy.json.JsonSlurper()
        def json = slurper.parseText(jsonText)

        state.reolinkToken = json[0]?.value?.Token?.name

        log.info "Token saved ${state.reolinkToken}"

        createDevices()

        startPolling()
    }
}

def createDevices() {

    def body = """[
        {
            "cmd": "GetChannelstatus",
            "param": {}
        }
    ]"""

    httpPost([
        uri: "http://${nvrIP}/api.cgi?cmd=GetChannelstatus&token=${state.reolinkToken}",
        headers: [
            "Content-Type": "application/json"
        ],
        body: body
    ]) { response ->

        def json = new groovy.json.JsonSlurper().parseText(response.data.toString())

        json[0]?.value?.status?.each { cam ->
            if (cam.online == 1) {
                def dni = "reolink-motion-${cam.channel}"

                if (!getChildDevice(dni)) {
                    addChildDevice(
                        "local",
                        "Reolink Motion Sensor",
                        dni,
                        [label: "${cam.name} Motion"]
                    )
                    log.info "Created ${cam.name} Motion"
                }
            }
        }
    }
}

def startPolling() {

    def seconds = pollTime ?: 10

    log.info "Starting Reolink polling every ${seconds} seconds"

    runIn(seconds as Integer, "pollReolink")
}

def pollReolink() {
    def token = state.reolinkToken
    if (!token) {
        loginReolink()
        return
    }

    for (int channel = 0; channel < 5; channel++) {
        getEvents(channel, token)
    }

    runIn(pollTime ?: 10, "pollReolink")
}

def getEvents(channel, token) {
    def body = """[
        {
            "cmd": "GetEvents",
            "param": {
                "channel": ${channel}
            }
        }
    ]"""

    httpPost([
        uri: "http://${nvrIP}/api.cgi?cmd=GetEvents&token=${token}",
        headers: [
            "Content-Type": "application/json"
        ],
        body: body
    ]) { response ->
        def jsonText = response.getData().toString()
//        log.debug "Channel ${channel} response: ${jsonText}"

        def slurper = new groovy.json.JsonSlurper()
        def json = slurper.parseText(jsonText)

        def md = json[0]?.value?.md?.alarm_state ?: 0
        def person = json[0]?.value?.ai?.people?.alarm_state ?: 0
        def vehicle = json[0]?.value?.ai?.vehicle?.alarm_state ?: 0

//        log.debug "Channel ${channel}: MD=${md} Person=${person} Vehicle=${vehicle}"
        
        def sensor = getChildDevice("reolink-motion-${channel}")
        if (!sensor) {
            log.warn "No child device found for channel ${channel}"
            return
        }

        if(md == 1 || person == 1 || vehicle == 1){

            log.warn "TRIGGER DETECTED: ${sensor.label}"

            sensor.updateMotion("active")

            sensor.sendEvent(name:"person", value:person)
            sensor.sendEvent(name:"vehicle", value:vehicle)
            sensor.sendEvent(
                name:"motionType",
                value:"Person=${person} Vehicle=${vehicle}"
            )
		state.clearDevices = state.clearDevices ?: [:]

		state.clearDevices[sensor.deviceNetworkId] = now()

		runIn(
		    15,
 		   "scheduleClearMotion"
		)

        }
        else {

//            log.debug "${sensor.label}: no event MD=${md} Person=${person} Vehicle=${vehicle}"

        }
    }
}

def scheduleClearMotion(){

    state.clearDevices.each { deviceId, time ->

        def sensor = getChildDevice(deviceId)

        if(sensor){

            sensor.updateMotion("inactive")

            sensor.sendEvent(name:"person", value:0)
            sensor.sendEvent(name:"vehicle", value:0)
            sensor.sendEvent(name:"motionType", value:"None")

            log.warn "${sensor.label}: Motion cleared"
        }
    }

		state.remove("clearDevices")
}



def testCameraNames() {
    def token = state.reolinkToken
    if (!token) {
        log.error "No token"
        return
    }

    def body = """[
        {
            "cmd": "GetChannelstatus",
            "param": {}
        }
    ]"""

    httpPost([
        uri: "http://${nvrIP}/api.cgi?cmd=GetChannelstatus&token=${token}",
        headers: [
            "Content-Type": "application/json"
        ],
        body: body
    ]) { response ->
        log.info "CHANNEL STATUS RESPONSE:"
        log.info response.data.toString()
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

def initialize() {

    log.info "Reolink Manager Started"

    unschedule()

    if(state.reolinkToken) {
        log.info "Existing token found - restarting polling"
        startPolling()
    }
    else {
        log.info "No token - login required"
    }
}
