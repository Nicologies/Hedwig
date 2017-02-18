package com.nicologis.hipchat

import com.nicologis.messenger.IMessenger
import com.nicologis.messenger.Recipient
import com.nicologis.slack.StatusColor
import com.nicologis.teamcity.BuildInfo
import io.evanwong.oss.hipchat.v2.HipChatClient
import io.evanwong.oss.hipchat.v2.rooms.MessageColor
import io.evanwong.oss.hipchat.v2.rooms.MessageFormat
import org.apache.log4j.Logger

class HipchatMessenger(private val hipchatToken: String) : IMessenger {
    private val LOG = Logger.getLogger(HipchatMessenger::class.java)
    override fun send(build: BuildInfo, recipient: Recipient) {
        try {

            val message = constructMessage(build)
            if (recipient.isRoom) {
                sendMsgToRoom(recipient, message, build.statusColor)
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
        sb.appendln("${build.statusText} ${build.buildFullName} ${build.branchName}")
        sb.appendln("build link: ${build.getBuildLink()}")
        sb.appendln("pull request: ${build.getEncodedPrUrl()}")
        build.messages.forEach { x -> sb.appendln("${x.key}: ${x.value}") }
        return sb.toString()
    }

    private fun sendMsgToRoom(recipient: Recipient, message: String, color: StatusColor) {
        val _client: HipChatClient = HipChatClient(hipchatToken)
        val msgBuilder = _client.prepareSendRoomNotificationRequestBuilder(recipient.getRecipientName(), message)
        msgBuilder.setColor(toHipchatColor(color)).setMessageFormat(MessageFormat.TEXT)
                .setNotify(true)
                .build().execute().get()
    }

    private fun sendMsgToUser(recipient: Recipient, message: String) {
        try {
            val _client: HipChatClient = HipChatClient(hipchatToken)
            val msgBuilder = _client.preparePrivateMessageUserRequestBuilder(recipient.getRecipientName(), message)
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
