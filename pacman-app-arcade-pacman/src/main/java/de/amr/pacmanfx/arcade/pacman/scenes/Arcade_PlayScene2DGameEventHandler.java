/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui.gamescene.BaseGameSceneHandler;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

public class Arcade_PlayScene2DGameEventHandler extends BaseGameSceneHandler {

    private final Arcade_PlayScene2D playScene2D;

    public Arcade_PlayScene2DGameEventHandler(Arcade_PlayScene2D playScene2D) {
        super(playScene2D.appContext());
        this.playScene2D = playScene2D;
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        // This is the sound in Ms. Pac-Man when the bonus wanders the maze. In Pac-Man, this is a no-op.
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
    public void onCreditAdded(CreditAddedEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        optGameLevel().ifPresent(playScene2D::resetActorAnimations);
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final boolean silent = appContext().currentGameContext().gameModel().isDemoLevelRunning() ||
            appContext().currentGameContext().gameState() instanceof TestState;
        if (!silent) {
            appContext().currentSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final GameState newState = (GameState) e.newState();

        if (GameStateID.GAME_LEVEL_COMPLETE.identifies(newState)) {
            appContext().currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
            playScene2D.levelCompletedAnimation().play();
        }
        else if (GameStateID.GAME_OVER.identifies(newState)) {
            appContext().currentGameContext().gameModel().hud().creditOn();
            appContext().currentSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        playScene2D.acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        appContext().currentGameContext().gameState().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = appContext().gameClock().tickCount();
        appContext().currentSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        appContext().currentSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
