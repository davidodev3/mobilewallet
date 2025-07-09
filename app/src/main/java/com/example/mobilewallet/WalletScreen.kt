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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class DocumentModel(application: Application, val name: String) : AndroidViewModel(application),
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
      doc.remove(id)
      with(_walletPrefs.edit()) {
         putStringSet(name, doc)
         commit()
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
      @Suppress("UNCHECKED_CAST") return DocumentModel(application, name) as T
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