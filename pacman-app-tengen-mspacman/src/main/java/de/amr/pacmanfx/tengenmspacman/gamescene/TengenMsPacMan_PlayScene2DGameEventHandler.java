/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.test.TestStateID;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.gamescene.common.BaseGameEventHandler;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PlayScene2DGameEventHandler extends BaseGameEventHandler {

    private final TengenMsPacMan_PlayScene2D playScene;

    public TengenMsPacMan_PlayScene2DGameEventHandler(TengenMsPacMan_PlayScene2D playScene) {
        super(playScene.game());
        this.playScene = requireNonNull(playScene);
    }

    public TengenMsPacMan_PlayScene2D gameScene() {
        return playScene;
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        gameModel().optGameLevel().ifPresent(level -> {
            gameScene().resetAnimations(level);
            gameScene().dynamicCamera().playIntroSequence();
            if (gameContext().model() instanceof TengenMsPacMan_GameModel tengenGame) {
                tengenGame.showMessage(level, GameLevelMessageType.READY);
            }
        });
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final GameModel gameModel = gameContext().model();
        final boolean silent = gameModel.isDemoLevelRunning() || gameState().id() instanceof TestStateID;
        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter game state '{}'", e.newState().name());
        if (e.newState() == TengenMsPacMan_GameState.GAME_LEVEL_COMPLETE.state()) {
            final GameLevel level = gameModel().assertLevel();
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().playLevelCompleteAnimation(level);
        }
        else if (e.newState() == TengenMsPacMan_GameState.GAME_OVER.state()) {
            final TengenMsPacMan_PlayScene2D playScene2D = gameScene();
            final PlayScene2DCamera camera = playScene2D.dynamicCamera();
            final GameLevel level = gameModel().assertLevel();
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            camera.enterManualMode();
            camera.setToTopPosition();
            level.optMessage().ifPresent(playScene2D::startGameOverMessageAnimation);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        gameScene().acceptGameLevel(e.level());
    }

    @Override
    public void onLevelStarted(LevelStartedEvent e) {
        gameModel().optGameLevel().ifPresent(level -> gameScene().resetAnimations(level));
        gameScene().dynamicCamera().playIntroSequence();
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        gameContext().state().triggerTimeout();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        gameScene().dynamicCamera().enterManualMode();
        optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = game().clock().currentTick();
        gameScene().optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
