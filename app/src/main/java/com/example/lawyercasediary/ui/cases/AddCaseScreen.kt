package com.example.lawyercasediary.ui.cases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaseScreen(viewModel: CaseEditorViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var caseNumber by remember { mutableStateOf("") }
    var courtName by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val clients by viewModel.clients.collectAsState()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is CaseEditorState.Success) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Case Enrollment") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Case Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = caseNumber,
                onValueChange = { caseNumber = it },
                label = { Text("Case Number (e.g. 101/2024)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = courtName,
                onValueChange = { courtName = it },
                label = { Text("Court Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Client Selector
            Box {
                OutlinedTextField(
                    value = clients.find { it.id == selectedClientId }?.name ?: "Select Client",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Associated Client") },
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    clients.forEach { client ->
                        DropdownMenuItem(
                            text = { Text(client.name) },
                            onClick = {
                                selectedClientId = client.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.createCase(title, caseNumber, courtName, selectedClientId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && selectedClientId.isNotBlank() && state !is CaseEditorState.Loading
            ) {
                if (state is CaseEditorState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Enrol Case")
                }
            }
        }
    }
}
