package com.apk.railone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apk.railone.data.StationRepository
import com.apk.railone.ui.components.StationSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: StationRepository,
    savedSourceCode: String?,
    savedDestinationCode: String?,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToResult: (String, String) -> Unit
) {
    // ─── State ───────────────────────────────────────────────────────────────
    var selectedSourceCode by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedDestinationCode by rememberSaveable { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Sync incoming saved state from search screen
    LaunchedEffect(savedSourceCode) {
        savedSourceCode?.let { selectedSourceCode = it }
    }
    LaunchedEffect(savedDestinationCode) {
        savedDestinationCode?.let { selectedDestinationCode = it }
    }

    // Derive Station objects from codes
    val selectedSource = remember(selectedSourceCode) {
        repository.stations.find { it.stationCode == selectedSourceCode }
    }
    val selectedDestination = remember(selectedDestinationCode) {
        repository.stations.find { it.stationCode == selectedDestinationCode }
    }

    // ─── UI ──────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Train,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Railway Distance",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(36.dp))

            // ── Title ────────────────────────────────────────────────────────
            Text(
                text = "Find Distance Between\nRailway Stations",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select source and destination to calculate",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Selection Card ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                        .copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Source
                    StationSelector(
                        label = "SOURCE STATION",
                        selectedStation = selectedSource,
                        onClick = {
                            showError = false
                            onNavigateToSearch("source")
                        }
                    )

                    // Swap Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        SmallFloatingActionButton(
                            onClick = {
                                val temp = selectedSourceCode
                                selectedSourceCode = selectedDestinationCode
                                selectedDestinationCode = temp
                                showError = false
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap Stations",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Destination
                    StationSelector(
                        label = "DESTINATION STATION",
                        selectedStation = selectedDestination,
                        onClick = {
                            showError = false
                            onNavigateToSearch("destination")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Error Message ────────────────────────────────────────────────
            AnimatedErrorMessage(
                showError = showError,
                errorMessage = errorMessage
            )

            if (showError) Spacer(modifier = Modifier.height(16.dp))

            // ── Calculate Button ─────────────────────────────────────────────
            Button(
                onClick = {
                    when {
                        selectedSource == null && selectedDestination == null -> {
                            showError = true
                            errorMessage =
                                "Please select both Source and Destination stations"
                        }
                        selectedSource == null -> {
                            showError = true
                            errorMessage = "Please select a Source station"
                        }
                        selectedDestination == null -> {
                            showError = true
                            errorMessage = "Please select a Destination station"
                        }
                        selectedSource.stationCode == selectedDestination.stationCode -> {
                            showError = true
                            errorMessage =
                                "Source and Destination cannot be the same station"
                        }
                        else -> {
                            showError = false
                            onNavigateToResult(
                                selectedSource.stationCode,
                                selectedDestination.stationCode
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Train,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Calculate Distance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Reset Button ─────────────────────────────────────────────────
            if (selectedSource != null || selectedDestination != null) {
                TextButton(
                    onClick = {
                        selectedSourceCode = null
                        selectedDestinationCode = null
                        showError = false
                    }
                ) {
                    Text(
                        text = "Reset Selection",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Animated Error Card ──────────────────────────────────────────────────────
@Composable
private fun AnimatedErrorMessage(
    showError: Boolean,
    errorMessage: String
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = showError,
        enter = androidx.compose.animation.fadeIn() +
                androidx.compose.animation.expandVertically(),
        exit = androidx.compose.animation.fadeOut() +
                androidx.compose.animation.shrinkVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Train,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}