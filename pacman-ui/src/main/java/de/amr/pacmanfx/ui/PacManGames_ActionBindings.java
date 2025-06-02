/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.uilib.ActionBindingManager;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui.PacManGames_Env.theJoypad;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;

public interface PacManGames_ActionBindings extends ActionBindingManager {

    default void bindArcadeInsertCoinAction() {
        bind(PacManGames_Actions.INSERT_COIN,  naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5));
    }

    default void bindArcadeStartGameAction() {
        bind(PacManGames_Actions.START_ARCADE_GAME, naked(KeyCode.DIGIT1), naked(KeyCode.NUMPAD1));
    }

    default void bindCheatActions() {
        bind(PacManGames_Actions.CHEAT_EAT_ALL_PELLETS, alt(KeyCode.E));
        bind(PacManGames_Actions.CHEAT_ADD_LIVES, alt(KeyCode.L));
        bind(PacManGames_Actions.CHEAT_ENTER_NEXT_LEVEL, alt(KeyCode.N));
        bind(PacManGames_Actions.CHEAT_KILL_GHOSTS, alt(KeyCode.X));
    }

    default void bindArcadePlayerSteeringActions() {
        bind(PacManGames_Actions.PLAYER_UP,    naked(KeyCode.UP),    control(KeyCode.UP));
        bind(PacManGames_Actions.PLAYER_DOWN,  naked(KeyCode.DOWN),  control(KeyCode.DOWN));
        bind(PacManGames_Actions.PLAYER_LEFT,  naked(KeyCode.LEFT),  control(KeyCode.LEFT));
        bind(PacManGames_Actions.PLAYER_RIGHT, naked(KeyCode.RIGHT), control(KeyCode.RIGHT));
    }

    default void bindJoypadPlayerSteeringActions() {
        bind(PacManGames_Actions.PLAYER_UP,    theJoypad().key(JoypadButton.UP),    control(KeyCode.UP));
        bind(PacManGames_Actions.PLAYER_DOWN,  theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN));
        bind(PacManGames_Actions.PLAYER_LEFT,  theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT));
        bind(PacManGames_Actions.PLAYER_RIGHT, theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT));
    }

    default void bindStartTestsActions() {
        bind(PacManGames_Actions.TEST_CUT_SCENES,     alt(KeyCode.C));
        bind(PacManGames_Actions.TEST_LEVELS_BONI,    alt(KeyCode.T));
        bind(PacManGames_Actions.TEST_LEVELS_TEASERS, Keyboard.alt_shift(KeyCode.T));
    }

    default void bindScene3DActions() {
        bind(PacManGames_Actions.PERSPECTIVE_PREVIOUS, alt(KeyCode.LEFT));
        bind(PacManGames_Actions.PERSPECTIVE_NEXT, alt(KeyCode.RIGHT));
        bind(PacManGames_Actions.TOGGLE_DRAW_MODE, alt(KeyCode.W));
    }
}