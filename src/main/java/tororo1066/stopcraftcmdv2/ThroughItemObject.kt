package tororo1066.stopcraftcmdv2

import com.mongodb.client.model.Updates
import org.bukkit.Material
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.database.SDBCondition
import tororo1066.tororopluginapi.database.SDBVariable
import tororo1066.tororopluginapi.database.SDatabase
import java.util.concurrent.CompletableFuture

object ThroughItemObject {
    val throughItems = HashMap<Material, ArrayList<Int>>()
    private var loadType = "yml"
    var database: SDatabase? = null

    init {
        load()
    }

    fun load() {
        throughItems.clear()
        SJavaPlugin.plugin.reloadConfig()
        loadType = SJavaPlugin.plugin.config.getString("database.type", "yml")!!
        if (loadType == "yml") {
            val yml = SJavaPlugin.sConfig.getConfig("throughItems")?:run {
                SJavaPlugin.plugin.saveResource("throughItems.yml", false)
                SJavaPlugin.sConfig.getConfig("throughItems")!!
            }
            yml.getKeys(false).forEach { materialName ->
                val material = Material.getMaterial(materialName.uppercase())
                if (material == null) {
                    SJavaPlugin.plugin.logger.warning("Material $materialName is not found.")
                    return@forEach
                }
                val list = ArrayList<Int>()
                yml.getIntegerList(materialName).forEach { list.add(it) }
                throughItems[material] = list
            }
        } else {
            try {
                database = SDatabase.newInstance(SJavaPlugin.plugin)
            } catch (e: Exception) {
                SJavaPlugin.plugin.logger.warning("Failed to connect to the database.")
                return
            }
            val db = database!!
            db.createTable("through_items", mapOf(
                "id" to SDBVariable(SDBVariable.Int, autoIncrement = true, index = SDBVariable.Index.PRIMARY),
                "material" to SDBVariable(SDBVariable.VarChar, length = 255, nullable = false, index = SDBVariable.Index.KEY),
                "data" to SDBVariable(SDBVariable.Int, nullable = false, index = SDBVariable.Index.KEY)
            ))

            if (db.isMongo) {
                db.backGroundSelect("through_items") { resultSets ->
                    resultSets.forEach {
                        val material = Material.getMaterial(it.getString("material").uppercase())
                        if (material == null) {
                            SJavaPlugin.plugin.logger.warning("Material ${it.getString("material")} is not found.")
                            return@forEach
                        }
                        throughItems[material] = ArrayList(it.getList("data"))
                    }
                }
            } else {
                db.backGroundSelect("through_items") { resultSets ->
                    resultSets.forEach {
                        val material = Material.getMaterial(it.getString("material").uppercase())
                        if (material == null) {
                            SJavaPlugin.plugin.logger.warning("Material ${it.getString("material")} is not found.")
                            return@forEach
                        }
                        if (!throughItems.containsKey(material)) {
                            throughItems[material] = ArrayList()
                        }
                        throughItems[material]!!.add(it.getInt("data"))
                    }
                }
            }
        }

    }

    fun addThroughItem(material: Material, data: Int): CompletableFuture<Boolean> {
        if (throughItems.containsKey(material)) {
            val list = throughItems[material]!!
            if (list.contains(data)) {
                return CompletableFuture.completedFuture(false)
            }
            list.add(data)
        } else {
            throughItems[material] = arrayListOf(data)
        }
        if (loadType == "yml") {
            val yml = SJavaPlugin.sConfig.getConfig("throughItems")!!
            val list = yml.getIntegerList(material.name)
            list.add(data)
            yml.set(material.name, list)
            return SJavaPlugin.sConfig.asyncSaveConfig(yml, "throughItems")
        } else {
            val db = database ?: return CompletableFuture.completedFuture(false)
            return if (db.isMongo) {
                db.asyncSelect("through_items", SDBCondition().equal("material", material.name)).thenApplyAsync { resultSets ->
                    if (resultSets.isEmpty()){
                        db.asyncInsert("through_items", mapOf(
                            "material" to material.name,
                            "data" to listOf(data)
                        )).get()
                    } else {
                        db.asyncUpdate("through_items", Updates.push("data", data),
                            SDBCondition().equal("material", material.name)).get()
                    }
                }
            } else {
                db.asyncInsert("through_items", mapOf(
                    "material" to material.name,
                    "data" to data
                ))
            }
        }
    }

    fun removeThroughItem(material: Material, data: Int): CompletableFuture<Boolean> {
        if (!throughItems.containsKey(material)) {
            return CompletableFuture.completedFuture(false)
        }
        val dataList = throughItems[material]!!
        if (!dataList.contains(data)) {
            return CompletableFuture.completedFuture(false)
        }
        dataList.remove(data)
        if (loadType == "yml") {
            val yml = SJavaPlugin.sConfig.getConfig("throughItems")!!
            val list = yml.getIntegerList(material.name)
            list.remove(data)
            yml.set(material.name, list)
            return SJavaPlugin.sConfig.asyncSaveConfig(yml, "throughItems")
        } else {
            val db = database ?: return CompletableFuture.completedFuture(false)
            return if (db.isMongo) {
                db.asyncUpdate("through_items", Updates.pull("data", data),
                    SDBCondition().equal("material", material.name))
            } else {
                db.asyncDelete("through_items",
                    SDBCondition().equal("material", material.name).and(SDBCondition().equal("data", data)))
            }
        }
    }

}