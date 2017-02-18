package com.nicologis.slack;

import com.nicologis.Messenger.IMessenger;
import com.nicologis.teamcity.BuildInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class SlackMessenger implements IMessenger
{
    public static final GsonBuilder GSON_BUILDER = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
    private static final Logger LOG = Logger.getLogger(SlackMessenger.class);
    protected String slackUrl;

    protected String botName;

    protected String recipient;

    protected String serverUrl;

    protected String pullRequestUrl;

    public SlackMessenger() {
    }

    public void send(BuildInfo build) throws IOException {
        String formattedPayload = getFormattedPayload(build);
        LOG.debug(formattedPayload);

        URL url = new URL(this.getSlackUrl());
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("User-Agent", "Enliven");
        httpsURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        httpsURLConnection.setDoOutput(true);

        DataOutputStream dataOutputStream = new DataOutputStream(
            httpsURLConnection.getOutputStream()
        );

        dataOutputStream.writeBytes(formattedPayload);
        dataOutputStream.flush();
        dataOutputStream.close();

        InputStream inputStream;
        String responseBody = "";

        try {
            inputStream = httpsURLConnection.getInputStream();
        }
        catch (IOException e) {
            responseBody = e.getMessage();
            inputStream = httpsURLConnection.getErrorStream();
            if (inputStream != null) {
                responseBody += ": ";
                responseBody = getResponseBody(inputStream, responseBody);
            }
            throw new IOException(responseBody);
        }

        getResponseBody(inputStream, responseBody);
    }

    @NotNull
    public String getFormattedPayload(BuildInfo build) {
        Gson gson = GSON_BUILDER.create();

        SlackPayload slackPayload = new SlackPayload(build,
                WebUtil.escapeUrlForQuotes(getServerUrl()), WebUtil.escapeUrlForQuotes(pullRequestUrl));
        slackPayload.setRecipient(getRecipient());
        slackPayload.setBotName(getBotName());

        return gson.toJson(slackPayload);
    }

    private String getResponseBody(InputStream inputStream, String responseBody) throws IOException {
        String line;

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream)
        );

        while ((line = bufferedReader.readLine()) != null) {
            responseBody += line + "\n";
        }

        bufferedReader.close();
        return responseBody;
    }

    public void setSlackUrl(String slackUrl)
    {
        this.slackUrl = slackUrl;
    }

    public String getSlackUrl()
    {
        return this.slackUrl;
    }

    public void setBotName(String botName){
        this.botName = botName;
    }

    public String getBotName()
    {
        return this.botName;
    }

    public void setRecipient(String recipient){
        this.recipient = recipient;
    }

    public String getRecipient(){
        return this.recipient;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setPullRequestUrl(String pullRequestUrl) {
        this.pullRequestUrl = pullRequestUrl;
    }
}
