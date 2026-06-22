package cz.bezcisobe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cz.bezcisobe.R
import cz.bezcisobe.data.repository.RideType

/**
 * Dialog for creating an OFFER or REQUEST. Performs client-side validation mirroring the
 * server rules; [serverError] surfaces a 400 from the backend.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRideDialog(
    isSubmitting: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onSubmit: (type: RideType, from: String, to: String?, car: String?, seats: Int, notes: String?) -> Unit,
) {
    var type by remember { mutableStateOf(RideType.OFFER) }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var car by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("3") }
    var notes by remember { mutableStateOf("") }

    var fromError by remember { mutableStateOf<String?>(null) }
    var carError by remember { mutableStateOf<String?>(null) }
    var seatsError by remember { mutableStateOf<String?>(null) }

    val fromRequired = stringResource(R.string.ride_form_from_required)
    val carRequired = stringResource(R.string.ride_form_car_required)
    val seatsInvalid = stringResource(R.string.ride_form_seats_invalid)

    fun validateAndSubmit() {
        fromError = if (from.isBlank()) fromRequired else null
        if (type == RideType.OFFER) {
            carError = if (car.isBlank()) carRequired else null
            val n = seats.toIntOrNull()
            seatsError = if (n == null || n < 1 || n > 10) seatsInvalid else null
        } else {
            carError = null
            seatsError = null
        }
        if (fromError == null && carError == null && seatsError == null) {
            onSubmit(
                type,
                from,
                to.ifBlank { null },
                if (type == RideType.OFFER) car else null,
                if (type == RideType.OFFER) seats.toInt() else 1,
                notes.ifBlank { null },
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true),
        title = {
            Text(
                if (type == RideType.OFFER) stringResource(R.string.ride_create_offer)
                else stringResource(R.string.ride_create_request)
            )
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Type selector
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = type == RideType.OFFER,
                        onClick = { type = RideType.OFFER },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text(stringResource(R.string.ride_type_offer)) }
                    SegmentedButton(
                        selected = type == RideType.REQUEST,
                        onClick = { type = RideType.REQUEST },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text(stringResource(R.string.ride_type_request)) }
                }

                OutlinedTextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text(stringResource(R.string.ride_form_from)) },
                    isError = fromError != null,
                    supportingText = fromError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text(stringResource(R.string.ride_form_to)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (type == RideType.OFFER) {
                    OutlinedTextField(
                        value = car,
                        onValueChange = { car = it },
                        label = { Text(stringResource(R.string.ride_form_car)) },
                        isError = carError != null,
                        supportingText = carError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = seats,
                        onValueChange = { v -> seats = v.filter { it.isDigit() }.take(2) },
                        label = { Text(stringResource(R.string.ride_form_seats)) },
                        isError = seatsError != null,
                        supportingText = seatsError?.let { { Text(it) } },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.ride_form_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                serverError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { validateAndSubmit() }, enabled = !isSubmitting) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
