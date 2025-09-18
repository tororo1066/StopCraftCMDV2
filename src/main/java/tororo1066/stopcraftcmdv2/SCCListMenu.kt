package tororo1066.stopcraftcmdv2

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import tororo1066.stopcraftcmdv2.StopCraftCMDV2.Companion.sendPrefixMsg
import tororo1066.tororopluginapi.SStr
import tororo1066.tororopluginapi.defaultMenus.LargeSInventory
import tororo1066.tororopluginapi.defaultMenus.NumericInputInventory
import tororo1066.tororopluginapi.lang.SLang
import tororo1066.tororopluginapi.sInventory.SInventoryItem
import java.util.function.Consumer

class SCCListMenu: LargeSInventory("§d§l一覧") {

    override fun renderMenu(p: Player): Boolean {
        val items = ArrayList<SInventoryItem>()
        ThroughItemObject.throughItems.forEach {
            items.add(SInventoryItem(ItemStack(it.key)
                .apply {
                    editMeta { meta ->
                        meta.displayName(
                            Component.text()
                                .color(TextColor.color(0x00ff00))
                                .append(Component.translatable(it.key.translationKey()))
                                .build()
                        )
                    }
                })
                .setCanClick(false)
                .setClickEvent { _ ->
                    val inv = object : LargeSInventory("§a§l管理") {
                        override fun renderMenu(p: Player): Boolean {
                            val editItems = ArrayList<SInventoryItem>()
                            it.value.forEach { data ->
                                editItems.add(SInventoryItem(it.key)
                                    .setDisplayName("§a§l${data}")
                                    .addLore("§c§lシフト右クリックで削除")
                                    .setCustomModelData(data)
                                    .setCanClick(false)
                                    .setClickEvent second@ { e ->
                                        if (e.click != ClickType.SHIFT_RIGHT) return@second
                                        ThroughItemObject.removeThroughItem(it.key, data).thenAccept { bool ->
                                            if (bool) {
                                                p.sendPrefixMsg(SStr("&a§l削除しました"))
                                            } else {
                                                p.sendPrefixMsg(SStr("&c§l削除に失敗しました"))
                                            }
                                            allRenderMenu(p)
                                        }
                                    }
                                )
                            }
                            setResourceItems(editItems)
                            return true
                        }

                        override fun afterRenderMenu() {
                            super.afterRenderMenu()
                            setItem(51, SInventoryItem(Material.EMERALD_BLOCK)
                                .setDisplayName("§a§l追加")
                                .setCanClick(false)
                                .setClickEvent { _ ->
                                    val inv = NumericInputInventory("§a§l追加するカスタムモデルデータを入力してください")
                                    inv.allowZero = false
                                    inv.onConfirm = Consumer { long ->
                                        ThroughItemObject.addThroughItem(it.key, long.toInt()).thenAccept { bool ->
                                            if (bool) {
                                                p.sendPrefixMsg(SStr("&a§l追加しました"))
                                            } else {
                                                p.sendPrefixMsg(SStr("&c§l追加に失敗しました"))
                                            }
                                        }
                                        p.closeInventory()
                                    }
                                    inv.onCancel = Consumer {
                                        p.closeInventory()
                                    }
                                    moveChildInventory(inv, p)
                                })
                        }
                    }
                    moveChildInventory(inv, p)
                }
            )
        }

        setResourceItems(items)
        return true
    }

    override fun afterRenderMenu() {
        super.afterRenderMenu()
        setItem(51, SInventoryItem(Material.EMERALD_BLOCK)
            .setDisplayName("§a§lここにドラッグで追加")
            .setCanClick(false)
            .setClickEvent { e ->
                val item = (e.cursor?:return@setClickEvent).clone()
                if (item.type.isAir) return@setClickEvent
                if (ThroughItemObject.throughItems.containsKey(item.type)){
                    return@setClickEvent
                }
                val inv = NumericInputInventory("§a§l追加するカスタムモデルデータを入力してください")
                inv.allowZero = false
                inv.onConfirm = Consumer { long ->
                    ThroughItemObject.addThroughItem(item.type, long.toInt()).thenAccept { bool ->
                        if (bool) {
                            e.whoClicked.inventory.addItem(item)
                            e.whoClicked.sendPrefixMsg(SStr("&a§l追加しました"))
                        } else {
                            e.whoClicked.sendPrefixMsg(SStr("&c§l追加に失敗しました"))
                        }
                    }
                    e.whoClicked.closeInventory()
                }
                inv.onCancel = Consumer {
                    it.whoClicked.closeInventory()
                }

                moveChildInventory(inv, e.whoClicked as Player)
            })
    }
}