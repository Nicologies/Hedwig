package com.nicologis.teamcity;

import com.nicologis.messenger.MessengerFactory;
import com.nicologis.github.PullRequestInfo;
import com.nicologis.slack.StatusColor;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class BuildStatusListener extends BuildServerAdapter{
    private static final Logger log = Logger.getLogger(BuildStatusListener.class);
    private SBuildServer _server;

    public BuildStatusListener(@NotNull SBuildServer server){
        _server = server;
        _server.addListener(this);
    }

    @Override
    public void buildFinished(SRunningBuild build) {

        Status buildStatus = build.getBuildStatus();

        if (buildStatus.equals(Status.FAILURE) || buildStatus.equals(Status.ERROR)) {
            String statusText = "failed: " + build.getStatusDescriptor().getText();
            SendNotificationForBuild(build, statusText, StatusColor.danger);
        }
    }

    private void SendNotificationForBuild(SRunningBuild build, String statusText, StatusColor statusColor) {
        ParametersProvider paramProvider = build.getParametersProvider();

        PullRequestInfo pr = new PullRequestInfo(build);
        BuildInfo bdInfo = new BuildInfo(build, statusText, statusColor,
                pr, new HashMap<>(), _server.getRootUrl());

        MessengerFactory.sendMsg(bdInfo, paramProvider,
                new ArrayList<>());
    }
}
