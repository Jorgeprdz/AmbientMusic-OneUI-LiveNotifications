package com.kieronquinn.app.ambientmusicmod.ui.screens.updates

import org.junit.Assert.assertEquals
import org.junit.Test

class UpdatesLinkConfigurationTest {

    @Test
    fun `About GitHub opens derivative repository`() {
        assertEquals(
            "https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications",
            UpdatesViewModelImpl.LINK_GITHUB
        )
    }

    @Test
    fun `AMM updater prefers derivative release APK`() {
        assertEquals(
            "AmbientMusicMod-OneUI.apk",
            UpdatesViewModelImpl.AMM_RELEASE_ASSET
        )
    }
}
