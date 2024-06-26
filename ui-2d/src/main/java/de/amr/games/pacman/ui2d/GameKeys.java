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

    HELP                (key(KeyCode.H)),
    PAUSE               (key(KeyCode.P)),
    QUIT                (key(KeyCode.Q)),
    NEXT_VARIANT        (key(KeyCode.V), key(KeyCode.RIGHT)),
    PREV_VARIANT        (key(KeyCode.LEFT)),
    AUTOPILOT           (alt(KeyCode.A)),
    IMMUNITY            (alt(KeyCode.I)),
    CUTSCENES           (alt(KeyCode.C)),
    DEBUG_INFO          (alt(KeyCode.D)),
    CHEAT_EAT_ALL       (alt(KeyCode.E)),
    CHEAT_ADD_LIVES     (alt(KeyCode.L)),
    CHEAT_NEXT_LEVEL    (alt(KeyCode.N)),
    CHEAT_KILL_GHOSTS   (alt(KeyCode.X)),
    TEST_MODE           (alt(KeyCode.T)),
    ENTER_GAME_PAGE     (key(KeyCode.SPACE), key(KeyCode.ENTER)),
    START_GAME          (key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1)),
    ADD_CREDIT          (key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5)),
    BOOT                (key(KeyCode.F3)),
    FULLSCREEN          (key(KeyCode.F11)),
    EDITOR              (shift_alt(KeyCode.E)),
    DASHBOARD           (key(KeyCode.F1), alt(KeyCode.B)),
    PIP_VIEW            (key(KeyCode.F2)),
    TWO_D_THREE_D       (alt(KeyCode.DIGIT3)),
    SIMULATION_STEP     (key(KeyCode.SPACE), shift(KeyCode.P)),
    SIMULATION_10_STEPS (shift(KeyCode.SPACE)),
    SIMULATION_FASTER   (alt(KeyCode.PLUS)),
    SIMULATION_SLOWER   (alt(KeyCode.MINUS)),
    SIMULATION_NORMAL   (alt(KeyCode.DIGIT0)),
    PREV_PERSPECTIVE    (alt(KeyCode.LEFT)),
    NEXT_PERSPECTIVE    (alt(KeyCode.RIGHT));

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
