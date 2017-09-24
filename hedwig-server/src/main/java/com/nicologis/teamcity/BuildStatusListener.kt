package com.nicologis.teamcity

import com.nicologis.github.PullRequestInfo
import com.nicologis.messenger.MessengerFactory
import com.nicologis.messenger.Recipient
import com.nicologis.slack.StatusColor
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.serverSide.BuildServerAdapter
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.SRunningBuild
import java.util.*

class BuildStatusListener(private val _server: SBuildServer) : BuildServerAdapter() {

    init {
        _server.addListener(this)
    }

    override fun buildFinished(build: SRunningBuild) {

        val buildStatus = build.buildStatus

        if (buildStatus == Status.FAILURE || buildStatus == Status.ERROR) {
            val statusText = "failed: " + build.statusDescriptor.text
            SendNotificationForBuild(build, statusText, StatusColor.danger)
        }
    }

    private fun SendNotificationForBuild(build: SRunningBuild, statusText: String, statusColor: StatusColor) {
        val paramProvider = build.parametersProvider

        val pr = PullRequestInfo(build)
        val bdInfo = BuildInfo(build, statusText, statusColor,
                pr, HashMap<String, String>(), _server.rootUrl)

        MessengerFactory.sendMsg(bdInfo, paramProvider,
                ArrayList<Recipient>())
    }
}
