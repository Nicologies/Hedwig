package com.enlivenhq.teamcity

import com.enlivenhq.slack.StatusColor
import jetbrains.buildServer.Build

class BuildInfo constructor(build: Build, var statusText:String, var statusColor: StatusColor,
                                   var branchName: String,var messages: Map<String, String>) {
    var buildId = build.buildId
    var buildTypeExternalId = build.buildTypeExternalId
    var buildFullName = build.fullName
    var buildNumber: String = build.buildNumber
}
