package com.probe.sdk.otherutils

internal class MitigationConfiguration {
    fun getStartupBuffDuration(): Int {
        return if (startupBuffDuration == 0) {
            15000 // returning default value as TS using in his source code.(Although 2500 is recommended from exoplayer)
        } else {
            startupBuffDuration
        }
    }

    internal fun setStartupBuffDuration(startupBuffDuration: Int) {

        if (startupBuffDuration != 0) {  // convert seconds to milliseconds
            this.startupBuffDuration = startupBuffDuration * 1000
        }
    }

    fun getRebufferingDuration(): Int {
        return if (rebufferingDuration == 0) {
            5000 // returning default value as TS using in his source code.(And 5000 is recommended from exoplayer)
        } else {
            rebufferingDuration
        }
    }

    internal fun setRebufferingDuration(rebufferingDuration: Int) {
        if (rebufferingDuration != 0) {  // convert seconds to milliseconds
            this.rebufferingDuration = rebufferingDuration * 1000
        }
    }

    private var minBufferMs = 0
    private var maxBufferMs = 0
    private var startupBuffDuration = 0
    private var rebufferingDuration = 0
    var estimatedDownloadRate = 0
    private var mitigationActiveOrNot: Boolean? = null

    // returning default value as TS using in his source code.(Although 15000 is recommended from exoplayer)

    fun getMinBufferMs(): Int {

        var findLargestNumber: Int = if (startupBuffDuration > rebufferingDuration) {
            startupBuffDuration
        } else {
            rebufferingDuration
        }

        return if (minBufferMs == 0 || (minBufferMs < findLargestNumber)) {
            findLargestNumber + 10000
        } else {
            30000 // returning default value as TS using in his source code.(Although 15000 is recommended from exoplayer)
        }
    }

    internal fun setMinBufferMs(minBufferMs: Int) {
        if (minBufferMs != 0) {  // convert seconds to milliseconds
            this.minBufferMs = minBufferMs * 1000
        }
    }

    // returning default value as TS using in his source code.(Although 5000 is recommended value from exoplayer)

    fun getMaxBufferMs(): Int {

        var findLargestNumber: Int = if (startupBuffDuration > rebufferingDuration) {
            startupBuffDuration
        } else {
            rebufferingDuration
        }


        return if (maxBufferMs == 0 || (maxBufferMs < findLargestNumber)) {
            findLargestNumber + 10000
        } else {
            120000 // returning default value as TS using in his source code.(Although 5000 is recommended value from exoplayer)
        }
    }

    internal fun setMaxBufferMs(maxBufferMs: Int) {
        if (maxBufferMs != 0) { // convert seconds to milliseconds
            this.maxBufferMs = maxBufferMs * 1000
        }

    }

    fun getEstDownloadRate(): Int {
        return estimatedDownloadRate
    }

    internal fun setEstimatedDownloadRate(givingEstimatedDownloadRate: Int) {
        this.estimatedDownloadRate = givingEstimatedDownloadRate
    }


    companion object {
        var mitigationConfiguration: MitigationConfiguration? = null
        val access: MitigationConfiguration?
            get() {
                if (mitigationConfiguration == null) {
                    mitigationConfiguration = MitigationConfiguration()
                }
                return mitigationConfiguration
            }
    }
}