/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Defines the configuration and selection logic for all game scenes belonging to a specific
 * game variant or UI mode.
 * <p>
 * A {@code GameSceneConfig} acts as the bridge between the game model ({@link Game}) and the
 * presentation layer. It determines:
 * <ul>
 *   <li>which scenes exist for a given variant,</li>
 *   <li>how scenes are identified,</li>
 *   <li>which scene should be active based on the current game state,</li>
 *   <li>whether a scene requires additional decoration (HUD, overlays, etc.).</li>
 * </ul>
 *
 * <h2>Scene Identification</h2>
 * Scenes are identified using the marker interface {@link SceneID}. Implementations may define
 * their own identifiers or use the built‑in {@link CommonSceneID} enumeration.
 *
 * <h2>Scene Selection</h2>
 * The method {@link #selectGameScene(Game)} determines which scene should be displayed for the
 * current game state. This allows each game variant to define its own scene flow.
 */
public interface GameSceneConfig {

    /**
     * Marker interface for scene identifiers.
     * <p>
     * Implementations typically use enums to define stable, type‑safe scene IDs.
     */
    interface SceneID {}

    /**
     * Common scene identifiers shared across most game variants.
     * <p>
     * These cover the standard Pac‑Man flow: boot, intro, start menu, 2D/3D play scenes,
     * and the four intermission cutscenes.
     */
    enum CommonSceneID implements SceneID {
        BOOT_SCENE,
        INTRO_SCENE,
        START_SCENE,
        PLAY_SCENE_2D,
        PLAY_SCENE_3D,
        CUTSCENE_1,
        CUTSCENE_2,
        CUTSCENE_3,
        CUTSCENE_4
    }

    /**
     * Returns the {@link SceneID} for the numbered cutscene.
     *
     * @param n cutscene number (1–4)
     * @return the corresponding {@link CommonSceneID}
     * @throws IllegalArgumentException if the number is outside the valid range
     */
    static SceneID cutSceneID(int n) {
        return switch (n) {
            case 1 -> CommonSceneID.CUTSCENE_1;
            case 2 -> CommonSceneID.CUTSCENE_2;
            case 3 -> CommonSceneID.CUTSCENE_3;
            case 4 -> CommonSceneID.CUTSCENE_4;
            default -> throw new IllegalArgumentException("Illegal cut scene number " + n);
        };
    }

    /**
     * Identifies whether a scene switch represents a transition between 2D and 3D play modes.
     * <p>
     * This is used by the UI layer to trigger special animations or transitions when switching
     * between {@link GameScene2D} and {@link PlayScene3D}.
     *
     * @param sceneBefore the scene previously displayed (may be {@code null})
     * @param sceneAfter  the scene that will be displayed next (never {@code null})
     * @return
     *     <ul>
     *       <li>{@code 23} for a 2D → 3D switch</li>
     *       <li>{@code 32} for a 3D → 2D switch</li>
     *       <li>{@code 0} if no meaningful switch can be determined</li>
     *     </ul>
     * @throws IllegalStateException if both parameters are {@code null}
     */
    static byte identifySceneSwitchType(GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case GameScene2D ignored when sceneAfter instanceof PlayScene3D -> 23;
            case PlayScene3D ignored when sceneAfter instanceof GameScene2D -> 32;
            case null, default -> 0; // may happen, it's ok
        };
    }

    /**
     * Returns a stream of all scenes managed by this configuration.
     * <p>
     * Implementations typically create all scenes eagerly and return them here for lookup,
     * decoration, or lifecycle management.
     *
     * @return a stream of all available {@link GameScene} instances
     */
    Stream<GameScene> gameScenes();

    /**
     * Checks whether the given scene has the specified identifier.
     * <p>
     * This allows implementations to associate scenes with IDs in a flexible way
     * (e.g., via metadata, naming conventions, or explicit mapping).
     *
     * @param gameScene the scene to test
     * @param sceneID   the identifier to compare against
     * @return {@code true} if the scene matches the given ID
     */
    boolean gameSceneHasID(GameScene gameScene, SceneID sceneID);

    /**
     * Selects the scene that should be displayed for the given game state.
     * <p>
     * This method encapsulates the variant‑specific scene flow logic. For example:
     * <ul>
     *   <li>showing the intro scene when the game has not started,</li>
     *   <li>switching to the play scene when a level begins,</li>
     *   <li>selecting the appropriate cutscene after a level is completed.</li>
     * </ul>
     *
     * @param game the current game model
     * @return the scene to display, or an empty {@code Optional} if no scene applies
     */
    Optional<GameScene> selectGameScene(Game game);

    /**
     * Indicates whether the given scene should be decorated with additional UI elements
     * such as HUD overlays, score panels, or debug information.
     * <p>
     * This allows variants to control which scenes are presented "as is" (e.g., cutscenes)
     * and which should include gameplay UI elements.
     *
     * @param gameScene the scene to evaluate
     * @return {@code true} if decoration should be applied
     */
    boolean sceneDecorationRequested(GameScene gameScene);
}
