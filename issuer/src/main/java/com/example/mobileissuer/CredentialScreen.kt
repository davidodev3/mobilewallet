package com.example.mobileissuer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mobileissuer.ui.theme.MobileWalletTheme

@Composable
fun CredentialScreen(credential: String) {
   val fields : @Composable () -> Unit

   /*Mapping of data to be passed to the generated credential*/
   val mapping = remember { mutableStateMapOf<String, String>() }

   /*Passing a map to generate a credential is the most convenient way
   but since we also need to show text inputs in real time
   we need some support variables to store the data, the map gets populated under the hood*/
   if (credential == "universityDegree") {
      fields = {
         var type by remember {mutableStateOf("")}
         var name by remember {mutableStateOf("")}
         OutlinedTextField(value = type, onValueChange = {v : String ->
            mapping["type"] = v
            type = v
         })
         OutlinedTextField(value = name, onValueChange = {v : String ->
            mapping["name"] = v
            name = v











         })
      }
   }

   else {
      fields = {}
   }
   MobileWalletTheme {
      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
         Column(modifier = Modifier.padding(innerPadding)) {
            Text("University Degree")
            fields()
            ElevatedButton(onClick = {generateCredential(credential, mapping)}) {
               Text("Generate")
            }
         }
      }
   }
}

fun generateCredential(type: String, map: Map<String, String>) {

   if (type == "universityDegree") {

   }

}