package com.enlivenhq.teamcity

import com.enlivenhq.github.PullRequestInfo
import com.enlivenhq.slack.StatusColor
import jetbrains.buildServer.Build

class BuildInfo constructor(build: Build, var statusText:String, var statusColor: StatusColor,
                            var prInfo: PullRequestInfo, var messages: Map<String, String>) {
    var buildId = build.buildId
    var buildTypeExternalId = build.buildTypeExternalId
    var buildFullName = build.fullName
    var buildNumber: String = build.buildNumber
    var branchName: String = prInfo.Branch;
    var prUrl : String = prInfo.Url;
}
