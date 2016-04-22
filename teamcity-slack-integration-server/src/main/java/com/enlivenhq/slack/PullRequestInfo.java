package com.enlivenhq.slack;

import jetbrains.buildServer.serverSide.SBuild;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PullRequestInfo {
    private String myAuthor;
    private String myAssignee;
    public String Url;

    public PullRequestInfo(SBuild build){
        myAuthor = build.getParametersProvider().get("teamcity.build.pull_req.author");

        myAssignee = build.getParametersProvider().get("teamcity.build.pull_req.assignee");

        Url = build.getParametersProvider().get("teamcity.build.pull_req.url");
    }
    public List<String> getChannels(){
        List<String> ret = new ArrayList<String>();
        if(StringUtils.isNotEmpty(myAuthor)){
            ret.add("@" + myAuthor);
        }
        if(StringUtils.isNotEmpty(myAssignee)){
            ret.add("@"+ myAssignee);
        }
        return ret;
    }
}
