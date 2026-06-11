/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.d3.UISettings3D;
import de.amr.pacmanfx.ui.game.UISettings;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.view.FlashMessageManager;
import de.amr.pacmanfx.ui.view.GameView;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

public record GameUI(
    FlashMessageManager flashMessages,
    GameSceneManager gameScenes,
    SoundManager sounds,
    SpriteAnimationManager sprites,
    TranslationManager translations,
    GameView view,
    SubViewManager subViews,
    UISettings settings,
    UISettings3D settings3D,
    GameUI_Extensions extensions
) {}
