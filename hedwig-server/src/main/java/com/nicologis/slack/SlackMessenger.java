package com.nicologis.slack;

import com.nicologis.messenger.IMessenger;
import com.nicologis.messenger.Recipient;
import com.nicologis.teamcity.BuildInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class SlackMessenger implements IMessenger
{
    public static final GsonBuilder GSON_BUILDER = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
    private static final Logger LOG = Logger.getLogger(SlackMessenger.class);
    protected String _slackWebhookUrl;

    protected String _botName;

    public SlackMessenger(String webhookUrl, String botName) {
        this._slackWebhookUrl = webhookUrl;
        this._botName = botName;
    }

    public void send(BuildInfo build, Recipient recipient){
        String formattedPayload = getFormattedPayload(build, recipient.getRecipientName());
        LOG.debug(formattedPayload);
        try {

            URL url = new URL(_slackWebhookUrl);
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
        }catch (IOException ex){
            ex.printStackTrace();
            LOG.error(ex.getMessage());
        }
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

    @NotNull
    public String getFormattedPayload(BuildInfo build, String recipient) {
        Gson gson = GSON_BUILDER.create();

        SlackPayload slackPayload = new SlackPayload(build);
        slackPayload.setRecipient(recipient);
        slackPayload.setBotName(_botName);

        return gson.toJson(slackPayload);
    }
}
