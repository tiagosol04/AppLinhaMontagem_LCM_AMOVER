package com.example.applinhamontagem.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object IdentifyMoto : Screen("identify")
    object OrdersList : Screen("orders")
    object RegisterVin : Screen("register_vin/{motaId}/{ordemId}") {
        fun createRoute(motaId: Int, ordemId: Int) = "register_vin/$motaId/$ordemId"
    }
    object Assembly : Screen("assembly/{motaId}") {
        fun createRoute(id: Int) = "assembly/$id"
    }
    object PostAssembly : Screen("post_assembly/{motaId}/{ordemId}") {
        fun createRoute(motaId: Int, ordemId: Int) = "post_assembly/$motaId/$ordemId"
    }
    object FinalControl : Screen("final_control/{motaId}/{ordemId}") {
        fun createRoute(motaId: Int, ordemId: Int) = "final_control/$motaId/$ordemId"
    }
    object Packaging : Screen("packaging/{motaId}") {
        fun createRoute(id: Int) = "packaging/$id"
    }
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(id: Int) = "order_detail/$id"
    }
}
