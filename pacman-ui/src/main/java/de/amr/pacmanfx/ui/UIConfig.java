/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.sound.GamePlaySoundEffects;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.ui.ArcadePalette.*;

/**
 * Defines the complete UI configuration for a specific game variant.
 *
 * <p>A {@code UIConfig} acts as the variant‑specific theme provider for the
 * {@link GameUI} framework. It supplies all assets, renderers, animations,
 * color schemes, and 3D models required to present the game according to the
 * visual and audio rules of the selected variant (e.g., Arcade Pac‑Man,
 * Ms. Pac‑Man, Tengen, etc.).</p>
 *
 * <p>The configuration is responsible for:</p>
 *
 * <ul>
 *   <li><strong>Asset management</strong> – loading, exposing, and disposing
 *       images, colors, localized texts, and sprite sheets via an
 *       {@link AssetMap}.</li>
 *
 *   <li><strong>Renderer factories</strong> – creating renderers for 2D game
 *       scenes, game levels, actors, and the heads‑up display.</li>
 *
 *   <li><strong>Animation factories</strong> – providing animation managers
 *       for Pac‑Man and each ghost personality.</li>
 *
 *   <li><strong>Color scheme selection</strong> – returning the appropriate
 *       {@link WorldMapColorScheme} for a given maze.</li>
 *
 *   <li><strong>Variant‑specific UI behavior</strong> – such as defining the
 *       delay between munching sound effects or selecting sprite regions for
 *       special scenes.</li>
 * </ul>
 *
 * <p>The lifecycle of a {@code UIConfig} is:</p>
 *
 * <ol>
 *   <li>{@link #init(GameUI)} is called once when the UI is created.</li>
 *   <li>Renderers, assets, and animations are requested on demand.</li>
 *   <li>{@link #dispose()} is called when the UI shuts down, which by default
 *       disposes all loaded assets.</li>
 * </ol>
 *
 * <p>Implementations of this interface are expected to be stateless aside from
 * their asset maps and any cached renderers they choose to maintain.</p>
 */
public interface UIConfig extends Disposable {

    EntityConfig DEFAULT_ENTITY_CONFIG = new EntityConfig(
        new PacConfig(
            ARCADE_YELLOW,
            Color.grayRgb(33),
            ARCADE_BROWN,
            ARCADE_RED,
            ARCADE_BLUE,
            ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0),
            8.0f,
            16.0f),
        List.of(
            new GhostConfig(8.0f, 15.5f,
                ARCADE_RED, ARCADE_WHITE, ARCADE_BLUE,
                ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE,
                ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED),
            new GhostConfig(8.0f, 15.5f,
                ARCADE_PINK, ARCADE_WHITE, ARCADE_BLUE,
                ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE,
                ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED),
            new GhostConfig(8.0f, 15.5f,
                ARCADE_CYAN, ARCADE_WHITE, ARCADE_BLUE,
                ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE,
                ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED),
            new GhostConfig(8.0f, 15.5f,
                ARCADE_ORANGE, ARCADE_WHITE, ARCADE_BLUE,
                ARCADE_BLUE, ARCADE_ROSE, ARCADE_ROSE,
                ARCADE_WHITE, ARCADE_ROSE, ARCADE_RED)
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
     * Initializes this UI configuration. Called once when the {@link GameUI}
     * is created.
     *
     * @param ui the game UI instance that owns this configuration
     */
    void init(GameUI ui);

    /**
     * Disposes all assets stored in the {@link AssetMap}. Called automatically
     * by {@link #dispose()} unless overridden.
     */
    default void disposeAssets() {
        Logger.info("Dispose {} assets", assets().numAssets());
        assets().dispose();
    }

    /**
     * Returns the asset map containing all variant‑specific images, colors,
     * localized texts, and other resources.
     *
     * @return the asset map for this UI configuration
     */
    AssetMap assets();

    /**
     * Returns the sprite sheet used by this game variant.
     *
     * @return the variant‑specific sprite sheet
     */
    SpriteSheet<?> spriteSheet();

    /**
     * Returns the entity configuration containing all parameters for rendering
     * Pac‑Man, ghosts, bonus items, and other entities in this variant.
     *
     * @return the entity configuration for this variant
     */
    default EntityConfig entityConfig() {
        return DEFAULT_ENTITY_CONFIG;
    }

    /**
     * Returns the sprite sheet region used by the Arcade boot scene to select
     * random content. Variants may override this to restrict the region.
     *
     * @return the sprite region used by the boot scene
     */
    default Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(
            0,
            0,
            spriteSheet().sourceImage().getWidth(),
            spriteSheet().sourceImage().getHeight()
        );
    }

    GamePlaySoundEffects createPlaySoundEffects(GameUI ui);

    /**
     * Returns the 2D image representing the bonus symbol for the given code.
     *
     * @param symbol the bonus symbol code
     * @return the image representing the bonus symbol
     */
    Image bonusSymbolImage(byte symbol);

    /**
     * Returns the 2D image representing the bonus value (points earned) for
     * the given bonus symbol code.
     *
     * @param symbol the bonus symbol code
     * @return the image representing the bonus value
     */
    Image bonusValueImage(byte symbol);

    /**
     * Returns the color scheme to use for the given world map (maze).
     *
     * @param worldMap the world map whose colors should be determined
     * @return the color scheme for the given map
     */
    WorldMapColorScheme colorScheme(WorldMap worldMap);

    /**
     * Creates a renderer for drawing the game level on the given canvas.
     *
     * @param canvas the canvas where the game level will be rendered
     * @return a new game level renderer
     */
    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    /**
     * Creates a renderer for drawing the specified 2D game scene.
     *
     * @param gameScene2D the 2D game scene to render
     * @param canvas the canvas where the scene will be rendered
     * @return a new renderer for the given scene
     */
    GameScene2D_Renderer createGameSceneRenderer(GameScene2D gameScene2D, Canvas canvas);

    /**
     * Creates a renderer for drawing the heads‑up display (HUD) of the given
     * 2D game scene.
     *
     * @param gameScene2D the game scene whose HUD should be rendered
     * @param canvas the canvas where the HUD will be rendered
     * @return a new HUD renderer
     */
    HeadsUpDisplay_Renderer createHUDRenderer(GameScene2D gameScene2D, Canvas canvas);

    /**
     * Creates a renderer for drawing actors (Pac‑Man, ghosts, bonus items)
     * in a 2D game scene.
     *
     * @param canvas the canvas where actors will be rendered
     * @return a new actor renderer
     */
    ActorRenderer createActorRenderer(Canvas canvas);

    /**
     * Creates a ghost instance with the specified personality and assigns it
     * the appropriate animation manager for this variant.
     *
     * @param personality the ghost personality code
     * @return a new ghost instance configured for this variant
     */
    Ghost createGhostWithAnimations(byte personality);

    /**
     * Creates an animation manager containing all animations for a ghost with
     * the specified personality.
     *
     * @param personality the ghost personality code
     * @return the animation manager for the ghost
     */
    AnimationManager createGhostAnimations(byte personality);

    /**
     * Creates an animation manager containing all animations for Pac‑Man or
     * Ms. Pac‑Man, depending on the game variant.
     *
     * @return the animation manager for Pac‑Man
     */
    AnimationManager createPacAnimations();

    /**
     * Returns the image representing the points earned for killing a ghost
     * at the given index in the sequence of killed ghosts.
     *
     * @param killedIndex the index of the killed ghost (0 = first, 1 = second, ...)
     * @return the image showing the points value
     */
    Image killedGhostPointsImage(int killedIndex);
}
