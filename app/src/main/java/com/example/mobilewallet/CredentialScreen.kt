package com.example.mobilewallet

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.w3c.PresentationBuilder
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CredentialModel(context: Application) : AndroidViewModel(context) {
  private val _prefs = context.getSharedPreferences("did", Context.MODE_PRIVATE)

  @OptIn(ExperimentalUuidApi::class)
  suspend fun generatePresentation(credential: String): String {
    val presentation = PresentationBuilder().apply {
      did = "did:example:aaaaaa" //TODO change
      nonce = Uuid.random().toString() //Generate a random string every time to be used as nonce
      addCredential(JsonPrimitive(credential))
    }
    val key = _prefs.getString("key", "") ?: ""
    return presentation.buildAndSign(JWKKey.importJWK(key).getOrNull()!!)
  }
}












@Composable
fun CredentialScreen(credential: String, credentialModel: CredentialModel = viewModel()) {
  val coroutineScope = rememberCoroutineScope()

  //TODO: dialog
  MobileWalletTheme {
    Scaffold { innerPadding ->
      Column(modifier = Modifier
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())) {
        Text(tokenToPayload(credential).toString())
        TextButton(onClick = {
          coroutineScope.launch {

            try {
              credentialModel.generatePresentation(credential)
            } finally {

            }
          }
        }) { Text("Generate presentation") }
      }
    }

  }
}