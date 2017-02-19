package com.nicologis.messenger

import jetbrains.buildServer.parameters.ParametersProvider

class Recipient constructor(private var name : String, var isRoom : Boolean){
    override fun equals(other: Any?): Boolean {
        if(other == null){ return false }
        if(other !is Recipient){ return false}
        return isRoom.equals(other.isRoom) && name.equals(other.name);
    }

    override fun hashCode(): Int {
        return name.hashCode()*23 + isRoom.hashCode();
    }

    fun getRecipientName(params: ParametersProvider, suffix: UserMappingSuffix): String {
        if (!isRoom) {
            var ret = MapUser(name, params, suffix)
            if (!ret.startsWith("@")) {
                ret = "@" + ret
            }
            return ret
        } else {
            return name
        }
    }

    private fun MapUser(name: String, params: ParametersProvider, suffix: UserMappingSuffix): String {
        val mappedName = params.get("user_mapping.$name.${suffix.name}")
        return mappedName?:name
    }
}
