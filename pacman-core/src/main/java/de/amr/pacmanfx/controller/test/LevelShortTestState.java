/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller.test;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.PacManGamesState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
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
        context.coinMechanism().setNumCoins(1);
        lastTestedLevelNumber = context.game().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.game().lastLevelNumber();
        timer.restartIndefinitely();
        context.game().prepareForNewGame();
        context.game().buildNormalLevel(1);
        context.game().startLevel(context.gameLevel());
        context.gameLevel().showPacAndGhosts();
    }

    @Override
    public void onUpdate(GameContext context) {
        final float START = 1.0f;
        final Game game = context.game();
        final GameLevel gameLevel = context.gameLevel();
        if (timer.atSecond(START)) {
            game.continueGame(gameLevel);
            GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
            message.setPosition(gameLevel.worldMap().terrainLayer().messageCenterPosition());
            gameLevel.setMessage(message);
            gameLevel.blinking().restart();
        }
        else if (timer.atSecond(START + 1)) {
            gameLevel.clearMessage();
        }
        else if (timer.atSecond(START + 3)) {
            game.activateNextBonus(gameLevel);
        }
        else if (timer.atSecond(START + 5)) {
            gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(2));
            context.eventManager().publishEvent(GameEventType.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus(gameLevel);
        }
        else if (timer.atSecond(START + 8)) {
            gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(2));
            context.eventManager().publishEvent(GameEventType.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 9)) {
            gameLevel.hidePacAndGhosts();
            gameLevel.blinking().stop();
            game.onLevelCompleted(gameLevel);
        }
        else if (timer.atSecond(START + 10)) {
            if (gameLevel.number() == lastTestedLevelNumber) {
                context.coinMechanism().setNumCoins(0);
                context.game().resetEverything();
                context.gameController().restart(PacManGamesState.BOOT);
            } else {
                timer.restartIndefinitely();
                game.startNextLevel();
                GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
                message.setPosition(gameLevel.worldMap().terrainLayer().messageCenterPosition());
                gameLevel.setMessage(message);
            }
        }
        gameLevel.bonus().ifPresent(bonus -> bonus.tick(context));
    }

    @Override
    public void onExit(GameContext context) {
        context.coinMechanism().setNumCoins(0);
        context.game().resetEverything();
        context.game().levelCounter().clear();
    }
}