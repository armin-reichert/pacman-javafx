/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;

public class LevelShortTestState implements FsmState<GameContext>, TestState {

    private final TickTimer timer = new TickTimer("Timer_" + name());
    private int lastTestedLevelNumber;

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }

    @Override
    public void onEnter(GameContext context) {
        final Game game = context.currentGame();
        context.coinMechanism().setNumCoins(1);
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        timer.restartIndefinitely();
        game.prepareForNewGame();
        game.buildNormalLevel(1);
        game.startLevel();
        game.level().showPacAndGhosts();
    }

    @Override
    public void onUpdate(GameContext context) {
        final float START = 1.0f;
        final Game game = context.currentGame();
        if (timer.atSecond(START)) {
            game.continueGame();
            GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
            message.setPosition(game.level().worldMap().terrainLayer().messageCenterPosition());
            game.level().setMessage(message);
            game.level().blinking().restart();
        }
        else if (timer.atSecond(START + 1)) {
            game.level().clearMessage();
        }
        else if (timer.atSecond(START + 3)) {
            game.activateNextBonus();
        }
        else if (timer.atSecond(START + 5)) {
            game.level().optBonus().ifPresent(bonus -> bonus.setEatenSeconds(2));
            game.publishGameEvent(GameEvent.Type.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus();
        }
        else if (timer.atSecond(START + 8)) {
            game.level().optBonus().ifPresent(bonus -> bonus.setEatenSeconds(2));
            game.publishGameEvent(GameEvent.Type.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 9)) {
            game.level().hidePacAndGhosts();
            game.level().blinking().stop();
            game.onLevelCompleted();
        }
        else if (timer.atSecond(START + 10)) {
            if (game.level().number() == lastTestedLevelNumber) {
                context.coinMechanism().setNumCoins(0);
                context.currentGame().resetEverything();
                context.currentGame().restart("BOOT");
            } else {
                timer.restartIndefinitely();
                game.startNextLevel();
                GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
                message.setPosition(game.level().worldMap().terrainLayer().messageCenterPosition());
                game.level().setMessage(message);
            }
        }
        game.level().optBonus().ifPresent(bonus -> bonus.tick(context));
    }

    @Override
    public void onExit(GameContext context) {
        context.coinMechanism().setNumCoins(0);
        context.currentGame().resetEverything();
        context.currentGame().levelCounter().clear();
    }
}