/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.core.event.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.gamescene.common.BaseGameEventHandler;
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationManager;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class Arcade_PlayScene2DGameEventHandler extends BaseGameEventHandler {

    private final Arcade_PlayScene2D playScene2D;

    public Arcade_PlayScene2DGameEventHandler(Arcade_PlayScene2D playScene2D) {
        super(playScene2D);
        this.playScene2D = requireNonNull(playScene2D);
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        // This is the sound in Ms. Pac-Man when the bonus wanders the maze. In Pac-Man, this is a no-op.
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
        gameModel().optLevel().ifPresent(ActorAnimationManager::resetActorAnimations);
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final boolean silent = gameContext().gamePlay().isDemoLevelRunning(gameModel())
            || gameState().id() instanceof TestStateID;
        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter game state '{}'", e.newState().name());
        final GameState newState = (GameState) e.newState();

        if (GameStateID.GAME_LEVEL_COMPLETE.identifies(newState)) {
            final GameLevel level = gameModel().assertLevel();
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);

            final var completedAnimation = new LevelCompletedAnimation(level, () -> gameState().triggerTimeout());
            playScene2D.setLevelCompletedAnimation(completedAnimation);
            completedAnimation.play();
        }
        else if (GameStateID.GAME_OVER.identifies(newState)) {
            gameModel().hudState().creditOn();
            optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        playScene2D.acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        gameState().triggerTimeout();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = actionContext().clock().currentTick();
        optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    @Override
    public void onTestStarted(TestStartedEvent e) {
        actionContext().ui().shortMessage("Testing level %d".formatted(e.level().number()));
    }
}
