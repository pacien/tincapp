package org.pacien.tincapp.ui.netlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import org.pacien.tincapp.R

@Composable
fun UnlockKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
  var passphrase by remember { mutableStateOf("") }
  val formIsComplete = passphrase.isNotEmpty()
  val submit = { onConfirm(passphrase) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.decrypt_key_modal_title)) },
    text = {
      Column {
        OutlinedTextField(
          value = passphrase,
          onValueChange = { passphrase = it },
          label = { Text(stringResource(R.string.decrypt_key_modal_field_passphrase)) },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = {
            if (formIsComplete) submit()
          }),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = submit,
        enabled = formIsComplete
      ) {
        Text(stringResource(R.string.decrypt_key_modal_action_unlock))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.decrypt_key_modal_action_cancel))
      }
    }
  )
}