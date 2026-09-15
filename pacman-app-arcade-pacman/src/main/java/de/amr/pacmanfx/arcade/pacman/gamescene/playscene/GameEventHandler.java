/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.playscene;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
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
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationManager;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import org.tinylog.Logger;

import java.util.Optional;

class GameEventHandler implements DefaultGameEventListener {

    private final Arcade_PlayScene2D gameScene;

    public GameEventHandler(Arcade_PlayScene2D gameScene) {
        this.gameScene = gameScene;
    }

    @Override
    public void onStopAllSounds(StopAllSoundsEvent event) {
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return gameScene.app().variantManager().currentVariantRuntime().uiConfig().optSoundEffects();
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
        final GameContext game = gameScene.game();
        //TODO Does not belong here
        final ActorSpriteAnimController animController = game.playConfig().systems().actorSpriteAnimController();
        game.session().optLevel().ifPresent(level -> ActorAnimationManager.resetActorAnimations(animController, level));
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final GameContext game = gameScene.game();
        final GameSession session = game.session();
        final boolean silent = session.isAttractMode() || game.state().id() instanceof TestStateID;
        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final State<GameContext> newState = e.newState();

        Logger.info("Entering game state '{}'", newState.name());

        final GameContext game = gameScene.game();
        if (CommonGameStateID.GAME_LEVEL_COMPLETE.hasSameNameAs(newState)) {
            final GameLevel level = game.session().level();
            final int numFlashes = game.playConfig().rules().numLevelFlashes(level.number());

            optSoundEffects().ifPresent(GameSoundEffects::stopAll);

            final var animation = new LevelCompletedAnimation(level, () -> game.state().triggerTimeout());
            gameScene.setLevelCompletedAnimation(animation);
            animation.play(numFlashes);
        }
        else if (CommonGameStateID.GAME_OVER.hasSameNameAs(newState)) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        final GameContext game = gameScene.game();
        gameScene.acceptGameLevel(game.session(), e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        final GameContext game = gameScene.game();
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        game.state().triggerTimeout();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = gameScene.app().clock().currentTick();
        optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacPowerStarts(PacPowerStartsEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacPowerEnds(PacPowerEndsEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScore(SpecialScoreEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    @Override
    public void onTestStarted(TestStartedEvent e) {
        gameScene.app().ui().shortMessage("Testing level %d".formatted(e.level().number()));
    }
}
