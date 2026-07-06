/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.Disposable;
import de.amr.basics.spriteanim.SpriteAnimationAccessor;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.world.WorldSettings;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Optional;

//TODO: Clean-up this mess!
public interface GameVariantConfig extends Disposable {

    void init(Game game);

    AssetMap assets();

    TranslationManager translations();

    Factory3D factory3D();

    Optional<GameSoundEffects> optSoundEffects();

    GameSceneConfig gameSceneConfig();

    SpriteSheet<?> spriteSheet();

    WorldSettings worldSettings();

    Image bonusSymbolImage(int symbolCode);

    Image bonusValueImage(int symbolCode);

    WorldMapColorScheme colorScheme(WorldMap worldMap);

    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, Canvas canvas);

    HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, Canvas canvas);

    ActorRenderer createActorRenderer(Canvas canvas);

    Ghost createAnimatedGhost(SpriteAnimationContainer animationSet, byte personality);

    SpriteAnimationAccessor createGhostAnimations(SpriteAnimationContainer animationSet, byte personality);

    SpriteAnimationAccessor createPacAnimations(SpriteAnimationContainer animationSet);

    Image killedGhostPointsImage(int killedIndex);


    //TODO move elsewhere
    default WorldMapColorScheme enhanceContrast(WorldMapColorScheme colorScheme) {
        final Color wallFillColor = Color.valueOf(colorScheme.wallFill());
        if (wallFillColor.getBrightness() < 0.1) {
            return new WorldMapColorScheme(
                worldSettings().maze().darkWallFillColor(),
                colorScheme.wallStroke(),
                colorScheme.door(),
                colorScheme.pellet());
        }
        return colorScheme;
    }
}