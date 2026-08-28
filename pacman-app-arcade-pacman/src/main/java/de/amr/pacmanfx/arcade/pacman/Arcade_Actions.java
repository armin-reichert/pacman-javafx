/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.gamestate.Arcade_GameState;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.event.gameplay.CreditAddedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;

public final class Arcade_Actions {

    private final GameAction actionInsertCoin;
    private final GameAction actionStartPlaying;

    private final Set<ActionKeyBinding> gameStartActionBindings;

    public Arcade_Actions() {

        actionInsertCoin = new GameAction("insert_coin") {
            @Override
            public void execute(GameAppContext app) {
                final CoinMechanism coinMechanism = app.game().coinMechanism();
                app.ui().soundManager().voice().stop();
                app.ui().soundManager().setEnabled(true);
                coinMechanism.insertCoin();
                app.game().eventManager().publishGameEvent(new CreditAddedEvent(1));
                app.game().variant().gameFlow().enterGameState(app.game(), CommonGameStateID.GAME_PREPARATION);
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                final GameSession session = app.game().session();
                final AbstractGameState gameState = app.game().state();
                if (app.game().coinMechanism().isFull()) {
                    return false;
                }
                // In demo level, coin can always be inserted
                if (session.isAttractMode()) {
                    return true;
                }
                return CommonGameStateID.GAME_INTRO.hasSameNameAs(gameState)
                    || CommonGameStateID.GAME_PREPARATION.hasSameNameAs(gameState);
            }
        };

        actionStartPlaying = new GameAction("start_playing") {
            @Override
            public void execute(GameAppContext app) {
                app.ui().soundManager().voice().stop();
                app.game().variant().gameFlow().enterState(app.game(), Arcade_GameState.GAME_OR_LEVEL_STARTING.state());
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                if (app.game().coinMechanism().isEmpty()) {
                    return false;
                }
                final AbstractGameState state = app.game().state();
                return (CommonGameStateID.GAME_INTRO.hasSameNameAs(state)
                    || CommonGameStateID.GAME_PREPARATION.hasSameNameAs(state));
            }
        };

        gameStartActionBindings = Set.of(
            new ActionKeyBinding(actionInsertCoin,   bareKey(KeyCode.DIGIT5), bareKey(KeyCode.NUMPAD5)),
            new ActionKeyBinding(actionStartPlaying, bareKey(KeyCode.DIGIT1), bareKey(KeyCode.NUMPAD1))
        );
    }

    public GameAction actionInsertCoin() {
        return actionInsertCoin;
    }

    public GameAction actionStartPlaying() {
        return actionStartPlaying;
    }

    public Set<ActionKeyBinding> gameStartActionBindings() {
        return gameStartActionBindings;
    }
}
