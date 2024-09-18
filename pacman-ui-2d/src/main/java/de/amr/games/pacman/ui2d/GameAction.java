/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import static de.amr.games.pacman.ui2d.util.KeyInput.*;

/**
 * @author Armin Reichert
 */
public enum GameAction {
    ADD_CREDIT          (key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5), key(KeyCode.UP)),
    AUTOPILOT           (alt(KeyCode.A)),
    BOOT                (key(KeyCode.F3)),
    CHEAT_ADD_LIVES     (alt(KeyCode.L)),
    CHEAT_EAT_ALL       (alt(KeyCode.E)),
    CHEAT_KILL_GHOSTS   (alt(KeyCode.X)),
    CHEAT_NEXT_LEVEL    (alt(KeyCode.N)),
    CUTSCENES           (alt(KeyCode.C)),
    DEBUG_INFO          (alt(KeyCode.D)),
    ENTER_GAME_PAGE     (key(KeyCode.SPACE), key(KeyCode.ENTER)),
    FULLSCREEN          (key(KeyCode.F11)),
    HELP                (key(KeyCode.H)),
    IMMUNITY            (alt(KeyCode.I)),
    MUTE                (alt(KeyCode.M)),
    NEXT_FLYER_PAGE     (key(KeyCode.DOWN)),
    NEXT_PERSPECTIVE    (alt(KeyCode.RIGHT)),
    NEXT_VARIANT        (key(KeyCode.V), key(KeyCode.RIGHT)),
    PAUSE               (key(KeyCode.P)),
    OPEN_EDITOR         (shift_alt(KeyCode.E)),
    PREV_FLYER_PAGE     (key(KeyCode.UP)),
    PREV_PERSPECTIVE    (alt(KeyCode.LEFT)),
    PREV_VARIANT        (key(KeyCode.LEFT)),
    QUIT                (key(KeyCode.Q)),
    SIMULATION_FASTER   (alt(KeyCode.PLUS)),
    SIMULATION_NORMAL   (alt(KeyCode.DIGIT0)),
    SIMULATION_SLOWER   (alt(KeyCode.MINUS)),
    SIMULATION_1_STEP   (key(KeyCode.SPACE), shift(KeyCode.P)),
    SIMULATION_10_STEPS (shift(KeyCode.SPACE)),
    START_GAME          (key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1), key(KeyCode.ENTER), key(KeyCode.SPACE)),
    START_TEST_MODE     (alt(KeyCode.T)),
    TOGGLE_DASHBOARD    (key(KeyCode.F1), alt(KeyCode.B)),
    TOGGLE_PIP_VIEW     (key(KeyCode.F2)),
    TWO_D_THREE_D       (alt(KeyCode.DIGIT3));

    GameAction(KeyCodeCombination... combinations) {
        trigger = KeyInput.register(combinations);
    }

    public KeyInput trigger() {
        return trigger;
    }

    /**
     * @return {@code true} if any key combination defined for this game key is pressed
     */
    public boolean triggered() {
        return Keyboard.pressed(trigger);
    }

    private final KeyInput trigger;
}