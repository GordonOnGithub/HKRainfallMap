package com.gordon.hkrainfallmap

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.intellij.lang.annotations.JdkConstants.FontStyle

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel, context: Context, navigationController: NavHostController){

    @Composable
    fun appInfoDialog(){
        AlertDialog(onDismissRequest = {
            settingsViewModel.showAppInfo.postValue(false)
        },
            title = {
                Text(context.getString(R.string.setting_about_app))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    for (info in settingsViewModel.getAppInfoArray()) {

                        Text(text = info.first, modifier = Modifier.clickable {
                            settingsViewModel.openURLExternally(info.second)
                        }, color = if (info.second != null) Color.Blue else Color.Black,
                            style = if (info.second != null) TextStyle(textDecoration = TextDecoration.Underline) else TextStyle.Default )

                    }

                }
            },
            confirmButton = {
                Button(onClick = {
                    settingsViewModel.showAppInfo.postValue(false)
                }) {
                    Text("OK")
                }
            }
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()) {

        Button(onClick = {
            settingsViewModel.onRateThisAppClicked()
        }, modifier = Modifier.padding(10.dp)) {
            Text(text = context.getString(R.string.setting_rate_app))
        }

        Button(onClick = {
            settingsViewModel.onAboutThisAppClicked()

        }, modifier = Modifier.padding(10.dp)) {
            Text(text = context.getString(R.string.setting_about_app))
        }

        Button(onClick = {
            navigationController.navigate(NavigationRoutes.map)
        }, modifier = Modifier.padding(10.dp)) {
            Text(text = context.getString(R.string.back_button))
        }
    }


    val showAppInfo: Boolean by settingsViewModel.showAppInfo.observeAsState(
        false
    )

    if (showAppInfo) {
        appInfoDialog()
    }

}