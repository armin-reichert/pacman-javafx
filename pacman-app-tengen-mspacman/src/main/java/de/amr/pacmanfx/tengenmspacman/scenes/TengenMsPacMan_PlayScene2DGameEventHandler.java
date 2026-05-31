/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessageType;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

public class TengenMsPacMan_PlayScene2DGameEventHandler extends GameScene.DefaultGameEventHandler {

    public TengenMsPacMan_PlayScene2DGameEventHandler(GameScene gameScene) {
        super(gameScene);
    }

    public TengenMsPacMan_GameModel game() {
        return facade().currentGame();
    }

    @Override
    public TengenMsPacMan_PlayScene2D gameScene() {
        return (TengenMsPacMan_PlayScene2D) super.gameScene();
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        optGameLevel().ifPresent(level -> {
            gameScene().resetAnimations(level);
            gameScene().dynamicCamera().playIntroSequence();
            game().showMessage(level, GameLevelMessageType.READY);
        });
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final boolean silent = game().isDemoLevelRunning() || game().flow().state() instanceof TestState;
        if (!silent) {
            gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        switch (e.newState()) {
            case TengenMsPacMan_GameState.LEVEL_COMPLETE -> {
                final GameLevel level = optGameLevel().orElseThrow();
                gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
                gameScene().playLevelCompleteAnimation(level);
            }
            case TengenMsPacMan_GameState.GAME_OVER -> {
                final GameLevel level = optGameLevel().orElseThrow();
                gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
                gameScene().dynamicCamera().enterManualMode();
                gameScene().dynamicCamera().setToTopPosition();
                level.optMessage().ifPresent(message -> gameScene().startGameOverMessageAnimation(message));
            }
            default -> {}
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
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
        game().flow().state().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        gameScene().dynamicCamera().enterManualMode();
        gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = facade().gameClock().tickCount();
        gameScene().facade().currentSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        gameScene().facade().currentSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
