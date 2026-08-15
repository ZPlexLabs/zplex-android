package zechs.zplex.feature_settings.kids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun KidsModeSetupRoute(
    onEnabled: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KidsModeSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                KidsModeSetupEvent.Enabled -> onEnabled()
            }
        }
    }
    KidsModeSetupScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsModeSetupScreen(
    state: KidsModeSetupState,
    onAction: (KidsModeSetupAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Kids mode") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Kids mode simplifies navigation to Home, Movies, and Shows. " +
                    "Content is already limited to this profile's rating ceiling. " +
                    "A PIN is required to exit.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { onAction(KidsModeSetupAction.RequestEnable) }) {
                Text("Enable kids mode")
            }
        }
    }

    if (state.showSetPinDialog) {
        AlertDialog(
            onDismissRequest = { onAction(KidsModeSetupAction.DismissSetPinDialog) },
            title = { Text("Set an exit PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.pin,
                        onValueChange = { onAction(KidsModeSetupAction.PinChanged(it)) },
                        label = { Text("4-digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.confirmPin,
                        onValueChange = { onAction(KidsModeSetupAction.ConfirmPinChanged(it)) },
                        label = { Text("Confirm PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.error != null) {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onAction(KidsModeSetupAction.ConfirmSetPin) }) { Text("Set PIN") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(KidsModeSetupAction.DismissSetPinDialog) }) { Text("Cancel") }
            }
        )
    }
}
