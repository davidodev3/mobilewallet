package com.example.mobilewallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController


import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
class Wallet(val name: String)
@Serializable
object Home

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()
      setContent {
         MyHost()
      }
   }
}

@Composable
fun MyHost(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
   NavHost(modifier = modifier, navController = navController, startDestination = Home) {
      composable<Home> {
         MainScreen {name ->
            navController.navigate(Wallet(name))
         }
      }
      composable<Wallet> { bsEntry ->
         val wallet: Wallet = bsEntry.toRoute()
         WalletScreen(wallet.name)
      }
   }
}

@Composable
fun MainScreen(onWalletClick: (String) -> Unit) {
   MobileWalletTheme {
      var showDialog by remember { mutableStateOf(false) }
      Scaffold(modifier = Modifier.fillMaxSize(),
         floatingActionButton = {
            AddButton(onClick = {
               showDialog = true
            })
         }) { innerPadding ->
         if (showDialog) {
            AddWalletDialog(onDismissRequest = { showDialog = false })
         }
         Column() {
            Greeting(
               name = "Android", modifier = Modifier.padding(innerPadding)
            )
            Subtitle()
            ListColumn(onClick = onWalletClick)
         }
      }
   }
}

class WalletModel(application: Application) : AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {
   private val _walletPrefs = (application.getSharedPreferences("wallets", Context.MODE_PRIVATE))

   private val _wallets = MutableStateFlow(_walletPrefs.all.keys.toMutableList())
   val wallets = _wallets.asStateFlow()

   init {
      _walletPrefs.registerOnSharedPreferenceChangeListener(this)
   }

   override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
      _wallets.value = sharedPreferences.all.keys.toMutableList()
   }

   fun addWallet(name: String) {
      with(_walletPrefs.edit()) {
         putStringSet(name, mutableSetOf())
         commit()
      }
   }

   fun removeWallet(name: String) {
      with(_walletPrefs.edit()) {
         remove(name)
         commit()
      }
   }

   override fun onCleared() {
      super.onCleared()
      _walletPrefs.unregisterOnSharedPreferenceChangeListener(this)
   }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
   Text(
      text = "Hello $name!", modifier = modifier, fontSize = TextUnit(38.0f, TextUnitType.Sp)
   )
}

@Composable
fun Subtitle() {
   Text(text = "Your wallets", fontSize = TextUnit(30.0f, TextUnitType.Sp))
}

@Composable
fun AddButton(onClick: () -> Unit) {
   FloatingActionButton(onClick = onClick) {
      Icon(
         Icons.Filled.Add, "Add new wallet or digital credential"
      )
   }
}

@Composable
fun CardWallet(name: String, onClick: (String) -> Unit, delete: () -> Unit) {
   Card(
      modifier = Modifier
         .fillMaxWidth()
         .padding(16.00.dp)
         .height(100.0.dp)
         .clickable {onClick(name)}
   ) {
      Row() {
         Text(name, Modifier.padding(16.00.dp))
         IconButton(onClick = delete) {
            Icon(
               Icons.Filled.Delete, "Delete selected wallet"
            )
         }
      }
   }
}

@Composable
fun ListColumn(walletModel: WalletModel = viewModel(), onClick: (String) -> Unit) {
   val wallets by walletModel.wallets.collectAsStateWithLifecycle()
   if (wallets.isEmpty()) {
      Text("You don't have any wallets yet.")
   }
   else {
      LazyColumn {
         items(wallets) { name ->
            CardWallet(name, onClick) {
               walletModel.removeWallet(name)
            }
         }
      }
   }
}

@Composable
fun AddWalletDialog(walletModel: WalletModel = viewModel(), onDismissRequest: () -> Unit) {
   var value by remember { mutableStateOf("") }
   AlertDialog(
      onDismissRequest = onDismissRequest,
      title = { Text(text = "Add wallet") },
      dismissButton = {
         TextButton(onClick = onDismissRequest) { Text("Dismiss") }
      },
      confirmButton = {
         TextButton(onClick = {
            walletModel.addWallet(value)
            onDismissRequest()
         }) { Text("Confirm") }
      },
      text = {
         OutlinedTextField(value = value, onValueChange = { v -> value = v }
         )
      },
   )
}