package com.apk.railone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember          // ✅ ADD this import
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apk.railone.data.StationRepository
import com.apk.railone.ui.screens.DistanceResultScreen
import com.apk.railone.ui.screens.FareScreen
import com.apk.railone.ui.screens.HomeScreen
import com.apk.railone.ui.screens.StationSearchScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object StationSearch : Screen("station_search/{selectionType}") {
        fun createRoute(selectionType: String) = "station_search/$selectionType"
    }
    object DistanceResult : Screen(
        "distance_result/{sourceCode}/{destinationCode}"
    ) {
        fun createRoute(sourceCode: String, destinationCode: String) =
            "distance_result/$sourceCode/$destinationCode"
    }
    object Fare : Screen(
        "fare/{sourceCode}/{destinationCode}/{fareType}"
    ) {
        fun createRoute(sourceCode: String, destinationCode: String, fareType: String) =
            "fare/$sourceCode/$destinationCode/$fareType"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ✅ FIX: Wrap with remember so it's not recreated every recomposition
    val repository = remember { StationRepository(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { backStackEntry ->

            val savedSource = backStackEntry.savedStateHandle
                .getStateFlow<String?>("source", null)
                .collectAsState()
            val savedDestination = backStackEntry.savedStateHandle
                .getStateFlow<String?>("destination", null)
                .collectAsState()

            HomeScreen(
                repository = repository,
                savedSourceCode = savedSource.value,
                savedDestinationCode = savedDestination.value,
                onNavigateToSearch = { selectionType ->
                    navController.navigate(
                        Screen.StationSearch.createRoute(selectionType)
                    )
                },
                onNavigateToResult = { sourceCode, destinationCode ->
                    navController.navigate(
                        Screen.DistanceResult.createRoute(sourceCode, destinationCode)
                    )
                }
            )
        }

        composable(
            route = Screen.StationSearch.route,
            arguments = listOf(
                navArgument("selectionType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val selectionType = backStackEntry.arguments
                ?.getString("selectionType") ?: "source"

            StationSearchScreen(
                repository = repository,
                selectionType = selectionType,
                onStationSelected = { station ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(selectionType, station.stationCode)
                    navController.popBackStack()
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.DistanceResult.route,
            arguments = listOf(
                navArgument("sourceCode") { type = NavType.StringType },
                navArgument("destinationCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceCode = backStackEntry.arguments
                ?.getString("sourceCode") ?: ""
            val destinationCode = backStackEntry.arguments
                ?.getString("destinationCode") ?: ""

            DistanceResultScreen(
                repository = repository,
                sourceCode = sourceCode,
                destinationCode = destinationCode,
                onNavigateToFare = { fareType ->
                    navController.navigate(
                        Screen.Fare.createRoute(sourceCode, destinationCode, fareType)
                    )
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Fare.route,
            arguments = listOf(
                navArgument("sourceCode") { type = NavType.StringType },
                navArgument("destinationCode") { type = NavType.StringType },
                navArgument("fareType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceCode = backStackEntry.arguments
                ?.getString("sourceCode") ?: ""
            val destinationCode = backStackEntry.arguments
                ?.getString("destinationCode") ?: ""
            val fareType = backStackEntry.arguments
                ?.getString("fareType") ?: "Local"

            FareScreen(
                repository = repository,
                sourceCode = sourceCode,
                destinationCode = destinationCode,
                fareType = fareType,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
