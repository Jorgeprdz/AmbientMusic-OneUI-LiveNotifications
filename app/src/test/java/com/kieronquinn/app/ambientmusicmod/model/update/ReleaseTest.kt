package com.kieronquinn.app.ambientmusicmod.model.update

import com.google.gson.Gson
import com.kieronquinn.app.ambientmusicmod.model.github.GitHubRelease
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseTest {

    @Test
    fun `preferred AMM asset wins even when PAM is first`() {
        val json = """
            {
              "html_url": "https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications/releases/tag/v0.1.0",
              "tag_name": "v0.1.0",
              "name": "v0.1.0 Experimental",
              "body": "test",
              "assets": [
                {
                  "browser_download_url": "https://example.invalid/PixelAmbientMusic-1.3.5-paired.apk",
                  "content_type": "application/vnd.android.package-archive",
                  "name": "PixelAmbientMusic-1.3.5-paired.apk"
                },
                {
                  "browser_download_url": "https://example.invalid/AmbientMusicMod-OneUI.apk",
                  "content_type": "application/vnd.android.package-archive",
                  "name": "AmbientMusicMod-OneUI.apk"
                }
              ]
            }
        """.trimIndent()

        val release = Gson().fromJson(json, GitHubRelease::class.java)
        val mapped = release.toRelease(
            "Ambient Music for One UI",
            "2.4",
            "AmbientMusicMod-OneUI.apk"
        )

        assertNotNull(mapped)
        assertEquals("AmbientMusicMod-OneUI.apk", mapped?.fileName)
    }

    @Test
    fun `preferred AMM asset fails closed instead of selecting PAM`() {
        val json = """
            {
              "html_url": "https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications/releases/tag/v0.1.0",
              "tag_name": "v0.1.0",
              "name": "v0.1.0 Experimental",
              "body": "test",
              "assets": [
                {
                  "browser_download_url": "https://example.invalid/PixelAmbientMusic-1.3.5-paired.apk",
                  "content_type": "application/vnd.android.package-archive",
                  "name": "PixelAmbientMusic-1.3.5-paired.apk"
                }
              ]
            }
        """.trimIndent()

        val release = Gson().fromJson(json, GitHubRelease::class.java)
        val mapped = release.toRelease(
            "Ambient Music for One UI",
            "2.4",
            "AmbientMusicMod-OneUI.apk"
        )

        assertNull(mapped)
    }

    @Test
    fun `apk filename fallback works when content type is generic`() {
        val json = """
            {
              "html_url": "https://github.com/Jorgeprdz/AmbientMusic-OneUI-LiveNotifications/releases/tag/v0.1.0",
              "tag_name": "v0.1.0",
              "name": "v0.1.0 Experimental",
              "body": "test",
              "assets": [
                {
                  "browser_download_url": "https://example.invalid/AmbientMusicMod-OneUI.apk",
                  "content_type": "application/octet-stream",
                  "name": "AmbientMusicMod-OneUI.apk"
                }
              ]
            }
        """.trimIndent()

        val release = Gson().fromJson(json, GitHubRelease::class.java)
        val mapped = release.toRelease(
            "Ambient Music for One UI",
            "2.4",
            "AmbientMusicMod-OneUI.apk"
        )

        assertNotNull(mapped)
        assertEquals("AmbientMusicMod-OneUI.apk", mapped?.fileName)
    }
}
