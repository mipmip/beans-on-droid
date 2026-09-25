package io.github.mipmip.beansondroid.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import io.github.mipmip.beansondroid.ui.screen.BeanDetailScreen
import io.github.mipmip.beansondroid.ui.screen.BeanListScreen
import io.github.mipmip.beansondroid.ui.screen.RepoScreen
import io.github.mipmip.beansondroid.viewmodel.AppViewModel

object Routes {
    const val BEANS = "beans"
    const val REPOS = "repos"
    const val DETAIL = "bean/{id}"

    fun detail(id: String) = "bean/$id"
}

@Composable
fun BeansNavHost(
    viewModel: AppViewModel,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.BEANS) {
        composable(Routes.BEANS) {
            BeanListScreen(
                viewModel = viewModel,
                onOpenBean = { navController.navigate(Routes.detail(it)) },
                onOpenRepos = { navController.navigate(Routes.REPOS) },
            )
        }
        composable(Routes.REPOS) {
            RepoScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            BeanDetailScreen(
                viewModel = viewModel,
                beanId = entry.arguments?.getString("id").orEmpty(),
                onOpenBean = { navController.navigate(Routes.detail(it)) },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
