package com.github.nenadjakic.keeptimer.service

import com.github.nenadjakic.keeptimer.domain.entity.Project
import com.github.nenadjakic.keeptimer.domain.entity.Timer
import com.github.nenadjakic.keeptimer.repository.ProjectManagementRepository
import kotlinx.datetime.Clock

class ProjectManagementService(private val projectManagementRepository: ProjectManagementRepository) {
    var projects: MutableList<Project> = projectManagementRepository.findAll()
    var favorites: MutableSet<Long> = projectManagementRepository.findFavorites()

    fun findFavoriteProjects(): MutableList<Project> = projects.filter { it.id in favorites }.toMutableList()

    fun saveProject(project: Project) = projectManagementRepository.saveAndFlushProject(project)

    fun deleteProject(id: Long) = projectManagementRepository.deleteProject(id)

    fun addFavoriteProject(id: Long) = projectManagementRepository.addToFavorites(id)

    fun removeFavoriteProject(id: Long) = projectManagementRepository.removeFromFavorites(id)

    fun findRunningTimerForProject(projectId: Long): Timer? = findRunningTimerForProject(projectId)

    fun startTimerForProject(projectId: Long): Timer {
        val newTimer = Timer(
            id = Clock.System.now().toEpochMilliseconds(),
            startTime = Clock.System.now().toEpochMilliseconds(),
            endTime = null
        )

        projects.forEach { project ->
            project.timers.forEach { timer ->
                if (timer.endTime == null) {
                    timer.endTime = Clock.System.now().toEpochMilliseconds()
                }
            }
        }

        val project = projects.find { it.id == projectId }
        project?.let {
            it.timers.add(newTimer)
        }

        projects.forEach { saveProject(it) }
        projectManagementRepository.flush()

        return newTimer
    }
}