package com.probe.sdk.models

internal class RegistrationResponseModel {
    var registration_response: RegistrationResponse? = null

    var err: String? = null

    inner class RegistrationResponse {
        var version: String? = null
        var bu: String? = null
        var kaInterval: String? = null
        var kcInterval: String? = null
        var serverClkOffset: String? = null
        var cfg: Cfg? = null
        var deviceMapping: DeviceMapping? = null

        inner class Cfg {
            var pc: Pc? = null
            var mc: Mc? = null

            inner class Pc {
                var renditionSwitch: String? = null
                var stalls: String? = null
                var userActions: String? = null
                var qualityChanged: String? = null
            }

            inner class Mc {
                var startupBuffDuration: String? = null
                var rebufferingDuration: String? = null
                var estimatedDownloadRate: String? = null
                var bufferingStyle: String? = null
                var mitigationID: String? = null
                var mitigationTimestamp: String? = null
            }
        }

        inner class DeviceMapping {
            var location: String? = null
            var deviceType: String? = null
        }
    }



}