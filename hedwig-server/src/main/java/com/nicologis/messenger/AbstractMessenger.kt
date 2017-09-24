package com.nicologis.messenger

import com.nicologis.teamcity.BuildInfo

abstract class AbstractMessenger {
    fun send(build: BuildInfo, recipients: HashSet<Recipient>){
        var mapped = mapRecipients(build, recipients)
        mapped.forEach {
            send(build, it)
        }
    }
    protected abstract fun send(build:BuildInfo, recipient: Recipient)
    protected abstract fun mapRecipients(build:BuildInfo, recipients: Collection<Recipient>) : Collection<Recipient>
}
