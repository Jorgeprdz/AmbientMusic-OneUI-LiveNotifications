package com.kieronquinn.app.ambientmusicmod.debug

import android.app.Application

/**
 * Minimal Application used only by the debug M0 build.
 *
 * The public Live Update probe must not initialise Ambient Music Mod's recognition,
 * Shizuku or repository graph before the platform capability test runs.
 */
class M0ProbeApplication : Application()
