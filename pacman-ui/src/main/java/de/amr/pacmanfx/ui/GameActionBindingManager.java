/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.uilib.ActionBindingManager;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui.PacManGamesEnv.theJoypad;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;

public interface GameActionBindingManager extends ActionBindingManager {

    default void bindArcadeInsertCoinAction() {
        bind(GameActions.INSERT_COIN,  naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5));
    }

    default void bindArcadeStartGameAction() {
        bind(GameActions.START_ARCADE_GAME, naked(KeyCode.DIGIT1), naked(KeyCode.NUMPAD1));
    }

    default void bindCheatActions() {
        bind(GameActions.CHEAT_EAT_ALL_PELLETS, alt(KeyCode.E));
        bind(GameActions.CHEAT_ADD_LIVES, alt(KeyCode.L));
        bind(GameActions.CHEAT_ENTER_NEXT_LEVEL, alt(KeyCode.N));
        bind(GameActions.CHEAT_KILL_GHOSTS, alt(KeyCode.X));
    }

    default void bindArcadePlayerSteeringActions() {
        bind(GameActions.PLAYER_UP,    naked(KeyCode.UP),    control(KeyCode.UP));
        bind(GameActions.PLAYER_DOWN,  naked(KeyCode.DOWN),  control(KeyCode.DOWN));
        bind(GameActions.PLAYER_LEFT,  naked(KeyCode.LEFT),  control(KeyCode.LEFT));
        bind(GameActions.PLAYER_RIGHT, naked(KeyCode.RIGHT), control(KeyCode.RIGHT));
    }

    default void bindJoypadPlayerSteeringActions() {
        bind(GameActions.PLAYER_UP,    theJoypad().key(JoypadButton.UP),    control(KeyCode.UP));
        bind(GameActions.PLAYER_DOWN,  theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN));
        bind(GameActions.PLAYER_LEFT,  theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT));
        bind(GameActions.PLAYER_RIGHT, theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT));
    }

    default void bindStartTestsActions() {
        bind(GameActions.TEST_CUT_SCENES,     alt(KeyCode.C));
        bind(GameActions.TEST_LEVELS_BONI,    alt(KeyCode.T));
        bind(GameActions.TEST_LEVELS_TEASERS, Keyboard.shift_alt(KeyCode.T));
    }

    default void bindScene3DActions() {
        bind(GameActions.PERSPECTIVE_PREVIOUS, alt(KeyCode.LEFT));
        bind(GameActions.PERSPECTIVE_NEXT, alt(KeyCode.RIGHT));
        bind(GameActions.TOGGLE_DRAW_MODE, alt(KeyCode.W));
    }
}