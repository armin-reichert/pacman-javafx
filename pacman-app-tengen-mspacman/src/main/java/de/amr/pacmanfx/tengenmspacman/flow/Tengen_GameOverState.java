/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Extras;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MessageAnimation;

import java.io.IOException;
import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class Tengen_GameOverState extends AbstractGameState {

    public static final int GAME_OVER_MESSAGE_DELAY_SEC = 2;
    public static final int COUNTDOWN_AFTER_ANIMATION = 180;

    private long countdownAfter;

    public Tengen_GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnterState(GameContext game) {
        final TengenMsPacMan_GamePlay tengenGamePlay = (TengenMsPacMan_GamePlay) gamePlay;
        final MapCategory mapCategory = tengenGamePlay.mapCategory(session);
        final GameLevel level = session.level();

        countdownAfter = 0;

        session.setGameRunning(false);
        session.cheats().clear();

        try {
            ScoreSystem.saveHighScoreIfNeeded(hud.highScore());
        } catch (IOException e) {
            //TODO improve error handling
            throw new RuntimeException(e);
        }

        gamePlay.showMessage(game, MessageType.GAME_OVER);

        if (!session.isAttractMode() && mapCategory != MapCategory.ARCADE) {
            timer().restartIndefinitely(); // animation end triggers state exit
            startGameOverMessageAnimation();
        }
        else {
            timer().restartTicks(session.gameOverStateTicks());
        }
    }

    @Override
    public void onUpdate(GameContext game) {
        final TengenMsPacMan_GamePlay tengenGamePlay = (TengenMsPacMan_GamePlay) gamePlay;

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
            final boolean continueGame = tengenGamePlay.checkGameContinuesOnGameOver(session);
            flow.enterGameState(game, continueGame ? CommonGameStateID.GAME_PREPARATION : CommonGameStateID.GAME_INTRO);
            return;
        }

        // Show animated game over message moving horizontally over scene and wrapping around
        animatedGameOverMessage(session).ifPresent(messageAnimation -> {
            if (messageAnimation.finished() && countdownAfter == 0) {
                countdownAfter = COUNTDOWN_AFTER_ANIMATION;
            } else {
                messageAnimation.update(systems.motor());
            }
        });
    }

    @Override
    public void onExit(GameContext game) {
        hud.clearMessage();
        session.clearValue(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION);
    }

    private Optional<MessageAnimation> animatedGameOverMessage(GameSession session) {
        return Optional.ofNullable(
            session.value(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class)
        );
    }

    // For map categories MINI, BIG and STRANGE, the GAME OVER message is animated
    private void startGameOverMessageAnimation() {
        final var messageAnimation = new MessageAnimation();
        session.setValue(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, messageAnimation);
        messageAnimation.setDelayTicks(GAME_OVER_MESSAGE_DELAY_SEC * GameConstants.SIMULATION_FPS);
        messageAnimation.start(computeMessageStartPosition(), systems.motor());
    }

    private Vector2f computeMessageStartPosition() {
        final House house = session.level().entities().house();
        final Vector2i houseSize = house.sizeInTiles();
        // Compute center position under house
        return house.floorplan().minTile()
            .toVector2f()
            .plus(houseSize.x() * 0.5f, houseSize.y() + 1)
            .scaled(TS);
    }
}
