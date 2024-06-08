/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.lang.reflect.Array;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.util.Keyboard.*;

/**
 * @author Armin Reichert
 */
public abstract class Keys {

    public static final KeyCodeCombination   HELP                = key(KeyCode.H);
    public static final KeyCodeCombination   PAUSE               = key(KeyCode.P);
    public static final KeyCodeCombination   QUIT                = key(KeyCode.Q);
    public static final KeyCodeCombination[] NEXT_VARIANT        = {key(KeyCode.V), key(KeyCode.RIGHT)};
    public static final KeyCodeCombination   PREV_VARIANT        = key(KeyCode.LEFT);
    public static final KeyCodeCombination   AUTOPILOT           = alt_key(KeyCode.A);
    public static final KeyCodeCombination   IMMUNITY            = alt_key(KeyCode.I);
    public static final KeyCodeCombination   CUTSCENES           = alt_key(KeyCode.C);
    public static final KeyCodeCombination   DEBUG_INFO          = alt_key(KeyCode.D);
    public static final KeyCodeCombination   CHEAT_EAT_ALL       = alt_key(KeyCode.E);
    public static final KeyCodeCombination   CHEAT_ADD_LIVES     = alt_key(KeyCode.L);
    public static final KeyCodeCombination   CHEAT_NEXT_LEVEL    = alt_key(KeyCode.N);
    public static final KeyCodeCombination   CHEAT_KILL_GHOSTS   = alt_key(KeyCode.X);
    public static final KeyCodeCombination   TEST_MODE           = alt_key(KeyCode.T);
    public static final KeyCodeCombination[] ENTER_GAME_PAGE     = {key(KeyCode.SPACE), key(KeyCode.ENTER)};
    public static final KeyCodeCombination[] START_GAME          = {key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1)};
    public static final KeyCodeCombination[] ADD_CREDIT          = {key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5)};
    public static final KeyCodeCombination   BOOT                = key(KeyCode.F3);
    public static final KeyCodeCombination   FULLSCREEN          = key(KeyCode.F11);
    public static final KeyCodeCombination   EDITOR              = shift_alt_key(KeyCode.E);
    public static final KeyCodeCombination[] DASHBOARD           = {key(KeyCode.F1), alt_key(KeyCode.B)};
    public static final KeyCodeCombination   PIP_VIEW            = key(KeyCode.F2);
    public static final KeyCodeCombination   TWO_D_THREE_D       = alt_key(KeyCode.DIGIT3);
    public static final KeyCodeCombination[] SIMULATION_STEP     = {key(KeyCode.SPACE), shift_key(KeyCode.P)};
    public static final KeyCodeCombination   SIMULATION_10_STEPS = shift_key(KeyCode.SPACE);
    public static final KeyCodeCombination   SIMULATION_FASTER   = alt_key(KeyCode.PLUS);
    public static final KeyCodeCombination   SIMULATION_SLOWER   = alt_key(KeyCode.MINUS);
    public static final KeyCodeCombination   SIMULATION_NORMAL   = alt_key(KeyCode.DIGIT0);
    public static final KeyCodeCombination   PREV_PERSPECTIVE    = alt_key(KeyCode.LEFT);
    public static final KeyCodeCombination   NEXT_PERSPECTIVE    = alt_key(KeyCode.RIGHT);

    public static void register() {
        for (var key : Keys.class.getDeclaredFields()) {
            Logger.info("Key code combination {} registered", key.getName());
        }
    }
}
