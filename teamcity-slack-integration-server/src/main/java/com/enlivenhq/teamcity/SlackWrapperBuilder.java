package com.enlivenhq.teamcity;

import com.enlivenhq.slack.PullRequestInfo;
import com.enlivenhq.slack.SlackWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class SlackWrapperBuilder {
    private static final Logger log = Logger.getLogger(SlackNotificator.class);
    public static List<SlackWrapper> getSlackWrappers(String configuredChannelOfTheUser,
                                                      PullRequestInfo pr, String urlKey,
                                                      String slackBotName, String teamcityServerUrl,
                                                      List<String> additionalChannels){
        List<String> channels = pr.getChannels();
        if(StringUtils.isNotEmpty(configuredChannelOfTheUser)) {
            channels.add(0, configuredChannelOfTheUser);
        }
        if(additionalChannels != null){
            channels.addAll(additionalChannels);
        }
        channels = new ArrayList<String>(new LinkedHashSet<String>(channels));// remove duplicate channels
        List<SlackWrapper> ret = new ArrayList<SlackWrapper>(channels.size());
        for(String channel : channels) {
            if (slackConfigurationIsInvalid(channel, slackBotName, urlKey)) {
                log.error("Could not send Slack notification. The Slack channel, username, or URL was null. " +
                        "Double check your Notification settings");
            }else{
                ret.add(constructSlackWrapper(channel, slackBotName, urlKey, pr.Url, teamcityServerUrl));
            }

        }
        return ret;
    }

    private static boolean slackConfigurationIsInvalid(String channel, String username, String url) {
        return channel == null || username == null || url == null;
    }

    private static SlackWrapper constructSlackWrapper(String channel, String username, String url, String pullReqUrl,
                                                      String teamcityServerUrl) {
        SlackWrapper slackWrapper = new SlackWrapper();

        slackWrapper.setChannel(channel);
        slackWrapper.setUsername(username);
        slackWrapper.setSlackUrl(url);
        slackWrapper.setPullRequestUrl(pullReqUrl);
        slackWrapper.setServerUrl(teamcityServerUrl);

        return slackWrapper;
    }
}
