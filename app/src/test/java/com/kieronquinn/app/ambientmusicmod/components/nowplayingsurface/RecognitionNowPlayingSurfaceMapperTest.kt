package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

import com.kieronquinn.app.ambientmusicmod.repositories.RecognitionRepository.RecognitionState
import com.kieronquinn.app.pixelambientmusic.model.RecognitionMetadata
import com.kieronquinn.app.pixelambientmusic.model.RecognitionResult
import com.kieronquinn.app.pixelambientmusic.model.RecognitionSource
import org.junit.Assert.assertEquals
import org.junit.Test

class RecognitionNowPlayingSurfaceMapperTest {

    @Test
    fun recognisedStateMapsTitleArtistAndRemainingTime() {
        val state = recognisedState(
            title = "Song",
            artist = "Artist",
            metadata = RecognitionMetadata(
                recognitionTime = 1_000L,
                currentPos = 5_000L,
                remainingTime = 45_000L
            )
        )

        assertEquals(
            NowPlayingSurfaceEvent(
                title = "Song",
                artist = "Artist",
                timeoutMillis = 45_000L
            ),
            RecognitionNowPlayingSurfaceMapper.map(state)
        )
    }

    @Test
    fun missingMetadataUsesSixtySecondFallback() {
        val state = recognisedState(
            title = "Song",
            artist = "Artist",
            metadata = null
        )

        assertEquals(
            NowPlayingSurfaceEvent(
                title = "Song",
                artist = "Artist",
                timeoutMillis = NowPlayingSurfaceEvent.DEFAULT_TIMEOUT_MILLIS
            ),
            RecognitionNowPlayingSurfaceMapper.map(state)
        )
    }

    private fun recognisedState(
        title: String,
        artist: String,
        metadata: RecognitionMetadata?
    ): RecognitionState.Recognised {
        return RecognitionState.Recognised(
            recognitionResult = RecognitionResult(
                trackName = title,
                artist = artist,
                recognitionSource = RecognitionSource.NNFP,
                players = emptyArray(),
                googleId = "google-id",
                audio = null
            ),
            metadata = metadata
        )
    }
}
