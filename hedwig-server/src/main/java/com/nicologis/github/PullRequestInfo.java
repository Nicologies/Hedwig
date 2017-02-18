package com.nicologis.github;

import com.nicologis.messenger.Recipient;
import com.nicologis.teamcity.Utils;
import jetbrains.buildServer.serverSide.SBuild;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<Recipient> getRecipients(){
        List<String> ret = new ArrayList<>();
        AddUserToMentionList(ret, _author);
        AddUserToMentionList(ret, _assignee);
        AddUserToMentionList(ret, _triggeredBy);
        if(_participants != null){
            for(String p : _participants){
                AddUserToMentionList(ret, p);
            }
        }
        return ret.stream().map(x -> new Recipient(x, false))
                .collect(Collectors.toList());
    }

    public void AddUserToMentionList(List<String> list, String user){
        if(StringUtils.isNotEmpty(user)) {
            if(!list.contains(user)) {
                list.add(user);
            }
        }
    }

    public void setAuthor(String author){
        _author = author;
    }
    public void setAssignee(String assignee){
        _assignee = assignee;
    }
}
