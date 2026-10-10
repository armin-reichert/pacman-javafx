/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.playscene;

import de.amr.basics.fsm.State;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.TestStartedEvent;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.event.gameplay.*;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.*;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationSystem;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import org.tinylog.Logger;

import java.util.Optional;

class PlaySceneGameEventHandler implements DefaultGameEventListener {

    private final Arcade_PlayScene2D gameScene;

    public PlaySceneGameEventHandler(Arcade_PlayScene2D gameScene) {
        this.gameScene = gameScene;
    }

    @Override
    public void onStopAllSounds(StopAllSoundsEvent event) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::stopAll);
    }

    public Optional<PacManGameSoundEffects> optSoundEffects() {
        return gameScene.engineContext().gameVariantManager().currentRuntime().uiConfig().optSoundEffects();
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        // This is the sound in Ms. Pac-Man when the bonus wanders the maze. In Pac-Man, this is a no-op.
        optSoundEffects().ifPresent(PacManGameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onGameContinued(GameContinuedEvent e) {
        gameScene.engineContext().optCurrentGame().ifPresent(game -> {
            //TODO Does not belong here
            final ActorSpriteAnimController animController = game.playConfig().systems().actorSpriteAnimController();
            game.session().optLevel().ifPresent(level -> ActorAnimationSystem.resetActorAnimations(animController, level));
        });
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        gameScene.engineContext().optCurrentGame().ifPresent(game -> {
            final GameSession session = game.session();
            final boolean silent = session.isAttractMode() || game.state().id() instanceof TestStateID;
            if (!silent) {
                optSoundEffects().ifPresent(PacManGameSoundEffects::playGameReadySound);
            }
        });
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final State<GameContext> newState = e.newState();
        Logger.info("Entering game state '{}'", newState.name());

        gameScene.engineContext().optCurrentGame().ifPresent(game -> {
            if (CommonGameStateID.GAME_LEVEL_COMPLETE.hasSameNameAs(newState)) {
                optSoundEffects().ifPresent(PacManGameSoundEffects::stopAll);

                final GameLevel level = game.session().level();
                final int numFlashes = game.playConfig().rules().numLevelFlashes(level.number());
                gameScene.levelCompletedAnimation().play(level, numFlashes);
            } else if (CommonGameStateID.GAME_OVER.hasSameNameAs(newState)) {
                optSoundEffects().ifPresent(PacManGameSoundEffects::playGameOverSound);
            }
        });
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        gameScene.engineContext().optCurrentGame().ifPresent(game -> gameScene.onAcceptGameLevel(game.session(), e.level()));
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        gameScene.engineContext().optCurrentGame().ifPresent(game -> game.state().triggerTimeout());
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = gameScene.engineContext().clock().currentTick();
        optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacPowerStarts(PacPowerStartsEvent e) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacPowerEnds(PacPowerEndsEvent e) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        optSoundEffects().ifPresent(PacManGameSoundEffects::playExtraLifeSound);
    }

    @Override
    public void onTestStarted(TestStartedEvent e) {
        gameScene.ui().shortMessage("Testing level %d".formatted(e.level().number()));
    }
}
