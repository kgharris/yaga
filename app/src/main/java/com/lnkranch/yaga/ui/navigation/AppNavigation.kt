package com.lnkranch.yaga.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lnkranch.yaga.DrillApplication
import com.lnkranch.yaga.domain.DrillInputMode
import com.lnkranch.yaga.domain.DrillMode
import com.lnkranch.yaga.ui.screen.DrillScreen
import com.lnkranch.yaga.ui.screen.HeatmapScreen
import com.lnkranch.yaga.ui.screen.HomeScreen
import com.lnkranch.yaga.ui.screen.ProgressionBuilderScreen
import com.lnkranch.yaga.ui.screen.SetupScreen
import com.lnkranch.yaga.ui.screen.SettingsScreen
import com.lnkranch.yaga.ui.screen.SummaryScreen
import com.lnkranch.yaga.ui.viewmodel.DrillViewModel
import com.lnkranch.yaga.ui.viewmodel.HeatmapViewModel
import com.lnkranch.yaga.ui.viewmodel.ProgressionBuilderViewModel
import com.lnkranch.yaga.ui.viewmodel.SetupViewModel
import com.lnkranch.yaga.ui.viewmodel.SettingsViewModel
import com.lnkranch.yaga.ui.viewmodel.SummaryViewModel

@Composable
fun AppNavigation(app: DrillApplication) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartChordDrill = { navController.navigate("setup") },
                onOpenHeatmap = { navController.navigate("heatmap") },
                onBuildProgression = { navController.navigate("builder") },
                onOpenSettings = { navController.navigate("settings") },
            )
        }

        composable("setup") {
            val vm: SetupViewModel = viewModel(factory = SetupViewModel.Factory(app.repository))
            SetupScreen(
                vm = vm,
                onStartDrill = { progressionId, tonicName, drillMode, inputMode ->
                    navController.navigate(
                        "drill/$progressionId/${Uri.encode(tonicName)}/${drillMode.name}/${inputMode.name}"
                    )
                },
                onBuildProgression = { navController.navigate("builder") },
            )
        }

        composable(
            route = "drill/{progressionId}/{tonicName}/{drillMode}/{inputMode}",
            arguments = listOf(
                navArgument("progressionId") { type = NavType.LongType },
                navArgument("tonicName") { type = NavType.StringType },
                navArgument("drillMode") { type = NavType.StringType },
                navArgument("inputMode") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val progressionId = backStackEntry.arguments!!.getLong("progressionId")
            val tonicName = backStackEntry.arguments!!.getString("tonicName")!!
            val drillMode = DrillMode.valueOf(backStackEntry.arguments!!.getString("drillMode")!!)
            val inputMode = DrillInputMode.valueOf(backStackEntry.arguments!!.getString("inputMode")!!)
            val vm: DrillViewModel = viewModel(
                factory = DrillViewModel.Factory(app.repository, progressionId, tonicName, drillMode, inputMode, app.settingsRepository),
            )
            DrillScreen(
                vm = vm,
                onSessionComplete = { complete ->
                    app.pendingSummary = complete
                    navController.navigate("summary") {
                        popUpTo("home") { inclusive = false }
                    }
                },
            )
        }

        composable("summary") {
            val vm: SummaryViewModel = viewModel(factory = SummaryViewModel.Factory(app))
            SummaryScreen(
                vm = vm,
                onPlayAgain = { navController.popBackStack("home", false) },
                onBackToSetup = { navController.popBackStack("home", false) },
            )
        }

        composable("heatmap") {
            val vm: HeatmapViewModel = viewModel(
                factory = HeatmapViewModel.Factory(app.repository),
            )
            HeatmapScreen(vm = vm)
        }

        composable("builder") {
            val vm: ProgressionBuilderViewModel = viewModel(
                factory = ProgressionBuilderViewModel.Factory(app.repository),
            )
            ProgressionBuilderScreen(
                vm = vm,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable("settings") {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(app.settingsRepository)
            )
            SettingsScreen(vm = vm, onBack = { navController.popBackStack() })
        }
    }
}
