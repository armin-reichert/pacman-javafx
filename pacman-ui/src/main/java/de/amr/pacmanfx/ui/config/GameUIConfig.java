/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.config;

import de.amr.basics.Disposable;
import de.amr.basics.spriteanim.SpriteAnimationAccessor;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Optional;

public interface GameUIConfig extends Disposable {

    void init(Game game);

    @Override
    default void dispose() {
        disposeAssets();
    }

    default void disposeAssets() {
        Logger.info("Disposing {} assets in {}", assets().numAssets(), getClass().getSimpleName());
        assets().dispose();
    }

    AssetMap assets();

    TranslationManager translations();

    Factory3D factory3D();

    Optional<GameSoundEffects> optSoundEffects();

    GameSceneConfig gameSceneConfig();

    SpriteSheet<?> spriteSheet();

    WorldSettings worldConfig();

    default Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(
            0, 0,
            spriteSheet().sourceImage().getWidth(),
            spriteSheet().sourceImage().getHeight()
        );
    }

    default WorldMapColorScheme enhanceContrast(WorldMapColorScheme colorScheme) {
        final Color wallFillColor = Color.valueOf(colorScheme.wallFill());
        if (wallFillColor.getBrightness() < 0.1) {
            return new WorldMapColorScheme(
                worldConfig().maze().darkWallFillColor(),
                colorScheme.wallStroke(),
                colorScheme.door(),
                colorScheme.pellet());
        }
        return colorScheme;
    }

    Image bonusSymbolImage(int symbolCode);

    Image bonusValueImage(int symbolCode);

    WorldMapColorScheme colorScheme(WorldMap worldMap);

    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    GameScene2D_Renderer createGameSceneRenderer(GameScene2D gameScene2D, Canvas canvas);

    HeadsUpDisplay_Renderer createHUDRenderer(GameScene2D gameScene2D, Canvas canvas);

    ActorRenderer createActorRenderer(Canvas canvas);

    Ghost createAnimatedGhost(SpriteAnimationContainer animationSet, byte personality);

    SpriteAnimationAccessor createGhostAnimations(SpriteAnimationContainer animationSet, byte personality);

    SpriteAnimationAccessor createPacAnimations(SpriteAnimationContainer animationSet);

    Image killedGhostPointsImage(int killedIndex);
}