package com.nicologis.teamcity;

import com.nicologis.messenger.MessengerFactory;
import com.nicologis.github.PullRequestInfo;
import com.nicologis.messenger.Recipient;
import com.nicologis.slack.StatusColor;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTranslator;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ServiceMessageHandler implements ServiceMessageTranslator {
    private SBuildServer _server;

    public ServiceMessageHandler(SBuildServer server){
        this._server = server;
    }
    @NotNull
    public List<BuildMessage1> translate(@NotNull SRunningBuild sRunningBuild, @NotNull BuildMessage1 buildMessage1,
                                         @NotNull ServiceMessage serviceMessage) {
        List<BuildMessage1> ret = Arrays.asList(buildMessage1);

        Map<String, String> attributes = serviceMessage.getAttributes();

        String status = attributes.get("Status");
        if (StringUtils.isEmpty(status)){
            return ret;
        }

        Map<String, String> messages = new HashMap<String, String>();
        for(int i = 0; i < 50; ++i){
            String msgName = attributes.get("MsgName" + i);
            String msgValue = attributes.get("MsgValue" + i);
            if(StringUtils.isEmpty(msgName) || StringUtils.isEmpty(msgValue)){
                continue;
            }
            messages.put(msgName, msgValue);
        }

        String statusType = attributes.get("StatusType");
        StatusColor statusColor = StatusColor.info;
        if(StringUtils.isNotEmpty(statusType)){
            String lowerCaseStatus = statusType.toLowerCase();
            if(lowerCaseStatus.equals("good") || lowerCaseStatus.equals("succeeded")) {
                statusColor = StatusColor.good;
            }
            else if(lowerCaseStatus.equals("danger")) {
                statusColor = StatusColor.danger;
            }
            else if(lowerCaseStatus.equals("warning")) {
                statusColor = StatusColor.warning;
            }
        }

        List<Recipient> rooms = getRecipients(attributes);

        PullRequestInfo prInfo = getPullRequestInfo(sRunningBuild, attributes);
        BuildInfo build = new BuildInfo(sRunningBuild, status, statusColor, prInfo, messages, _server.getRootUrl());
        MessengerFactory.sendMsg(build, sRunningBuild.getParametersProvider(), rooms);

        return ret;
    }

    @NotNull
    private PullRequestInfo getPullRequestInfo(@NotNull SRunningBuild sRunningBuild, Map<String, String> attributes) {
        PullRequestInfo prInfo = new PullRequestInfo(sRunningBuild);
        String prAuthor = attributes.get("PrAuthor");
        if(StringUtils.isNotEmpty(prAuthor)){
            prInfo.setAuthor(prAuthor);
        }
        String prAssignee = attributes.get("PrAssignee");
        if(StringUtils.isNotEmpty(prAssignee)){
            prInfo.setAssignee(prAssignee);
        }
        String prUrl = attributes.get("PrUrl");
        if(StringUtils.isNotEmpty(prUrl)){
            prInfo.Url = prUrl;
        }

        String branchName = attributes.get("Branch");
        if(StringUtils.isNotEmpty(branchName)){
            prInfo.Branch = branchName;
        }
        return prInfo;
    }

    @NotNull
    private List<Recipient> getRecipients(Map<String, String> attributes) {
        List<Recipient> recipients = new ArrayList<>();
        GetRooms(attributes, recipients, "Channels");
        GetRooms(attributes, recipients, "Rooms");
        String usersStr = attributes.get("Users");
        if (StringUtils.isNotEmpty(usersStr)) {
            for (String user : usersStr.split(";")) {
                if (StringUtils.isEmpty(user)) {
                    continue;
                }
                String trimmedUser = user.trim();
                if (trimmedUser.startsWith("@")) {
                    trimmedUser = trimmedUser.substring(1);
                }
                recipients.add(new Recipient(trimmedUser, false));
            }
        }

        return recipients;
    }

    private void GetRooms(Map<String, String> attributes, List<Recipient> recipients, String rooms) {
        String channelsStr = attributes.get(rooms);

        if (StringUtils.isNotEmpty(channelsStr)) {
            for(String channel : channelsStr.split(";")) {
                if(StringUtils.isEmpty(channel)){
                   continue;
                }
                recipients.add(new Recipient(channel.trim(), true));
            }
        }
    }

    @NotNull
    public String getServiceMessageName() {
        return "Hedwig";
    }
}
