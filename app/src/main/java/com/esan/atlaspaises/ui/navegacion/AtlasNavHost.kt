package com.esan.atlaspaises.ui.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.esan.atlaspaises.ui.detalle.DetalleScreen
import com.esan.atlaspaises.ui.explorar.ExplorarScreen

private object Rutas {
    const val EXPLORAR = "explorar"
    const val MIS_PAISES = "mis_paises"
    const val DETALLE = "detalle/{codigo}"
    fun detalle(codigo: String) = "detalle/$codigo"
}

private enum class Pestana(
    val ruta: String,
    val etiqueta: String,
    val iconoSeleccionado: ImageVector,
    val icono: ImageVector
) {
    EXPLORAR(Rutas.EXPLORAR, "Explorar", Icons.Filled.Explore, Icons.Outlined.Explore),
    MIS_PAISES(Rutas.MIS_PAISES, "Mis países", Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks)
}

@Composable
fun AtlasNavHost(navController: NavHostController = rememberNavController()) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val enPestana = Pestana.entries.any { pestana -> destinoActual?.hierarchy?.any { it.route == pestana.ruta } == true }

    Scaffold(
        // Cada pantalla maneja sus propios márgenes del sistema; aquí solo se reserva la barra inferior.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (enPestana) {
                NavigationBar {
                    Pestana.entries.forEach { pestana ->
                        val seleccionada = destinoActual?.hierarchy?.any { it.route == pestana.ruta } == true
                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                navController.navigate(pestana.ruta) {
                                    // Conserva el estado de cada pestaña para no volver a consultar la API al regresar.
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (seleccionada) pestana.iconoSeleccionado else pestana.icono,
                                    contentDescription = null
                                )
                            },
                            label = { Text(pestana.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.EXPLORAR,
            modifier = Modifier.padding(padding)
        ) {
            composable(Rutas.EXPLORAR) {
                ExplorarScreen(onAbrirPais = { navController.navigate(Rutas.detalle(it)) })
            }
            composable(Rutas.MIS_PAISES) {
                PantallaProvisional("Mis países")
            }
            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("codigo") { type = NavType.StringType })
            ) {
                DetalleScreen(onVolver = { navController.popBackStack() })
            }
        }
    }
}

/** Pantalla temporal mientras se implementa la sección real. */
@Composable
private fun PantallaProvisional(titulo: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
    }
}
