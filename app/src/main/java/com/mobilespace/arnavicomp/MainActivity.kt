package com.mobilespace.arnavicomp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobilespace.arnavicomp.ar.ArActivity
import com.mobilespace.arnavicomp.ui.MapScreen
import com.mobilespace.arnavicomp.ui.theme.ARNaviCompTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ARNaviCompTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onNavigateToAR = {
                            startActivity(Intent(this, ArActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(onNavigateToAR: () -> Unit) {
    var showMap by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (showMap) {
            MapScreen(
                modifier = Modifier.weight(1f),
                onNavigationReady = { /* Handle navigation ready */ }
            )
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Map view disabled")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showMap = !showMap },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text(if (showMap) "Hide Map" else "Show Map")
            }

            Button(
                onClick = onNavigateToAR,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Start AR Navigation")
            }
        }
    }
}
