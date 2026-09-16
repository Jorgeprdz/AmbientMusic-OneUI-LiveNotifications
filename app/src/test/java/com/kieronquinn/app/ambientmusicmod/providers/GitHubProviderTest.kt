package com.kieronquinn.app.ambientmusicmod.providers

import org.junit.Assert.assertEquals
import org.junit.Test

class GitHubProviderTest {

    @Test
    fun `AMM derivative base URL uses Jorgeprdz repo`() {
        assertEquals(
            "https://api.github.com/repos/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications/",
            GitHubProvider.getGitHubBaseUrl(
                "Jorgeprdz",
                "AmbientMusic-OneUI-LiveNotifications"
            )
        )
    }

    @Test
    fun `PAM base URL remains KieronQuinn NowPlaying`() {
        assertEquals(
            "https://api.github.com/repos/KieronQuinn/NowPlaying/",
            GitHubProvider.getGitHubBaseUrl("KieronQuinn", "NowPlaying")
        )
    }
}
