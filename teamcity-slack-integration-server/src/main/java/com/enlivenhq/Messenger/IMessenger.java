package com.enlivenhq.Messenger;

import com.enlivenhq.teamcity.BuildInfo;

import java.io.IOException;

public interface IMessenger{
    void send(BuildInfo build) throws IOException;
}
