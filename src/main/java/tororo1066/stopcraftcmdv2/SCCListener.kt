package tororo1066.stopcraftcmdv2

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import tororo1066.stopcraftcmdv2.events.StopCmdInteractEvent
import tororo1066.tororopluginapi.annotation.SEventHandler

class SCCListener {

    private var craftingList = mutableListOf(
        InventoryType.WORKBENCH,
//        InventoryType.ANVIL, //Anvilは別途処理
        InventoryType.SMITHING,
        InventoryType.FURNACE,
        InventoryType.BEACON,
        InventoryType.ENCHANTING,
        InventoryType.GRINDSTONE,
        InventoryType.SMITHING,
        InventoryType.BREWING,
        InventoryType.BLAST_FURNACE
    )

    @Suppress("UnstableApiUsage")
    private fun shouldCancel(item: ItemStack, e: InventoryClickEvent): Boolean {
        if (item.hasData(DataComponentTypes.ITEM_MODEL) || item.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)){
            val event = StopCmdInteractEvent(e)
            event.callEvent()
            return !event.isCancelled
        }

        return false
    }

    @Suppress("UnstableApiUsage")
    @SEventHandler(priority =  EventPriority.HIGH)
    fun clickAnvilEvent(e: InventoryClickEvent) {
        if (e.view.type != InventoryType.ANVIL) return
        if (e.clickedInventory == e.whoClicked.inventory) return

        val item = e.currentItem?:return

        //Anvilは強制的にキャンセルする
        if (item.hasData(DataComponentTypes.ITEM_MODEL) || item.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)){
            e.isCancelled = true
        }
    }

    @SEventHandler(priority = EventPriority.HIGH)
    fun clickEvent(e: InventoryClickEvent) {
        if (!craftingList.contains(e.view.type)) return

        if (e.action == InventoryAction.HOTBAR_SWAP){
            e.isCancelled = true
            return
        }

//        if (e.action == InventoryAction.HOTBAR_MOVE_AND_READD){
//            e.whoClicked.debugMsg("cancel by HOTBAR_MOVE_AND_READD", 1)
//            e.isCancelled = true
//            return
//        }
        if (e.clickedInventory != e.whoClicked.inventory)return

        val item = e.currentItem?:return

//        if (item.itemMeta.hasCustomModelData() && item.itemMeta.customModelData != 0) {
//            if (ThroughItemObject.throughItems.containsKey(item.type)){
//                val data = ThroughItemObject.throughItems[item.type]!!
//                if (data.contains(item.itemMeta.customModelData)){
//                    return
//                }
//            }
//            e.whoClicked.debugMsg("cancel by customModelData", 1)
//            e.isCancelled = true
//        }

        e.isCancelled = shouldCancel(item, e)
    }

    @SEventHandler(priority = EventPriority.HIGH)
    fun craftEvent(e: CraftItemEvent){

        val items = e.inventory.storageContents

        for (item in items){
            item?:continue
            if (item.type == Material.AIR)continue
            if (!item.hasItemMeta())continue
            if (item.isSimilar(e.recipe.result))continue

//            if (item.itemMeta.hasCustomModelData() && item.itemMeta.customModelData!=0){
//                if (ThroughItemObject.throughItems.containsKey(item.type)){
//                    val data = ThroughItemObject.throughItems[item.type]!!
//                    if (data.contains(item.itemMeta.customModelData)){
//                        continue
//                    }
//                }
//                e.isCancelled = true
//                return
//            }
            if (shouldCancel(item, e)){
                e.isCancelled = true
                return
            }

        }

    }
}