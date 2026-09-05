/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Extras;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MessageAnimation;

import java.io.IOException;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class Tengen_GameOverState extends AbstractGameState {

    public static final int GAME_OVER_MESSAGE_DELAY_SEC = 2;
    public static final int COUNTDOWN_AFTER_ANIMATION = 180;

    private long countdownAfter;
    private GameLevel level;

    public Tengen_GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnterState(GameContext game) {
        level = session.level();

        countdownAfter = 0;

        session.setGameRunning(false);
        session.cheats().clear();

        try {
            systems.scoreSystem().saveHighScoreIfNeeded(hud.highScore());
        } catch (IOException e) {
            game.eventManager().publishGameEvent(new HighScoreAccessErrorEvent(e));
        }

        level.showMessage(MessageType.GAME_OVER);

        final MapCategory mapCategory = TengenMsPacMan_GamePlay.mapCategory(session);
        if (!session.isAttractMode() && mapCategory != MapCategory.ARCADE) {
            startGameOverMessageAnimation();
            timer().restartIndefinitely(); // animation completion triggers state exit
        }
        else {
            timer().restartTicks(session.gameOverStateTicks());
        }
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (countdownAfter > 0) {
            --countdownAfter;
            if (countdownAfter == 0) {
                timer().expire();
            }
        }

        if (timer().hasExpired()) {
            if (session.isAttractMode()) {
                flow.enterGameState(game, TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
                return;
            }
            final boolean continueGame = TengenMsPacMan_GamePlay.checkGameContinuesOnGameOver(session);
            flow.enterGameState(game, continueGame ? CommonGameStateID.GAME_PREPARATION : CommonGameStateID.GAME_INTRO);
            return;
        }

        // Show animated game over message moving horizontally over scene and wrapping around
        final var messageAnimation = session.value(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class);
        if (messageAnimation != null) {
            if (messageAnimation.finished() && countdownAfter == 0) {
                countdownAfter = COUNTDOWN_AFTER_ANIMATION;
            } else {
                messageAnimation.update(systems.motor());
            }
        }
    }

    @Override
    public void onExit(GameContext game) {
        session.level().clearMessage();
        session.clearValue(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION);
    }

    // For map categories MINI, BIG and STRANGE, the GAME OVER message is animated
    private void startGameOverMessageAnimation() {
        final var messageAnimation = new MessageAnimation();
        session.setValue(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, messageAnimation);
        messageAnimation.setDelayTicks(GAME_OVER_MESSAGE_DELAY_SEC * GameConstants.SIMULATION_FPS);
        messageAnimation.start(computeMessageStartPosition(), systems.motor());
    }

    private Vector2f computeMessageStartPosition() {
        final House house = level.entities().house();
        final Vector2i houseSize = house.sizeInTiles();
        // Compute center position under house
        return house.floorplan().minTile()
            .toVector2f()
            .plus(houseSize.x() * 0.5f, houseSize.y() + 1)
            .scaled(TS);
    }
}
