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
import de.amr.pacmanfx.ui._3d.config.*;
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
 *   <li><strong>3D model factories</strong> – constructing 3D representations
 *       of Pac‑Man and the lives counter when the 3D play scene is enabled.</li>
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

    Config3D DEFAULT_CONFIG_3D = new Config3D(
        new ActorConfig3D(16.0f, 15.5f, 8.0f, 14.5f),
        new EnergizerConfig3D(3, 3.5f, 6.0f, 0.2f, 1.0f),
        new FloorConfig3D(5f, 0.5f),
        new HouseConfig3D(12.0f, 0.4f, 12.0f, 2.5f),
        new PelletConfig3D(1.0f, 6.0f)
    );

    enum ConfigKey { COLOR_SCHEME, COLOR_MAP_INDEX, MAP_NUMBER }

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

    default Config3D config3D() {
        return DEFAULT_CONFIG_3D;
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

    /**
     * Returns the minimum number of game ticks that must pass between two
     * consecutive pellet‑munching sound effects.
     *
     * @return the required delay in ticks (0 means no delay)
     */
    default byte munchingSoundDelay() {
        return 0;
    }

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
     * @param ui the owning game UI
     * @param canvas the canvas where the scene will be rendered
     * @param gameScene2D the 2D game scene to render
     * @return a new renderer for the given scene
     */
    GameScene2D_Renderer createGameSceneRenderer(GameUI ui, Canvas canvas, GameScene2D gameScene2D);

    /**
     * Creates a renderer for drawing the heads‑up display (HUD) of the given
     * 2D game scene.
     *
     * @param canvas the canvas where the HUD will be rendered
     * @param gameScene2D the game scene whose HUD should be rendered
     * @return a new HUD renderer
     */
    HeadsUpDisplay_Renderer createHUDRenderer(Canvas canvas, GameScene2D gameScene2D);

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
     * Creates the 3D representation of Pac‑Man or Ms. Pac‑Man for this game
     * variant, including model, materials, and animation bindings.
     *
     * @param animationRegistry the registry where animations are stored
     * @param pac the Pac‑Man actor whose animations and state drive the model
     * @param size the desired size of the 3D model
     * @return the 3D representation of Pac‑Man
     */
    PacBase3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size);

    /**
     * Creates the 3D representation of the lives counter for this variant.
     *
     * @param size the desired size of the 3D shape
     * @return the 3D node representing a life icon
     */
    Node createLivesCounterShape3D(double size);

    /**
     * Returns the image representing the points earned for killing a ghost
     * at the given index in the sequence of killed ghosts.
     *
     * @param killedIndex the index of the killed ghost (0 = first, 1 = second, ...)
     * @return the image showing the points value
     */
    Image killedGhostPointsImage(int killedIndex);
}
