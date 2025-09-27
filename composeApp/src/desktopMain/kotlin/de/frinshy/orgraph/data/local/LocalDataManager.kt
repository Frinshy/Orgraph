package de.frinshy.orgraph.data.local

import de.frinshy.orgraph.data.config.ConfigManager
import de.frinshy.orgraph.data.models.School
import kotlinx.serialization.json.Json

class LocalDataManager {
    private val configManager = ConfigManager()
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val schoolFileName = "school_data.json"

    suspend fun saveSchool(school: School) {
        try {
            configManager.saveConfig(schoolFileName, school, School.serializer())
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to save school data: ${e.message}")
        }
    }

    suspend fun loadSchool(): School? {
        return try {
            configManager.loadConfig(schoolFileName, School.serializer())
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to load school data: ${e.message}")
            null
        }
    }

    suspend fun clearData() {
        try {
            configManager.deleteConfig(schoolFileName)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to clear school data: ${e.message}")
        }
    }
    
    fun getDataDirectory(): String = configManager.getConfigDirectory()
}