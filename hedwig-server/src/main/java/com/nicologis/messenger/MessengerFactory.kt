package com.nicologis.messenger

import com.intellij.openapi.util.text.StringUtil
import com.nicologis.hipchat.HipchatMessenger
import com.nicologis.slack.SlackMessenger
import com.nicologis.teamcity.ParameterNames
import com.nicologis.teamcity.BuildInfo
import jetbrains.buildServer.parameters.ParametersProvider
import org.apache.log4j.Logger
import java.util.*

object MessengerFactory {
    private val log = Logger.getLogger(MessengerFactory::class.java)

    private fun distinctRecipients(recipients: List<Recipient>): HashSet<Recipient> {
        return LinkedHashSet(recipients)// remove duplicate recipients
    }

    private fun slackConfigurationIsInvalid(url: String?): Boolean {
        return url == null
    }

    private fun createSlackMessenger(botName: String, webhookUrl: String, params: ParametersProvider): SlackMessenger {
        return SlackMessenger(webhookUrl, botName, params)
    }

    fun sendMsg(build: BuildInfo,
                paramsProvider: ParametersProvider,
                recipients: List<Recipient>) {
        val exclusion = paramsProvider.get(ParameterNames.ExcludeMessage)
        val statusText = build.statusText
        if (StringUtil.isNotEmpty(exclusion)) {
            if (StringUtil.isNotEmpty(statusText) && statusText.contains(exclusion!!)) {
                return
            }
        }
        var slackBotName = paramsProvider.get(ParameterNames.SlackBotName)
        if (StringUtil.isEmptyOrSpaces(slackBotName)) {
            slackBotName = "teamcity"
        }
        val messengers = ArrayList<AbstractMessenger>(2)
        val urlKey = paramsProvider.get(ParameterNames.SlackWebHookURL)
        if (slackConfigurationIsInvalid(urlKey)) {
            log.warn("Could not send Slack notification. The Slack webhook URL or bot name was null. ")
        } else {
            val slackMessenger = createSlackMessenger(slackBotName!!, urlKey!!, paramsProvider)
            messengers.add(slackMessenger)
        }
        val hipchatToken = paramsProvider.get(ParameterNames.HipChatToken)
        if (StringUtil.isNotEmpty(hipchatToken)) {
            val hipchat = createHipchatMessenger(hipchatToken!!, paramsProvider)
            messengers.add(hipchat)
        } else {
            log.warn("Could not send hipchat notification. The token was null")
        }

        val distinctRecipients = distinctRecipients(recipients)
        for (m in messengers) {
            m.send(build, distinctRecipients)
        }
    }

    private fun createHipchatMessenger(hipchatToken: String, paramsProvider: ParametersProvider): AbstractMessenger {
        return HipchatMessenger(hipchatToken, paramsProvider)
    }
}