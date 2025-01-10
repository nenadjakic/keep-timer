package com.github.nenadjakic.keeptimer.repository

import com.github.nenadjakic.keeptimer.domain.entity.Project
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class ProjectsData(
    val projects: List<Project>,
    val favorites: List<Long>
)

class ProjectRepository(private val fileName: String = "/tmp/projects.json") {
    var projects: MutableList<Project> = mutableListOf()
    var favorites: MutableSet<Long> = mutableSetOf()

    init {
        loadProjectsFromFile()
    }

    private fun loadProjectsFromFile() {
        val path = Path(fileName)

        if (SystemFileSystem.exists(path)) {
            val source = SystemFileSystem.source(path)
            val jsonData = source.buffered().readString()
            val data = Json.decodeFromString<ProjectsData>(jsonData)
            projects = data.projects.toMutableList()
            favorites = data.favorites.toMutableSet()
        }
    }

    fun findAll() = projects

    fun findFavorites() = projects.filter { it.id in favorites }
}