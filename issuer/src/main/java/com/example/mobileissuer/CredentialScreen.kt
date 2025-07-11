package com.example.mobileissuer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileissuer.ui.theme.MobileWalletTheme
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.w3c.CredentialBuilder
import id.walt.w3c.vc.vcs.W3CVC

@Composable
fun CredentialScreen(credential: String) {
  val fields: @Composable () -> Unit

  //Mapping of data to be passed to the generated credential
  val mapping = remember { mutableStateMapOf<String, String>() }
  var showDialog by remember {mutableStateOf(false)}
  var content by remember {mutableStateOf("")}

  /*Passing a map to generate a credential is the most convenient way
  but since we also need to show text inputs in real time
  we need some support variables to store the data, the map gets populated under the hood*/
  if (credential == "universityDegree") {
    fields = {
      var type by remember { mutableStateOf("") }
      var name by remember { mutableStateOf("") }
      OutlinedTextField(value = type, onValueChange = { v: String ->


        mapping["type"] = v
        type = v
      })
      OutlinedTextField(value = name, onValueChange = { v: String ->
        mapping["name"] = v
        name = v
      })
    }
  } else {
    fields = {}
  }

  if (showDialog) {
    CredentialDialog(content) {showDialog = false}
  }

  MobileWalletTheme {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {
        Text("University Degree")
        fields()
        ElevatedButton(onClick = {
          content = generateCredential(credential, mapping, "did:example:xyz")
          showDialog = true
        }) {
          Text("Generate")
        }
      }
    }
  }
}

fun generateCredential(type: String, map: Map<String, String>, issuer: String) : String {
  //TODO: Remake
  var credential: W3CVC = W3CVC(mutableMapOf())
  if (type == "universityDegree") {
    credential = CredentialBuilder().apply {
      addContext("https://www.w3.org/2018/credentials/examples/v1")
      addType("UniversityDegree")
      issuerDid = issuer
      validFromNow()
      subjectDid = "<subject>"
      useCredentialSubject(map.toJsonObject())
    }.buildW3C()
  }
  return credential.toPrettyJson()
}

@Composable
fun CredentialDialog(content: String, onDismissRequest: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(text = "Issued credential:") },
    dismissButton = {
      TextButton(onClick = onDismissRequest) { Text("Dismiss") }
    },
    confirmButton = {
      TextButton(onClick = {
        onDismissRequest()
      }) { Text("Confirm") }
    },
    text = {
      SelectionContainer {
        Text(content)
      }
    },






  )
}