package tororo1066.stopcraftcmdv2

import org.bukkit.command.CommandSender
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.utils.sendMessage

class StopCraftCMDV2: SJavaPlugin(UseOption.SConfig) {

    companion object {
        val prefix = SStr("&6[&cStopCraftCMDV2&6]&r")

        fun CommandSender.sendPrefixMsg(msg: SStr){
            sendMessage(prefix + msg)
        }
    }

    override fun onStart() {
//        ThroughItemObject
//        SCCCommand()
    }

    override fun onEnd() {

    }
}