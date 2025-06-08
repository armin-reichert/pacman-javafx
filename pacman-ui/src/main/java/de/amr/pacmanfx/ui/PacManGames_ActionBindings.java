/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.ActionBindingsProvider;
import de.amr.pacmanfx.uilib.GameAction;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.ui.PacManGames_Actions.*;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;
import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

public interface PacManGames_ActionBindings extends ActionBindingsProvider {

    Map<GameAction, List<KeyCombination>> DEFAULT_MAPPINGS = Map.ofEntries(
        entry(ARCADE_INSERT_COIN,      List.of(nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5))),
        entry(ARCADE_START_GAME,       List.of(nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1))),
        entry(BOOT_SHOW_GAME_VIEW,     List.of(nude(KeyCode.F3))),
        entry(CHEAT_EAT_ALL_PELLETS,   List.of(alt(KeyCode.E))),
        entry(CHEAT_ADD_LIVES,         List.of(alt(KeyCode.L))),
        entry(CHEAT_ENTER_NEXT_LEVEL,  List.of(alt(KeyCode.N))),
        entry(CHEAT_KILL_GHOSTS,       List.of(alt(KeyCode.X))),
        entry(PERSPECTIVE_PREVIOUS,    List.of(alt(KeyCode.LEFT))),
        entry(PERSPECTIVE_NEXT,        List.of(alt(KeyCode.RIGHT))),
        entry(PLAYER_UP,               List.of(nude(KeyCode.UP), control(KeyCode.UP))),
        entry(PLAYER_DOWN,             List.of(nude(KeyCode.DOWN), control(KeyCode.DOWN))),
        entry(PLAYER_LEFT,             List.of(nude(KeyCode.LEFT), control(KeyCode.LEFT))),
        entry(PLAYER_RIGHT,            List.of(nude(KeyCode.RIGHT), control(KeyCode.RIGHT))),
        entry(QUIT_GAME_SCENE,         List.of(nude(KeyCode.Q))),
        entry(SIMULATION_SLOWER,       List.of(alt(KeyCode.MINUS))),
        entry(SIMULATION_FASTER,       List.of(alt(KeyCode.PLUS))),
        entry(SIMULATION_RESET,        List.of(alt(KeyCode.DIGIT0))),
        entry(SIMULATION_ONE_STEP,     List.of(shift(KeyCode.P))),
        entry(SIMULATION_TEN_STEPS,    List.of(shift(KeyCode.SPACE))),
        entry(TEST_CUT_SCENES,         List.of(alt(KeyCode.C))),
        entry(TEST_LEVELS_BONI,        List.of(alt(KeyCode.T))),
        entry(TEST_LEVELS_TEASERS,     List.of(alt_shift(KeyCode.T))),
        entry(TOGGLE_AUTOPILOT,        List.of(alt(KeyCode.A))),
        entry(TOGGLE_DEBUG_INFO,       List.of(alt(KeyCode.D))),
        entry(TOGGLE_PAUSED,           List.of(nude(KeyCode.P))),
        entry(TOGGLE_DASHBOARD,        List.of(nude(KeyCode.F1), alt(KeyCode.B))),
        entry(TOGGLE_IMMUNITY,         List.of(alt(KeyCode.I))),
        entry(TOGGLE_PIP_VISIBILITY,   List.of(nude(KeyCode.F2))),
        entry(TOGGLE_PLAY_SCENE_2D_3D, List.of(alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3))),
        entry(TOGGLE_DRAW_MODE,        List.of(alt(KeyCode.W)))
    );

    default void bindToDefaultKeys(GameAction gameAction) {
        requireNonNull(gameAction);
        if (DEFAULT_MAPPINGS.containsKey(gameAction)) {
            bind(gameAction, DEFAULT_MAPPINGS.get(gameAction));
        } else {
            Logger.error("No keyboard binding found for game action {}", gameAction);
        }
    }
}