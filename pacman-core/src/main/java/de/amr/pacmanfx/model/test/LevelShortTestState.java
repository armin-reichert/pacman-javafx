/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.BonusEatenEvent;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

public class LevelShortTestState extends TestState {

    private int lastTestedLevelNumber;

    public LevelShortTestState() {
    }

    @Override
    public String name() {
        return "LevelShortTestState";
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel game = context.game();
        //coinMechanism.setNumCoins(1);
        lastTestedLevelNumber = game.rules().lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.rules().lastLevelNumber();
        lock();
        game.prepareNewGame();
        game.buildNormalLevel(1);
        game.startLevel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel game = context.game();
        final GameLevel level = game.optGameLevel().orElseThrow();
        final float START = 1.0f;
        if (timer.atSecond(START)) {
            game.prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
            game.showLevelMessage(level, GameLevelMessageType.READY);
            game.hud().credit(false).livesCounter(true);

            GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
            message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
            level.setMessage(message);
            level.heartbeat().restart();
        }
        else if (timer.atSecond(START + 1)) {
            level.clearMessage();
        }
        else if (timer.atSecond(START + 3)) {
            game.activateNextBonus(level);
        }
        else if (timer.atSecond(START + 5)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                game.flow().publishGameEvent(new BonusEatenEvent(context, bonus));
            });
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus(level);
        }
        else if (timer.atSecond(START + 8)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                game.flow().publishGameEvent(new BonusEatenEvent(context, bonus));
            });
        }
        else if (timer.atSecond(START + 9)) {
            level.hidePacAndGhosts();
            level.heartbeat().stop();
            game.onLevelCompleted(level);
        }
        else if (timer.atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                //coinMechanism.setNumCoins(0);
                game.flow().restartState(GameStateID.BOOT.name());
            } else {
                lock();
                game.startNextLevel();
                GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
                message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
                level.setMessage(message);
            }
        } else {
            game.optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> bonus.update(level));
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel game = context.game();
        //coinMechanism.setNumCoins(0);
        game.init();
        game.levelCounter().clear();
    }
}