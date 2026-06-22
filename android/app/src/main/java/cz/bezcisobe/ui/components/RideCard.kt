package cz.bezcisobe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.bezcisobe.R
import cz.bezcisobe.data.repository.Ride
import cz.bezcisobe.data.repository.RideType

@Composable
fun RideCard(
    ride: Ride,
    isLoggedIn: Boolean,
    isOwner: Boolean,
    isPassenger: Boolean,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            if (ride.type == RideType.OFFER) stringResource(R.string.ride_type_offer)
                            else stringResource(R.string.ride_type_request)
                        )
                    },
                )
                Spacer(Modifier.weight(1f))
                Text(ride.displayName, style = MaterialTheme.typography.labelLarge)
            }

            val route = if (ride.to.isNullOrBlank()) ride.from
            else stringResource(R.string.ride_route, ride.from, ride.to)
            Text(route, style = MaterialTheme.typography.titleMedium)

            if (ride.type == RideType.OFFER) {
                val seats = if (ride.freeSeats > 0)
                    stringResource(R.string.ride_seats_free, ride.freeSeats)
                else stringResource(R.string.ride_full)
                Text(
                    "$seats • " + stringResource(R.string.ride_seats_taken, ride.occupiedSeats, ride.availableSeats),
                    style = MaterialTheme.typography.bodyMedium,
                )
                ride.car?.takeIf { it.isNotBlank() }?.let {
                    Text(stringResource(R.string.ride_car, it), style = MaterialTheme.typography.bodyMedium)
                }
            }

            ride.notes?.takeIf { it.isNotBlank() }?.let {
                Text(stringResource(R.string.ride_notes, it), style = MaterialTheme.typography.bodySmall)
            }

            if (isLoggedIn) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when {
                        isOwner -> OutlinedButton(onClick = onDelete) {
                            Text(stringResource(R.string.ride_delete))
                        }
                        ride.type == RideType.OFFER && isPassenger -> OutlinedButton(onClick = onCancel) {
                            Text(stringResource(R.string.ride_cancel))
                        }
                        ride.type == RideType.OFFER && !ride.isFull -> Button(onClick = onAccept) {
                            Text(stringResource(R.string.ride_accept))
                        }
                    }
                }
            }
        }
    }
}
