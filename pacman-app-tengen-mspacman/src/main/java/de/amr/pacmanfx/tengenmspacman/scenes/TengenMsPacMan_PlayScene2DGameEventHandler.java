/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessageType;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

public class TengenMsPacMan_PlayScene2DGameEventHandler extends GameScene.DefaultGameEventHandler {

    public TengenMsPacMan_PlayScene2DGameEventHandler(GameScene gameScene) {
        super(gameScene);
    }

    @Override
    public TengenMsPacMan_PlayScene2D gameScene() {
        return (TengenMsPacMan_PlayScene2D) super.gameScene();
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        context().currentSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        context().currentSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        context().currentSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        optGameLevel().ifPresent(level -> {
            gameScene().resetAnimations(level);
            gameScene().dynamicCamera().playIntroSequence();
            if (context().currentGame() instanceof TengenMsPacMan_GameModel tengenGame) {
                tengenGame.showMessage(level, GameLevelMessageType.READY);
            }
        });
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final GameModel game = context().currentGame();
        final boolean silent = game.isDemoLevelRunning() || context().currentGameState() instanceof TestState;
        if (!silent) {
            context().currentSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        switch (e.newState()) {
            case TengenMsPacMan_GameState.LEVEL_COMPLETE -> {
                final GameLevel level = optGameLevel().orElseThrow();
                context().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
                gameScene().playLevelCompleteAnimation(level);
            }
            case TengenMsPacMan_GameState.GAME_OVER -> {
                final TengenMsPacMan_PlayScene2D playScene2D = gameScene();
                final PlayScene2DCamera camera = playScene2D.dynamicCamera();
                final GameLevel level = optGameLevel().orElseThrow();
                context().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
                camera.enterManualMode();
                camera.setToTopPosition();
                level.optMessage().ifPresent(playScene2D::startGameOverMessageAnimation);
            }
            default -> {}
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        context().currentSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
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
        context().currentGameState().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        gameScene().dynamicCamera().enterManualMode();
        context().currentSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = context().gameClock().tickCount();
        gameScene().context().currentSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        gameScene().context().currentSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        gameScene().context().currentSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        gameScene().context().currentSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
