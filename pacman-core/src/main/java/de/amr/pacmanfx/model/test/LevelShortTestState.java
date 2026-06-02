/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.event.BonusEatenEvent;
import de.amr.pacmanfx.flow.GameStateID;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

import static java.util.Objects.requireNonNull;

public class LevelShortTestState<GAME extends GameModel> extends TestState<GAME> {

    private final CoinMechanism coinMechanism;
    private int lastTestedLevelNumber;

    public LevelShortTestState(CoinMechanism coinMechanism) {
        this.coinMechanism = requireNonNull(coinMechanism);
    }

    @Override
    public String name() {
        return "LevelShortTestState";
    }

    @Override
    public void onEnter(GAME game) {
        coinMechanism.setNumCoins(1);
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        lock();
        game.prepareNewGame();
        game.buildNormalLevel(1);
        game.startLevel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);
    }

    @Override
    public void onUpdate(GAME game) {
        final GameLevel level = game.optGameLevel().orElseThrow();
        final float START = 1.0f;
        if (timer.atSecond(START)) {
            game.continuePlayingLevel(1);
            GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
            message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
            level.setMessage(message);
            level.blinking().restart();
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
                game.flow().publishGameEvent(new BonusEatenEvent(game, bonus));
            });
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus(level);
        }
        else if (timer.atSecond(START + 8)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                game.flow().publishGameEvent(new BonusEatenEvent(game, bonus));
            });
        }
        else if (timer.atSecond(START + 9)) {
            level.hidePacAndGhosts();
            level.blinking().stop();
            game.onLevelCompleted(level);
        }
        else if (timer.atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                coinMechanism.setNumCoins(0);
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
    public void onExit(GAME game) {
        coinMechanism.setNumCoins(0);
        game.init();
        game.levelCounter().clear();
    }
}