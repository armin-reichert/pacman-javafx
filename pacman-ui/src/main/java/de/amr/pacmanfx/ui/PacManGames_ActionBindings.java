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

    private static Map.Entry<GameAction, List<KeyCombination>> mapping(GameAction action, KeyCombination... combinations) {
        return entry(action, List.of(combinations));
    }

    Map<GameAction, List<KeyCombination>> DEFAULT_MAPPINGS = Map.ofEntries(
        mapping(ARCADE_INSERT_COIN,      nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        mapping(ARCADE_START_GAME,       nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        mapping(BOOT_SHOW_GAME_VIEW,     nude(KeyCode.F3)),
        mapping(CHEAT_EAT_ALL_PELLETS,   alt(KeyCode.E)),
        mapping(CHEAT_ADD_LIVES,         alt(KeyCode.L)),
        mapping(CHEAT_ENTER_NEXT_LEVEL,  alt(KeyCode.N)),
        mapping(CHEAT_KILL_GHOSTS,       alt(KeyCode.X)),
        mapping(PERSPECTIVE_PREVIOUS,    alt(KeyCode.LEFT)),
        mapping(PERSPECTIVE_NEXT,        alt(KeyCode.RIGHT)),
        mapping(PLAYER_UP,               nude(KeyCode.UP), control(KeyCode.UP)),
        mapping(PLAYER_DOWN,             nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        mapping(PLAYER_LEFT,             nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        mapping(PLAYER_RIGHT,            nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        mapping(QUIT_GAME_SCENE,         nude(KeyCode.Q)),
        mapping(SIMULATION_SLOWER,       alt(KeyCode.MINUS)),
        mapping(SIMULATION_FASTER,       alt(KeyCode.PLUS)),
        mapping(SIMULATION_RESET,        alt(KeyCode.DIGIT0)),
        mapping(SIMULATION_ONE_STEP,     shift(KeyCode.P)),
        mapping(SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE)),
        mapping(TEST_CUT_SCENES,         alt(KeyCode.C)),
        mapping(TEST_LEVELS_BONI,        alt(KeyCode.T)),
        mapping(TEST_LEVELS_TEASERS,     alt_shift(KeyCode.T)),
        mapping(TOGGLE_AUTOPILOT,        alt(KeyCode.A)),
        mapping(TOGGLE_DEBUG_INFO,       alt(KeyCode.D)),
        mapping(TOGGLE_PAUSED,           nude(KeyCode.P)),
        mapping(TOGGLE_DASHBOARD,        nude(KeyCode.F1), alt(KeyCode.B)),
        mapping(TOGGLE_IMMUNITY,         alt(KeyCode.I)),
        mapping(TOGGLE_PIP_VISIBILITY,   nude(KeyCode.F2)),
        mapping(TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        mapping(TOGGLE_DRAW_MODE,        alt(KeyCode.W))
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