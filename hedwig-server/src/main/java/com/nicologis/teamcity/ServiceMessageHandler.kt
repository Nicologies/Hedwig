package com.nicologis.teamcity

import com.nicologis.messenger.MessengerFactory
import com.nicologis.github.PullRequestInfo
import com.nicologis.messenger.Recipient
import com.nicologis.slack.StatusColor
import jetbrains.buildServer.messages.BuildMessage1
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTranslator
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.SRunningBuild
import org.apache.commons.lang.StringUtils

import java.util.*

class ServiceMessageHandler(private val _server: SBuildServer) : ServiceMessageTranslator {
    override fun translate(sRunningBuild: SRunningBuild, buildMessage1: BuildMessage1,
                           serviceMessage: ServiceMessage): List<BuildMessage1> {
        val ret = Arrays.asList(buildMessage1)

        val attributes = serviceMessage.attributes

        val status = attributes["Status"]
        if (StringUtils.isEmpty(status)) {
            return ret
        }

        val messages = HashMap<String, String>()
        for (i in 0..49) {
            val msgName = attributes["MsgName" + i]
            val msgValue = attributes["MsgValue" + i]
            if (StringUtils.isEmpty(msgName) || StringUtils.isEmpty(msgValue)) {
                continue
            }
            messages.put(msgName!!, msgValue!!)
        }

        val statusType = attributes["StatusType"]
        var statusColor = StatusColor.info
        if (StringUtils.isNotEmpty(statusType)) {
            val lowerCaseStatus = statusType!!.toLowerCase()
            if (lowerCaseStatus == "good" || lowerCaseStatus == "succeeded") {
                statusColor = StatusColor.good
            } else if (lowerCaseStatus == "danger") {
                statusColor = StatusColor.danger
            } else if (lowerCaseStatus == "warning") {
                statusColor = StatusColor.warning
            }
        }

        val rooms = getRecipients(attributes)

        val prInfo = getPullRequestInfo(sRunningBuild, attributes)
        val build = BuildInfo(sRunningBuild, status!!, statusColor, prInfo, messages, _server.rootUrl)
        MessengerFactory.sendMsg(build, sRunningBuild.parametersProvider, rooms)

        return ret
    }

    private fun getPullRequestInfo(sRunningBuild: SRunningBuild, attributes: Map<String, String>): PullRequestInfo {
        val prInfo = PullRequestInfo(sRunningBuild)
        val prAuthor = attributes["PrAuthor"]
        if (StringUtils.isNotEmpty(prAuthor)) {
            prInfo.setAuthor(prAuthor!!)
        }
        val prAssignee = attributes["PrAssignee"]
        if (StringUtils.isNotEmpty(prAssignee)) {
            prInfo.setAssignee(prAssignee!!)
        }
        val prUrl = attributes["PrUrl"]
        if (StringUtils.isNotEmpty(prUrl)) {
            prInfo.Url = prUrl
        }

        val branchName = attributes["Branch"]
        if (StringUtils.isNotEmpty(branchName)) {
            prInfo.Branch = branchName!!
        }
        return prInfo
    }

    private fun getRecipients(attributes: Map<String, String>): List<Recipient> {
        val recipients = ArrayList<Recipient>()
        getRooms(attributes, recipients, "Channels")
        getRooms(attributes, recipients, "Rooms")
        val usersStr = attributes["Users"]
        if (StringUtils.isNotEmpty(usersStr)) {
            for (user in usersStr!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (StringUtils.isEmpty(user)) {
                    continue
                }
                var trimmedUser = user.trim { it <= ' ' }
                if (trimmedUser.startsWith("@")) {
                    trimmedUser = trimmedUser.substring(1)
                }
                recipients.add(Recipient(trimmedUser, false))
            }
        }

        return recipients
    }

    private fun getRooms(attributes: Map<String, String>, recipients: MutableList<Recipient>, rooms: String) {
        val channelsStr = attributes[rooms]

        if (StringUtils.isNotEmpty(channelsStr)) {
            channelsStr!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    .asSequence()
                    .filterNot { StringUtils.isEmpty(it) }
                    .mapTo(recipients) { channel -> Recipient(channel.trim { it <= ' ' }, true) }
        }
    }

    override fun getServiceMessageName(): String {
        return "Hedwig"
    }
}
