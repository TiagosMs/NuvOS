package com.decklauncher

import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import com.decklauncher.model.LaunchRoute
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchRouteTest {

    private val emulatorApp = AppItem(
        id = "app_ppsspp",
        label = "PPSSPP",
        packageName = "org.ppsspp.ppsspp",
        category = AppCategory.EMULADORES,
        itemType = ItemType.APP
    )

    private val nativeGame = AppItem(
        id = "app_deadcells",
        label = "Dead Cells",
        packageName = "com.playdigious.deadcells.curse",
        category = AppCategory.JOGOS,
        itemType = ItemType.APP
    )

    private val romGame = AppItem(
        id = "rom_mario",
        label = "Super Mario",
        packageName = "me.magnum.melonds",
        category = AppCategory.JOGOS,
        itemType = ItemType.ROM
    )

    @Test
    fun `when tapping emulator on carousel, routes to OpenEmulatorDialog`() {
        val route = LaunchRoute.resolve(emulatorApp, isFromEmulatorDialog = false)
        assertTrue(route is LaunchRoute.OpenEmulatorDialog)
    }

    @Test
    fun `when clicking Abrir App do Emulador inside dialog, NEVER routes to OpenEmulatorDialog`() {
        val route = LaunchRoute.resolve(emulatorApp, isFromEmulatorDialog = true)
        assertTrue("Deve abrir o emulador diretamente sem reabrir o diálogo", route is LaunchRoute.LaunchNow)
    }

    @Test
    fun `when launching normal app or rom, routes to LaunchNow`() {
        val routeGame = LaunchRoute.resolve(nativeGame, isFromEmulatorDialog = false)
        val routeRom = LaunchRoute.resolve(romGame, isFromEmulatorDialog = false)
        assertTrue(routeGame is LaunchRoute.LaunchNow)
        assertTrue(routeRom is LaunchRoute.LaunchNow)
    }
}
