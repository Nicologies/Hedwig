package com.nicologis.slack;
import com.nicologis.teamcity.BuildInfo;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlackPayload {
    @Expose
    protected String text;
    @Expose
    protected String channel;
    @Expose
    protected String username;
    @Expose
    protected List<Attachment> attachments;

    private class Attachment {
        @Expose
        protected String fallback;
        @Expose
        protected String pretext;
        @Expose
        protected String color;
        @Expose
        protected List<AttachmentField> fields;
    }

    private class AttachmentField {
        public AttachmentField (String name, String val, boolean isShort) {
            title = name;
            value = val;
            this.isShort = isShort;
        }
        @Expose
        protected String title;
        @Expose
        protected String value;
        @Expose
        protected boolean isShort;
    }

    public void setRecipient(String recipient) {
        this.channel = recipient;
    }

    public void setBotName(String botName) {
        this.username = botName;
    }

    public SlackPayload(BuildInfo build) {
        String branch = build.getBranchName();
        String escapedBranch = branch.length() > 0 ? " [" + branch + "]" : "";
        String statusText = "<" + build.getBuildLink() + "|" + build.getStatusText() + ">";

        String statusEmoji;
        StatusColor statusColor = build.getStatusColor();
        if(statusColor.equals(StatusColor.danger)) {
            statusEmoji = ":x: ";
        }else if(statusColor.equals(StatusColor.warning)){
            statusEmoji = "";
        }else if(statusColor.equals(StatusColor.info)){
            statusEmoji = ":information_source: ";
        }
        else{
            statusEmoji = ":white_check_mark: ";
        }

        String payloadText = statusEmoji + build.getBuildFullName()
                + escapedBranch + " #" + build.getBuildNumber() + " " + statusText;
        this.text = payloadText;

        Attachment attachment = new Attachment();
        attachment.color = statusColor.name();
        attachment.pretext = "Build Information";
        attachment.fallback = payloadText;
        attachment.fields = new ArrayList<>();

        String encodedPrUrl = build.getEncodedPrUrl();
        if(StringUtils.isNotEmpty(encodedPrUrl)){
            AttachmentField prUrlField = new AttachmentField("PullRequest URL", encodedPrUrl, false);
            attachment.fields.add(prUrlField);
        }

        for(Map.Entry<String, String> entry : build.getMessages().entrySet()){
            AttachmentField field = new AttachmentField(entry.getKey(), entry.getValue(), false);
            attachment.fields.add(field);
        }

        this.attachments = new ArrayList<>();
        this.attachments.add(0, attachment);
    }
}