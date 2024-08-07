/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui2d.util.KeyInput.*;

/**
 * @author Armin Reichert
 */
public enum GameKeys {

    ADD_CREDIT          (key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5)),
    AUTOPILOT           (alt(KeyCode.A)),
    BOOT                (key(KeyCode.F3)),
    CHEAT_ADD_LIVES     (alt(KeyCode.L)),
    CHEAT_EAT_ALL       (alt(KeyCode.E)),
    CHEAT_KILL_GHOSTS   (alt(KeyCode.X)),
    CHEAT_NEXT_LEVEL    (alt(KeyCode.N)),
    CUTSCENES           (alt(KeyCode.C)),
    DASHBOARD           (key(KeyCode.F1), alt(KeyCode.B)),
    DEBUG_INFO          (alt(KeyCode.D)),
    EDITOR              (shift_alt(KeyCode.E)),
    ENTER_GAME_PAGE     (key(KeyCode.SPACE), key(KeyCode.ENTER)),
    FULLSCREEN          (key(KeyCode.F11)),
    HELP                (key(KeyCode.H)),
    IMMUNITY            (alt(KeyCode.I)),
    MUTE                (alt(KeyCode.M)),
    NEXT_PERSPECTIVE    (alt(KeyCode.RIGHT)),
    NEXT_VARIANT        (key(KeyCode.V), key(KeyCode.RIGHT)),
    PAUSE               (key(KeyCode.P)),
    PIP_VIEW            (key(KeyCode.F2)),
    PREV_PERSPECTIVE    (alt(KeyCode.LEFT)),
    PREV_VARIANT        (key(KeyCode.LEFT)),
    QUIT                (key(KeyCode.Q)),
    SIMULATION_FASTER   (alt(KeyCode.PLUS)),
    SIMULATION_NORMAL   (alt(KeyCode.DIGIT0)),
    SIMULATION_SLOWER   (alt(KeyCode.MINUS)),
    SIMULATION_1_STEP   (key(KeyCode.SPACE), shift(KeyCode.P)),
    SIMULATION_10_STEPS (shift(KeyCode.SPACE)),
    START_GAME          (key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1)),
    TEST_MODE           (alt(KeyCode.T)),
    TWO_D_THREE_D       (alt(KeyCode.DIGIT3));

    GameKeys(KeyCodeCombination... combinations) {
        input = KeyInput.of(combinations);
    }

    /**
     * @return {@code true} if any key combination defined for this game key is pressed
     */
    public boolean pressed() {
        if (Keyboard.pressed(input)) {
            Logger.debug("{} pressed", this);
            return true;
        }
        return false;
    }

    private final KeyInput input;
}
