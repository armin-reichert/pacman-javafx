/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.flow.GameStateID;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

import java.util.List;

public class LevelMediumTestState<GAME extends GameModel> extends TestState<GAME> {

    static final int TEST_DURATION_SEC = 10;

    private int lastTestedLevelNumber;

    private void configureLevelForTest(GAME game) {
        final GameLevel level = game.optGameLevel().orElseThrow();

        final Pac pac = level.entities().pac();
        pac.usingAutopilotProperty().unbind();
        pac.setUsingAutopilot(true);
        pac.animations().playSelected();
        pac.show();

        final List<Ghost> ghosts = level.entities().ghosts();
        ghosts.forEach(ghost -> ghost.animations().playSelected());
        ghosts.forEach(Ghost::show);

        final var message = new GameLevelMessage(GameLevelMessageType.TEST);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);

        game.hud().show();

        game.flow().publishGameEvent(new StopAllSoundsEvent(game));
    }

    @Override
    public String name() {
        return "LevelMediumTestState";
    }

    @Override
    public void onEnter(GAME game) {
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        timer.restartSeconds(TEST_DURATION_SEC);
        game.prepareNewGame();
        game.buildNormalLevel(1);
        game.startLevel();
        configureLevelForTest(game);
    }

    @Override
    public void onUpdate(GAME game) {
        final GameLevel level = game.optGameLevel().orElseThrow();

        level.entities().pac().update(level);

        level.entities().ghosts().forEach(ghost -> ghost.update(level));

        level.optBonus().ifPresent(bonus -> bonus.update(level));

        game.doLevelPlaying();
        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                game.flow().publishGameEvent(new StopAllSoundsEvent(game));
                game.flow().enterState(GameStateID.GAME_INTRO.name());
            } else {
                timer().restartSeconds(TEST_DURATION_SEC);
                game.startNextLevel();
                configureLevelForTest(game);
            }
        }
        else if (game.isLevelCompleted()) {
            game.flow().enterState(GameStateID.GAME_INTRO.name());
        } else if (game.hasPacManBeenKilled()) {
            expire();
        } else if (game.hasGhostBeenKilled()) {
            game.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST.name());
        }
    }

    @Override
    public void onExit(GAME game) {
        game.levelCounter().clear();
    }
}
