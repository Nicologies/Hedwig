package com.enlivenhq.slack;

import com.enlivenhq.github.PullRequestInfo;
import com.enlivenhq.teamcity.BuildInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class SlackMessengerFactory {
    private static final Logger log = Logger.getLogger(SlackMessengerFactory.class);
    private static List<SlackMessenger> get(String configuredChannelOfTheUser,
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
        List<SlackMessenger> ret = new ArrayList<SlackMessenger>(channels.size());
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

    private static SlackMessenger constructSlackWrapper(String channel, String username, String url, String pullReqUrl,
                                                        String teamcityServerUrl) {
        SlackMessenger slackMessenger = new SlackMessenger();

        slackMessenger.setChannel(channel);
        slackMessenger.setUsername(username);
        slackMessenger.setSlackUrl(url);
        slackMessenger.setPullRequestUrl(pullReqUrl);
        slackMessenger.setServerUrl(teamcityServerUrl);

        return slackMessenger;
    }

    public static void sendMsg(BuildInfo build, String preDefinedChannel,
                               String urlKey,
                               String teamcityBotName, String teamcityServerUrl,
                               List<String> additionalChannels) {
        List<SlackMessenger> messengers = get(preDefinedChannel,
                build.getPrInfo(), urlKey, teamcityBotName, teamcityServerUrl,
                additionalChannels);

        for(SlackMessenger messenger : messengers){
            try {
                messenger.send(build);
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }
}
