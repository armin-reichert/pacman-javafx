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

    Map<GameAction, Set<KeyCombination>> COMMON_BINDING_MAP = Map.ofEntries(
        actionBinding(ARCADE_INSERT_COIN,      nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        actionBinding(ARCADE_START_GAME,       nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        actionBinding(BOOT_SHOW_GAME_VIEW,     nude(KeyCode.F3)),
        actionBinding(CHEAT_EAT_ALL_PELLETS,   alt(KeyCode.E)),
        actionBinding(CHEAT_ADD_LIVES,         alt(KeyCode.L)),
        actionBinding(CHEAT_ENTER_NEXT_LEVEL,  alt(KeyCode.N)),
        actionBinding(CHEAT_KILL_GHOSTS,       alt(KeyCode.X)),
        actionBinding(PERSPECTIVE_PREVIOUS,    alt(KeyCode.LEFT)),
        actionBinding(PERSPECTIVE_NEXT,        alt(KeyCode.RIGHT)),
        actionBinding(PLAYER_UP,               nude(KeyCode.UP), control(KeyCode.UP)),
        actionBinding(PLAYER_DOWN,             nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        actionBinding(PLAYER_LEFT,             nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        actionBinding(PLAYER_RIGHT,            nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        actionBinding(QUIT_GAME_SCENE,         nude(KeyCode.Q)),
        actionBinding(SIMULATION_SLOWER,       alt(KeyCode.MINUS)),
        actionBinding(SIMULATION_FASTER,       alt(KeyCode.PLUS)),
        actionBinding(SIMULATION_RESET,        alt(KeyCode.DIGIT0)),
        actionBinding(SIMULATION_ONE_STEP,     shift(KeyCode.P)),
        actionBinding(SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE)),
        actionBinding(TEST_CUT_SCENES,         alt(KeyCode.C)),
        actionBinding(TEST_LEVELS_BONI,        alt(KeyCode.T)),
        actionBinding(TEST_LEVELS_TEASERS,     alt_shift(KeyCode.T)),
        actionBinding(TOGGLE_AUTOPILOT,        alt(KeyCode.A)),
        actionBinding(TOGGLE_DEBUG_INFO,       alt(KeyCode.D)),
        actionBinding(TOGGLE_PAUSED,           nude(KeyCode.P)),
        actionBinding(TOGGLE_DASHBOARD,        nude(KeyCode.F1), alt(KeyCode.B)),
        actionBinding(TOGGLE_IMMUNITY,         alt(KeyCode.I)),
        actionBinding(TOGGLE_PIP_VISIBILITY,   nude(KeyCode.F2)),
        actionBinding(TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        actionBinding(TOGGLE_DRAW_MODE,        alt(KeyCode.W))
    );

    default void bindActionToCommonKeys(GameAction gameAction) {
        bindActionToKeys(gameAction, COMMON_BINDING_MAP);
    }

    default void bindActionToKeys(GameAction gameAction, Map<GameAction, Set<KeyCombination>> bindingMap) {
        requireNonNull(gameAction);
        if (bindingMap.containsKey(gameAction)) {
            bind(gameAction, bindingMap.get(gameAction));
        } else {
            Logger.error("No keyboard binding found for game action {}", gameAction);
        }
    }
}