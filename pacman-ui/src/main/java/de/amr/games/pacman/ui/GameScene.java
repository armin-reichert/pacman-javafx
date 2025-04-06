/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.uilib.ActionProvider;
import de.amr.games.pacman.uilib.Keyboard;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;

import java.util.List;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.uilib.Keyboard.*;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener, ActionProvider {

    default void bindDefaultArcadeControllerActions(ArcadeKeyBinding arcadeKeys) {
        bind(GameAction.INSERT_COIN,  arcadeKeys.key(Arcade.Button.COIN), naked(KeyCode.NUMPAD5));
        bind(GameAction.START_GAME,   arcadeKeys.key(Arcade.Button.START), naked(KeyCode.NUMPAD1));
        bind(GameAction.PLAYER_UP,    arcadeKeys.key(Arcade.Button.UP));
        bind(GameAction.PLAYER_DOWN,  arcadeKeys.key(Arcade.Button.DOWN));
        bind(GameAction.PLAYER_LEFT,  arcadeKeys.key(Arcade.Button.LEFT));
        bind(GameAction.PLAYER_RIGHT, arcadeKeys.key(Arcade.Button.RIGHT));
    }

    default void bindAlternativePlayerControlActions() {
        bind(GameAction.PLAYER_UP,    control(KeyCode.UP));
        bind(GameAction.PLAYER_DOWN,  control(KeyCode.DOWN));
        bind(GameAction.PLAYER_LEFT,  control(KeyCode.LEFT));
        bind(GameAction.PLAYER_RIGHT, control(KeyCode.RIGHT));
    }

    default void bindCheatActions() {
        bind(GameAction.CHEAT_EAT_ALL,     alt(KeyCode.E));
        bind(GameAction.CHEAT_ADD_LIVES,   alt(KeyCode.L));
        bind(GameAction.CHEAT_NEXT_LEVEL,  alt(KeyCode.N));
        bind(GameAction.CHEAT_KILL_GHOSTS, alt(KeyCode.X));
    }

    default void bindTestsStartingActions() {
        bind(GameAction.TEST_CUT_SCENES,     alt(KeyCode.C));
        bind(GameAction.TEST_LEVELS_BONI,    alt(KeyCode.T));
        bind(GameAction.TEST_LEVELS_TEASERS, Keyboard.shift_alt(KeyCode.T));
    }

    default <GAME extends GameModel> GAME game() { return THE_GAME_CONTROLLER.game(); }
    default GameState gameState() { return THE_GAME_CONTROLLER.state(); }

    /**
     * Called when the scene becomes the current one.
     */
    void init();

    /**
     * Called when the scene needs to be updated.
     */
    void update();

    /**
     * Called when the scene ends and gets replaced by another scene.
     */
    void end();

    /**
     * @return (unscaled) scene size in pixels e.g. 224x288
     */
    Vector2f sizeInPx();

    /**
     * Called when scene variants for 2D and 3D exist and variant changes between 2D and 3D.
     *
     * @param oldScene scene that was displayed before this scene
     */
    default void onSceneVariantSwitch(GameScene oldScene) {}

    /**
     * @param e event associated with opening of context menu
     * @return menu items provided by this game scene which are merged into the final context menu
     */
    default List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) { return List.of(); }

    /**
     * @return scene name as used by logging output
     */
    default String displayName() {
        return "%s (%s)".formatted(getClass().getSimpleName(), THE_GAME_CONTROLLER.selectedGameVariant());
    }
}