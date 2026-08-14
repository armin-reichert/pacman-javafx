/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.event.gameplay.*;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.*;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import org.tinylog.Logger;

import java.util.Optional;

public interface TengenMsPacMan_PlayScene2DGameEventHandler extends DefaultGameEventListener {

    GameAppContext app();

    default Optional<GameSoundEffects> optSoundEffects() {
        return app().gameVariants().currentGameVariant().uiConfig().optSoundEffects();
    }

    default GameContext game() {
        return app().game();
    }

    TengenMsPacMan_PlayScene2D gameScene();

    @Override
    default void onBonusActivated(BonusActivatedEvent e) {
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
        final GameSystems systems = game().variantConfig().systems();
        final GameSession session = game().session();
        session.optLevel().ifPresent(level -> {
            gameScene().resetActorAnimations(systems.spriteAnim(), session, level);
            gameScene().dynamicCamera().playIntroSequence();
            if (game().variantConfig().gamePlay() instanceof TengenMsPacMan_GamePlay tengenGame) {
                tengenGame.showLevelMessage(game(), level, GameLevelMessageType.READY);
            }
        });
    }

    @Override
    default void onGameStarted(GameStartedEvent e) {
        final GameContext game = e.game();
        final GameSession session = game.session();
        final boolean silent = session.isAttractMode() || session.gameState().id() instanceof TestStateID;
        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    default void onGameStateChange(GameStateChangeEvent e) {
        Logger.info("Enter game state '{}'", e.newState().name());
        final GameSession session = game().session();
        if (e.newState() == TengenMsPacMan_GameState.GAME_LEVEL_COMPLETE.state()) {
            final GameLevel level = session.assertLevel();
            final int numFlashes = game().variantConfig().rules().numLevelFlashes(level.number());
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().playLevelCompleteAnimation(level, numFlashes);
        }
        else if (e.newState() == TengenMsPacMan_GameState.GAME_OVER.state()) {
            final TengenMsPacMan_PlayScene2D playScene2D = gameScene();
            final PlayScene2DCamera camera = playScene2D.dynamicCamera();
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            session.hud().optMessage().ifPresent(playScene2D::startGameOverMessageAnimation);
            camera.enterManualMode();
            camera.setToTopPosition();
        }
    }

    @Override
    default void onGhostEaten(GhostEatenEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    default void onLevelCreated(LevelCreatedEvent e) {
        gameScene().acceptGameLevel(game().session(), e.level());
    }

    @Override
    default void onLevelStarted(LevelStartedEvent e) {
        final GameSession session = game().session();
        session.optLevel().ifPresent(
            level -> gameScene().resetActorAnimations(game().variantConfig().systems().spriteAnim(), session, level));
        gameScene().dynamicCamera().playIntroSequence();
    }

    @Override
    default void onPacDead(PacDeadEvent e) {
        game().session().gameState().triggerTimeout();
    }

    @Override
    default void onPacDying(PacDyingEvent e) {
        gameScene().dynamicCamera().enterManualMode();
        optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    default void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = app().clock().currentTick();
        gameScene().optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    default void onPacGetsPower(PacGetsPowerEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    default void onPacLostPower(PacLostPowerEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    default void onSpecialScore(SpecialScoreEvent e) {
        gameScene().optSoundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }
}
