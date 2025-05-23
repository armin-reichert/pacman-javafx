/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.uilib.ActionProvider;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui.PacManGamesEnv.theJoypad;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;

public interface CommonActionProvider extends ActionProvider {

    default void bindArcadeInsertCoinAction() {
        bind(GameAction.INSERT_COIN,  naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5));
    }

    default void bindArcadeStartGameAction() {
        bind(GameAction.START_ARCADE_GAME, naked(KeyCode.DIGIT1), naked(KeyCode.NUMPAD1));
    }

    default void bindCheatActions() {
        bind(GameAction.CHEAT_EAT_ALL_PELLETS, alt(KeyCode.E));
        bind(GameAction.CHEAT_ADD_LIVES, alt(KeyCode.L));
        bind(GameAction.CHEAT_ENTER_NEXT_LEVEL, alt(KeyCode.N));
        bind(GameAction.CHEAT_KILL_GHOSTS, alt(KeyCode.X));
    }

    default void bindArcadePlayerActions() {
        bind(GameAction.createPlayerSteeringAction(Direction.UP),    naked(KeyCode.UP),    control(KeyCode.UP));
        bind(GameAction.createPlayerSteeringAction(Direction.DOWN),  naked(KeyCode.DOWN),  control(KeyCode.DOWN));
        bind(GameAction.createPlayerSteeringAction(Direction.LEFT),  naked(KeyCode.LEFT),  control(KeyCode.LEFT));
        bind(GameAction.createPlayerSteeringAction(Direction.RIGHT), naked(KeyCode.RIGHT), control(KeyCode.RIGHT));
    }

    default void bindJoypadPlayerActions() {
        bind(GameAction.createPlayerSteeringAction(Direction.UP),    theJoypad().key(JoypadButton.UP),    control(KeyCode.UP));
        bind(GameAction.createPlayerSteeringAction(Direction.DOWN),  theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN));
        bind(GameAction.createPlayerSteeringAction(Direction.LEFT),  theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT));
        bind(GameAction.createPlayerSteeringAction(Direction.RIGHT), theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT));
    }

    default void bindStartTestsActions() {
        bind(GameAction.TEST_CUT_SCENES,     alt(KeyCode.C));
        bind(GameAction.TEST_LEVELS_BONI,    alt(KeyCode.T));
        bind(GameAction.TEST_LEVELS_TEASERS, Keyboard.shift_alt(KeyCode.T));
    }
}
