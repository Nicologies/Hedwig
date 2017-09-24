package com.nicologis.teamcity

import jetbrains.buildServer.serverSide.SBuild
import org.apache.commons.lang.StringUtils

object Utils {
    fun getBranchName(build: SBuild): String {
        val branchNameForPullRequest = build.parametersProvider.get("teamcity.build.pull_req.branch_name")
        if (StringUtils.isNotEmpty(branchNameForPullRequest)) {
            return branchNameForPullRequest!!
        }
        val branch = build.branch
        return if (branch != null && branch.name != "<default>") {
            branch.displayName
        } else {
            ""
        }
    }
}
