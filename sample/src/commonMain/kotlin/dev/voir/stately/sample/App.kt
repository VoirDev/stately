package dev.voir.stately.sample

import StatelyActionExampleScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.voir.stately.sample.examples.StatelyFetchBoundaryExampleScreen
import dev.voir.stately.sample.examples.StatelyFetchContentExampleScreen
import dev.voir.stately.sample.examples.StatelyFetchExampleScreen

sealed class AppScreen(val route: String) {
    object Menu : AppScreen("menu")
    object FetchExample : AppScreen("fetch")
    object ActionExample : AppScreen("action")
    object FetchContentExample : AppScreen("fetch_content")
    object FetchBoundaryExample : AppScreen("fetch_boundary")
}

@Composable
fun App() {
    val navController = rememberNavController()

    MaterialTheme {
        NavHost(navController = navController, startDestination = AppScreen.Menu.route) {
            composable(AppScreen.Menu.route) {
                MenuScreen(navController)
            }
            composable(AppScreen.FetchExample.route) {
                StatelyFetchExampleScreen()
            }
            composable(AppScreen.ActionExample.route) {
                StatelyActionExampleScreen()
            }
            composable(AppScreen.FetchContentExample.route) {
                StatelyFetchContentExampleScreen()
            }
            composable(AppScreen.FetchBoundaryExample.route) {
                StatelyFetchBoundaryExampleScreen()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Stately Examples") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Choose an example", style = MaterialTheme.typography.titleLarge)

            Button(onClick = { navController.navigate(AppScreen.FetchExample.route) }) {
                Text("Stately Fetch")
            }

            Button(onClick = { navController.navigate(AppScreen.ActionExample.route) }) {
                Text("Stately Action")
            }

            Button(onClick = { navController.navigate(AppScreen.FetchContentExample.route) }) {
                Text("Fetch with StatelyFetchContent")
            }

            Button(onClick = { navController.navigate(AppScreen.FetchBoundaryExample.route) }) {
                Text("Fetch with StatelyFetchBoundary")
            }
        }
    }
}
