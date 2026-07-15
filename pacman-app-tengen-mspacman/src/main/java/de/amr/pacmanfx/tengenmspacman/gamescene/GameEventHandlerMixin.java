/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.pacmanfx.core.event.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneGameEventHandler;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import org.tinylog.Logger;

public interface GameEventHandlerMixin extends GameSceneGameEventHandler {

    @Override
    TengenMsPacMan_PlayScene2D gameScene();

    @Override
    default void onBonusActivated(BonusActivatedEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    default void onBonusEaten(BonusEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    default void onBonusExpired(BonusExpiredEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    default void onGameContinued(GameContinuedEvent e) {
        gameContext().model().optLevel().ifPresent(level -> {
            gameScene().resetActorAnimations(level);
            gameScene().dynamicCamera().playIntroSequence();
            if (gameContext().gamePlay() instanceof TengenMsPacMan_GamePlay tengenGame) {
                tengenGame.showLevelMessage(level, GameLevelMessageType.READY);
            }
        });
    }

    @Override
    default void onGameStarted(GameStartedEvent e) {
        final boolean silent = gameContext().gamePlay().isDemoLevelRunning(gameContext().model())
            || gameContext().state().id() instanceof TestStateID;
        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    default void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter game state '{}'", e.newState().name());
        if (e.newState() == TengenMsPacMan_GameState.GAME_LEVEL_COMPLETE.state()) {
            final GameLevel level = gameContext().model().assertLevel();
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().playLevelCompleteAnimation(level);
        }
        else if (e.newState() == TengenMsPacMan_GameState.GAME_OVER.state()) {
            final TengenMsPacMan_PlayScene2D playScene2D = gameScene();
            final PlayScene2DCamera camera = playScene2D.dynamicCamera();
            final GameLevel level = gameContext().model().assertLevel();
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            camera.enterManualMode();
            camera.setToTopPosition();
            level.optMessage().ifPresent(playScene2D::startGameOverMessageAnimation);
        }
    }

    @Override
    default void onGhostEaten(GhostEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    default void onLevelCreated(LevelCreatedEvent e) {
        gameScene().acceptGameLevel(e.level());
    }

    @Override
    default void onLevelStarted(LevelStartedEvent e) {
        gameContext().model().optLevel().ifPresent(level -> gameScene().resetActorAnimations(level));
        gameScene().dynamicCamera().playIntroSequence();
    }

    @Override
    default void onPacDead(PacDeadEvent e) {
        gameContext().state().triggerTimeout();
    }

    @Override
    default void onPacDying(PacDyingEvent e) {
        gameScene().dynamicCamera().enterManualMode();
        optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    default void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = actionContext().clock().currentTick();
        gameScene().optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    default void onPacGetsPower(PacGetsPowerEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    default void onPacLostPower(PacLostPowerEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    default void onSpecialScore(SpecialScoreEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
