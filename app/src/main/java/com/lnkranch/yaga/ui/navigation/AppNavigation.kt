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
import com.lnkranch.yaga.domain.DrillMode
import com.lnkranch.yaga.ui.screen.DrillScreen
import com.lnkranch.yaga.ui.screen.ProgressionBuilderScreen
import com.lnkranch.yaga.ui.screen.SetupScreen
import com.lnkranch.yaga.ui.screen.SummaryScreen
import com.lnkranch.yaga.ui.viewmodel.DrillViewModel
import com.lnkranch.yaga.ui.viewmodel.ProgressionBuilderViewModel
import com.lnkranch.yaga.ui.viewmodel.SetupViewModel
import com.lnkranch.yaga.ui.viewmodel.SummaryViewModel

@Composable
fun AppNavigation(app: DrillApplication) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "setup") {
        composable("setup") {
            val vm: SetupViewModel = viewModel(factory = SetupViewModel.Factory(app.repository))
            SetupScreen(
                vm = vm,
                onStartDrill = { progressionId, tonicName, drillMode ->
                    navController.navigate("drill/$progressionId/${Uri.encode(tonicName)}/${drillMode.name}")
                },
                onBuildProgression = { navController.navigate("builder") },
            )
        }

        composable(
            route = "drill/{progressionId}/{tonicName}/{drillMode}",
            arguments = listOf(
                navArgument("progressionId") { type = NavType.LongType },
                navArgument("tonicName") { type = NavType.StringType },
                navArgument("drillMode") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val progressionId = backStackEntry.arguments!!.getLong("progressionId")
            val tonicName = backStackEntry.arguments!!.getString("tonicName")!!
            val drillMode = DrillMode.valueOf(backStackEntry.arguments!!.getString("drillMode")!!)
            val vm: DrillViewModel = viewModel(
                factory = DrillViewModel.Factory(app.repository, progressionId, tonicName, drillMode),
            )
            DrillScreen(
                vm = vm,
                onSessionComplete = { complete ->
                    app.pendingSummary = complete
                    navController.navigate("summary") {
                        popUpTo("setup") { inclusive = false }
                    }
                },
            )
        }

        composable("summary") {
            val vm: SummaryViewModel = viewModel(factory = SummaryViewModel.Factory(app))
            SummaryScreen(
                vm = vm,
                onPlayAgain = { navController.popBackStack("setup", false) },
                onBackToSetup = { navController.popBackStack("setup", false) },
            )
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
    }
}
