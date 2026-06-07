/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.BonusEatenEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

public class LevelShortTestState extends GameState implements TestState {

    private int lastTestedLevelNumber;

    public LevelShortTestState() {
        super("Short Level Test State");
    }

    @Override
    public String name() {
        return "LevelShortTestState";
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.gameModel();
        //coinMechanism.setNumCoins(1);
        lastTestedLevelNumber = gameContext.gameRules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : gameContext.gameRules().lastLevelNumber();
        lock();
        gameModel.prepareNewGame();
        gameModel.buildNormalLevel(gameContext, 1);
        gameModel.startLevel(gameContext);
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.gameModel();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        final float START = 1.0f;
        if (timer().atSecond(START)) {
            gameModel.prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
            gameModel.showLevelMessage(level, GameLevelMessageType.READY);
            gameModel.hud().creditOff().livesCounterOn();

            GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
            message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
            level.setMessage(message);
            level.heartbeat().restart();
        }
        else if (timer().atSecond(START + 1)) {
            level.clearMessage();
        }
        else if (timer().atSecond(START + 3)) {
            gameModel.activateNextBonus(gameContext, level);
        }
        else if (timer().atSecond(START + 5)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                gameContext.gameFlow().publishGameEvent(new BonusEatenEvent(gameContext, bonus));
            });
        }
        else if (timer().atSecond(START + 6)) {
            gameModel.activateNextBonus(gameContext, level);
        }
        else if (timer().atSecond(START + 8)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                gameContext.gameFlow().publishGameEvent(new BonusEatenEvent(gameContext, bonus));
            });
        }
        else if (timer().atSecond(START + 9)) {
            level.hidePacAndGhosts();
            level.heartbeat().stop();
            gameModel.onLevelCompleted(level);
        }
        else if (timer().atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                //coinMechanism.setNumCoins(0);
                gameContext.gameFlow().restartState(GameStateID.BOOT);
            } else {
                lock();
                gameModel.startNextLevel(gameContext);
                GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
                message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
                level.setMessage(message);
            }
        } else {
            gameModel.optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> bonus.update(gameContext, level));
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel game = context.gameModel();
        //coinMechanism.setNumCoins(0);
        game.init();
        game.levelCounter().clear();
    }
}