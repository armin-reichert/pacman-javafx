/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.gamescene.BaseGameSceneHandler;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PlayScene2DGameEventHandler extends BaseGameSceneHandler {

    private final TengenMsPacMan_PlayScene2D playScene;

    public TengenMsPacMan_PlayScene2DGameEventHandler(TengenMsPacMan_PlayScene2D playScene) {
        super(playScene.appContext());
        this.playScene = requireNonNull(playScene);
    }

    public TengenMsPacMan_PlayScene2D gameScene() {
        return playScene;
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        optGameLevel().ifPresent(level -> {
            gameScene().resetAnimations(level);
            gameScene().dynamicCamera().playIntroSequence();
            if (appContext().currentGameContext().model() instanceof TengenMsPacMan_GameModel tengenGame) {
                tengenGame.showMessage(level, GameLevelMessageType.READY);
            }
        });
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final GameModel game = appContext().currentGameContext().model();
        final boolean silent = game.isDemoLevelRunning() || appContext().currentGameContext().state() instanceof TestState;
        if (!silent) {
            appContext().currentSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.newState() == TengenMsPacMan_GameState.GAME_LEVEL_COMPLETE.state()) {
            final GameLevel level = optGameLevel().orElseThrow();
            appContext().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().playLevelCompleteAnimation(level);
        }
        else if (e.newState() == TengenMsPacMan_GameState.GAME_OVER.state()) {
            final TengenMsPacMan_PlayScene2D playScene2D = gameScene();
            final PlayScene2DCamera camera = playScene2D.dynamicCamera();
            final GameLevel level = optGameLevel().orElseThrow();
            appContext().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
            camera.enterManualMode();
            camera.setToTopPosition();
            level.optMessage().ifPresent(playScene2D::startGameOverMessageAnimation);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        gameScene().acceptGameLevel(e.level());
    }

    @Override
    public void onLevelStarted(LevelStartedEvent e) {
        optGameLevel().ifPresent(level -> gameScene().resetAnimations(level));
        gameScene().dynamicCamera().playIntroSequence();
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        appContext().currentGameContext().state().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        gameScene().dynamicCamera().enterManualMode();
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = appContext().gameClock().tickCount();
        gameScene().appContext().currentSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        gameScene().appContext().currentSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        gameScene().appContext().currentSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        gameScene().appContext().currentSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
