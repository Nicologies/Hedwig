package com.enlivenhq.slack;

import com.enlivenhq.teamcity.Utils;
import jetbrains.buildServer.serverSide.SBuild;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PullRequestInfo {
    private String _author;
    private String _assignee;
    private String _triggeredBy;
    private String[] _participants;
    public String Url;
    public String Branch;

    public PullRequestInfo(SBuild build){
        _author = build.getParametersProvider().get("teamcity.build.pull_req.author");

        _assignee = build.getParametersProvider().get("teamcity.build.pull_req.assignee");

        Url = build.getParametersProvider().get("teamcity.build.pull_req.url");

        _triggeredBy = build.getParametersProvider().get("teamcity.build.triggered_by.mapped_user");
        if(StringUtils.isEmpty(_triggeredBy)){
           _triggeredBy = build.getParametersProvider().get("teamcity.build.triggeredBy.username");
        }

        String rawParticipants = build.getParametersProvider().get("teamcity.build.pull_req.participants");
        if(StringUtils.isNotEmpty(rawParticipants)){
            _participants = rawParticipants.split(";");
        }

        Branch = Utils.getBranchName(build);
    }
    public List<String> getChannels(){
        List<String> ret = new ArrayList<String>();
        AddUserToMentionList(ret, _author);
        AddUserToMentionList(ret, _assignee);
        AddUserToMentionList(ret, _triggeredBy);
        if(_participants != null){
            for(String p : _participants){
                AddUserToMentionList(ret, p);
            }
        }
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

    public void setAuthor(String author){
        _author = author;
    }
    public void setAssignee(String assignee){
        _assignee = assignee;
    }

    public void setTriggeredBy(String triggeredBy) {
        this._triggeredBy = triggeredBy;
    }
}
