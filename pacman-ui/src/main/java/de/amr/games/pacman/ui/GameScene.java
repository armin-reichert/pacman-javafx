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
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.uilib.Keyboard;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;

import java.util.List;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;

/**
 * Common interface of all game scenes (2D and 3D).
 *
 * @author Armin Reichert
 */
public interface GameScene extends GameEventListener, GameActionProvider {

    default void bindDefaultArcadeControllerActions(ArcadeKeyBinding arcadeKeys) {
        bind(GameActions2D.INSERT_COIN,  arcadeKeys.key(Arcade.Button.COIN));
        bind(GameActions2D.START_GAME,   arcadeKeys.key(Arcade.Button.START));
        bind(GameActions2D.PLAYER_UP,    arcadeKeys.key(Arcade.Button.UP));
        bind(GameActions2D.PLAYER_DOWN,  arcadeKeys.key(Arcade.Button.DOWN));
        bind(GameActions2D.PLAYER_LEFT,  arcadeKeys.key(Arcade.Button.LEFT));
        bind(GameActions2D.PLAYER_RIGHT, arcadeKeys.key(Arcade.Button.RIGHT));
    }

    default void bindAlternativePlayerControlActions() {
        bind(GameActions2D.PLAYER_UP,    Keyboard.control(KeyCode.UP));
        bind(GameActions2D.PLAYER_DOWN,  Keyboard.control(KeyCode.DOWN));
        bind(GameActions2D.PLAYER_LEFT,  Keyboard.control(KeyCode.LEFT));
        bind(GameActions2D.PLAYER_RIGHT, Keyboard.control(KeyCode.RIGHT));
    }

    default void bindCheatActions() {
        bind(GameActions2D.CHEAT_EAT_ALL,     Keyboard.alt(KeyCode.E));
        bind(GameActions2D.CHEAT_ADD_LIVES,   Keyboard.alt(KeyCode.L));
        bind(GameActions2D.CHEAT_NEXT_LEVEL,  Keyboard.alt(KeyCode.N));
        bind(GameActions2D.CHEAT_KILL_GHOSTS, Keyboard.alt(KeyCode.X));
    }

    default void bindTestsStartingActions() {
        bind(GameActions2D.TEST_CUT_SCENES,     Keyboard.alt(KeyCode.C));
        bind(GameActions2D.TEST_LEVELS_BONI,    Keyboard.alt(KeyCode.T));
        bind(GameActions2D.TEST_LEVELS_TEASERS, Keyboard.shift_alt(KeyCode.T));
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