/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.gamestate.Arcade_GameState;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.event.gameplay.CreditAddedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;

public final class Arcade_Actions {

    private static class SingletonHolder {
        static final Arcade_Actions SINGLETON = new Arcade_Actions();
    }

    public static Arcade_Actions instance() {
        return SingletonHolder.SINGLETON;
    }

    private final GameAction<EngineUIContext> actionInsertCoin;
    private final GameAction<EngineUIContext> actionStartPlaying;

    private final Set<ActionKeyBinding<EngineUIContext>> gameStartActionBindings;

    private Arcade_Actions() {

        actionInsertCoin = new GameAction<>("insert_coin") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.optCurrentGame().ifPresent(game -> {
                    final GameVariantRuntime runtime = engine.gameVariantManager().currentRuntime();
                    engine.soundManager().voice().stop();
                    engine.soundManager().setEnabled(true);
                    game.coinMechanism().insertCoin();
                    game.runtime().gameFlow().enterGameState(game, CommonGameStateID.GAME_PREPARATION);
                    game.eventManager().publishEvent(new CreditAddedEvent(1));
                    engine.uiConfig(runtime).optSoundEffects().ifPresent(PacManGameSoundEffects::playCoinInsertedSound);
                });
            }

            @Override
            public boolean isEnabled(EngineUIContext engine) {
                final GameContext game = engine.optCurrentGame().orElse(null);
                if (game == null) return false;

                if (game.coinMechanism().isFull()) {
                    return false;
                }

                // In demo level, coin can always be inserted
                if (game.session().booleanValue(SessionKey.DEMO_LEVEL_MODE)) {
                    return true;
                }

                final AbstractGameState gameState = game.state();
                return CommonGameStateID.GAME_INTRO.hasSameNameAs(gameState)
                    || CommonGameStateID.GAME_PREPARATION.hasSameNameAs(gameState);
            }
        };

        actionStartPlaying = new GameAction<>("start_playing") {
            @Override
            public void execute(EngineUIContext engine) {
                engine.soundManager().voice().stop();
                engine.optCurrentGame().ifPresent(game ->
                    game.runtime().gameFlow().enterState(game, Arcade_GameState.GAME_OR_LEVEL_STARTING.state()));
            }

            @Override
            public boolean isEnabled(EngineUIContext engine) {
                final GameContext game = engine.optCurrentGame().orElse(null);
                if (game == null) return false;

                if (game.coinMechanism().isEmpty()) {
                    return false;
                }

                final AbstractGameState state = game.state();
                return (CommonGameStateID.GAME_INTRO.hasSameNameAs(state)
                    || CommonGameStateID.GAME_PREPARATION.hasSameNameAs(state));
            }
        };

        gameStartActionBindings = Set.of(
            new ActionKeyBinding<>(actionInsertCoin(),   bareKey(KeyCode.DIGIT5), bareKey(KeyCode.NUMPAD5)),
            new ActionKeyBinding<>(actionStartPlaying(), bareKey(KeyCode.DIGIT1), bareKey(KeyCode.NUMPAD1))
        );
    }

    public GameAction<EngineUIContext> actionInsertCoin() {
        return actionInsertCoin;
    }

    public GameAction<EngineUIContext> actionStartPlaying() {
        return actionStartPlaying;
    }

    public Set<ActionKeyBinding<EngineUIContext>> gameStartActionBindings() {
        return gameStartActionBindings;
    }
}
