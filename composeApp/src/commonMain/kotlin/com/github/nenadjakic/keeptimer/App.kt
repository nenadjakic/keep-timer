package com.github.nenadjakic.keeptimer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nenadjakic.keeptimer.domain.entity.Project
import com.github.nenadjakic.keeptimer.domain.entity.Timer
import com.github.nenadjakic.keeptimer.repository.ProjectManagementRepository
import com.github.nenadjakic.keeptimer.service.ProjectManagementService
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    val projectManagementService = ProjectManagementService(ProjectManagementRepository())

    val scrollState = rememberScrollState()
    var projects by remember { mutableStateOf(projectManagementService.projects.toMutableList()) }
    var favorites by remember { mutableStateOf(projectManagementService.favorites.toMutableSet()) }

    MaterialTheme {

        Column(
            Modifier.fillMaxWidth().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FavoritesPanel(
                projects,
                favorites,
                onFavoriteChange = { project, addToFavorites ->
                    if (addToFavorites) {
                        projectManagementService.addFavoriteProject(project.id!!)
                    } else {
                        projectManagementService.removeFavoriteProject(project.id!!)
                    }
                    favorites = projectManagementService.favorites.toMutableSet()
                },
            )

            Spacer(modifier = Modifier.height(4.dp))

            ProjectsPanel(
                projects,
                favorites,
                onAdd = {
                    projectManagementService.saveProject(it)
                    projects = projectManagementService.projects.toMutableList()
                },
                onEdit = {
                    projectManagementService.saveProject(it)
                    projects = projectManagementService.projects.toMutableList()
                },
                onDelete = {
                    projectManagementService.deleteProject(it)
                    projects = projectManagementService.projects.toMutableList()
                },
                onFavoriteChange = { project, addToFavorites ->
                    if (addToFavorites) {
                        projectManagementService.addFavoriteProject(project.id!!)
                    } else {
                        projectManagementService.removeFavoriteProject(project.id!!)
                    }
                    favorites = projectManagementService.favorites.toMutableSet()
                },
            )
        }
    }
}


@Composable
@Preview
fun EditProjectDialog(
    showDialog: Boolean,
    project: Project?,
    onDismiss: () -> Unit,
    onSave: (Project) -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var editableName by remember { mutableStateOf(project?.name ?: "") }
                    var isNameEmpty by remember { mutableStateOf(false) }
                    var showWarning by remember { mutableStateOf(false) }

                    Text(
                        text = if (project == null) "Add New Project" else "Edit Project",
                        style = MaterialTheme.typography.h6.copy(color = Color(0xFF6200EE)),
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    TextField(
                        value = editableName,
                        onValueChange = {
                            editableName = it
                            isNameEmpty = it.isEmpty()
                            showWarning = false
                        },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color(0xFF6200EE),
                            unfocusedIndicatorColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                val updatedProject =
                                    project?.copy(name = editableName) ?: Project(
                                        id = Clock.System.now().toEpochMilliseconds(), name = editableName
                                    )
                                onSave(updatedProject)

                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE)),
                            enabled = editableName.isNotBlank()
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun ProjectsPanel(
    projects: MutableList<Project>,
    favorites: MutableSet<Long>,
    onAdd: (project: Project) -> Unit,
    onEdit: (project: Project) -> Unit,
    onDelete: (id: Long) -> Unit,
    onFavoriteChange: (project: Project, addToFavorites: Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    val stateProjects by rememberUpdatedState(projects)
    val stateFavorites by rememberUpdatedState(favorites)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .border(1.dp, Color.Gray)
            .background(Color.White)
    ) {

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Projects", style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier
                        .padding(start = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isDialogVisible = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add")
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 32.dp)) {
                    stateProjects.forEach { project ->
                        ProjectItem(
                            project = project,
                            isFavorite = stateFavorites.contains(project.id!!),
                            onEdit = {
                                onEdit(it)
                            },
                            onDelete = {
                                onDelete(it.id!!)
                            },
                            showTimers = true,
                            showCrudButtons = true,
                            onFavoriteChange = { project, addToFavorites ->
                                onFavoriteChange(project, addToFavorites)
                            }
                        )
                    }
                }
            }
        }
    }

    EditProjectDialog(
        showDialog = isDialogVisible,
        project = null,
        onDismiss = { isDialogVisible = false },
        onSave = {
            onAdd(it)
            isDialogVisible = false
        }
    )
}

@Composable
@Preview
fun FavoritesPanel(
    projects: MutableList<Project>,
    favorites: MutableSet<Long>,
    onFavoriteChange: (project: Project, addToFavorites: Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val stateProjects by rememberUpdatedState(projects)
    val stateFavorites by rememberUpdatedState(favorites)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .border(1.dp, Color.Gray)
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Favorites", style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 32.dp)) {
                    stateProjects
                        .filter { stateFavorites.contains(it.id!!) }
                        .forEach { project ->
                            ProjectItem(
                                project = project,
                                isFavorite = true,
                                onEdit = { },
                                onDelete = { },
                                showTimers = false,
                                showCrudButtons = false,
                                onFavoriteChange = { project, add -> onFavoriteChange(project, add) }
                            )
                        }
                }
            }
        }
    }
}

@Composable
@Preview
fun ProjectItem(
    project: Project,
    isFavorite: Boolean,
    showTimers: Boolean,
    showCrudButtons: Boolean,
    onEdit: (project: Project) -> Unit,
    onDelete: (project: Project) -> Unit,
    onFavoriteChange: (project: Project, add: Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    var currentProject by remember { mutableStateOf(project) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dp(4f)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            currentProject.name,
            style = MaterialTheme.typography.subtitle1.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Row {
            IconButton(onClick = {
                onFavoriteChange(currentProject, !isFavorite)
            }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = if (!isFavorite) "Add to favorites" else "Remove from favorites"
                )
            }

            if (showCrudButtons) {
                IconButton(onClick = { isDialogVisible = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { isDeleteDialogVisible = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            if (showTimers) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
        }
    }
    AnimatedVisibility(visible = expanded) {
        Column(modifier = Modifier.padding(start = 32.dp)) {
            currentProject.timers.forEach { timer ->
                TimerItem(timer = timer)
            }
        }
    }

    EditProjectDialog(
        showDialog = isDialogVisible,
        project = currentProject,
        onDismiss = { isDialogVisible = false },
        onSave = {
            onEdit(it)
            currentProject = it
            isDialogVisible = false
        }
    )

    if (isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogVisible = false },
            title = {
                Text(text = "Are you sure you want to delete project with name ${currentProject.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(currentProject)
                        isDeleteDialogVisible = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isDeleteDialogVisible = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}


@Composable
@Preview
fun TimerItem(timer: Timer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                "Start: ${formatDateTime(timer.startTime)}", style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "End: ${formatDateTime(timer.endTime)}", style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Duration: ${calculateDuration(timer.startTime, timer.endTime)}",
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium
                )
            )

        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Start",
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.StopCircle,
                    contentDescription = "Stop",
                )
            }
        }
    }
}

fun formatDateTime(timeInMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(timeInMillis)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return dateTime.toString()
}

fun calculateDuration(startTime: Long, endTime: Long?): String {
    if (endTime == null) {
        return "NaN"
    }
    val startInstant = Instant.fromEpochMilliseconds(startTime)
    val endInstant = Instant.fromEpochMilliseconds(endTime)

    val durationInSeconds = (endInstant.toEpochMilliseconds() - startInstant.toEpochMilliseconds()) / 1000

    val hours = durationInSeconds / 3600
    val minutes = (durationInSeconds % 3600) / 60
    val seconds = durationInSeconds % 60

    return "$hours:$minutes:$seconds"
}