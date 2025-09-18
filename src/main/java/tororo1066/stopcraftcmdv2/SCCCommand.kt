package tororo1066.stopcraftcmdv2

import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import tororo1066.commandapi.CommandArguments
import tororo1066.commandapi.ToolTip
import tororo1066.commandapi.argumentType.IntArg
import tororo1066.commandapi.argumentType.StringArg
import tororo1066.stopcraftcmdv2.StopCraftCMDV2.Companion.sendPrefixMsg
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.annotation.SCommandV2Body
import tororo1066.tororopluginapi.lang.SLang
import tororo1066.tororopluginapi.sCommand.v2.SCommandV2
import java.util.UUID

class SCCCommand: SCommandV2("sccv2") {

    companion object {
        val japaneseMaterial = HashMap<String, Material>()
        val debug = HashMap<UUID, Int>()

        fun Entity.debugMsg(msg: String, level: Int = 0) {
            if ((debug[uniqueId] ?: 0) >= level) {
                sendMessage(msg)
            }
        }
    }

    init {
        root.setPermission("sccv2.op")
        SLang.downloadMcLangFile("ja_jp") {
            Material.values().forEach { material ->
                japaneseMaterial[it[material.translationKey()]?.asString?:return@forEach] = material
            }
        }
    }

    private fun getMaterial(args: CommandArguments, sender: CommandSender): Material? {
        val material = args.getArgument("material", String::class.java).let {
            if (japaneseMaterial.containsKey(it.replace("\"",""))) japaneseMaterial[it.replace("\"","")]!! else Material.getMaterial(it.uppercase())
        }?:run {
            sender.sendPrefixMsg(SStr("&cMaterialが見つかりませんでした"))
            return null
        }
        return material
    }

    @SCommandV2Body
    val scc = command {

        literal("reload") {
            setPermission("sccv2.op.reload")
            setPlayerFunctionExecutor { sender, _, _ ->
                ThroughItemObject.load()
                sender.sendPrefixMsg(SStr("&aリロードしました"))
            }
        }

        literal("add") {
            setPermission("sccv2.op.add")
            argument("material", StringArg.phrase()) {
                suggest { _, _, _ ->
                    japaneseMaterial.keys.map { ToolTip("\"${it}\"") }.plus(Material.values().map { ToolTip(it.name.lowercase()) })
                }

                argument("data", IntArg(min = 1)) {
                    setPlayerFunctionExecutor { sender, _, args ->
                        val material = getMaterial(args, sender)?:return@setPlayerFunctionExecutor
                        if (material.isAir) {
                            sender.sendPrefixMsg(SStr("&cAirは追加できません"))
                            return@setPlayerFunctionExecutor
                        }
                        val data = args.getArgument("data", Int::class.java)
                        ThroughItemObject.addThroughItem(material, data).thenAccept {
                            if (it) {
                                sender.sendPrefixMsg(SStr("&a追加しました"))
                            } else {
                                sender.sendPrefixMsg(SStr("&c追加に失敗しました (既に追加されている可能性があります)"))
                            }
                        }
                    }
                }
            }
        }

        literal("remove") {
            setPermission("sccv2.op.remove")
            argument("material", StringArg.phrase()) {
                suggest { _, _, _ ->
                    japaneseMaterial.keys.map { ToolTip("\"${it}\"") }.plus(Material.values().map { ToolTip(it.name.lowercase()) })
                }

                argument("data", IntArg(min = 1)) {
                    setPlayerFunctionExecutor { sender, _, args ->
                        val material = getMaterial(args, sender)?:return@setPlayerFunctionExecutor
                        val data = args.getArgument("data", Int::class.java)
                        ThroughItemObject.removeThroughItem(material, data).thenAccept {
                            if (it) {
                                sender.sendPrefixMsg(SStr("&a削除しました"))
                            } else {
                                sender.sendPrefixMsg(SStr("&c削除に失敗しました (存在しない可能性があります)"))
                            }
                        }
                    }
                }
            }
        }

        literal("list") {
            setPermission("sccv2.op.list")
            setPlayerFunctionExecutor { sender, _, _ ->
                ThroughItemObject.throughItems.forEach { (material, data) ->
                    sender.sendMessage("${material.name} : ${data.joinToString(", ")}")
                }
            }

            argument("material", StringArg.phrase()) {
                suggest { _, _, _ ->
                    japaneseMaterial.keys.map { ToolTip("\"${it}\"") }.plus(Material.values().map { ToolTip(it.name.lowercase()) })
                }
                setPlayerFunctionExecutor { sender, _, args ->
                    val material = getMaterial(args, sender)?:return@setPlayerFunctionExecutor
                    ThroughItemObject.throughItems[material]?.let {
                        sender.sendMessage("${material.name} : ${it.joinToString(", ")}")
                    }?:run {
                        sender.sendPrefixMsg(SStr("&cそのMaterialは登録されていません"))
                    }
                }
            }
        }

        literal("gui") {
            setPermission("sccv2.op.gui")
            setPlayerFunctionExecutor { sender, _, _ ->
                SCCListMenu().open(sender)
            }
        }

        literal("debug") {
            setPermission("sccv2.op.debug")
            argument("level", IntArg(min = 0)) {
                setPlayerFunctionExecutor { sender, _, args ->
                    val level = args.getArgument("level", Int::class.java)
                    debug[sender.uniqueId] = level
                    sender.sendPrefixMsg(SStr("&aDebugLevelを${level}に設定しました"))
                }
            }

            literal("off") {
                setPlayerFunctionExecutor { sender, _, _ ->
                    debug.remove(sender.uniqueId)
                    sender.sendPrefixMsg(SStr("&aDebugLevelをオフに設定しました"))
                }
            }
        }
    }
}