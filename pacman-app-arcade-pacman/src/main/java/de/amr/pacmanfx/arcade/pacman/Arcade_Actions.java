/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.gamestate.Arcade_GameState;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.event.gameplay.CreditAddedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;

public final class Arcade_Actions {

    private final GameAction<GameEngineContext> actionInsertCoin;
    private final GameAction<GameEngineContext> actionStartPlaying;

    private final Set<ActionKeyBinding> gameStartActionBindings;

    public Arcade_Actions() {

        actionInsertCoin = new GameAction<>("insert_coin") {
            @Override
            public void execute(GameEngineContext context) {
                context.soundManager().voice().stop();
                context.soundManager().setEnabled(true);
                context.currentGame().coinMechanism().insertCoin();
                context.gameVariantManager().currentRuntime().uiConfig().optSoundEffects().ifPresent(PacManGameSoundEffects::playCoinInsertedSound);
                context.currentGame().playConfig().gameFlow().enterGameState(context.currentGame(), CommonGameStateID.GAME_PREPARATION);
                context.currentGame().eventManager().publishEvent(new CreditAddedEvent(1));
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
                final GameSession session = context.currentGame().session();
                final AbstractGameState gameState = context.currentGame().state();
                if (context.currentGame().coinMechanism().isFull()) {
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

        actionStartPlaying = new GameAction<>("start_playing") {
            @Override
            public void execute(GameEngineContext context) {
                context.soundManager().voice().stop();
                context.currentGame().playConfig().gameFlow().enterState(context.currentGame(), Arcade_GameState.GAME_OR_LEVEL_STARTING.state());
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
                if (context.currentGame().coinMechanism().isEmpty()) {
                    return false;
                }
                final AbstractGameState state = context.currentGame().state();
                return (CommonGameStateID.GAME_INTRO.hasSameNameAs(state)
                    || CommonGameStateID.GAME_PREPARATION.hasSameNameAs(state));
            }
        };

        gameStartActionBindings = Set.of(
            new ActionKeyBinding(actionInsertCoin,   bareKey(KeyCode.DIGIT5), bareKey(KeyCode.NUMPAD5)),
            new ActionKeyBinding(actionStartPlaying, bareKey(KeyCode.DIGIT1), bareKey(KeyCode.NUMPAD1))
        );
    }

    public GameAction<GameEngineContext> actionInsertCoin() {
        return actionInsertCoin;
    }

    public GameAction<GameEngineContext> actionStartPlaying() {
        return actionStartPlaying;
    }

    public Set<ActionKeyBinding> gameStartActionBindings() {
        return gameStartActionBindings;
    }
}
