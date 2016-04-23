package com.enlivenhq.slack;

import jetbrains.buildServer.serverSide.SBuild;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PullRequestInfo {
    private String _author;
    private String _assignee;
    private String _triggeredBy;
    public String Url;

    public PullRequestInfo(SBuild build){
        _author = build.getParametersProvider().get("teamcity.build.pull_req.author");

        _assignee = build.getParametersProvider().get("teamcity.build.pull_req.assignee");

        Url = build.getParametersProvider().get("teamcity.build.pull_req.url");

        _triggeredBy = build.getParametersProvider().get("teamcity.build.triggered_by.mapped_user");
    }
    public List<String> getChannels(){
        List<String> ret = new ArrayList<String>();
        AddUserToMentionList(ret, _author);
        AddUserToMentionList(ret, _assignee);
        AddUserToMentionList(ret, _triggeredBy);
        return ret;
    }

    public void AddUserToMentionList(List<String> list, String user){
        if(StringUtils.isNotEmpty(user)) {
            String mentionAssignee = "@" +user;
            if(!list.contains(mentionAssignee)) {
                list.add(mentionAssignee);
            }
        }
    }
}
