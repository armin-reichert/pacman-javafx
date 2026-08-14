/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Extras;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlayOptions;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MessageAnimation;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.Optional;

public class GameOverState extends GameState {

    public static final int GAME_OVER_MESSAGE_DELAY_SEC = 2;
    public static final int COUNTDOWN_AFTER_ANIMATION = 180;

    private long countdownAfter;

    public GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final GamePlay gamePlay = game.variantConfig().gamePlay();

        countdownAfter = 0;

        // Needed if state entry was triggered by user interaction
        LivesCounterSystem.setNumLives(session.livesCounter(), 0);
        session.setPlaying(false);
        session.cheats().clear();

        try {
            ScoreSystem.saveHighScoreIfNeeded(session.highScore());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gamePlay.showLevelMessage(game, level, GameLevelMessageType.GAME_OVER);

        final MapCategory mapCategory = session.value(TengenMsPacMan_GamePlayOptions.MAP_CATEGORY, MapCategory.class);
        if (!session.isAttractMode() && mapCategory != MapCategory.ARCADE) {
            timer().restartIndefinitely(); // animation end triggers state exit
            startGameOverMessageAnimation(
                session,
                level.entities().theOne(House.class),
                game.variantConfig().systems().motor()
            );
        }
        else {
            timer().restartTicks(session.gameOverStateTicks());
        }
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();

        if (countdownAfter > 0) {
            --countdownAfter;
            if (countdownAfter == 0) {
                timer().expire();
                Logger.info("Countdown ends, expire game over state");
            }
        }

        if (timer().hasExpired()) {
            final var gamePlay = (TengenMsPacMan_GamePlay) game.variantConfig().gamePlay();

            final Named nextStateID = session.isAttractMode()
                ? TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME
                : gamePlay.canContinueOnGameOver(session)
                    ? CommonGameStateID.GAME_PREPARATION
                    : CommonGameStateID.GAME_INTRO;

            session.gameFlow().enterState(game, nextStateID);

            return;
        }

        getMessageAnimation(session).ifPresent(animation -> {
            if (animation.finished() && countdownAfter == 0) {
                countdownAfter = COUNTDOWN_AFTER_ANIMATION;
                Logger.info("Start countdown after animation: {} ticks", countdownAfter);
            } else {
                animation.update(game.variantConfig().systems().motor());
            }
        });
    }

    @Override
    public void onExit(GameContext game) {
        final GameSession session = game.session();
        session.hud().clearMessage();
        session.clearValue(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION);
    }

    private Optional<MessageAnimation> getMessageAnimation(GameSession session) {
        return Optional.ofNullable(
            session.value(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class)
        );
    }

    // For map categories "mini", "big" or "strange", the "game over" message is animated
    private void startGameOverMessageAnimation(GameSession session, House house, MovementSystem motor) {
        final var messageAnimation = new MessageAnimation();
        session.setValue(TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, messageAnimation);
        messageAnimation.setDelayTicks(GAME_OVER_MESSAGE_DELAY_SEC * GameConstants.SIMULATION_FPS);
        messageAnimation.start(house, motor);
    }
}
