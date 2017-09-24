package com.nicologis.messenger

import jetbrains.buildServer.parameters.ParametersProvider

class Recipient constructor(private var name : String, var isRoom : Boolean){
    override fun equals(other: Any?): Boolean {
        if(other == null){ return false }
        if(other !is Recipient){ return false}
        return isRoom == other.isRoom && name == other.name;
    }

    override fun hashCode(): Int {
        return name.hashCode()*23 + isRoom.hashCode()
    }

    fun getRecipientName(params: ParametersProvider, suffix: UserMappingSuffix): String {
        return if (!isRoom) {
            var ret = mapUser(name, params, suffix)
            if (!ret.startsWith("@")) {
                ret = "@" + ret
            }
            ret
        } else {
            name
        }
    }

    private fun mapUser(name: String, params: ParametersProvider, suffix: UserMappingSuffix): String {
        val mappedName = params.get("user_mapping.$name.${suffix.name}")
        return mappedName?:name
    }
}
