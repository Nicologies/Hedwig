package com.enlivenhq.teamcity;
import com.enlivenhq.slack.StatusColor;
import com.google.gson.annotations.Expose;
import jetbrains.buildServer.Build;
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
    private List<Attachment> _attachments;

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

    private boolean useAttachments = true;

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUseAttachments (boolean useAttachments) {
        this.useAttachments = useAttachments;
        if (!useAttachments) {
            _attachments = attachments;
            attachments = null;
        } else {
            attachments = _attachments;
        }
    }

    public boolean hasAttachments () {
        return attachments != null && attachments.size() > 0;
    }

    public SlackPayload(Build build, String branch, String statusText, StatusColor statusColor,
                        String serverUrl, String pullRequestUrl, Map<String, String> messages) {
        String escapedBranch = branch.length() > 0 ? " [" + branch + "]" : "";
        statusText = "<" + serverUrl + "/viewLog.html?buildId=" + build.getBuildId() + "&buildTypeId=" + build.getBuildTypeExternalId() + "|" + statusText + ">";

        String statusEmoji;
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

        String payloadText = statusEmoji + build.getFullName() + escapedBranch + " #" + build.getBuildNumber() + " " + statusText;
        this.text = payloadText;

        Attachment attachment = new Attachment();
        attachment.color = statusColor.name();
        attachment.pretext = "Build Status";
        attachment.fallback = payloadText;
        attachment.fields = new ArrayList<AttachmentField>();

        AttachmentField attachmentProject = new AttachmentField("Project", build.getFullName(), false);
        AttachmentField attachmentBuild = new AttachmentField("Build", build.getBuildNumber(), true);
        AttachmentField attachmentStatus = new AttachmentField("Status", statusText, false);
        AttachmentField attachmentBranch;

        attachment.fields.add(attachmentProject);
        attachment.fields.add(attachmentBuild);
        if (branch.length() > 0) {
            attachmentBranch = new AttachmentField("Branch", branch, false);
            attachment.fields.add(attachmentBranch);
        }
        attachment.fields.add(attachmentStatus);

        if(StringUtils.isNotEmpty(pullRequestUrl)){
            AttachmentField prUrlField = new AttachmentField("PullRequest URL", pullRequestUrl, false);
            attachment.fields.add(prUrlField);
        }

        for(Map.Entry<String, String> entry : messages.entrySet()){
            AttachmentField field = new AttachmentField(entry.getKey(), entry.getValue(), false);
            attachment.fields.add(field);
        }

        this._attachments = new ArrayList<Attachment>();
        this._attachments.add(0, attachment);

        if (this.useAttachments) {
            attachments = _attachments;
        }
    }
}