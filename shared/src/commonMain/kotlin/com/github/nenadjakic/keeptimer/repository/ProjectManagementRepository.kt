package com.github.nenadjakic.keeptimer.repository

import com.github.nenadjakic.keeptimer.domain.entity.Project
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ProjectsData(
    val projects: List<Project>,
    val favorites: List<Long>
)

class ProjectManagementRepository(filePath: String = "/tmp/projects.json") {
    private var projects: MutableList<Project> = mutableListOf()
    private var favorites: MutableSet<Long> = mutableSetOf()
    private val path = Path(filePath)

    init {
        loadProjectsFromFile()
    }

    private fun loadProjectsFromFile() {
        if (SystemFileSystem.exists(path)) {
            val source = SystemFileSystem.source(path)
            val jsonData = source.buffered().readString()
            val data = Json.decodeFromString<ProjectsData>(jsonData)
            projects = data.projects.toMutableList()
            favorites = data.favorites.toMutableSet()
        }
    }

    fun findAll() = projects

    fun findFavorites() = favorites

    fun save(project: Project) {
        val index = projects.indexOfFirst { it.id == project.id }
        if (index == -1) {
            projects.add(project)
        } else {
            projects[index] = project
        }
        flush()
    }

    fun deleteProject(projectId: Long) {
        projects.removeAll { it.id == projectId }
        favorites.remove(projectId)
        flush()
    }

    fun addToFavorites(projectId: Long) {
        if (projects.any { it.id == projectId }) {
            favorites.add(projectId)
            flush()
        }
    }

    fun removeFromFavorites(projectId: Long) {
        if (favorites.contains(projectId)) {
            favorites.remove(projectId)
            flush()
        }
    }

    private fun flush() {
        val data = ProjectsData(
            projects = projects,
            favorites = favorites.toList()
        )

        val jsonData = Json { prettyPrint = true }.encodeToString(data)
        SystemFileSystem.sink(path).buffered().use { it.writeString(jsonData) }
    }
}