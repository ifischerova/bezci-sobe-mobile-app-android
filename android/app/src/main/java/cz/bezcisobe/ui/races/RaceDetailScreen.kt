package cz.bezcisobe.ui.races

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.bezcisobe.R
import cz.bezcisobe.data.repository.Race
import cz.bezcisobe.data.repository.Ride
import cz.bezcisobe.data.repository.RideType
import cz.bezcisobe.ui.components.CreateRideDialog
import cz.bezcisobe.ui.components.EmptyState
import cz.bezcisobe.ui.components.LoadingState
import cz.bezcisobe.ui.components.RideCard

@Composable
fun RaceDetailScreen(viewModel: RaceDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ridesState by viewModel.rides.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val currentUsername by viewModel.currentUsername.collectAsStateWithLifecycle()
    val createError by viewModel.createError.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    when (val s = state) {
        is RaceDetailUiState.Loading -> LoadingState()
        is RaceDetailUiState.NotFound -> EmptyState(stringResource(R.string.race_not_found))
        is RaceDetailUiState.Success -> {
            Scaffold(
                floatingActionButton = {
                    if (isLoggedIn) {
                        ExtendedFloatingActionButton(
                            onClick = { showCreateDialog = true },
                            text = { Text(stringResource(R.string.ride_create)) },
                            icon = {},
                        )
                    }
                },
            ) { padding ->
                RaceDetailContent(
                    race = s.race,
                    ridesState = ridesState,
                    isLoggedIn = isLoggedIn,
                    currentUserId = currentUserId,
                    currentUsername = currentUsername,
                    onAccept = viewModel::acceptRide,
                    onCancel = viewModel::cancelRide,
                    onDeleteRequest = { pendingDeleteId = it },
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateRideDialog(
            isSubmitting = isSubmitting,
            serverError = createError,
            onDismiss = {
                showCreateDialog = false
                viewModel.clearCreateError()
            },
            onSubmit = { type, from, to, car, seats, notes ->
                viewModel.createRide(type, from, to, car, seats, notes) {
                    showCreateDialog = false
                }
            },
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(stringResource(R.string.ride_delete_confirm_title)) },
            text = { Text(stringResource(R.string.ride_delete_confirm_text)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteRide(id)
                    pendingDeleteId = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun RaceDetailContent(
    race: Race,
    ridesState: RidesUiState,
    isLoggedIn: Boolean,
    currentUserId: String?,
    currentUsername: String?,
    onAccept: (String) -> Unit,
    onCancel: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(race.name, style = MaterialTheme.typography.headlineSmall)
            Text("${race.place} • ${race.date}${race.startTime?.let { " $it" } ?: ""}")
            race.trackType?.let { Text(stringResource(R.string.race_type, it)) }
            race.trackLength?.let { Text(stringResource(R.string.race_length, it)) }
            race.web?.let { Text(stringResource(R.string.race_web, it)) }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.rides_title), style = MaterialTheme.typography.titleLarge)
            if (!isLoggedIn) {
                Text(
                    stringResource(R.string.rides_login_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        when (ridesState) {
            is RidesUiState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RidesUiState.Error -> item {
                Text(
                    stringResource(R.string.rides_error),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            is RidesUiState.Success -> {
                val rides = ridesState.rides
                if (rides.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.rides_empty),
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    rideSection(R.string.rides_offers, rides.filter { it.type == RideType.OFFER },
                        isLoggedIn, currentUserId, currentUsername, onAccept, onCancel, onDeleteRequest)
                    rideSection(R.string.rides_requests, rides.filter { it.type == RideType.REQUEST },
                        isLoggedIn, currentUserId, currentUsername, onAccept, onCancel, onDeleteRequest)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.rideSection(
    headerRes: Int,
    rides: List<Ride>,
    isLoggedIn: Boolean,
    currentUserId: String?,
    currentUsername: String?,
    onAccept: (String) -> Unit,
    onCancel: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
) {
    if (rides.isEmpty()) return
    item(key = "header_$headerRes") {
        Spacer(Modifier.height(8.dp))
        Text(stringResource(headerRes), style = MaterialTheme.typography.titleMedium)
    }
    items(rides, key = { it.id }) { ride ->
        val isOwner = currentUserId != null && ride.userId == currentUserId
        val isPassenger = (currentUserId != null && ride.passengers.contains(currentUserId)) ||
            (currentUsername != null && ride.passengers.contains(currentUsername))
        RideCard(
            ride = ride,
            isLoggedIn = isLoggedIn,
            isOwner = isOwner,
            isPassenger = isPassenger,
            onAccept = { onAccept(ride.id) },
            onCancel = { onCancel(ride.id) },
            onDelete = { onDeleteRequest(ride.id) },
        )
    }
}
