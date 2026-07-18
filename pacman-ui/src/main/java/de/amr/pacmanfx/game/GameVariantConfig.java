/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Disposable;
import de.amr.basics.spriteanim.SpriteAnimationAccessor;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.image.Image;

import java.util.Optional;

//TODO: Clean-up this mess!
public interface GameVariantConfig extends Disposable {

    GameVariantRenderConfig renderConfig();

    void init(GameAppContext appContext);

    AssetMap assets();

    TranslationManager translations();

    Factory3D factory3D();

    Optional<GameSoundEffects> optSoundEffects();

    GameSceneConfig gameSceneConfig();

    SpriteSheet<?> spriteSheet();

    WorldSettings worldSettings();

    Image killedGhostPointsImage(int killedGhostIndex);

    Image bonusSymbolImage(int symbolCode);

    Image bonusValueImage(int symbolCode);

    WorldMapColorScheme colorScheme(WorldMap worldMap);

    Ghost createAnimatedGhost(SpriteAnimationContainer container, byte personality);

    SpriteAnimationAccessor createGhostAnimations(SpriteAnimationContainer container, byte personality);

    SpriteAnimationAccessor createPacAnimations(SpriteAnimationContainer container);
}