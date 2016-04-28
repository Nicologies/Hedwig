package com.enlivenhq.teamcity;

import jetbrains.buildServer.Build;
import jetbrains.buildServer.BuildType;
import jetbrains.buildServer.StatusDescriptor;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.userChanges.CanceledInfo;
import jetbrains.buildServer.tests.TestInfo;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.users.UserSet;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import jetbrains.buildServer.vcs.VcsModification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class SlackPayloadTest {

    String project = "project";
    String buildNum = "build";
    String branch = "";
    String statusText = "status";
    String statusColor = "color";
    String btId = "btId";
    long buildId = 0;
    String serverUrl = "localhost";
    String channel = "#channel";
    String username = "bot";
    String pullRequestUrl = "https://github.com/dummyOwner/dummyRepo/pull/7";
    SlackPayload slackPayload;
    Map<String, String> messages = new HashMap<String, String>();
    
    Build build = new Build() {
        @NotNull
        public Date getStartDate() {
            return null;
        }

        public String getAgentName() {
            return null;
        }

        public long getBuildId() {
            return buildId;
        }

        public StatusDescriptor getStatusDescriptor() {
            return null;
        }

        public List<String> getLogMessages(int i, int i1) {
            return null;
        }

        public List<TestInfo> getTestMessages(int i, int i1) {
            return null;
        }

        public List<String> getCompilationErrorMessages() {
            return null;
        }

        @Nullable
        public BuildType getBuildType() {
            return null;
        }

        @NotNull
        public String getBuildTypeId() {
            return btId;
        }

        @NotNull
        public String getBuildTypeExternalId() {
            return btId;
        }

        @NotNull
        public String getBuildTypeName() {
            return null;
        }

        @NotNull
        public String getFullName() {
            return project;
        }

        @Nullable
        public String getProjectId() {
            return null;
        }

        @Nullable
        public String getProjectExternalId() {
            return null;
        }

        public List<? extends VcsModification> getChanges(SelectPrevBuildPolicy selectPrevBuildPolicy, boolean b) {
            return null;
        }

        public List<? extends VcsModification> getContainingChanges() {
            return null;
        }

        public boolean isPersonal() {
            return false;
        }

        public Status getBuildStatus() {
            return null;
        }

        public boolean isFinished() {
            return false;
        }

        public UserSet<? extends User> getCommitters(SelectPrevBuildPolicy selectPrevBuildPolicy) {
            return null;
        }

        public String getBuildNumber() {
            return buildNum;
        }

        @Nullable
        public Date getFinishDate() {
            return null;
        }

        public CanceledInfo getCanceledInfo() {
            return null;
        }

        public long getDuration() {
            return 0;
        }
    };

    @AfterMethod
    public void tearDown() throws Exception {
        slackPayload = null;
    }

    @Test
    public void testSlackPayloadDoesNotRequiresUserAndChannel() {
        slackPayload = new SlackPayload(build, branch, statusText, statusColor, serverUrl,
                pullRequestUrl, messages);
        assertFalse(slackPayload == null);
    }

    @Test
    public void testSlackPayloadWithoutAttachment() {
        slackPayload = new SlackPayload(build, branch, statusText, statusColor, serverUrl,
                pullRequestUrl, messages);
        slackPayload.setUseAttachments(false);
        assertFalse(slackPayload.hasAttachments());
    }

    @Test
    public void testSlackPayloadUsesAttachmentByDefault() {
        slackPayload = new SlackPayload(build, branch, statusText, statusColor, serverUrl,
                pullRequestUrl, messages);
        assertTrue(slackPayload.hasAttachments());
    }

    @Test
    public void testSlackPayloadIsUpdatedWithUsername() {
        slackPayload = new SlackPayload(build, branch, statusText, statusColor, serverUrl,
                pullRequestUrl, messages);
        slackPayload.setUseAttachments(false);
        slackPayload.setUsername(username);
        assertTrue(slackPayload.getUsername() == username);
    }

    @org.testng.annotations.Test
    public void testSlackPayloadIsUpdatedWithChannel() {
        slackPayload = new SlackPayload(build, branch, statusText, statusColor, serverUrl,
                pullRequestUrl, messages);
        slackPayload.setUseAttachments(false);
        slackPayload.setChannel(channel);
        assertTrue(slackPayload.getChannel() == channel);
    }
}