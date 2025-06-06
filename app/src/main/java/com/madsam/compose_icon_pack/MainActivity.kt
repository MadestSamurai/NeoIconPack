package com.madsam.compose_icon_pack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.madsam.compose_icon_pack.screens.AllIconsScreen
import com.madsam.compose_icon_pack.screens.LostIconsScreen
import com.madsam.compose_icon_pack.screens.MatchedIconsScreen
import com.madsam.compose_icon_pack.ui.theme.ComposeIconPackTheme
import com.madsam.compose_icon_pack.util.AppPreferences
import com.madsam.compose_icon_pack.util.ExtraUtil
import com.madsam.compose_icon_pack.util.PkgUtil
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var preferences: AppPreferences
    private var enableStatsModule = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = AppPreferences(this)
        enableStatsModule = resources.getBoolean(R.bool.enable_req_stats_module)

        // 使用lifecycleScope而不是在Compose中使用协程
        setContent {
            ComposeIconPackTheme {
                MainScreen(
                    onShowAbout = { startActivity(Intent(this, AboutActivity::class.java)) },
                    onShowSearch = { startActivity(Intent(this, SearchActivity::class.java)) },
                    onShowWhatsNew = {
                        lifecycleScope.launch {
                            preferences.saveBoolean(
                                "hideLatest" + PkgUtil.getAppVer(this@MainActivity, "%1\$s"),
                                true
                            )
                            startActivity(Intent(this@MainActivity, WhatsNewActivity::class.java))
                        }
                    },
                    onStatsRequest = {
                        if (ExtraUtil.isNetworkConnected(this)) {
                            startActivity(Intent(this, ReqStatsActivity::class.java))
                        }
                    },
                    showStatsModule = enableStatsModule
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    onShowAbout: () -> Unit,
    onShowSearch: () -> Unit,
    onShowWhatsNew: () -> Unit,
    onStatsRequest: () -> Unit,
    showStatsModule: Boolean
) {
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(1) } // 默认选中matched
    var lostIconsCount by remember { mutableStateOf(0) }
    var matchedIconsCount by remember { mutableStateOf(0) }
    var allIconsCount by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("lost") {
                            popUpTo("lost") { inclusive = true }
                        }
                    },
                    icon = { Icon(painterResource(id = R.drawable.ic_nav_lost), null) },
                    label = { Text("${stringResource(R.string.nav_lost)} ($lostIconsCount)") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("matched") {
                            popUpTo("matched") { inclusive = true }
                        }
                    },
                    icon = { Icon(painterResource(id = R.drawable.ic_nav_matched), null) },
                    label = { Text("${stringResource(R.string.nav_matched)} ($matchedIconsCount)") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate("all") {
                            popUpTo("all") { inclusive = true }
                        }
                    },
                    icon = { Icon(painterResource(id = R.drawable.ic_nav_all), null) },
                    label = { Text("${stringResource(R.string.nav_all)} ($allIconsCount)") }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController, startDestination = "matched") {
                composable("lost") {
                    LostIconsScreen(
                        onIconCountUpdated = { count -> lostIconsCount = count },
                        onDoubleClick = if (showStatsModule) onStatsRequest else null
                    )
                }
                composable("matched") {
                    MatchedIconsScreen(
                        onIconCountUpdated = { count -> matchedIconsCount = count }
                    )
                }
                composable("all") {
                    AllIconsScreen(
                        onIconCountUpdated = { count -> allIconsCount = count },
                        onShowAbout = onShowAbout,
                        onShowSearch = onShowSearch,
                        onShowWhatsNew = onShowWhatsNew
                    )
                }
            }
        }
    }
}