/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import org.tinylog.Logger;

/**
 * Game-variant specific configuration of the UI. Provides factory methods for renderers and actors and accessors to
 * the game-variant specific assets.
 */
public interface GameUI_Config extends Disposable {

    enum ConfigKey { COLOR_SCHEME, COLOR_MAP_INDEX, MAP_NUMBER }

    void init(GameUI ui);

    @Override
    default void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());
        Logger.info("Dispose {} assets", assets().numAssets());
        assets().dispose();
    }

    /**
     * @return the game variant specific asset map
     */
    AssetMap assets();

    /**
     * @return the spritesheet for this game variant
     */
    SpriteSheet<?> spriteSheet();

    /**
     * @return spritesheet region from which the Arcade boot scene takes its random content.
     */
    default Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(0, 0, spriteSheet().sourceImage().getWidth(), spriteSheet().sourceImage().getHeight());
    }

    /**
     * Minimum number of ticks since the last pellet eaten time until the next munching sound is played.
     *
     * @return if the munching sound should be played
     */
    default byte munchingSoundDelay() {
        return 0;
    }

    /**
     * @param symbol bonus symbol code
     * @return image representing the bonus symbol in a 2D scene
     */
    Image bonusSymbolImage(byte symbol);

    /**
     * @param symbol bonus symbol code
     * @return image representing the bonus value (points earned) in a 2D scene
     */
    Image bonusValueImage(byte symbol);

    /**
     * @param worldMap a world map (maze)
     * @return the color scheme to use for this maze
     */
    WorldMapColorScheme colorScheme(WorldMap worldMap);

    /**
     * @param canvas the canvas where the 2D scene gets rendered
     * @return a new renderer for the game scene
     */
    GameScene2D_Renderer createGameSceneRenderer(Canvas canvas, GameScene2D gameScene2D);

    /**
     * @param canvas the canvas where the 2D scene gets rendered
     * @return a new renderer for a game level
     */
    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    /**
     * @param canvas canvas
     * @param gameScene2D game scene
     * @return a new renderer for the heads-up display (HUD)
     */
    HeadsUpDisplay_Renderer createHUDRenderer(Canvas canvas, GameScene2D gameScene2D);

    /**
     * @param canvas the canvas where the 2D scene gets rendered
     * @return a new renderer for the actors
     */
    ActorRenderer createActorRenderer(Canvas canvas);

    /**
     * @param personality a ghost personality
     * @return a ghost instance with this personality's behavior
     */
    Ghost createGhostWithAnimations(byte personality);

    /**
     * @param personality a ghost personality
     * @return an animation manager containing the animations for a  ghost instance with this personality's behavior
     */
    AnimationManager createGhostAnimations(byte personality);

    /**
     * @return an animation manager with Pac-Man or Ms. Pac-Man animations (depending on the game variant)
     */
    AnimationManager createPacAnimations();

    /**
     * @param animationRegistry the registry where animations are stored
     * @param pac Pac-Man or Ms Pac-Man actor
     * @return 3D representation for Pac-Man or Ms. Pac-Man in this game variant
     */
    PacBase3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size);

    /**
     * @return 3D representation of a lives counter
     */
    Node createLivesCounterShape3D();

    /**
     * @param killedIndex index in sequence of killed ghosts
     * @return image of the points value earned for killing the ghost
     */
    Image killedGhostPointsImage(int killedIndex);
}