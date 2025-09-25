package de.frinshy.orgraph.data.io

import de.frinshy.orgraph.data.config.ConfigManager
import de.frinshy.orgraph.data.models.OrgraphBackup
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.data.models.Scope
import de.frinshy.orgraph.data.models.Teacher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ImportExportManager {
    private val configManager = ConfigManager()
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Export school data to a JSON file
    suspend fun exportToFile(
        school: School,
        filePath: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val backup = OrgraphBackup(
                    version = "1.0.0",
                    exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    school = school.toSerializableSchool()
                )
                
                val jsonString = json.encodeToString(backup)
                Files.write(Paths.get(filePath), jsonString.toByteArray())
                
                Result.success("Backup exported successfully to: $filePath")
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Exception("Failed to export backup: ${e.message}"))
            }
        }
    }

    // Import data from a JSON file
    suspend fun importFromFile(filePath: String): Result<ImportResult> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext Result.failure(Exception("File not found: $filePath"))
                }
                
                val jsonString = String(Files.readAllBytes(Paths.get(filePath)))
                val backup = json.decodeFromString<OrgraphBackup>(jsonString)
                
                val school = backup.school.toSchool()
                
                Result.success(ImportResult(
                    school = school,
                    version = backup.version,
                    exportDate = backup.exportDate
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Exception("Failed to import backup: ${e.message}"))
            }
        }
    }

    // Quick export to default location with timestamp
    suspend fun quickExport(
        school: School
    ): Result<String> {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val fileName = "orgraph_backup_$timestamp.json"
        val configDir = configManager.getConfigDirectory()
        val filePath = Paths.get(configDir, "backups", fileName)
        
        // Create backups directory if it doesn't exist
        Files.createDirectories(filePath.parent)
        
        return exportToFile(school, filePath.toString())
    }

    // Get list of available backups in the config directory
    suspend fun getAvailableBackups(): List<BackupInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val backupDir = Paths.get(configManager.getConfigDirectory(), "backups")
                if (!Files.exists(backupDir)) {
                    return@withContext emptyList()
                }
                
                Files.list(backupDir).use { stream ->
                    stream.filter { it.fileName.toString().endsWith(".json") }
                          .map { path ->
                              val fileName = path.fileName.toString()
                              val fileSize = Files.size(path)
                              val lastModified = Files.getLastModifiedTime(path).toInstant()
                              
                              BackupInfo(
                                  fileName = fileName,
                                  filePath = path.toString(),
                                  fileSize = fileSize,
                                  lastModified = lastModified.toString()
                              )
                          }
                          .sorted { a, b -> b.lastModified.compareTo(a.lastModified) }
                          .toList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}

data class ImportResult(
    val school: School,
    val version: String,
    val exportDate: String
)

data class BackupInfo(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val lastModified: String
)

// Extension functions for conversion
private fun School.toSerializableSchool(): de.frinshy.orgraph.data.models.SerializableSchool {
    return de.frinshy.orgraph.data.models.SerializableSchool(
        id = id,
        name = name,
        address = address,
        teachers = teachers.map { it.toSerializableTeacher() },
        scopes = scopes.map { it.toSerializableScope() }
    )
}

private fun Teacher.toSerializableTeacher(): de.frinshy.orgraph.data.models.SerializableTeacher {
    return de.frinshy.orgraph.data.models.SerializableTeacher(
        id = id,
        name = name,
        subtitle = subtitle,
        backgroundImage = backgroundImage,
        email = email,
        phone = phone,
        scopes = scopes.map { it.toSerializableScope() },
        description = description,
        experience = experience
    )
}

private fun Scope.toSerializableScope(): de.frinshy.orgraph.data.models.SerializableScope {
    return de.frinshy.orgraph.data.models.SerializableScope(
        id = id,
        name = name,
        subtitle = subtitle,
        backgroundImage = backgroundImage,
        color = color, // Direct Color serialization
        description = description
    )
}

private fun de.frinshy.orgraph.data.models.SerializableSchool.toSchool(): School {
    return School(
        id = id,
        name = name,
        address = address,
        teachers = teachers.map { it.toTeacher() },
        scopes = scopes.map { it.toScope() }
    )
}

private fun de.frinshy.orgraph.data.models.SerializableTeacher.toTeacher(): Teacher {
    return Teacher(
        id = id,
        name = name,
        subtitle = subtitle,
        backgroundImage = backgroundImage,
        email = email,
        phone = phone,
        scopes = scopes.map { it.toScope() },
        description = description,
        experience = experience
    )
}

private fun de.frinshy.orgraph.data.models.SerializableScope.toScope(): Scope {
    return Scope(
        id = id,
        name = name,
        subtitle = subtitle,
        backgroundImage = backgroundImage,
        color = color, // Direct Color deserialization (handled by ColorSerializer)
        description = description
    )
}