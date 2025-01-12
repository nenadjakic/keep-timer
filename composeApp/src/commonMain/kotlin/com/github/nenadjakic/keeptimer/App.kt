package com.github.nenadjakic.keeptimer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

    var showDialog by remember { mutableStateOf(false) }
    var projects by remember { mutableStateOf(projectManagementService.projects) }
    var favorites by remember { mutableStateOf(projectManagementService.findFavoriteProjects()) }

    MaterialTheme {

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FavoritesPanel(projectManagementService)

            Spacer(modifier = Modifier.height(4.dp))

            ProjectsPanel(
                projects,
                onAdd = {
                    projectManagementService.saveProject(it)
                },
                onEdit = {
                    projectManagementService.saveProject(it)
                },
                onDelete = {
                    projectManagementService.deleteProject(it)
                    projects = projectManagementService.projects
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
    onAdd: (project: Project) -> Unit,
    onEdit: (project: Project) -> Unit,
    onDelete: (id: Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var currentProjects by remember { mutableStateOf(projects) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(2.dp, Color.Gray)
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
                    currentProjects.forEach { project ->
                        ProjectItem(
                            project = project,
                            onEdit = {
                                onEdit(it)
                            },
                            onDelete = {
                                onDelete(it.id!!)
                                currentProjects = currentProjects.filterNot { it == project }.toMutableList()
                            },

                            showTimers = true
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
fun FavoritesPanel(projectManagementService: ProjectManagementService) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(2.dp, Color.Gray)
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
                    "Favorites", style = MaterialTheme.typography.h6.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE),
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
                    projectManagementService.findFavoriteProjects().forEach { project ->
                        ProjectItem(
                            //projectManagementService = projectManagementService,
                            project = project,
                            onEdit = { },
                            onDelete = { },
                            showTimers = false
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
    showTimers: Boolean,
    onEdit: (project: Project) -> Unit,
    onDelete: (project: Project) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    var currentProject by remember { mutableStateOf(project) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dp(8F)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            currentProject.name,
            color = Color.Blue,
            style = MaterialTheme.typography.subtitle1.copy(
                color = Color.Blue,
                fontWeight = FontWeight.Bold
            )
        )
        Row {
            IconButton(onClick = { isDialogVisible = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
            }
            IconButton(onClick = { isDeleteDialogVisible = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Blue)
            }
            if (showTimers) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.ExpandMore, contentDescription = "More", tint = Color.Blue)
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                "Start: ${formatDateTime(timer.startTime)}", style = MaterialTheme.typography.body2.copy(
                    color = Color.Red,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "End: ${formatDateTime(timer.endTime)}", style = MaterialTheme.typography.body2.copy(
                    color = Color.Red,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Duration: ${calculateDuration(timer.startTime, timer.endTime)}",
                style = MaterialTheme.typography.body2.copy(
                    color = Color.Red,
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
                    tint = Color.Red,
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.StopCircle,
                    contentDescription = "Stop",
                    tint = Color.Red,
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