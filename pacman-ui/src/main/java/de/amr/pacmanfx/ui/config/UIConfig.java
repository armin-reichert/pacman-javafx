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
import de.amr.pacmanfx.uilib.model3D.ghost.GhostComponentColors;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostConfig;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostStateColors;
import de.amr.pacmanfx.uilib.model3D.pac.MsPacManComponentColors;
import de.amr.pacmanfx.uilib.model3D.pac.PacColors;
import de.amr.pacmanfx.uilib.model3D.pac.PacConfig;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public interface UIConfig extends Disposable {

    WorldConfig DEFAULT_WORLD_CONFIG = new WorldConfig(
        new PacConfig(
            new PacColors(
                ARCADE_YELLOW, // headColor
                ARCADE_BROWN,  // palateColor
                Color.grayRgb(33) // eyesColor
            ),
            new MsPacManComponentColors(
                ARCADE_RED, // hair bow
                ARCADE_BLUE, // hair bow pearls
                ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0) // boobs
            ),
            8.0f,
            16.0f),
        List.of(
            new GhostConfig(8.0f, 15.5f,
                new GhostStateColors(
                    new GhostComponentColors(ARCADE_RED, ARCADE_WHITE, ARCADE_BLUE),
                    new GhostComponentColors(ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE),
                    new GhostComponentColors(ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED))
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostStateColors(
                    new GhostComponentColors(ARCADE_PINK, ARCADE_WHITE, ARCADE_BLUE),
                    new GhostComponentColors(ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE),
                    new GhostComponentColors(ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED))
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostStateColors(
                    new GhostComponentColors(ARCADE_CYAN, ARCADE_WHITE, ARCADE_BLUE),
                    new GhostComponentColors(ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE),
                    new GhostComponentColors(ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED))
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostStateColors(
                    new GhostComponentColors(ARCADE_ORANGE, ARCADE_WHITE, ARCADE_BLUE),
                    new GhostComponentColors(ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE),
                    new GhostComponentColors(ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED))
            )
        ),
        new BonusConfig(8.0f, 14.5f),
        new EnergizerConfig3D(3, 3.5f, 6.0f, 0.2f, 1.0f),
        new FloorConfig3D(20f, 0.5f),
        new HouseConfig3D(12.0f, 0.4f, 12.0f, 2.5f),
        new LevelCounterConfig3D(10.0f, 6.0f),
        new LivesCounterConfig3D(5, 12.0f),
        new MazeConfig3D(4.0f, 4.0f, 1.0f, 2.25f, "0x282828"),
        new PelletConfig3D(0.8f, 6.0f)
    );

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

    default WorldConfig worldConfig() {
        return DEFAULT_WORLD_CONFIG;
    }

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