package com.github.nenadjakic.keeptimer.service

import com.github.nenadjakic.keeptimer.domain.entity.Project
import com.github.nenadjakic.keeptimer.repository.ProjectRepository

class ProjectService(val projectRepository: ProjectRepository) {

    fun findAll(): MutableList<Project> = projectRepository.findAll()

    fun findFavorites(): MutableList<Project> = projectRepository.findFavorites()
}