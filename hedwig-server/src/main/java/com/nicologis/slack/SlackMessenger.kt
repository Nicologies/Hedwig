package com.nicologis.slack

import com.google.gson.GsonBuilder
import com.nicologis.messenger.AbstractMessenger
import com.nicologis.messenger.Recipient
import com.nicologis.messenger.UserMappingSuffix
import com.nicologis.teamcity.BuildInfo
import jetbrains.buildServer.parameters.ParametersProvider
import org.apache.log4j.Logger
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SlackMessenger(private var _slackWebhookUrl: String, private var _botName: String,
                     private val _params: ParametersProvider) : AbstractMessenger() {

    override fun send(build: BuildInfo, recipient: Recipient) {
        val formattedPayload = getFormattedPayload(build,
                recipient.getRecipientName(_params, UserMappingSuffix.slack))
        LOG.debug(formattedPayload)
        try {

            val url = URL(_slackWebhookUrl)
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty("User-Agent", "Enliven")
            httpsURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5")
            httpsURLConnection.doOutput = true

            val dataOutputStream = DataOutputStream(
                    httpsURLConnection.outputStream
            )
            val array = formattedPayload.toByteArray(charset("UTF-8"))
            dataOutputStream.write(array, 0, array.size)
            dataOutputStream.flush()
            dataOutputStream.close()
            var inputStream: InputStream?
            var responseBody = ""

            try {
                inputStream = httpsURLConnection.inputStream
            } catch (e: IOException) {
                responseBody = e.message!!
                inputStream = httpsURLConnection.errorStream
                if (inputStream != null) {
                    responseBody += ": "
                    responseBody = getResponseBody(inputStream, responseBody)
                }
                throw IOException(responseBody)
            }

            getResponseBody(inputStream, responseBody)
        } catch (ex: IOException) {
            ex.printStackTrace()
            LOG.error(ex.message)
        }

    }

    @Throws(IOException::class)
    private fun getResponseBody(inputStream: InputStream, responseBody: String): String {
        return responseBody + inputStream.bufferedReader().use { it.readText() }
    }

    private fun getFormattedPayload(build: BuildInfo, recipient: String): String {
        val gson = GSON_BUILDER.create()

        val slackPayload = SlackPayload(build)
        slackPayload.setRecipient(recipient)
        slackPayload.setBotName(_botName)

        return gson.toJson(slackPayload)
    }

    companion object {
        val GSON_BUILDER = GsonBuilder().excludeFieldsWithoutExposeAnnotation()
        private val LOG = Logger.getLogger(SlackMessenger::class.java)
    }

    override fun mapRecipients(build: BuildInfo, recipients: Collection<Recipient>): Collection<Recipient> {
        return LinkedHashSet<Recipient>(recipients.map {
            Recipient(it.getRecipientName(_params, UserMappingSuffix.slack), it.isRoom)
        })
    }
}
