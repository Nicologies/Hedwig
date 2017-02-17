package com.enlivenhq.teamcity;

import com.enlivenhq.Messenger.MessengerFactory;
import com.enlivenhq.github.PullRequestInfo;
import com.enlivenhq.slack.SlackParameters;
import com.enlivenhq.slack.StatusColor;
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
    public void buildStarted(SRunningBuild build) {
        String reportStarting = build.getParametersProvider().get("system.slack.report_starting");
        if(StringUtils.isNotEmpty(reportStarting) && reportStarting.toLowerCase().equals("true")){
            String statusText = "started";
            SendNotificationForBuild(build, statusText, StatusColor.warning);
        }
    }

    @Override
    public void buildFinished(SRunningBuild build) {

        Status buildStatus = build.getBuildStatus();

        if (buildStatus.equals(Status.FAILURE) || buildStatus.equals(Status.ERROR)) {
            String reportFailure = build.getParametersProvider().get("system.slack.report_failure");
            if (StringUtils.isNotEmpty(reportFailure) && reportFailure.toLowerCase().equals("true")) {
                String statusText = "failed: " + build.getStatusDescriptor().getText();
                SendNotificationForBuild(build, statusText, StatusColor.danger);
            }
        }
        else if(buildStatus.equals(Status.NORMAL) || buildStatus.equals(Status.WARNING)){
            String reportSuccess = build.getParametersProvider().get("system.slack.report_success");
            if(StringUtils.isNotEmpty(reportSuccess) && reportSuccess.toLowerCase().equals("true")){
                String statusText = "built successfully. Finished: " + build.getStatusDescriptor().getText();
                SendNotificationForBuild(build, statusText, StatusColor.good);
            }
        }
    }

    private void SendNotificationForBuild(SRunningBuild build, String statusText, StatusColor statusColor) {
        ParametersProvider paramProvider = build.getParametersProvider();

        String urlKey = paramProvider.get(SlackParameters.SystemWideSlackUrlKey);
        String teamcityBotName = paramProvider.get(SlackParameters.SystemWideSlackUserName);
        PullRequestInfo pr = new PullRequestInfo(build);
        BuildInfo bdInfo = new BuildInfo(build, statusText, statusColor,
                pr, new HashMap<String, String>());

        MessengerFactory.sendMsg(bdInfo, urlKey, teamcityBotName, _server.getRootUrl(),
                new ArrayList<String>());


    }
}
