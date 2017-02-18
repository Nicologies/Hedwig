package com.nicologis.messenger

class Recipient constructor(private var name : String, var isRoom : Boolean){
    override fun equals(other: Any?): Boolean {
        if(other == null){ return false }
        if(other !is Recipient){ return false}
        return isRoom.equals(other.isRoom) && name.equals(other.name);
    }

    override fun hashCode(): Int {
        return name.hashCode()*23 + isRoom.hashCode();
    }

    fun getRecipientName(): String {
        if (!isRoom) {
            var ret = name
            if (!ret.startsWith("@")) {
                ret = "@" + ret
            }
            return ret
        } else {
            return name
        }
    }
}
