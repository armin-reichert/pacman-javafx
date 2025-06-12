/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.ActionBindingSupport;
import de.amr.pacmanfx.uilib.GameAction;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.uilib.ActionBindingSupport.createBinding;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;
import static java.util.Objects.requireNonNull;

/**
 * Actions bindings available to all Pac-Man game scenes.
 */
public interface PacManGames_ActionBinding extends ActionBindingSupport {

    Map<GameAction, Set<KeyCombination>> COMMON_ACTION_BINDINGS = Map.ofEntries(
        createBinding(ACTION_ARCADE_INSERT_COIN,      nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        createBinding(ACTION_ARCADE_START_GAME,       nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        createBinding(ACTION_BOOT_SHOW_GAME_VIEW,     nude(KeyCode.F3)),
        createBinding(ACTION_CHEAT_EAT_ALL_PELLETS,   alt(KeyCode.E)),
        createBinding(ACTION_CHEAT_ADD_LIVES,         alt(KeyCode.L)),
        createBinding(ACTION_CHEAT_ENTER_NEXT_LEVEL,  alt(KeyCode.N)),
        createBinding(ACTION_CHEAT_KILL_GHOSTS,       alt(KeyCode.X)),
        createBinding(ACTION_PERSPECTIVE_PREVIOUS,    alt(KeyCode.LEFT)),
        createBinding(ACTION_PERSPECTIVE_NEXT,        alt(KeyCode.RIGHT)),
        createBinding(ACTION_STEER_UP,               nude(KeyCode.UP), control(KeyCode.UP)),
        createBinding(ACTION_STEER_DOWN,             nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        createBinding(ACTION_STEER_LEFT,             nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        createBinding(ACTION_STEER_RIGHT,            nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        createBinding(ACTION_QUIT_GAME_SCENE,         nude(KeyCode.Q)),
        createBinding(ACTION_SIMULATION_SLOWER,       alt(KeyCode.MINUS)),
        createBinding(ACTION_SIMULATION_FASTER,       alt(KeyCode.PLUS)),
        createBinding(ACTION_SIMULATION_RESET,        alt(KeyCode.DIGIT0)),
        createBinding(ACTION_SIMULATION_ONE_STEP,     shift(KeyCode.P)),
        createBinding(ACTION_SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE)),
        createBinding(ACTION_TEST_CUT_SCENES,         alt(KeyCode.C)),
        createBinding(ACTION_TEST_LEVELS_BONI,        alt(KeyCode.T)),
        createBinding(ACTION_TEST_LEVELS_TEASERS,     alt_shift(KeyCode.T)),
        createBinding(ACTION_TOGGLE_AUTOPILOT,        alt(KeyCode.A)),
        createBinding(ACTION_TOGGLE_DEBUG_INFO,       alt(KeyCode.D)),
        createBinding(ACTION_TOGGLE_PAUSED,           nude(KeyCode.P)),
        createBinding(ACTION_TOGGLE_DASHBOARD,        nude(KeyCode.F1), alt(KeyCode.B)),
        createBinding(ACTION_TOGGLE_IMMUNITY,         alt(KeyCode.I)),
        createBinding(ACTION_TOGGLE_PIP_VISIBILITY,   nude(KeyCode.F2)),
        createBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        createBinding(ACTION_TOGGLE_DRAW_MODE,        alt(KeyCode.W))
    );

    default void bindAction(GameAction gameAction, Map<GameAction, Set<KeyCombination>> bindings) {
        requireNonNull(gameAction);
        requireNonNull(bindings);
        if (bindings.values().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found null value in key bindings map");
        }
        if (bindings.containsKey(gameAction)) {
            for (KeyCombination combination : bindings.get(gameAction)) {
                actionBindings().put(combination, gameAction);
            }
        } else {
            Logger.error("No keyboard binding found for game action {}", gameAction);
        }
    }
}