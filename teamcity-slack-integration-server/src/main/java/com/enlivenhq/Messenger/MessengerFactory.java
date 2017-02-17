package com.enlivenhq.Messenger;

import com.enlivenhq.github.PullRequestInfo;
import com.enlivenhq.slack.SlackMessenger;
import com.enlivenhq.slack.SlackParameters;
import com.enlivenhq.teamcity.BuildInfo;
import jetbrains.buildServer.parameters.ParametersProvider;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class MessengerFactory {
    private static final Logger log = Logger.getLogger(MessengerFactory.class);
    private static List<IMessenger> get(PullRequestInfo pr, ParametersProvider paramProvider,
                                            String teamcityServerUrl,
                                            List<String> additionalRecipients){

        List<String> recipients = pr.getRecipients();
        if(additionalRecipients != null){
            recipients.addAll(additionalRecipients);
        }

        recipients = new ArrayList<String>(new LinkedHashSet<String>(recipients));// remove duplicate recipients

        List<IMessenger> ret = new ArrayList<IMessenger>(recipients.size() * 2);

        createSlackMessengers(pr, paramProvider, teamcityServerUrl, recipients, ret);

        return ret;
    }

    private static void createSlackMessengers(PullRequestInfo pr, ParametersProvider paramProvider,
                                              String teamcityServerUrl, List<String> recipients, List<IMessenger> ret) {
        String urlKey = paramProvider.get(SlackParameters.SystemWideSlackUrlKey);
        String slackBotName = paramProvider.get(SlackParameters.SystemWideSlackUserName);

        for(String recipient : recipients) {
            if (slackConfigurationIsInvalid(recipient, slackBotName, urlKey)) {
                log.error("Could not send Slack notification. The Slack channel, username, or URL was null. " +
                        "Double check your Notification settings");
            }else{
                ret.add(createSlackMessenger(recipient, slackBotName, urlKey, pr.Url, teamcityServerUrl));
            }
        }
    }

    private static boolean slackConfigurationIsInvalid(String channel, String username, String url) {
        return channel == null || username == null || url == null;
    }

    private static SlackMessenger createSlackMessenger(String channel, String username, String url, String pullReqUrl,
                                                       String teamcityServerUrl) {
        SlackMessenger slackMessenger = new SlackMessenger();

        slackMessenger.setChannel(channel);
        slackMessenger.setUsername(username);
        slackMessenger.setSlackUrl(url);
        slackMessenger.setPullRequestUrl(pullReqUrl);
        slackMessenger.setServerUrl(teamcityServerUrl);

        return slackMessenger;
    }

    public static void sendMsg(BuildInfo build,
                               ParametersProvider paramProvider, String teamcityServerUrl,
                               List<String> additionalChannels) {
        List<IMessenger> messengers = get(build.getPrInfo(), paramProvider, teamcityServerUrl,
                additionalChannels);

        for(IMessenger messenger : messengers){
            try {
                messenger.send(build);
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }
}
