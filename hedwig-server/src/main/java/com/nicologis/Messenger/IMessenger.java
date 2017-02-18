package com.nicologis.Messenger;

import com.nicologis.teamcity.BuildInfo;

import java.io.IOException;

public interface IMessenger{
    void send(BuildInfo build) throws IOException;
}
