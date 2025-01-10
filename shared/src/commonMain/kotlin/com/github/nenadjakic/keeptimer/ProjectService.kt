package com.github.nenadjakic.keeptimer

import com.github.nenadjakic.keeptimer.domain.entity.Project
import com.github.nenadjakic.keeptimer.repository.ProjectRepository

class ProjectService(val projectRepository: ProjectRepository) {

    fun findAll(): Collection<Project> = projectRepository.findAll()
}