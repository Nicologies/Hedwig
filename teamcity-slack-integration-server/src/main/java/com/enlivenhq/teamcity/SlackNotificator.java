package com.enlivenhq.teamcity;

import com.enlivenhq.slack.PullRequestInfo;
import com.enlivenhq.slack.SlackWrapper;
import com.enlivenhq.slack.StatusColor;
import jetbrains.buildServer.Build;
import jetbrains.buildServer.notification.Notificator;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.users.NotificatorPropertyKey;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.vcs.VcsRoot;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SlackNotificator implements Notificator {

    private static final Logger log = Logger.getLogger(SlackNotificator.class);

    private static final String type = "SlackNotificator";

    private static final String slackChannelKey = "slack.Channel";
    private static final String slackUsernameKey = "slack.Username";
    private static final String slackUrlKey = "slack.Url";

    private static final PropertyKey slackChannel = new NotificatorPropertyKey(type, slackChannelKey);
    private static final PropertyKey slackUsername = new NotificatorPropertyKey(type, slackUsernameKey);
    private static final PropertyKey slackUrl = new NotificatorPropertyKey(type, slackUrlKey);

    public static final String SystemWideSlackChannel = "system.slack.channel";
    public static final String SystemWideSlackUrlKey = "system.slack.url_key";
    public static final String SystemWideSlackUserName = "system.slack.user_name";

    private SBuildServer myServer;

    public SlackNotificator(NotificatorRegistry notificatorRegistry, SBuildServer server) {
        registerNotificatorAndUserProperties(notificatorRegistry);
        myServer = server;
    }

    @NotNull
    public String getNotificatorType() {
        return type;
    }

    @NotNull
    public String getDisplayName() {
        return "Slack Notifier";
    }

    public void notifyBuildFailed(@NotNull SRunningBuild sRunningBuild, @NotNull Set<SUser> users) {
        sendNotification("failed: " + sRunningBuild.getStatusDescriptor().getText(), StatusColor.danger, users, sRunningBuild);
    }

    public void notifyBuildFailedToStart(@NotNull SRunningBuild sRunningBuild, @NotNull Set<SUser> users) {
        sendNotification("failed to start", StatusColor.danger, users, sRunningBuild);
    }

    public void notifyBuildSuccessful(@NotNull SRunningBuild sRunningBuild, @NotNull Set<SUser> users) {
        sendNotification("built successfully", StatusColor.good, users, sRunningBuild);
    }

    public void notifyLabelingFailed(@NotNull Build build, @NotNull VcsRoot vcsRoot, @NotNull Throwable throwable, @NotNull Set<SUser> sUsers) {
        sendNotification("labeling failed", StatusColor.danger, sUsers, build);
    }

    public void notifyBuildFailing(@NotNull SRunningBuild sRunningBuild, @NotNull Set<SUser> sUsers) {
        sendNotification("failing", StatusColor.danger, sUsers, sRunningBuild);
    }

    public void notifyBuildProbablyHanging(@NotNull SRunningBuild sRunningBuild, @NotNull Set<SUser> sUsers) {
        sendNotification("probably hanging", StatusColor.warning, sUsers, sRunningBuild);
    }

    public void notifyBuildStarted(@NotNull SRunningBuild sRunningBuild, @NotNull Set<SUser> sUsers) {
        sendNotification("started", StatusColor.warning, sUsers, sRunningBuild);
    }

    public void notifyResponsibleChanged(@NotNull SBuildType sBuildType, @NotNull Set<SUser> sUsers) {

    }

    public void notifyResponsibleAssigned(@NotNull SBuildType sBuildType, @NotNull Set<SUser> sUsers) {

    }

    public void notifyResponsibleChanged(@Nullable TestNameResponsibilityEntry testNameResponsibilityEntry, @NotNull TestNameResponsibilityEntry testNameResponsibilityEntry2, @NotNull SProject sProject, @NotNull Set<SUser> sUsers) {

    }

    public void notifyResponsibleAssigned(@Nullable TestNameResponsibilityEntry testNameResponsibilityEntry, @NotNull TestNameResponsibilityEntry testNameResponsibilityEntry2, @NotNull SProject sProject, @NotNull Set<SUser> sUsers) {

    }

    public void notifyResponsibleChanged(@NotNull Collection<TestName> testNames, @NotNull ResponsibilityEntry responsibilityEntry, @NotNull SProject sProject, @NotNull Set<SUser> sUsers) {

    }

    public void notifyResponsibleAssigned(@NotNull Collection<TestName> testNames, @NotNull ResponsibilityEntry responsibilityEntry, @NotNull SProject sProject, @NotNull Set<SUser> sUsers) {

    }

    public void notifyBuildProblemResponsibleAssigned(@NotNull Collection<BuildProblemInfo> buildProblemInfos, @NotNull ResponsibilityEntry responsibilityEntry, @NotNull SProject sProject, @NotNull Set<SUser> sUsers) {

    }

    public void notifyBuildProblemResponsibleChanged(@NotNull Collection<BuildProblemInfo> buildProblemInfos, @NotNull ResponsibilityEntry responsibilityEntry, @NotNull SProject sProject, @NotNull Set<SUser> sUsers) {

    }

    public void notifyTestsMuted(@NotNull Collection<STest> sTests, @NotNull MuteInfo muteInfo, @NotNull Set<SUser> sUsers) {

    }

    public void notifyTestsUnmuted(@NotNull Collection<STest> sTests, @NotNull MuteInfo muteInfo, @Nullable SUser sUser, @NotNull Set<SUser> sUsers) {

    }

    public void notifyBuildProblemsMuted(@NotNull Collection<BuildProblemInfo> buildProblemInfos, @NotNull MuteInfo muteInfo, @NotNull Set<SUser> sUsers) {

    }

    public void notifyBuildProblemsUnmuted(@NotNull Collection<BuildProblemInfo> buildProblemInfos, @NotNull MuteInfo muteInfo, @Nullable SUser sUser, @NotNull Set<SUser> sUsers) {

    }

    private void registerNotificatorAndUserProperties(NotificatorRegistry notificatorRegistry) {
        ArrayList<UserPropertyInfo> userPropertyInfos = getUserPropertyInfosList();
        notificatorRegistry.register(this, userPropertyInfos);
    }

    private ArrayList<UserPropertyInfo> getUserPropertyInfosList() {
        ArrayList<UserPropertyInfo> userPropertyInfos = new ArrayList<UserPropertyInfo>();

        userPropertyInfos.add(new UserPropertyInfo(slackChannelKey,
                "Slack Channel(Leave empty to use %" + SystemWideSlackChannel + "%)"));
        userPropertyInfos.add(new UserPropertyInfo(slackUsernameKey,
                "Slack Username(Leave empty to use %" + SystemWideSlackUserName + "%)"));
        userPropertyInfos.add(new UserPropertyInfo(slackUrlKey,
                "Slack Webhook URL(Leave empty to use %" + SystemWideSlackUrlKey + "%)"));

        return userPropertyInfos;
    }

    private void sendNotification(String statusText, StatusColor statusColor, Set<SUser> users, Build bt) {
        if(!(bt instanceof SBuild)){
            return;
        }
        SBuild sbuild = (SBuild)bt;
        PullRequestInfo prInfo = new PullRequestInfo(sbuild);
        // reset the author, assignee and triggered_by
        // because this is a personal notification.
        prInfo.setAssignee("");
        prInfo.setAuthor("");
        prInfo.setTriggeredBy("");
        for (SUser user : users) {
            String username = user.getPropertyValue(slackUsername);
            if(StringUtils.isEmpty(username)){
                username = sbuild.getParametersProvider().get(SystemWideSlackUserName);
            }

            String url = user.getPropertyValue(slackUrl);
            if(StringUtils.isEmpty(url)){
                url = sbuild.getParametersProvider().get(SystemWideSlackUrlKey);
            }

            String configuredChannel = user.getPropertyValue(slackChannel);
            if(StringUtils.isEmpty(configuredChannel)){
                configuredChannel = sbuild.getParametersProvider().get(SystemWideSlackChannel);
            }

            List<SlackWrapper> slackWrappers = SlackWrapperBuilder.getSlackWrappers(configuredChannel, prInfo, url,
                    username, myServer.getRootUrl(), null);

            for(SlackWrapper slackWrapper : slackWrappers){
                try {
                    slackWrapper.send(Utils.getBranchName(sbuild), statusText, statusColor, bt);
                }
                catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
