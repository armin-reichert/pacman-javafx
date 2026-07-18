/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.image.Image;

import java.util.Optional;

public interface GameVariantConfig extends Disposable {

    GameVariantRenderConfig renderConfig();

    void init(GameAppContext appContext);

    AssetMap assets();

    TranslationManager translations();

    Factory3D factory3D();

    Optional<GameSoundEffects> optSoundEffects();

    GameSceneConfig gameSceneConfig();

    WorldSettings worldSettings();
}