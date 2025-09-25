package de.frinshy.orgraph.data.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConfigManager {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val configDir: Path by lazy {
        val userHome = System.getProperty("user.home")
        val osName = System.getProperty("os.name").lowercase()
        val appDataDir = when {
            // Windows
            osName.contains("windows") -> {
                System.getenv("APPDATA")?.let { Paths.get(it) } 
                    ?: Paths.get(userHome, "AppData", "Roaming")
            }
            // macOS
            osName.contains("mac") -> {
                Paths.get(userHome, "Library", "Application Support")
            }
            // Linux and others
            else -> {
                System.getenv("XDG_CONFIG_HOME")?.let { Paths.get(it) }
                    ?: Paths.get(userHome, ".config")
            }
        }
        appDataDir.resolve("Orgraph").also { dir ->
            if (!Files.exists(dir)) {
                Files.createDirectories(dir)
            }
        }
    }
    
    suspend fun <T> saveConfig(fileName: String, data: T, serializer: kotlinx.serialization.KSerializer<T>) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = json.encodeToString(serializer, data)
                val configFile = configDir.resolve(fileName)
                Files.write(configFile, jsonString.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to save config file: $fileName - ${e.message}")
            }
        }
    }
    
    suspend fun <T> loadConfig(fileName: String, serializer: kotlinx.serialization.KSerializer<T>): T? {
        return withContext(Dispatchers.IO) {
            try {
                val configFile = configDir.resolve(fileName)
                if (Files.exists(configFile)) {
                    val jsonString = String(Files.readAllBytes(configFile))
                    json.decodeFromString(serializer, jsonString)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to load config file: $fileName - ${e.message}")
                null
            }
        }
    }
    
    suspend fun saveText(fileName: String, content: String) {
        withContext(Dispatchers.IO) {
            try {
                val configFile = configDir.resolve(fileName)
                Files.write(configFile, content.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to save text file: $fileName - ${e.message}")
            }
        }
    }
    
    suspend fun loadText(fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val configFile = configDir.resolve(fileName)
                if (Files.exists(configFile)) {
                    String(Files.readAllBytes(configFile))
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to load text file: $fileName - ${e.message}")
                null
            }
        }
    }
    
    suspend fun deleteConfig(fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                val configFile = configDir.resolve(fileName)
                Files.deleteIfExists(configFile)
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to delete config file: $fileName - ${e.message}")
            }
        }
    }
    
    fun getConfigDirectory(): String = configDir.toString()
    
    suspend fun listConfigFiles(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                Files.list(configDir).use { stream ->
                    stream.map { it.fileName.toString() }
                          .sorted()
                          .toList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to list config files - ${e.message}")
                emptyList()
            }
        }
    }
}