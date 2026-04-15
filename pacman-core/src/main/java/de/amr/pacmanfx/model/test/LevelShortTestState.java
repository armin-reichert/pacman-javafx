/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.BonusEatenEvent;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Ghost;

import static java.util.Objects.requireNonNull;

public class LevelShortTestState<GAME extends Game> extends TestState<GAME> {

    private final CoinMechanism coinMechanism;
    private int lastTestedLevelNumber;

    public LevelShortTestState(CoinMechanism coinMechanism) {
        this.coinMechanism = requireNonNull(coinMechanism);
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
        level.pac().show();
        level.ghosts().forEach(Ghost::show);
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
            game.activateNextBonus();
        }
        else if (timer.atSecond(START + 5)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                game.flow().publishGameEvent(new BonusEatenEvent(game, bonus));
            });
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus();
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
            game.onLevelCompleted();
        }
        else if (timer.atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                coinMechanism.setNumCoins(0);
                game.init();
                game.flow().restartStateWithName(GameFlow.CanonicalGameState.BOOT.name());
            } else {
                lock();
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
    public void onExit(GAME game) {
        coinMechanism.setNumCoins(0);
        game.init();
        game.levelCounter().clear();
    }
}