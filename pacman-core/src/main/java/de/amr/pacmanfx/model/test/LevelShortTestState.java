/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.BonusEatenEvent;
import de.amr.pacmanfx.lib.fsm.AbstractState;
import de.amr.pacmanfx.model.*;

import static java.util.Objects.requireNonNull;

public class LevelShortTestState extends AbstractState<Game> implements TestState {

    private final CoinMechanism coinMechanism;
    private int lastTestedLevelNumber;

    public LevelShortTestState(CoinMechanism coinMechanism) {
        this.coinMechanism = requireNonNull(coinMechanism);
    }

    @Override
    public void onEnter(Game game) {
        final GameLevel level = game.optGameLevel().orElseThrow();
        coinMechanism.setNumCoins(1);
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        timer.restartIndefinitely();
        game.prepareNewGame();
        game.buildNormalLevel(1);
        game.startLevel(level);
        level.showPacAndGhosts();
    }

    @Override
    public void onUpdate(Game game) {
        final GameLevel level = game.optGameLevel().orElseThrow();
        final float START = 1.0f;
        if (timer.atSecond(START)) {
            game.continuePlaying(level, 1);
            GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
            message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
            level.setMessage(message);
            level.blinking().restart();
        }
        else if (timer.atSecond(START + 1)) {
            game.clearLevelMessage();
        }
        else if (timer.atSecond(START + 3)) {
            game.activateNextBonus(level);
        }
        else if (timer.atSecond(START + 5)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.setEatenSeconds(2);
                game.publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus(level);
        }
        else if (timer.atSecond(START + 8)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.setEatenSeconds(2);
                game.publishGameEvent(new BonusEatenEvent(bonus));
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
                game.boot();
                game.control().restartStateWithName(GameControl.CommonGameState.BOOT.name());
            } else {
                timer.restartIndefinitely();
                game.startNextLevel();
                GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
                message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
                level.setMessage(message);
            }
        } else {
            game.optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> bonus.tick(game));
        }
    }

    @Override
    public void onExit(Game game) {
        coinMechanism.setNumCoins(0);
        game.boot();
        game.levelCounter().clear();
    }
}