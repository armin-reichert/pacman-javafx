/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.ActionProvider;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.uilib.input.Keyboard.alt;
import static de.amr.pacmanfx.uilib.input.Keyboard.naked;

public interface CommonActionProvider extends ActionProvider {

    default void bindArcadeInsertCoinAction() {
        bind(GameAction.INSERT_COIN,  naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5));
    }

    default void bindArcadeStartGameAction() {
        bind(GameAction.START_ARCADE_GAME, naked(KeyCode.DIGIT1), naked(KeyCode.NUMPAD1));
    }

    default void bindStartTestsActions() {
        bind(GameAction.TEST_CUT_SCENES,     alt(KeyCode.C));
        bind(GameAction.TEST_LEVELS_BONI,    alt(KeyCode.T));
        bind(GameAction.TEST_LEVELS_TEASERS, Keyboard.shift_alt(KeyCode.T));
    }

    default void bindCheatActions() {
        bind(GameAction.CHEAT_EAT_ALL_PELLETS, alt(KeyCode.E));
        bind(GameAction.CHEAT_ADD_LIVES, alt(KeyCode.L));
        bind(GameAction.CHEAT_ENTER_NEXT_LEVEL, alt(KeyCode.N));
        bind(GameAction.CHEAT_KILL_GHOSTS, alt(KeyCode.X));
    }

}
