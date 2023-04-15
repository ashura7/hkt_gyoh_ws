package service

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import model.Orphanage
import model.Parents
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.JsonSupporter

object AccountService {
    private val email2orphanage = mutableMapOf<String, Orphanage>()
    private val email2parents = mutableMapOf<String, Parents>()

    fun addOrphanage(orphanage: Orphanage): String {
        val email = orphanage.email

        if(email2orphanage.contains(email)) {
            return "Orphanage's email is already used"
        }

        if(email2parents.contains(email)) {
            return "Orphanage's email is already used as parents"
        }

        email2orphanage[email] = orphanage

        return ""
    }

    fun addParents(parents: Parents): String {
        val email = parents.email

        if(email2orphanage.contains(email)) {
            return "Parents' email is already used as orphanage"
        }

        if(email2parents.contains(email)) {
            return "Parents' email is already used"
        }

        email2parents[email] = parents

        return ""
    }

    fun getAccount(email: String, password: String): Pair<String, JsonObject?> {
        if(email2orphanage.contains(email)) {
            val orphanage = email2orphanage[email]!!

            if(orphanage.password != password) {
                return "Invalid password!" to null
            }

            val json = JsonSupporter.toJsonElement(orphanage).jsonObject


            return "Success" to JsonObject(json.plus("type" to JsonPrimitive("orphanage")))
        }

        if(email2parents.contains(email)) {
            val parents = email2parents[email]!!

            if(parents.password != password) {
                return "Invalid password!" to null
            }
            val json = JsonSupporter.toJsonElement(parents).jsonObject

            return "Success" to JsonObject(json.plus("type" to JsonPrimitive("parent")))
        }

        return "Invalid email" to null
    }
}