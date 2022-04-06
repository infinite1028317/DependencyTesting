package com.probe.sdk.sdk

import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.probe.sdk.otherutils.MitigationConfiguration
import com.probe.sdk.otherutils.MyUtils

internal object MitigationSdk {


    fun applyBufferingConfiguration(builder: DefaultLoadControl.Builder) {
        try {
            builder.setBufferDurationsMs(
                MitigationConfiguration.access?.getMinBufferMs()!!,
                MitigationConfiguration.access?.getMaxBufferMs()!!,
                MitigationConfiguration.access?.getStartupBuffDuration()!!,
                MitigationConfiguration.access?.getRebufferingDuration()!!
            )
        } catch (e: Exception) { // If anything goes wrong, just apply the default configuration
            builder.setBufferDurationsMs(
                30000, 120000,
                15000, 5000
            )

        }

        /*builder.setBufferDurationsMs(
            30000, 30000,
            10500, 5000
        )*/

    }

    fun applyBitrateConfiguration(): DefaultTrackSelector.Parameters? {
        return try {
            if ((MitigationConfiguration.access?.getEstDownloadRate()) != 0) {
                MitigationConfiguration.access?.getEstDownloadRate()?.let {
                    DefaultTrackSelector.ParametersBuilder(MyUtils.getLibContext()!!)
                        .setMaxVideoBitrate(
                            it
                        ).setForceHighestSupportedBitrate(true).build()
                }
            } else {
                MyUtils.getLibContext()?.let { DefaultTrackSelector.ParametersBuilder(it).build() }
            }
        } catch (e: Exception) { // If anything goes wrong, just apply the default configuration
            MyUtils.getLibContext()?.let { DefaultTrackSelector.ParametersBuilder(it).build() }
        }

    }


}