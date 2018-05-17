package com.nicologis.slack

import com.nicologis.teamcity.BuildInfo
import com.google.gson.annotations.Expose
import org.apache.commons.lang.StringUtils

import java.util.ArrayList

class SlackPayload(build: BuildInfo) {
    @Expose
    private var text: String
    @Expose
    private lateinit var channel: String
    @Expose
    private lateinit var username: String
    @Expose
    private var attachments: MutableList<Attachment>

    inner class Attachment {
        @Expose
        var fallback: String? = null
        @Expose
        var pretext: String? = null
        @Expose
        var color: String? = null
        @Expose
        var fields: MutableList<AttachmentField>? = null
    }

    inner class AttachmentField(
            @field:Expose
            private var title: String,
            @field:Expose
            private var value: String,
            @field:Expose
            private var isShort: Boolean
    )

    fun setRecipient(recipient: String) {
        this.channel = recipient
    }

    fun setBotName(botName: String) {
        this.username = botName
    }

    private fun escape(s: String): String {
        return s.replace("<", "&lt;").replace(">", "&gt;")
    }

    private fun escapeNewline(s: String): String {
        return s.replace("\n", "\\n")
    }

    init {
        val branch = escape(build.branchName)
        val escapedBranch = if (branch.isNotEmpty()) " [$branch]" else ""
        val statusText = ("<" + build.getBuildLink { x -> this.escape(x) }
                + "|" + escape(escapeNewline(build.statusText)) + ">")

        val statusEmoji: String
        val statusColor = build.statusColor
        statusEmoji = when (statusColor) {
            StatusColor.danger -> ":x: "
            StatusColor.warning -> ""
            StatusColor.info -> ":information_source: "
            else -> ":white_check_mark: "
        }

        val payloadText = (statusEmoji + build.buildFullName
                + escapedBranch + " #" + build.buildNumber + " " + statusText)
        this.text = payloadText

        val attachment = Attachment()
        attachment.color = statusColor.name
        attachment.pretext = "Build Information"
        attachment.fallback = payloadText
        attachment.fields = ArrayList()

        val encodedPrUrl = build.getEncodedPrUrl()
        if (StringUtils.isNotEmpty(encodedPrUrl)) {
            val prUrlField = AttachmentField("PullRequest URL", encodedPrUrl, false)
            attachment.fields!!.add(prUrlField)
        }

        for ((key, value) in build.messages) {
            val field = AttachmentField(key, value, false)
            attachment.fields!!.add(field)
        }

        this.attachments = ArrayList()
        if (!attachment.fields!!.isEmpty()) {
            this.attachments.add(0, attachment)
        }
    }
}
