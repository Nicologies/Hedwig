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

    private static boolean slackConfigurationIsInvalid(String url) {
        return url == null;
    }

    private static SlackMessenger createSlackMessenger(String botName, String webhookUrl, ParametersProvider params) {
        SlackMessenger slackMessenger = new SlackMessenger(webhookUrl, botName, params);

        return slackMessenger;
    }

    public static void sendMsg(BuildInfo build,
                               ParametersProvider paramsProvider,
                               List<Recipient> additionalRecipients) {
        String urlKey = paramsProvider.get(ParameterNames.SlackWebHookURL);
        String slackBotName = paramsProvider.get(ParameterNames.SlackBotName);
        String exclusion = paramsProvider.get(ParameterNames.ExcludeMessage);
        String statusText = build.getStatusText();
        if(StringUtil.isNotEmpty(exclusion)) {
            if (StringUtil.isNotEmpty(statusText)
                    && statusText.contains(exclusion)) {
                return;
            }
        }
        if(StringUtil.isEmptyOrSpaces(slackBotName)){
            slackBotName = "teamcity";
        }
        List<IMessenger> messengers = new ArrayList<IMessenger>(2);
        if (slackConfigurationIsInvalid(urlKey)) {
            log.warn("Could not send Slack notification. The Slack webhook URL or bot name was null. ");
        }else {
            IMessenger slackMessenger = createSlackMessenger(slackBotName, urlKey, paramsProvider);
            messengers.add(slackMessenger);
        }
        String hipchatToken = paramsProvider.get(ParameterNames.HipChatToken);
        if(StringUtil.isNotEmpty(hipchatToken)){
            IMessenger hipchat = createHipchatMessenger(hipchatToken, paramsProvider);
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

    private static IMessenger createHipchatMessenger(String hipchatToken, ParametersProvider paramsProvider) {
        return new HipchatMessenger(hipchatToken, paramsProvider);
    }
}
