package com.nicologis.messenger;

import com.intellij.openapi.util.text.StringUtil;
import com.nicologis.github.PullRequestInfo;
import com.nicologis.hipchat.HipchatMessenger;
import com.nicologis.slack.SlackMessenger;
import com.nicologis.teamcity.ParameterNames;
import com.nicologis.teamcity.BuildInfo;
import jetbrains.buildServer.parameters.ParametersProvider;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class MessengerFactory {
    private static final Logger log = Logger.getLogger(MessengerFactory.class);

    @NotNull
    private static List<Recipient> getRecipients(PullRequestInfo pr, List<Recipient> additionalRecipients) {
        List<Recipient> recipients = pr.getRecipients();
        if(additionalRecipients != null){
            recipients.addAll(additionalRecipients);
        }

        recipients = new ArrayList<>(new LinkedHashSet<>(recipients));// remove duplicate recipients
        return recipients;
    }

    private static boolean slackConfigurationIsInvalid(String botName, String url) {
        return botName == null || url == null;
    }

    private static SlackMessenger createSlackMessenger(String botName, String webhookUrl) {
        SlackMessenger slackMessenger = new SlackMessenger(webhookUrl, botName);

        return slackMessenger;
    }

    public static void sendMsg(BuildInfo build,
                               ParametersProvider paramProvider,
                               List<Recipient> additionalRecipients) {
        String urlKey = paramProvider.get(ParameterNames.SlackWebHookURL);
        String slackBotName = paramProvider.get(ParameterNames.SlackBotName);
        List<IMessenger> messengers = new ArrayList<IMessenger>(2);
        if (slackConfigurationIsInvalid(slackBotName, urlKey)) {
            log.warn("Could not send Slack notification. The Slack webhook URL or bot name was null. ");
        }else {
            IMessenger slackMessenger = createSlackMessenger(slackBotName, urlKey);
            messengers.add(slackMessenger);
        }
        String hipchatToken = paramProvider.get(ParameterNames.HipChatToken);
        if(StringUtil.isNotEmpty(hipchatToken)){
            IMessenger hipchat = createHipchatMessenger(hipchatToken);
            messengers.add(hipchat);
        }else{
            log.warn("Could not send hipchat notification. The token was null");
        }

        List<Recipient> recipients = getRecipients(build.getPrInfo(), additionalRecipients);
        for(Recipient r : recipients){
            for(IMessenger m : messengers){
                m.send(build, r);
            }
        }
    }

    private static IMessenger createHipchatMessenger(String hipchatToken) {
        return new HipchatMessenger(hipchatToken);
    }
}
