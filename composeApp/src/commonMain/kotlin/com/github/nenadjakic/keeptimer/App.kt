package com.github.nenadjakic.keeptimer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.github.nenadjakic.keeptimer.repository.ProjectRepository
import com.github.nenadjakic.keeptimer.service.ProjectService
import keep_timer.composeapp.generated.resources.Res
import keep_timer.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    val projectService = ProjectService(ProjectRepository())
    var showDialog by remember { mutableStateOf(false) }

    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var projects by remember { mutableStateOf(projectService.findAll()) }
        var favorites by remember { mutableStateOf(projectService.findFavorites()) }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            FavoritesPanel(favorites)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Projects", style = MaterialTheme.typography.caption)

                Spacer(modifier = Modifier.height(Dp(16F)))

                IconButton(onClick = { }) {
                    Icon(Icons.Default.Stop, contentDescription = "Add")
                }

            }

            Spacer(modifier = Modifier.height(Dp(16F)))

            Button(onClick = { showDialog = true }) {
                Text("Show Dialog")
            }

            MyModalDialog(
                showDialog = showDialog,
                project = projects[0],
                onDismiss = { showDialog = false }
            )

            Spacer(modifier = Modifier.height(Dp(8F)))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(projects) { project ->
                    //ProjectItem(project, onEdit = { }, onDelete = { })
                }
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}

@Composable
@Preview
fun MyModalDialog(showDialog: Boolean, project: Project, onDismiss: () -> Unit) {
    if (showDialog) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .size(300.dp, 200.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var editableName by remember { mutableStateOf(project.name) }

                    Text(
                        text = "Edit Project",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextField(
                        value = editableName,
                        onValueChange = { editableName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Update the project's name when the dialog is dismissed
                    DisposableEffect(Unit) {
                        onDispose {
                            project.name = editableName
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxHeight().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(onClick = {}) {
                            Text("Save")
                        }
                    }

                }
            }
        }
    }
}


@Composable
@Preview
fun FavoritesPanel(favorites: MutableList<Project>) {
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

            if (expanded) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(favorites) { project ->
                        ProjectItem(project, onEdit = { }, onDelete = { })
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun FavoriteItem() {
}

@Composable
@Preview
fun ProjectItem(project: Project, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dp(8F)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(project.name)
        Row {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.ExpandMore, contentDescription = "More")
            }
        }
    }
    AnimatedVisibility(true) {
        Column(modifier = Modifier.padding(start = 16.dp)) {
            project.timers.forEach { timer ->
                TimerItem(timer = timer)
            }
        }
    }
}

@Composable
fun TimerItem(timer: Timer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(timer.startTime.toString(), style = MaterialTheme.typography.body2)
        Text(timer.endTime.toString(), style = MaterialTheme.typography.body2)

        IconButton(onClick = { }) {
            Icon(Icons.Default.PlayCircle, contentDescription = "Start")
        }
        //implementation(libs.androidx.material.icons.extended)
        IconButton(onClick = { }) {
            Icon(Icons.Default.StopCircle, contentDescription = "Stop")
        }
    }
}