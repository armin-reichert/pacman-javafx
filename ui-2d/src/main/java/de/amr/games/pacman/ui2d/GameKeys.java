/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;
import javafx.scene.input.KeyCode;

import static de.amr.games.pacman.ui2d.util.KeyInput.*;

/**
 * @author Armin Reichert
 */
public abstract class GameKeys {

    public static final KeyInput HELP                = KeyInput.of(key(KeyCode.H));
    public static final KeyInput PAUSE               = KeyInput.of(key(KeyCode.P));
    public static final KeyInput QUIT                = KeyInput.of(key(KeyCode.Q));
    public static final KeyInput NEXT_VARIANT        = KeyInput.of(key(KeyCode.V), key(KeyCode.RIGHT));
    public static final KeyInput PREV_VARIANT        = KeyInput.of(key(KeyCode.LEFT));
    public static final KeyInput AUTOPILOT           = KeyInput.of(alt(KeyCode.A));
    public static final KeyInput IMMUNITY            = KeyInput.of(alt(KeyCode.I));
    public static final KeyInput CUTSCENES           = KeyInput.of(alt(KeyCode.C));
    public static final KeyInput DEBUG_INFO          = KeyInput.of(alt(KeyCode.D));
    public static final KeyInput CHEAT_EAT_ALL       = KeyInput.of(alt(KeyCode.E));
    public static final KeyInput CHEAT_ADD_LIVES     = KeyInput.of(alt(KeyCode.L));
    public static final KeyInput CHEAT_NEXT_LEVEL    = KeyInput.of(alt(KeyCode.N));
    public static final KeyInput CHEAT_KILL_GHOSTS   = KeyInput.of(alt(KeyCode.X));
    public static final KeyInput TEST_MODE           = KeyInput.of(alt(KeyCode.T));
    public static final KeyInput ENTER_GAME_PAGE     = KeyInput.of(key(KeyCode.SPACE), key(KeyCode.ENTER));
    public static final KeyInput START_GAME          = KeyInput.of(key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1));
    public static final KeyInput ADD_CREDIT          = KeyInput.of(key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5));
    public static final KeyInput BOOT                = KeyInput.of(key(KeyCode.F3));
    public static final KeyInput FULLSCREEN          = KeyInput.of(key(KeyCode.F11));
    public static final KeyInput EDITOR              = KeyInput.of(shift_alt(KeyCode.E));
    public static final KeyInput DASHBOARD           = KeyInput.of(key(KeyCode.F1), alt(KeyCode.B));
    public static final KeyInput PIP_VIEW            = KeyInput.of(key(KeyCode.F2));
    public static final KeyInput TWO_D_THREE_D       = KeyInput.of(alt(KeyCode.DIGIT3));
    public static final KeyInput SIMULATION_STEP     = KeyInput.of(key(KeyCode.SPACE), shift(KeyCode.P));
    public static final KeyInput SIMULATION_10_STEPS = KeyInput.of(shift(KeyCode.SPACE));
    public static final KeyInput SIMULATION_FASTER   = KeyInput.of(alt(KeyCode.PLUS));
    public static final KeyInput SIMULATION_SLOWER   = KeyInput.of(alt(KeyCode.MINUS));
    public static final KeyInput SIMULATION_NORMAL   = KeyInput.of(alt(KeyCode.DIGIT0));
    public static final KeyInput PREV_PERSPECTIVE    = KeyInput.of(alt(KeyCode.LEFT));
    public static final KeyInput NEXT_PERSPECTIVE    = KeyInput.of(alt(KeyCode.RIGHT));
}
