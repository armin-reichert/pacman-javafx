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
        facade().currentSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        facade().currentSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        facade().currentSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        facade().currentSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        optGameLevel().ifPresent(level -> gameScene().resetActorAnimations(level));
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final boolean silent = facade().currentGame().isDemoLevelRunning() || 
            facade().currentGameState() instanceof TestState;
        if (!silent) {
            facade().currentSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.newState() == Arcade_GameState.LEVEL_COMPLETE) {
            facade().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().levelCompletedAnimation().play();
        } else if (e.newState() == Arcade_GameState.GAME_OVER) {
            facade().currentSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
            facade().currentGame().hud().credit(true);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        facade().currentSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        gameScene().acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        facade().currentGameState().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        facade().currentSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = facade().gameClock().tickCount();
        facade().currentSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        facade().currentSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        facade().currentSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        facade().currentSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
