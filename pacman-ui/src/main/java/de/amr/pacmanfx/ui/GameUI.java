/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.config.UIConfigurationsManager;
import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.view.GameView;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

public record GameUI(
    UIConfigurationsManager configurations,
    FlashMessageManager flashMessages,
    GameSceneManager gameScenes,
    SoundManager sounds,
    SpriteAnimationManager sprites,
    TranslationManager translations,
    GameView view,
    SubViewManager subViews
) {}
