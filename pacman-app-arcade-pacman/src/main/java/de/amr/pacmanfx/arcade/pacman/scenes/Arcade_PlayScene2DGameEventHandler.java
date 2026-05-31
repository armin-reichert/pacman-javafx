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
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        optGameLevel().ifPresent(level -> gameScene().resetActorAnimations(level));
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final boolean silent = game().isDemoLevelRunning() || game().flow().state() instanceof TestState;
        if (!silent) {
            gameScene().optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.newState() == Arcade_GameState.LEVEL_COMPLETE) {
            gameScene().optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().levelCompletedAnimation().play();
        } else if (e.newState() == Arcade_GameState.GAME_OVER) {
            gameScene().optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
            game().hud().credit(true);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        gameScene().acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        game().flow().state().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = gameContext().clock().tickCount();
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
