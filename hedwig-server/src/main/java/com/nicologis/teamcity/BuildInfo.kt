package com.nicologis.teamcity

import com.nicologis.github.PullRequestInfo
import com.nicologis.slack.StatusColor
import jetbrains.buildServer.Build
import jetbrains.buildServer.web.util.WebUtil

class BuildInfo constructor(build: Build, var statusText:String, var statusColor: StatusColor,
                            var prInfo: PullRequestInfo, var messages: Map<String, String>,
                            private var serverUrl: String) {
    var buildId = build.buildId
    var buildTypeExternalId = build.buildTypeExternalId
    var buildFullName = build.fullName
    var buildNumber: String = build.buildNumber
    var branchName: String = prInfo.Branch
    var prUrl : String? = prInfo.Url

    fun getBuildLink(escapeFunc : (org:String) -> String): String {
        val ret = "${this.serverUrl}/viewLog.html?buildId=${this.buildId}&buildTypeId=${escapeFunc(this.buildTypeExternalId)}"
        return WebUtil.escapeUrlForQuotes(ret)
    }
    fun getEncodedPrUrl(): String {
       return WebUtil.escapeUrlForQuotes(this.prUrl)
    }
}