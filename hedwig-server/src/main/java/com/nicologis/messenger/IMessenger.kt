package com.nicologis.messenger

import com.nicologis.teamcity.BuildInfo

interface IMessenger {
    fun send(build: BuildInfo, recipient: Recipient)
}
