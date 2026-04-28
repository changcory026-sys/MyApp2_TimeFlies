package com.jetpackcomposeexecise.timeflies.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jetpackcomposeexecise.timeflies.ui.screen.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeScreenRoute

@Serializable
object ManagerStoneScreenRoute

@Serializable
data class TimeStatisticsScreenRoute(val currentDate: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDailyDishApp() {
    val navController = rememberNavController()


    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ){ innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeScreenRoute
        ) {
            composable<HomeScreenRoute> {
                HomeScreen(
                    modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
                )
            }
        }
    }
}