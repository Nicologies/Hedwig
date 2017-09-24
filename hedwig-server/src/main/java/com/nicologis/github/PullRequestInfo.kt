package com.nicologis.github

import com.intellij.openapi.util.text.StringUtil
import com.nicologis.messenger.Recipient
import com.nicologis.teamcity.ParameterNames
import com.nicologis.teamcity.Utils
import jetbrains.buildServer.serverSide.SBuild
import org.apache.commons.lang.StringUtils
import java.util.*

class PullRequestInfo(build: SBuild) {
    private var _author: String? = null
    private var _assignee: String? = null
    private val _triggeredBy: String?
    private var _participants: Array<String>? = null
    var Url: String? = null
    var Branch: String

    init {
        _author = build.parametersProvider.get("teamcity.build.pull_req.author")

        _assignee = build.parametersProvider.get("teamcity.build.pull_req.assignee")

        Url = build.parametersProvider.get("teamcity.build.pull_req.url")

        _triggeredBy = build.parametersProvider.get("teamcity.build.triggeredBy.username")
        val notifyParticipants = build.parametersProvider.get(ParameterNames.NotifyParticipants)
        if (StringUtil.isNotEmpty(notifyParticipants) && notifyParticipants!!.toLowerCase() == "true") {
            val rawParticipants = build.parametersProvider.get("teamcity.build.pull_req.participants")
            if (StringUtils.isNotEmpty(rawParticipants)) {
                _participants = rawParticipants!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }
        }

        Branch = Utils.getBranchName(build)
    }

    val recipients: List<Recipient>
        get() {
            val ret = ArrayList<String>()
            addUserToMentionList(ret, _author)
            addUserToMentionList(ret, _assignee)
            addUserToMentionList(ret, _triggeredBy)
            if (_participants != null) {
                for (p in _participants!!) {
                    addUserToMentionList(ret, p)
                }
            }
            return ret.map { x -> Recipient(x, x.startsWith("#")) }.toList()
        }

    private fun addUserToMentionList(list: MutableList<String>, user: String?) {
        if (StringUtils.isNotEmpty(user)) {
            if (!list.contains(user)) {
                list.add(user!!)
            }
        }
    }

    fun setAuthor(author: String) {
        _author = author
    }

    fun setAssignee(assignee: String) {
        _assignee = assignee
    }
}
