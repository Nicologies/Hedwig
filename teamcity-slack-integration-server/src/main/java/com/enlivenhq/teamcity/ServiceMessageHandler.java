package com.enlivenhq.teamcity;

import com.enlivenhq.slack.PullRequestInfo;
import com.enlivenhq.slack.SlackWrapper;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTranslator;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ServiceMessageHandler implements ServiceMessageTranslator {
    private static final Logger LOG = Logger.getLogger(ServiceMessageHandler.class);
    private SBuildServer _server;

    public ServiceMessageHandler(SBuildServer server){
        this._server = server;
    }
    @NotNull
    public List<BuildMessage1> translate(@NotNull SRunningBuild sRunningBuild, @NotNull BuildMessage1 buildMessage1,
                                         @NotNull ServiceMessage serviceMessage) {
        List<BuildMessage1> ret = Arrays.asList(buildMessage1);
        String urlKey = sRunningBuild.getParametersProvider().get(SlackNotificator.SystemWideSlackUrlKey);
        if(StringUtils.isEmpty(urlKey)) {
            return ret;
        }

        Map<String, String> attributes = serviceMessage.getAttributes();

        String message = attributes.get("Message");
        if (StringUtils.isEmpty(message)){
            return ret;
        }

        String userName = getSlackUserName(sRunningBuild, attributes);

        String sysWideChannel = sRunningBuild.getParametersProvider().get(SlackNotificator.SystemWideSlackChannel);


        List<String> sendToChannels = getChannels(attributes);

        PullRequestInfo prInfo = getPullRequestInfo(sRunningBuild, attributes);
        List<SlackWrapper> slackWrappers = SlackWrapperBuilder.getSlackWrappers(sysWideChannel, prInfo, urlKey,
                userName, _server.getRootUrl(), sendToChannels);
        for(SlackWrapper slackWrapper : slackWrappers){
            try {
                slackWrapper.send(sRunningBuild, message, prInfo.Branch);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("Failed to send slack message", e);
            }
        }

        return ret;
    }

    @NotNull
    private String getSlackUserName(@NotNull SRunningBuild sRunningBuild, Map<String, String> attributes) {
        String userName = attributes.get("UserName");
        if (StringUtils.isEmpty(userName)) {
            userName = sRunningBuild.getParametersProvider().get(SlackNotificator.SystemWideSlackUserName);
            if (StringUtils.isEmpty(userName)) {
                userName = "Teamcity";
            }
        }
        return userName;
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
    private List<String> getChannels(Map<String, String> attributes) {
        List<String> sendToChannels = new ArrayList<String>();
        String channelsStr = attributes.get("Channels");

        if (StringUtils.isNotEmpty(channelsStr)) {
            for(String channel : channelsStr.split(",")) {
                if(StringUtils.isEmpty(channel)){
                   continue;
                }
                sendToChannels.add(channel.trim());
            }
        }
        String usersStr = attributes.get("Users");
        if (StringUtils.isNotEmpty(usersStr)) {
            for (String user : usersStr.split(",")) {
                if (StringUtils.isEmpty(user)) {
                    continue;
                }
                String trimmedUser = user.trim();
                if (!trimmedUser.startsWith("@")) {
                    sendToChannels.add("@" + trimmedUser);
                } else {
                    sendToChannels.add(trimmedUser);
                }
            }
        }

        return sendToChannels;
    }

    @NotNull
    public String getServiceMessageName() {
        return "Slack";
    }
}
