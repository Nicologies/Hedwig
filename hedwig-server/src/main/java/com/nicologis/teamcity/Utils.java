package com.nicologis.teamcity;

import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.SBuild;
import org.apache.commons.lang.StringUtils;

public class Utils {
    public static String getBranchName(SBuild build) {
        String branchNameForPullRequest = build.getParametersProvider().get("teamcity.build.pull_req.branch_name");
        if (StringUtils.isNotEmpty(branchNameForPullRequest)) {
            return branchNameForPullRequest;
        }
        Branch branch = build.getBranch();
        if (branch != null && !branch.getName().equals("<default>")) {
            return branch.getDisplayName();
        } else {
            return "";
        }
    }
}
