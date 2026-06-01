/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.ui.config.ConfigurationsManager;
import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

public record GameUI_Services(
    GameClock gameClock,
    DirectoryWatchdog customDirWatchdog,
    ConfigurationsManager configurations,
    FlashMessageManager flashMessages,
    GameSceneManager gameScenes,
    PreferencesManager prefs,
    SoundManager sounds,
    SpriteAnimationManager sprites,
    TranslationManager translations,
    SubViewManager subViews) {}
