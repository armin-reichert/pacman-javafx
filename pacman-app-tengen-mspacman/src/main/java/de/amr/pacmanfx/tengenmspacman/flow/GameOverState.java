/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.MessageAnimation;

import java.io.IOException;
import java.util.Optional;

public class GameOverState extends GameState {

    public static final int GAME_OVER_MESSAGE_DELAY_SEC = 2;

    public GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.variantConfig().gamePlay();

        LivesCounterSystem.setNumLives(session.livesCounter(), 0); // Needed if state entry was triggered by user interaction
        session.setPlaying(false);
        session.cheats().clear();

        try {
            ScoreSystem.saveHighScoreIfNeeded(session.highScore());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gamePlay.showLevelMessage(game, level, GameLevelMessageType.GAME_OVER);

        final MapCategory mapCategory = session.value(TengenMsPacMan_GamePlay.GamePlayOptions.MAP_CATEGORY, MapCategory.class);
        if (!session.isAttractMode() && mapCategory != MapCategory.ARCADE) {
            final House house = level.entities().theOne(House.class);
            startGameOverMessageAnimation(session, house, game.variantConfig().systems().motor());
            timer().restartIndefinitely(); // animation end triggers state exit
        }
        else {
            timer().restartTicks(session.gameOverStateTicks());
        }
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();

        getMessageAnimation(session).ifPresent(animation -> {
            animation.update(game.variantConfig().systems().motor());
            if (messageAnimationHasFinished(session)) {
                timer().expire();
            }
        });

        if (timer().hasExpired()) {
            final var gamePlay = (TengenMsPacMan_GamePlay) game.variantConfig().gamePlay();
            final Named nextStateID = session.isAttractMode()
                ? TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME
                : gamePlay.canContinueOnGameOver(session)
                ? CommonGameStateID.GAME_PREPARATION : CommonGameStateID.GAME_INTRO;
            session.gameFlow().enterState(game, nextStateID);
        }
    }

    @Override
    public void onExit(GameContext game) {
        final GameSession session = game.session();
        session.hud().clearMessage();
        session.clearValue(TengenMsPacMan_GamePlay.EXTRAS.GAME_OVER_MESSAGE_ANIMATION);
    }

    private Optional<MessageAnimation> getMessageAnimation(GameSession session) {
        return Optional.ofNullable(
            session.value(TengenMsPacMan_GamePlay.EXTRAS.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class)
        );
    }

    // For map categories "mini", "big" or "strange", the "game over" message is animated
    private void startGameOverMessageAnimation(GameSession session, House house, MovementSystem motor) {
        final var messageAnimation = new MessageAnimation();
        session.setValue(TengenMsPacMan_GamePlay.EXTRAS.GAME_OVER_MESSAGE_ANIMATION, messageAnimation);
        messageAnimation.setDelayTicks(GAME_OVER_MESSAGE_DELAY_SEC * GameConstants.SIMULATION_FPS);
        messageAnimation.start(house, motor);
    }

    private boolean messageAnimationHasFinished(GameSession session) {
        return getMessageAnimation(session).map(MessageAnimation::finished).orElse(true);
    }
}
