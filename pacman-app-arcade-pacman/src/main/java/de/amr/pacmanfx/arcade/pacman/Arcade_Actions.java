/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.gamestate.Arcade_GameState;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.event.gameplay.CreditAddedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
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
            public void execute(PacManGamesEngine app) {
                app.soundManager().voice().stop();
                app.soundManager().setEnabled(true);
                app.currentGame().coinMechanism().insertCoin();
                app.gameVariantManager().currentRuntime().uiConfig().optSoundEffects().ifPresent(PacManGameSoundEffects::playCoinInsertedSound);
                app.currentGame().playConfig().gameFlow().enterGameState(app.currentGame(), CommonGameStateID.GAME_PREPARATION);
                app.currentGame().eventManager().publishEvent(new CreditAddedEvent(1));
            }

            @Override
            public boolean isEnabled(PacManGamesEngine app) {
                final GameSession session = app.currentGame().session();
                final AbstractGameState gameState = app.currentGame().state();
                if (app.currentGame().coinMechanism().isFull()) {
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
            public void execute(PacManGamesEngine app) {
                app.soundManager().voice().stop();
                app.currentGame().playConfig().gameFlow().enterState(app.currentGame(), Arcade_GameState.GAME_OR_LEVEL_STARTING.state());
            }

            @Override
            public boolean isEnabled(PacManGamesEngine app) {
                if (app.currentGame().coinMechanism().isEmpty()) {
                    return false;
                }
                final AbstractGameState state = app.currentGame().state();
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
