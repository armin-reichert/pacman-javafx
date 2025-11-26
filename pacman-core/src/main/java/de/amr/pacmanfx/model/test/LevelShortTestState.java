/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.*;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;

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
        THE_GAME_BOX.setNumCoins(1);
        lastTestedLevelNumber = context.currentGame().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.currentGame().lastLevelNumber();
        timer.restartIndefinitely();
        context.currentGame().prepareForNewGame();
        context.currentGame().buildNormalLevel(1);
        context.currentGame().startLevel(context.gameLevel());
        context.gameLevel().showPacAndGhosts();
    }

    @Override
    public void onUpdate(GameContext context) {
        final float START = 1.0f;
        final Game game = context.currentGame();
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
            game.publishGameEvent(GameEvent.Type.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus(gameLevel);
        }
        else if (timer.atSecond(START + 8)) {
            gameLevel.bonus().ifPresent(bonus -> bonus.setEaten(2));
            game.publishGameEvent(GameEvent.Type.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 9)) {
            gameLevel.hidePacAndGhosts();
            gameLevel.blinking().stop();
            game.onLevelCompleted(gameLevel);
        }
        else if (timer.atSecond(START + 10)) {
            if (gameLevel.number() == lastTestedLevelNumber) {
                THE_GAME_BOX.setNumCoins(0);
                context.currentGame().resetEverything();
                context.currentGame().restart("BOOT");
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
        THE_GAME_BOX.setNumCoins(0);
        context.currentGame().resetEverything();
        context.currentGame().levelCounter().clear();
    }
}