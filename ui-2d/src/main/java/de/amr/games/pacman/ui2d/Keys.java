/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import static de.amr.games.pacman.ui2d.util.Keyboard.*;

/**
 * @author Armin Reichert
 */
public interface Keys {
    KeyCodeCombination   HELP                = key(KeyCode.H);
    KeyCodeCombination   PAUSE               = key(KeyCode.P);
    KeyCodeCombination   QUIT                = key(KeyCode.Q);
    KeyCodeCombination[] NEXT_VARIANT        = {key(KeyCode.V), key(KeyCode.RIGHT)};
    KeyCodeCombination   PREV_VARIANT        = key(KeyCode.LEFT);
    KeyCodeCombination   AUTOPILOT           = alt_key(KeyCode.A);
    KeyCodeCombination   IMMUNITY            = alt_key(KeyCode.I);
    KeyCodeCombination   CUTSCENES           = alt_key(KeyCode.C);
    KeyCodeCombination   DEBUG_INFO          = alt_key(KeyCode.D);
    KeyCodeCombination   CHEAT_EAT_ALL       = alt_key(KeyCode.E);
    KeyCodeCombination   CHEAT_ADD_LIVES     = alt_key(KeyCode.L);
    KeyCodeCombination   CHEAT_NEXT_LEVEL    = alt_key(KeyCode.N);
    KeyCodeCombination   CHEAT_KILL_GHOSTS   = alt_key(KeyCode.X);
    KeyCodeCombination   TEST_MODE           = alt_key(KeyCode.T);
    KeyCodeCombination[] ENTER_GAME_PAGE     = {key(KeyCode.SPACE), key(KeyCode.ENTER)};
    KeyCodeCombination[] START_GAME          = {key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1)};
    KeyCodeCombination[] ADD_CREDIT          = {key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5)};
    KeyCodeCombination   BOOT                = key(KeyCode.F3);
    KeyCodeCombination   FULLSCREEN          = key(KeyCode.F11);
    KeyCodeCombination   EDITOR              = shift_alt_key(KeyCode.E);
    KeyCodeCombination[] DASHBOARD           = {key(KeyCode.F1), alt_key(KeyCode.B)};
    KeyCodeCombination   PIP_VIEW            = key(KeyCode.F2);
    KeyCodeCombination   TWO_D_THREE_D       = alt_key(KeyCode.DIGIT3);
    KeyCodeCombination[] SIMULATION_STEP     = {key(KeyCode.SPACE), shift_key(KeyCode.P)};
    KeyCodeCombination   SIMULATION_10_STEPS = shift_key(KeyCode.SPACE);
    KeyCodeCombination   SIMULATION_FASTER   = alt_key(KeyCode.PLUS);
    KeyCodeCombination   SIMULATION_SLOWER   = alt_key(KeyCode.MINUS);
    KeyCodeCombination   SIMULATION_NORMAL   = alt_key(KeyCode.DIGIT0);
    KeyCodeCombination   PREV_PERSPECTIVE    = alt_key(KeyCode.LEFT);
    KeyCodeCombination   NEXT_PERSPECTIVE    = alt_key(KeyCode.RIGHT);
}
