package com.probe.sdk.models

internal class MitigationConfigModel {


    var mitigation_config_response: MitigationConfigResponse? = null
    var err: String? = null


    inner class MitigationConfigResponse {
        var version: String? = null
        var mc: MC? = null

        inner class MC {
            var startupBuffDuration: String? = null
            var rebufferingDuration: String? = null
            var estimatedDownloadRate: String? = null
            var bufferingStyle: String? = null
            var mitigationID: String? = null
            var mitigationTimestamp: String? = null
        }


    }
}