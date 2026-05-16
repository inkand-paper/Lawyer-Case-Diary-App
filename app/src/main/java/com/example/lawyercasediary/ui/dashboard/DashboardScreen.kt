package com.example.lawyercasediary.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lawyercasediary.models.Case

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onAddCase: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lawyer Case Diary") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCase) {
                Text("+")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.cases) { case ->
                            CaseItem(case)
                        }
                    }
                }
                is DashboardUiState.Empty -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No cases found", style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = { viewModel.loadData() }) {
                            Text("Refresh Dashboard")
                        }
                    }
                }
                is DashboardUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadData() }) {
                            Text("Retry Synchronization")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseList(cases: List<Case>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(cases) { legalCase ->
            CaseItem(legalCase)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CaseItem(legalCase: Case) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = legalCase.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Case No: ${legalCase.caseNumber}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Court: ${legalCase.courtName}", style = MaterialTheme.typography.bodySmall)
            
            Badge(
                containerColor = if (legalCase.status == "ACTIVE") 
                    MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(legalCase.status)
            }
        }
    }
}
