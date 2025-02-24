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
import kotlinx.coroutines.delay
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
    val _projects = projectManagementService.projects.toList()
    val projects = remember { mutableStateOf(_projects) }
    var favorites by remember { mutableStateOf(projectManagementService.favorites.toMutableSet()) }

    MaterialTheme {

        Column(
            Modifier.fillMaxWidth().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /*
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
            */

            Spacer(modifier = Modifier.height(4.dp))

            ProjectsPanel(
                _projects.toMutableList(),
                favorites,
                onAdd = {
                    projectManagementService.saveProject(it)
                    projects.value = projectManagementService.projects.toMutableList()
                },
                onEdit = {
                    projectManagementService.saveProject(it)
                    projects.value = projectManagementService.projects.toMutableList()
                },
                onDelete = {
                    projectManagementService.deleteProject(it)
                    projects.value = projectManagementService.projects.toMutableList()
                },
                onFavoriteChange = { project, addToFavorites ->
                    if (addToFavorites) {
                        projectManagementService.addFavoriteProject(project.id!!)
                    } else {
                        projectManagementService.removeFavoriteProject(project.id!!)
                    }
                    favorites = projectManagementService.favorites.toMutableSet()
                },
                onTimerStart = {
                    var timer = projectManagementService.startTimerForProject(it.id!!)
                    projects.value = projectManagementService.projects.toMutableList()
                    return@ProjectsPanel timer
                },
                onTimerStop = { project ->
                    projectManagementService.saveProject(project)
                    projects.value = projectManagementService.projects.toMutableList()
                }
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
    projects: List<Project>,
    favorites: MutableSet<Long>,
    onAdd: (project: Project) -> Unit,
    onEdit: (project: Project) -> Unit,
    onDelete: (id: Long) -> Unit,
    onFavoriteChange: (project: Project, addToFavorites: Boolean) -> Unit,
    onTimerStart: (project: Project) -> Timer,
    onTimerStop: (project: Project) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    //val stateProjects by remember { mutableStateOf(projects) }
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
                    projects.forEach { project ->
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
                            showTimerButtons = true,
                            onFavoriteChange = { project, addToFavorites ->
                                onFavoriteChange(project, addToFavorites)
                            },
                            onTimerStart = {
                                val timer = onTimerStart(project)
                                expanded = false
                                return@ProjectItem timer
                            },
                            onTimerStop = {
                                onTimerStop(project)
                            }
                        )
                        Divider()
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
/*
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
                                showTimerButtons = true,
                                onFavoriteChange = { project, add -> onFavoriteChange(project, add) }
                            )
                        }
                }
            }
        }
    }
}
*/
@Composable
@Preview
fun ProjectItem(
    project: Project,
    isFavorite: Boolean,
    showTimers: Boolean,
    showCrudButtons: Boolean,
    showTimerButtons: Boolean,
    onEdit: (project: Project) -> Unit,
    onDelete: (project: Project) -> Unit,
    onFavoriteChange: (project: Project, add: Boolean) -> Unit,
    onTimerStart: (project: Project) -> Timer,
    onTimerStop: (project: Project) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    var currentProject by remember { mutableStateOf(project) }
    var runningTimer: Timer? by remember { mutableStateOf(currentProject.timers.find { it.endTime == null }) }

    var timerStarted by remember { mutableStateOf(runningTimer != null) }

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

            if (showTimerButtons) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (!timerStarted) {
                                runningTimer = onTimerStart(currentProject)
                                onTimerStart(currentProject)
                                timerStarted = true
                            }
                        },
                        enabled = !timerStarted
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Start",
                        )
                    }
                    IconButton(
                        onClick = {
                            runningTimer!!.endTime = Clock.System.now().toEpochMilliseconds()
                            onEdit(currentProject)
                            onTimerStop(currentProject)
                            runningTimer = null
                            timerStarted = false
                        },
                        enabled = timerStarted
                    ) {
                        Icon(
                            imageVector = Icons.Default.StopCircle,
                            contentDescription = "Stop",
                        )
                    }
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
                TimerItem(timer, showTimerButtons)
                Divider()
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
fun TimerItem(
    timer: Timer,
    showTimerButtons: Boolean
) {
    var timerStarted by remember { mutableStateOf(timer.endTime == null) }
    var currentDuration by remember { mutableStateOf(calculateDuration(timer.startTime, timer.endTime)) }

    LaunchedEffect(timerStarted) {
        while (timerStarted) {
            currentDuration = calculateDuration(timer.startTime, timer.endTime)
            delay(1000L)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .background(if (timer.endTime == null) Color.LightGray else Color.Transparent),
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
                "End: ${if (timer.endTime != null) formatDateTime(timer.endTime!!) else "" }", style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Duration: $currentDuration",
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

fun formatDateTime(timeInMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(timeInMillis)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return dateTime.toString()
}

fun calculateDuration(startTime: Long, endTime: Long?): String {
    val endInstant = if (endTime == null) {
        Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
    } else {
        Instant.fromEpochMilliseconds(endTime)
    }

    val startInstant = Instant.fromEpochMilliseconds(startTime)
    val durationInSeconds = (endInstant.toEpochMilliseconds() - startInstant.toEpochMilliseconds()) / 1000

    val hours = durationInSeconds / 3600
    val minutes = (durationInSeconds % 3600) / 60
    val seconds = durationInSeconds % 60

    return "$hours:$minutes:$seconds"
}

fun hasStartedTimer(project: Project): Boolean = project.timers.isNotEmpty() && project.timers.any { it.endTime == null }