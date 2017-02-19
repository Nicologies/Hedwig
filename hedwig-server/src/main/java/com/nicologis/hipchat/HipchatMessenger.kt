package com.nicologis.hipchat

import com.nicologis.messenger.IMessenger
import com.nicologis.messenger.Recipient
import com.nicologis.messenger.UserMappingSuffix
import com.nicologis.slack.StatusColor
import com.nicologis.teamcity.BuildInfo
import io.evanwong.oss.hipchat.v2.HipChatClient
import io.evanwong.oss.hipchat.v2.rooms.MessageColor
import io.evanwong.oss.hipchat.v2.rooms.MessageFormat
import jetbrains.buildServer.parameters.ParametersProvider
import org.apache.log4j.Logger

class HipchatMessenger(private val hipchatToken: String, private val paramsProvider: ParametersProvider) : IMessenger {
    private val LOG = Logger.getLogger(HipchatMessenger::class.java)
    override fun send(build: BuildInfo, recipient: Recipient) {
        try {

            val message = constructMessage(build)
            if (recipient.isRoom) {
                sendMsgToRoom(recipient, message, build)
            } else {
                sendMsgToUser(recipient, message)
            }
        }catch(ex: Exception){
            ex.printStackTrace()
            LOG.error(ex.message)
        }
    }

    private fun constructMessage(build: BuildInfo): String{
        val sb = StringBuilder()
        sb.appendln("-------------------------------------------------------------------------------")
        sb.appendln("${build.statusText} ${build.buildFullName} ${build.branchName}")
        sb.appendln("build link: ${build.getBuildLink()}")
            sb.appendln("pull request: ${build.getEncodedPrUrl()}")
        build.messages.forEach { x -> sb.appendln("${x.key}: ${x.value}") }
        sb.appendln("-------------------------------------------------------------------------------")
        sb.appendln("")
        return sb.toString()
    }

    private fun sendMsgToRoom(recipient: Recipient, message: String, build: BuildInfo) {
        val _client: HipChatClient = HipChatClient(hipchatToken)
        val msgBuilder = _client.prepareSendRoomNotificationRequestBuilder(
                recipient.getRecipientName(paramsProvider, UserMappingSuffix.hipchat), message)
        msgBuilder.setColor(toHipchatColor(build.statusColor)).setMessageFormat(MessageFormat.TEXT)
                .setNotify(true)
                .build().execute().get()
    }

    private fun sendMsgToUser(recipient: Recipient, message: String) {
        try {
            val _client: HipChatClient = HipChatClient(hipchatToken)
            val msgBuilder = _client.preparePrivateMessageUserRequestBuilder(
                    recipient.getRecipientName(paramsProvider, UserMappingSuffix.hipchat), message)
            msgBuilder.setNotify(true).setMessageFormat(MessageFormat.TEXT)
                    .build().execute().get()
        }catch(ex: Exception){
            ex.printStackTrace()
            LOG.error(ex.message)
        }
    }

    private fun toHipchatColor(color: StatusColor): MessageColor{
        when(color){
            StatusColor.danger -> return MessageColor.RED
            StatusColor.info -> return MessageColor.YELLOW
            StatusColor.good -> return MessageColor.GREEN
            else -> return MessageColor.GREEN
        }
    }
}
