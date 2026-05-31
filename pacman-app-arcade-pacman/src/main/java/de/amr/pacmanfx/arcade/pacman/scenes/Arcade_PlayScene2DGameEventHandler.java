/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

public class Arcade_PlayScene2DGameEventHandler extends GameScene.DefaultGameEventHandler {

    public Arcade_PlayScene2DGameEventHandler(Arcade_PlayScene2D gameScene) {
        super(gameScene);
    }

    @Override
    public Arcade_PlayScene2D gameScene() {
        return (Arcade_PlayScene2D) super.gameScene();
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        // This is the sound in Ms. Pac-Man when the bonus wanders the maze. In Pac-Man, this is a no-op.
        services().currentSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        optGameLevel().ifPresent(level -> gameScene().resetActorAnimations(level));
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final boolean silent = services().currentGame().isDemoLevelRunning() ||
            services().currentGameState() instanceof TestState;
        if (!silent) {
            services().currentSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.newState() == Arcade_GameState.LEVEL_COMPLETE) {
            services().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().levelCompletedAnimation().play();
        } else if (e.newState() == Arcade_GameState.GAME_OVER) {
            services().currentSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
            services().currentGame().hud().credit(true);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        gameScene().acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        services().currentGameState().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = services().gameClock().tickCount();
        services().currentSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        services().currentSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
