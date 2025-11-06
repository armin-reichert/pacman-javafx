/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller.test;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.Bonus;

public class LevelShortTestState implements TestGameState {

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
        final GameLevel gameLevel = context.gameLevel();
        if (timer.atSecond(START)) {
            context.game().continueGame(gameLevel);
            GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
            message.setPosition(context.gameLevel().worldMap().terrainLayer().messageCenterPosition());
            context.gameLevel().setMessage(message);
        }
        else if (timer.atSecond(START + 1)) {
            gameLevel.clearMessage();
        }
        else if (timer.atSecond(START + 3)) {
            context.game().activateNextBonus(gameLevel);
        }
        else if (timer.atSecond(START + 4)) {
            gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(2));
            context.eventManager().publishEvent(GameEventType.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 5)) {
            context.game().activateNextBonus(gameLevel);
        }
        else if (timer.atSecond(START + 6)) {
            gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(2));
            context.eventManager().publishEvent(GameEventType.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 7)) {
            gameLevel.hidePacAndGhosts();
            context.game().onLevelCompleted(gameLevel);
        }
        else if (timer.atSecond(START + 10)) {
            if (gameLevel.number() == lastTestedLevelNumber) {
                context.coinMechanism().setNumCoins(0);
                context.game().resetEverything();
                context.gameController().restart(GamePlayState.BOOT);
            } else {
                timer.restartIndefinitely();
                context.game().startNextLevel();
                GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
                message.setPosition(context.gameLevel().worldMap().terrainLayer().messageCenterPosition());
                context.gameLevel().setMessage(message);
            }
        }
    }

    @Override
    public void onExit(GameContext context) {
        context.coinMechanism().setNumCoins(0);
        context.game().resetEverything();
        context.game().levelCounter().clear();
    }
}
