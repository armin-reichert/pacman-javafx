/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
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
        gameScene().soundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        gameScene().soundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        gameScene().soundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        gameScene().soundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        e.game().optGameLevel().ifPresent(level -> gameScene().resetActorAnimations(level));
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final Game game = e.game();
        final boolean silent = game.isDemoLevelRunning() || game.flow().state() instanceof TestState;
        if (!silent) {
            gameScene().soundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final Game game = e.game();
        if (e.newState() == Arcade_GameState.LEVEL_COMPLETE) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            gameScene().soundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().levelCompletedAnimation().play();
        } else if (e.newState() == Arcade_GameState.GAME_OVER) {
            gameScene().soundEffects().ifPresent(GameSoundEffects::playGameOverSound);
            game.hud().credit(true);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        gameScene().soundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        gameScene().acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        e.game().flow().state().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        gameScene().soundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = gameScene().gameContext().clock().tickCount();
        gameScene().soundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        gameScene().soundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        gameScene().soundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        gameScene().soundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
