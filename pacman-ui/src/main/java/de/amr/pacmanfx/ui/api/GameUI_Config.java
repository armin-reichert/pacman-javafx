/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.HUD_Renderer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

/**
 * Game-variant specific configuration of the UI. Provides factory methods for renderers and actors and accessors to
 * the game-variant specific assets.
 */
public interface GameUI_Config extends Disposable {

    String CONFIG_KEY_COLOR_MAP = "colorMap";

    String CONFIG_KEY_COLOR_MAP_INDEX = "colorMapIndex";

    String CONFIG_KEY_MAP_NUMBER = "mapNumber";

    /**
     * @return the game variant specific asset map
     */
    AssetMap assets();

    /**
     * Loads the game variant specific asset map
     */
    void loadAssets();

    /**
     * @return the spritesheet for this game variant
     */
    SpriteSheet<?> spriteSheet();

    /**
     * @return the sound manager for this game variant
     */
    SoundManager soundManager();

    /**
     * @return the scene configuration for this game variant
     */
    GameScene_Config sceneConfig();

    /**
     * Crappy attempt to tell the play scene if the munching sound should be played at each eaten food or after eating
     * two pellets
     *
     * @param eatenFoodCount the number of pellets already eaten when this method is called
     * @return if the munching sound should be played
     */
    default boolean munchingSoundPlayed(int eatenFoodCount) {
        return true;
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
     * @return a new renderer for a game level
     */
    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    /**
     * @param canvas the canvas where the 2D scene gets rendered
     * @return a new renderer for the heads-up display (HUD)
     */
    HUD_Renderer createHUDRenderer(Canvas canvas);

    /**
     * @param canvas the canvas where the 2D scene gets rendered
     * @return a new renderer for the actors
     */
    ActorRenderer createActorRenderer(Canvas canvas);

    /**
     * @param personality a ghost personality
     * @return a ghost instance with this personality's behavior
     */
    Ghost createAnimatedGhost(byte personality);

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
     * @param gameLevel TODO: check if this can be removed
     * @param pac Pac-Man or Ms Pac-Man actor
     * @return 3D representation for Pac-Man or Ms. Pac-Man in this game variant
     */
    PacBase3D createPac3D(AnimationRegistry animationRegistry, GameLevel gameLevel, Pac pac, double size);

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