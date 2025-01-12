package com.github.nenadjakic.keeptimer.service

import com.github.nenadjakic.keeptimer.domain.entity.Project
import com.github.nenadjakic.keeptimer.repository.ProjectManagementRepository

class ProjectManagementService(private val projectManagementRepository: ProjectManagementRepository) {
    var projects: MutableList<Project> = projectManagementRepository.findAll()
    var favorites: MutableSet<Long> = projectManagementRepository.findFavorites()

    fun findFavoriteProjects(): MutableList<Project> = projects.filter { it.id in favorites }.toMutableList()

    fun saveProject(project: Project) = projectManagementRepository.save(project)

    fun deleteProject(id: Long) = projectManagementRepository.deleteProject(id)

    fun addFavoriteProject(id: Long) = projectManagementRepository.addToFavorites(id)

    fun removeFavoriteProject(id: Long) = projectManagementRepository.removeFromFavorites(id)
}