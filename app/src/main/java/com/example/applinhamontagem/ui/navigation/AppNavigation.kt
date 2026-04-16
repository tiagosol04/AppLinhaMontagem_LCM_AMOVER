package com.example.applinhamontagem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.applinhamontagem.ui.view.*
import com.example.applinhamontagem.ui.viewmodel.ViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val factory = remember { ViewModelFactory() }

    val authVM: com.example.applinhamontagem.ui.viewmodel.AuthViewModel = viewModel(factory = factory)
    val prodVM: com.example.applinhamontagem.ui.viewmodel.ProductionViewModel = viewModel(factory = factory)
    val ordersVM: com.example.applinhamontagem.ui.viewmodel.OrdersViewModel = viewModel(factory = factory)

    NavHost(navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) { LoginScreen(navController, authVM) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController, authVM, prodVM) }
        composable(Screen.IdentifyMoto.route) { IdentifyMotoScreen(navController, prodVM) }

        composable(
            Screen.RegisterVin.route,
            arguments = listOf(
                navArgument("motaId") { type = NavType.IntType },
                navArgument("ordemId") { type = NavType.IntType }
            )
        ) {
            val mid = it.arguments?.getInt("motaId") ?: 0
            val oid = it.arguments?.getInt("ordemId") ?: 0
            RegisterVinScreen(navController, prodVM, mid, oid)
        }

        composable(
            Screen.Assembly.route,
            arguments = listOf(navArgument("motaId") { type = NavType.IntType })
        ) {
            val id = it.arguments?.getInt("motaId") ?: 0
            DynamicAssemblyScreen(navController, prodVM, id)
        }

        composable(
            Screen.PostAssembly.route,
            arguments = listOf(
                navArgument("motaId") { type = NavType.IntType },
                navArgument("ordemId") { type = NavType.IntType }
            )
        ) {
            val mid = it.arguments?.getInt("motaId") ?: 0
            val oid = it.arguments?.getInt("ordemId") ?: 0
            PostAssemblyScreen(navController, prodVM, mid, oid)
        }

        composable(
            Screen.FinalControl.route,
            arguments = listOf(
                navArgument("motaId") { type = NavType.IntType },
                navArgument("ordemId") { type = NavType.IntType }
            )
        ) {
            val mid = it.arguments?.getInt("motaId") ?: 0
            val oid = it.arguments?.getInt("ordemId") ?: 0
            FinalControlScreen(navController, prodVM, mid, oid)
        }

        composable(
            Screen.Packaging.route,
            arguments = listOf(navArgument("motaId") { type = NavType.IntType })
        ) {
            val id = it.arguments?.getInt("motaId") ?: 0
            PackagingScreen(navController, authVM, prodVM, id)
        }

        composable(Screen.OrdersList.route) { OrdersListScreen(navController, ordersVM) }

        composable(
            Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) {
            val oid = it.arguments?.getInt("orderId") ?: 0
            OrderDetailScreen(navController, ordersVM, oid)
        }
    }
}
