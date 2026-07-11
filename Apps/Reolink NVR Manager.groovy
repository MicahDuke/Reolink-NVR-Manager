/**
 * Reolink NVR Manager
 *
 * Hubitat integration for Reolink NVR systems
 *
 * Version: 0.2.0
 * Author: Micah Duke
 *
 * License: MIT
 */

definition(
    name: "Reolink NVR Manager",
    namespace: "MicahDuke",
    author: "Micah Duke",
    description: "Local Hubitat integration for Reolink NVR AI events",
    category: "Security",
    singleInstance: true,
    iconUrl: "https://raw.githubusercontent.com/hubitat/HubitatPublic/master/resources/images/icon.png",
    iconX2Url: "https://raw.githubusercontent.com/hubitat/HubitatPublic/master/resources/images/icon.png"
)


preferences {

    page(
        name:"mainPage"
    )

}


def mainPage() {


    dynamicPage(
        name: "mainPage",
        title: "Reolink NVR Manager",
        install: true,
        uninstall: true
    ) {


        section("NVR Settings") {


            input(
                name: "nvrIP",
                type: "text",
                title: "NVR IP Address",
                defaultValue: "192.168.0.32",
                required: true
            )


            input(
                name: "username",
                type: "text",
                title: "Username",
                required: true
            )


            input(
                name: "password",
                type: "password",
                title: "Password",
                required: true
            )

        }


section("Actions") {


    input(
        name: "testLogin",
        type: "button",
        title: "Test Reolink Login"
    )


    input(
        name: "discoverCameras",
        type: "button",
        title: "Discover Cameras"
    )

}


        section("Status") {


            paragraph "Reolink NVR Manager v0.1.0"

        }

    }

}



def appButtonHandler(btn) {


    switch(btn) {


        case "testLogin":

            loginReolink()

            break



        case "discoverCameras":

            discoverCameras()

            break

    }

}




def loginReolink() {


    log.info "Testing Reolink Login"


    def body = """
[
 {
  "cmd":"Login",
  "param":{
   "User":{
    "userName":"${settings.username}",
    "password":"${settings.password}"
   }
  }
 }
]
"""


    try {


        httpPost(
            [
                uri:
                "http://${settings.nvrIP}/api.cgi?cmd=Login",

                headers:[
                    "Content-Type":"application/json"
                ],

                body:body
            ]

        ){ response ->


            def jsonText =
                response.data.toString()


            log.info "Login Response:"
            log.info jsonText


            def json =
                new groovy.json.JsonSlurper()
                .parseText(jsonText)



            if(json[0].code == 0) {


                state.reolinkToken =
                    json[0].value.Token.name


                log.info "Login successful"

                log.info "Token stored"


            }
            else {


                log.error "Login failed"

            }


        }


    }
    catch(Exception e) {


        log.error "Reolink Login Error: ${e.message}"

    }


}

def discoverCameras() {


    log.info "Starting camera discovery"


    def token = state.reolinkToken


    if(!token) {

        log.error "No token. Login first."

        return

    }



    def body = """
[
 {
  "cmd":"GetChannelstatus",
  "param":{}
 }
]
"""


    httpPost(

        [
            uri:
            "http://${settings.nvrIP}/api.cgi?cmd=GetChannelstatus&token=${token}",

            headers:[
                "Content-Type":"application/json"
            ],

            body:body
        ]

    ){ response ->



        def jsonText =
            response.data.toString()


        log.info "Channel Response:"
        log.info jsonText



        def json =
            new groovy.json.JsonSlurper()
            .parseText(jsonText)



        def cameras =
            []


        json[0].value.status.each { cam ->


            if(cam.online == 1) {


                cameras << [

                    channel:cam.channel,

                    name:cam.name

                ]


                log.info "Found camera: ${cam.channel} - ${cam.name}"

            }

        }


        state.cameras = cameras


        log.info "Camera discovery complete"

    }

}

def installed() {

    log.info "Reolink NVR Manager installed"

}



def updated() {

    log.info "Reolink NVR Manager updated"

}
