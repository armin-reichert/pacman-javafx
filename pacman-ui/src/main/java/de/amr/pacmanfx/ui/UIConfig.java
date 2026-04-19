/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.basics.Disposable;
import de.amr.basics.spriteanim.AnimationSet;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.model3D.actor.*;
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

/**
 * Central configuration interface for the presentation layer of a specific game variant
 * (e.g. Arcade Pac-Man, Ms. Pac-Man, etc.).
 *
 * <p>A {@code UIConfig} implementation defines the complete visual and audio appearance
 * of one game variant within the {@link GameUI} framework. It acts as a theme provider,
 * supplying:</p>
 *
 * <ul>
 *   <li>assets (images, sprite sheets, colors, localized strings)</li>
 *   <li>renderers for scenes, levels, HUD and actors</li>
 *   <li>animation sets for Pac-Man/Ms. Pac-Man and the four ghosts</li>
 *   <li>maze color schemes</li>
 *   <li>variant-specific behavior (sound timing, bonus visuals, boot-screen regions, …)</li>
 *   <li>3D entity rendering parameters (via {@link #entityConfig()})</li>
 * </ul>
 *
 * <p>Implementations are typically created once per game variant and remain active for
 * the lifetime of the {@link GameUI} instance.</p>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>{@link #init(GameUI)} is called once during UI construction</li>
 *   <li>Assets are loaded lazily or eagerly during {@code init()}</li>
 *   <li>Renderer and animation factories are called on demand during rendering</li>
 *   <li>{@link #dispose()} is called when the UI is shut down
 *       (default implementation disposes the {@link AssetMap})</li>
 * </ol>
 *
 * <p>Most implementations maintain internal state only for cached assets and scenes.
 * The interface is designed to be stateless where possible.</p>
 *
 * @see GameSceneConfig  companion interface handling scene creation and selection logic
 * @see GameUI           owner component that uses this configuration
 */
public interface UIConfig extends Disposable {

    /**
     * Default 3D entity rendering configuration used when a variant does not override it.
     * Values are tuned to the classic Arcade Pac-Man look.
     */
    EntityConfig DEFAULT_ENTITY_CONFIG = new EntityConfig(
        new PacConfig(
            new PacComponentColors(
                ARCADE_YELLOW, // head
                ARCADE_BROWN,  // palate
                Color.grayRgb(33) // eyes
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
                new GhostComponentColors(ARCADE_RED, ARCADE_WHITE, ARCADE_BLUE),
                new GhostComponentColors(ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE),
                new GhostComponentColors(ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED)
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostComponentColors(ARCADE_PINK, ARCADE_WHITE, ARCADE_BLUE),
                new GhostComponentColors(ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE),
                new GhostComponentColors(ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED)
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostComponentColors(ARCADE_CYAN, ARCADE_WHITE, ARCADE_BLUE),
                new GhostComponentColors(ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE),
                new GhostComponentColors(ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED)
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostComponentColors(ARCADE_ORANGE, ARCADE_WHITE, ARCADE_BLUE),
                new GhostComponentColors(ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE),
                new GhostComponentColors(ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED)
            )
        ),
        new BonusConfig(8.0f, 14.5f),
        new EnergizerConfig3D(3, 3.5f, 6.0f, 0.2f, 1.0f),
        new FloorConfig3D(5f, 0.5f),
        new HouseConfig3D(12.0f, 0.4f, 12.0f, 2.5f),
        new LevelCounterConfig3D(10.0f, 6.0f),
        new LivesCounterConfig3D(
            5,
            Color.grayRgb(120),
            Color.grayRgb(180),
            12.0f),
        new MazeConfig3D(4.0f, 4.0f, 1.0f, 2.25f, "0x2a2a2a"),
        new PelletConfig3D(1.0f, 6.0f)
    );

    /**
     * Initializes this configuration. Called exactly once when the owning {@link GameUI}
     * is constructed.
     * <p>
     * Typical tasks performed here:
     * <ul>
     *   <li>loading images and sprite sheets into {@link #assets()}</li>
     *   <li>registering sound effects with the {@link SoundManager}</li>
     *   <li>preparing any internal caches</li>
     * </ul>
     *
     * @param ui the {@code GameUI} instance that owns this configuration
     */
    void init(GameUI ui);

    /**
     * Disposes all resources held by this configuration.
     * <p>
     * The default implementation disposes the {@link AssetMap} and logs the action.
     * Subclasses may override to release additional resources (cached renderers, etc.).
     */
    @Override
    default void dispose() {
        disposeAssets();
    }

    /**
     * Disposes all assets currently stored in the {@link AssetMap}.
     * Called by the default {@link #dispose()} implementation.
     */
    default void disposeAssets() {
        Logger.info("Disposing {} assets in {}", assets().numAssets(), getClass().getSimpleName());
        assets().dispose();
    }

    /**
     * Returns the map holding all variant-specific resources (images, colors,
     * localized text bundles, etc.).
     *
     * @return the asset map for this variant
     */
    AssetMap assets();

    Factory3D factory3D();

    Optional<GameSoundEffects> soundEffects();

    /**
     * Returns the scene-configuration object that determines which scene should be
     * displayed for each game state and manages scene creation/caching.
     *
     * @return the associated {@link GameSceneConfig}
     */
    GameSceneConfig gameSceneConfig();

    /**
     * Returns the sprite sheet used by renderers in this variant.
     *
     * @return the sprite sheet instance
     */
    SpriteSheet<?> spriteSheet();

    /**
     * Returns the 3D rendering parameters for entities (Pac-Man, ghosts, pellets, …).
     * <p>
     * Most variants can return {@link #DEFAULT_ENTITY_CONFIG}.
     *
     * @return 3D entity rendering configuration
     */
    default EntityConfig entityConfig() {
        return DEFAULT_ENTITY_CONFIG;
    }

    /**
     * Returns the sub-region of the sprite sheet to be used by the boot scene
     * for randomized background content.
     * <p>
     * The default implementation returns the full sprite-sheet bounds.
     *
     * @return rectangle defining the usable sprite region
     */
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
                entityConfig().maze().darkWallFillColor(),
                colorScheme.wallStroke(),
                colorScheme.door(),
                colorScheme.pellet());
        }
        return colorScheme;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Bonus symbol & value images
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Returns the 2D image of the bonus symbol (cherry, strawberry, …) for the given code.
     *
     * @param symbol bonus symbol identifier (usually 0–7)
     * @return corresponding bonus symbol image
     */
    Image bonusSymbolImage(byte symbol);

    /**
     * Returns the 2D image showing the point value awarded for eating the bonus.
     *
     * @param symbol bonus symbol identifier
     * @return image displaying the corresponding point value
     */
    Image bonusValueImage(byte symbol);

    // ─────────────────────────────────────────────────────────────────────────────
    // Color & rendering factories
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Returns the wall/door color scheme appropriate for the given maze.
     *
     * @param worldMap the maze whose colors should be determined
     * @return color scheme to use when rendering the maze
     */
    WorldMapColorScheme colorScheme(WorldMap worldMap);

    /**
     * Creates a renderer responsible for drawing the static game level (maze, pellets, …).
     *
     * @param canvas target canvas
     * @return level renderer instance
     */
    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    /**
     * Creates a renderer for the specified 2D game scene.
     *
     * @param gameScene2D the scene to render
     * @param canvas      target canvas
     * @return scene-specific renderer
     */
    GameScene2D_Renderer createGameSceneRenderer(GameScene2D gameScene2D, Canvas canvas);

    /**
     * Creates the heads-up display renderer (score, lives, level counter, …).
     *
     * @param gameScene2D the scene whose HUD should be rendered
     * @param canvas      target canvas
     * @return HUD renderer instance
     */
    HeadsUpDisplay_Renderer createHUDRenderer(GameScene2D gameScene2D, Canvas canvas);

    /**
     * Creates the renderer used to draw all dynamic actors (Pac-Man, ghosts, bonus).
     *
     * @param canvas target canvas
     * @return actor renderer instance
     */
    ActorRenderer createActorRenderer(Canvas canvas);

    // ─────────────────────────────────────────────────────────────────────────────
    // Animation & ghost factories
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Creates a fully configured ghost instance with the correct personality
     * and assigns it the variant-specific animation set.
     *
     * @param personality ghost personality code (e.g. RED_GHOST_SHADOW, …)
     * @return configured ghost ready to be added to the game
     */
    Ghost createGhostWithAnimations(byte personality);

    /**
     * Creates the animation manager containing all movement & state animations
     * for a ghost with the given personality.
     *
     * @param personality ghost personality code
     * @return animation manager for that ghost type
     */
    AnimationSet createGhostAnimations(byte personality);

    /**
     * Creates the animation manager for Pac-Man (or Ms. Pac-Man in Ms. Pac-Man variants).
     *
     * @return Pac-Man animation manager
     */
    AnimationSet createPacAnimations();

    /**
     * Returns the image showing the points awarded for eating a ghost
     * (200, 400, 800, 1600).
     *
     * @param killedIndex 0 = first ghost eaten in sequence, 1 = second, …
     * @return image displaying the corresponding points value
     */
    Image killedGhostPointsImage(int killedIndex);
}