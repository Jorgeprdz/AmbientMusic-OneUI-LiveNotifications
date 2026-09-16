package com.kieronquinn.app.ambientmusicmod.repositories

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateSourceConfigurationTest {

    @Test
    fun `AMM self update source is derivative repository`() {
        assertEquals("Jorgeprdz", UpdatesRepositoryImpl.AMM_GITHUB_OWNER)
        assertEquals(
            "AmbientMusic-OneUI-LiveNotifications",
            UpdatesRepositoryImpl.AMM_GITHUB_REPOSITORY
        )
    }

    @Test
    fun `PAM update source remains upstream NowPlaying`() {
        assertEquals("KieronQuinn", UpdatesRepositoryImpl.PAM_GITHUB_OWNER)
        assertEquals("NowPlaying", UpdatesRepositoryImpl.PAM_GITHUB_REPOSITORY)
    }
}
