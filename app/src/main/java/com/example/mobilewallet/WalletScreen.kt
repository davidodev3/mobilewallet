package com.example.mobilewallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.credentials.DigitalCredential
import androidx.credentials.ExperimentalDigitalCredentialApi
import androidx.credentials.registry.provider.RegisterCredentialsRequest
import androidx.credentials.registry.provider.RegistryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(name: String) {
  val context = LocalContext.current
  MobileWalletTheme {
    Scaffold(modifier = Modifier.fillMaxSize(), floatingActionButton = {











      AddButton(onClick = {})
    }) { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {
        WalletName(name)
        DocumentList(
          documentModel = viewModel(
            factory = DocumentModelFactory(
              context.applicationContext as Application, name
            )
          )
        )
      }
    }
  }
}

class DocumentModel(val name: String, private val application: Application) : AndroidViewModel(application),
  SharedPreferences.OnSharedPreferenceChangeListener {
  private val _walletPrefs = (application.getSharedPreferences("wallets", Context.MODE_PRIVATE))

  private val _documents = MutableStateFlow(
    _walletPrefs.getStringSet(name, mutableSetOf<String>())?.toMutableList()
      ?: mutableListOf<String>()
  )
  val documents = _documents.asStateFlow()

  init {
    _walletPrefs.registerOnSharedPreferenceChangeListener(this)
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
    _documents.value =
      sharedPreferences.getStringSet(key, mutableSetOf<String>())?.toMutableList()
        ?: mutableListOf<String>()
  }

  fun removeDocument(id: String) {
    val doc = _documents.value.toMutableSet()
    //Remove the element from the new list
    doc.remove(id)
    with(_walletPrefs.edit()) {
      //Update preferences with new list
      putStringSet(name, doc)

      commit()
    }
  }

  @OptIn(ExperimentalDigitalCredentialApi::class)
  fun addDocument(jwt: String) {

    //TODO: Decode JWT and take document id
    val registryManager = RegistryManager.create(application)

    try {
      //This gets executed in a background coroutine with no return value.
      viewModelScope.launch {
        /*Explanation: the API is experimental and does not manage data storage for now.
        So we need to register the credential in the "RegistryManager" and store the actual document somewhere else.
        Also data is treated as "opaque blobs" (binary large objects) so everything has to be binary data.*/
        registryManager.registerCredentials(request = object : RegisterCredentialsRequest(
          DigitalCredential.TYPE_DIGITAL_CREDENTIAL,
          "document",
          jwt.encodeToByteArray(),
          readBinary("openidvp.wasm", application) //Matcher provided by Google on their repository
        ) {})
      }
    } finally {
      /*When the request has completed we save the credential in a relational db fashion with SharedPreferences.
      SQLite was definitely an option to store the actual JSON data (decoded from the issued JWT),
      but probably we need to work with JWTs more so using SharedPreferences to store strings seems more efficient.*/
      val doc = _documents.value.toMutableSet()
      doc.add("document")

      with (_walletPrefs.edit()) {
        putStringSet(name, doc)
        commit()
      }
      val docPrefs = application.getSharedPreferences("documents", Context.MODE_PRIVATE)
      with (docPrefs.edit()) {
        putString("document", jwt)
        commit()
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    _walletPrefs.unregisterOnSharedPreferenceChangeListener(this)











  }
}

//A factory is needed for the viewmodel because extra parameters are needed.
class DocumentModelFactory(private val application: Application, private val name: String) :
  ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
    @Suppress("UNCHECKED_CAST") return DocumentModel(name, application) as T
  }
}

@Composable
fun DocumentList(documentModel: DocumentModel) {

  val documents by documentModel.documents.collectAsStateWithLifecycle()
  if (documents.isEmpty()) {
    Text("No credentials are stored in this wallet yet.")
  } else {
    LazyColumn {
      items(documents) { document ->
        Text(document)
      }
    }

  }
}

@Composable
fun WalletName(name: String) {
  Text(name, fontSize = TextUnit(38.0f,           TextUnitType.Sp))
}

fun readBinary(filename: String, application: Application) : ByteArray {

  val input = application.assets.open(filename)
  val binary = ByteArray(input.available())
  input.read(binary)
  input.close()
  return binary

}