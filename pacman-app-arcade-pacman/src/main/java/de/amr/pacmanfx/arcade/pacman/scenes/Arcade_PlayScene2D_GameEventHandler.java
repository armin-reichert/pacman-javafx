/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.event.TestStartedEvent;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.event.gameplay.*;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.*;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationManager;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import org.tinylog.Logger;

import java.util.Optional;

public interface Arcade_PlayScene2D_GameEventHandler extends DefaultGameEventListener {

    Arcade_PlayScene2D playScene();

    GameAppContext app();

    default Optional<GameSoundEffects> optSoundEffects() {
        return app().gameVariants().currentGameVariant().uiConfig().optSoundEffects();
    }

    default GameContext game() {
        return app().game();
    }

    @Override
    default void onBonusActivated(BonusActivatedEvent e) {
        // This is the sound in Ms. Pac-Man when the bonus wanders the maze. In Pac-Man, this is a no-op.
        optSoundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    default void onBonusEaten(BonusEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    default void onBonusExpired(BonusExpiredEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    default void onGameContinued(GameContinuedEvent e) {
        final ActorSpriteAnimController animSystem = game().variant().systems().actorSpriteAnimController();
        //TODO make animation systems from animation manager class
        game().session().optLevel().ifPresent(level -> ActorAnimationManager.resetActorAnimations(animSystem, level));
    }

    @Override
    default void onGameStarted(GameStartedEvent e) {
        final GameContext game = e.game();
        final GameSession session = game.session();
        final boolean silent = session.isAttractMode() || game.state().id() instanceof TestStateID;
        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    default void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter game state '{}'", e.newState().name());
        final AbstractGameState newState = (AbstractGameState) e.newState();

        if (CommonGameStateID.GAME_LEVEL_COMPLETE.hasSameNameAs(newState)) {
            final GameLevel level = game().session().level();
            final int numFlashes = game().variant().rules().numLevelFlashes(level.number());

            optSoundEffects().ifPresent(GameSoundEffects::stopAll);

            final var completedAnimation = new LevelCompletedAnimation(level, () -> game().state().triggerTimeout());
            playScene().setLevelCompletedAnimation(completedAnimation);
            completedAnimation.play(numFlashes);
        }
        else if (CommonGameStateID.GAME_OVER.hasSameNameAs(newState)) {
            game().session().hud().showCredit();
            optSoundEffects().ifPresent(GameSoundEffects::playGameOverSound);
        }
    }

    @Override
    default void onGhostEaten(GhostEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    default void onLevelCreated(LevelCreatedEvent e) {
        playScene().acceptGameLevel(game().session(), e.level());
    }

    @Override
    default void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        game().state().triggerTimeout();
    }

    @Override
    default void onPacDying(PacDyingEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    default void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = app().clock().currentTick();
        optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    default void onPacPowerStarts(PacPowerStartsEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    default void onPacPowerEnds(PacPowerEndsEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    default void onSpecialScore(SpecialScoreEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    @Override
    default void onTestStarted(TestStartedEvent e) {
        app().ui().shortMessage("Testing level %d".formatted(e.level().number()));
    }
}
