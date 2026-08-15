/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Disposable;
import de.amr.basics.Named;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.Optional;

public interface GameVariantUIConfig extends Disposable {

    //TODO remove app parameter
    /**
     * Called when the corresponding game variant gets the current one, e.g. by selecting its start page.
     *
     * @param app the global context
     */
    void init(GameAppContext app, SoundManager soundManager);

    void unloadSounds(SoundManager soundManager);

    /**
     * @return the game scene configuration mapping game states to scenes.
     */
    GameSceneConfig gameSceneConfig();

    /**
     * @return the renderer configuration (spritesheets, sprite animations etc.)
     */
    GameVariantRenderConfig renderConfig();

    /**
     * @return the game-variant specific assets (images, fonts etc.)
     */
    AssetMap assets();

    /**
     * @return the game-variant specific text translations
     */
    TranslationManager translations();

    /**
     * @return the factory for creating the 3D actors for this game variant
     */
    Factory3D factory3D();

    /**
     * @return the sound effects for this game variant
     */
    Optional<GameSoundEffects> optSoundEffects();

    /**
     * @return the game level map ("world") settings
     */
    WorldSettings worldSettings();

    <T> T getExtensionValue(Named id, Class<T> type);
}