package com.kieronquinn.app.ambientmusicmod.components.nowplayingsurface

import com.kieronquinn.app.ambientmusicmod.repositories.RecognitionRepository.RecognitionState
import com.kieronquinn.app.pixelambientmusic.model.RecognitionFailure
import com.kieronquinn.app.pixelambientmusic.model.RecognitionFailureReason
import com.kieronquinn.app.pixelambientmusic.model.RecognitionMetadata
import com.kieronquinn.app.pixelambientmusic.model.RecognitionResult
import com.kieronquinn.app.pixelambientmusic.model.RecognitionSource
import org.junit.Assert.assertEquals
import org.junit.Test

class NowPlayingSurfaceLifecycleTest {

    @Test
    fun recognisedPublishesMappedEvent() {
        val publisher = RecordingPublisher()
        val lifecycle = NowPlayingSurfaceLifecycle(publisher)

        lifecycle.onRecognitionState(
            RecognitionState.Recognised(
                recognitionResult = RecognitionResult(
                    trackName = "Song",
                    artist = "Artist",
                    recognitionSource = RecognitionSource.NNFP,
                    players = emptyArray(),
                    googleId = "google-id",
                    audio = null
                ),
                metadata = RecognitionMetadata(
                    recognitionTime = 1_000L,
                    currentPos = 5_000L,
                    remainingTime = 45_000L
                )
            )
        )

        assertEquals(
            listOf(NowPlayingSurfaceEvent("Song", "Artist", 45_000L)),
            publisher.events
        )
        assertEquals(0, publisher.clearCount)
    }

    @Test
    fun failedRecognitionClearsStaleSurface() {
        val publisher = RecordingPublisher()
        val lifecycle = NowPlayingSurfaceLifecycle(publisher)

        lifecycle.onRecognitionState(
            RecognitionState.Failed(
                RecognitionFailure(
                    RecognitionFailureReason.NoMatch,
                    RecognitionSource.NNFP,
                    null
                )
            )
        )

        assertEquals(1, publisher.clearCount)
    }

    @Test
    fun errorClearsStaleSurface() {
        val publisher = RecordingPublisher()
        val lifecycle = NowPlayingSurfaceLifecycle(publisher)

        lifecycle.onRecognitionState(RecognitionState.Error(RecognitionState.ErrorReason.DISABLED))

        assertEquals(1, publisher.clearCount)
    }

    private class RecordingPublisher: NowPlayingSurfacePublisher {
        val events = mutableListOf<NowPlayingSurfaceEvent>()
        var clearCount = 0

        override fun publish(event: NowPlayingSurfaceEvent) {
            events += event
        }

        override fun clear() {
            clearCount++
        }
    }
}
