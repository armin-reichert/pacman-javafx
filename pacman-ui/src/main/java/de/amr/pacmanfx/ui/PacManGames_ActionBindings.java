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

import static de.amr.pacmanfx.uilib.input.Keyboard.*;
import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

public interface PacManGames_ActionBindings extends ActionBindingsProvider {

    Map<GameAction, List<KeyCombination>> DEFAULT_MAPPINGS = Map.ofEntries(
        entry(PacManGames_Actions.ARCADE_INSERT_COIN,     List.of(naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5))),
        entry(PacManGames_Actions.ARCADE_START_GAME,      List.of(naked(KeyCode.DIGIT1), naked(KeyCode.NUMPAD1))),
        entry(PacManGames_Actions.CHEAT_EAT_ALL_PELLETS,  List.of(alt(KeyCode.E))),
        entry(PacManGames_Actions.CHEAT_ADD_LIVES,        List.of(alt(KeyCode.L))),
        entry(PacManGames_Actions.CHEAT_ENTER_NEXT_LEVEL, List.of(alt(KeyCode.N))),
        entry(PacManGames_Actions.CHEAT_KILL_GHOSTS,      List.of(alt(KeyCode.X))),
        entry(PacManGames_Actions.PLAYER_UP,              List.of(naked(KeyCode.UP), control(KeyCode.UP))),
        entry(PacManGames_Actions.PLAYER_DOWN,            List.of(naked(KeyCode.DOWN), control(KeyCode.DOWN))),
        entry(PacManGames_Actions.PLAYER_LEFT,            List.of(naked(KeyCode.LEFT), control(KeyCode.LEFT))),
        entry(PacManGames_Actions.PLAYER_RIGHT,           List.of(naked(KeyCode.RIGHT), control(KeyCode.RIGHT))),
        entry(PacManGames_Actions.TEST_CUT_SCENES,        List.of(alt(KeyCode.C))),
        entry(PacManGames_Actions.TEST_LEVELS_BONI,       List.of(alt(KeyCode.T))),
        entry(PacManGames_Actions.TEST_LEVELS_TEASERS,    List.of(alt_shift(KeyCode.T))),
        entry(PacManGames_Actions.PERSPECTIVE_PREVIOUS,   List.of(alt(KeyCode.LEFT))),
        entry(PacManGames_Actions.PERSPECTIVE_NEXT,       List.of(alt(KeyCode.RIGHT))),
        entry(PacManGames_Actions.TOGGLE_DRAW_MODE,       List.of(alt(KeyCode.W)))
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