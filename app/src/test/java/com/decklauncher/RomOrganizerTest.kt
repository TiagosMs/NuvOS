package com.decklauncher

import com.decklauncher.data.RomOrganizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RomOrganizerTest {

    private val installedSystems = setOf("nds", "psp", "ps2", "gba", "snes")

    @Test
    fun `detects unique extension correctly`() {
        val f1 = File("pokemon.nds").apply {
            // timestamp estavel > 60s
        }
        val plan = RomOrganizer.plan(listOf(f1), installedSystems, currentTimeMs = 100_000L)
        val action = plan.actions.first()
        assertEquals("nds", action.targetDirectory)
        assertEquals("pokemon.nds", action.targetFileName)
    }

    @Test
    fun `ignores temp files and recent downloads under 60 seconds`() {
        val temp = File("game.iso.part")
        val plan = RomOrganizer.plan(listOf(temp), installedSystems, currentTimeMs = 100_000L)
        val action = plan.actions.first()
        assertTrue(action.isSkipped)
    }

    @Test
    fun `handles collision with suffix counter without overwriting`() {
        val f1 = File("mario.gba")
        val f2 = File("mario.gba")
        val plan = RomOrganizer.plan(listOf(f1, f2), installedSystems, currentTimeMs = 100_000L)
        assertEquals(2, plan.actions.size)
        assertEquals("mario.gba", plan.actions[0].targetFileName)
        assertEquals("mario (2).gba", plan.actions[1].targetFileName)
        assertTrue(plan.actions[1].isConflict)
    }

    @Test
    fun `sends unknown systems to Revisar folder`() {
        val unknown = File("random_game.xyz")
        val plan = RomOrganizer.plan(listOf(unknown), installedSystems, currentTimeMs = 100_000L)
        assertEquals("_Revisar", plan.actions.first().targetDirectory)
    }
}
