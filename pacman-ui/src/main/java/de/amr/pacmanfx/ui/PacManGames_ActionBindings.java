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

import java.util.Map;
import java.util.Set;

import static de.amr.pacmanfx.ui.PacManGames_Actions.*;
import static de.amr.pacmanfx.uilib.ActionBindingsProvider.actionBinding;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;
import static java.util.Objects.requireNonNull;

public interface PacManGames_ActionBindings extends ActionBindingsProvider {

    Map<GameAction, Set<KeyCombination>> COMMON_ACTION_BINDINGS = Map.ofEntries(
        actionBinding(ACTION_ARCADE_INSERT_COIN,      nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        actionBinding(ACTION_ARCADE_START_GAME,       nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        actionBinding(ACTION_BOOT_SHOW_GAME_VIEW,     nude(KeyCode.F3)),
        actionBinding(ACTION_CHEAT_EAT_ALL_PELLETS,   alt(KeyCode.E)),
        actionBinding(ACTION_CHEAT_ADD_LIVES,         alt(KeyCode.L)),
        actionBinding(ACTION_CHEAT_ENTER_NEXT_LEVEL,  alt(KeyCode.N)),
        actionBinding(ACTION_CHEAT_KILL_GHOSTS,       alt(KeyCode.X)),
        actionBinding(ACTION_PERSPECTIVE_PREVIOUS,    alt(KeyCode.LEFT)),
        actionBinding(ACTION_PERSPECTIVE_NEXT,        alt(KeyCode.RIGHT)),
        actionBinding(ACTION_PLAYER_UP,               nude(KeyCode.UP), control(KeyCode.UP)),
        actionBinding(ACTION_PLAYER_DOWN,             nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        actionBinding(ACTION_PLAYER_LEFT,             nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        actionBinding(ACTION_PLAYER_RIGHT,            nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        actionBinding(ACTION_QUIT_GAME_SCENE,         nude(KeyCode.Q)),
        actionBinding(ACTION_SIMULATION_SLOWER,       alt(KeyCode.MINUS)),
        actionBinding(ACTION_SIMULATION_FASTER,       alt(KeyCode.PLUS)),
        actionBinding(ACTION_SIMULATION_RESET,        alt(KeyCode.DIGIT0)),
        actionBinding(ACTION_SIMULATION_ONE_STEP,     shift(KeyCode.P)),
        actionBinding(ACTION_SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE)),
        actionBinding(ACTION_TEST_CUT_SCENES,         alt(KeyCode.C)),
        actionBinding(ACTION_TEST_LEVELS_BONI,        alt(KeyCode.T)),
        actionBinding(ACTION_TEST_LEVELS_TEASERS,     alt_shift(KeyCode.T)),
        actionBinding(ACTION_TOGGLE_AUTOPILOT,        alt(KeyCode.A)),
        actionBinding(ACTION_TOGGLE_DEBUG_INFO,       alt(KeyCode.D)),
        actionBinding(ACTION_TOGGLE_PAUSED,           nude(KeyCode.P)),
        actionBinding(ACTION_TOGGLE_DASHBOARD,        nude(KeyCode.F1), alt(KeyCode.B)),
        actionBinding(ACTION_TOGGLE_IMMUNITY,         alt(KeyCode.I)),
        actionBinding(ACTION_TOGGLE_PIP_VISIBILITY,   nude(KeyCode.F2)),
        actionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        actionBinding(ACTION_TOGGLE_DRAW_MODE,        alt(KeyCode.W))
    );

    default void bindAction(GameAction gameAction) {
        bindAction(gameAction, COMMON_ACTION_BINDINGS);
    }

    default void bindAction(GameAction gameAction, Map<GameAction, Set<KeyCombination>> bindingMap) {
        requireNonNull(gameAction);
        if (bindingMap.containsKey(gameAction)) {
            bindActionToKeys(gameAction, bindingMap.get(gameAction));
        } else {
            Logger.error("No keyboard binding found for game action {}", gameAction);
        }
    }
}