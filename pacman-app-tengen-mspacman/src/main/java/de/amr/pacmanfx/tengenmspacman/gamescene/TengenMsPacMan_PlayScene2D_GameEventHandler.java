/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
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
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Extras;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.flow.Tengen_GameState;
import de.amr.pacmanfx.tengenmspacman.model.MessageAnimation;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.basics.util.Ufx.textWidth;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.GAME_OVER_MESSAGE_TEXT;

public interface TengenMsPacMan_PlayScene2D_GameEventHandler extends DefaultGameEventListener {

    TengenMsPacMan_PlayScene2D gameScene();

    default Optional<GameSoundEffects> optSoundEffects() {
        return gameScene().app().gameVariants().currentGameVariant().uiConfig().optSoundEffects();
    }

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
        final GameContext game = gameScene().game();
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        session.optLevel().ifPresent(level -> {
            resetActorAnimations(systems.actorSpriteAnimController(), session, level);
            gameScene().dynamicCamera().playIntroSequence();
            if (game.variant().gamePlay() instanceof TengenMsPacMan_GamePlay tengenGame) {
                tengenGame.showLevelMessage(game, level, GameLevelMessageType.READY);
            }
        });
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
        final GameContext game = gameScene().game();
        Logger.info("Enter game state '{}'", e.newState().name());
        final GameSession session = game.session();
        if (e.newState() == Tengen_GameState.GAME_LEVEL_COMPLETE.state()) {
            final GameLevel level = session.level();
            final int numFlashes = game.variant().rules().numLevelFlashes(level.number());
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene().playLevelCompleteAnimation(level, numFlashes);
        }
        else if (e.newState() == Tengen_GameState.GAME_OVER.state()) {
            final TengenMsPacMan_PlayScene2D playScene2D = gameScene();
            final PlayScene2DCamera camera = playScene2D.dynamicCamera();

            optSoundEffects().ifPresent(GameSoundEffects::stopAll);

            final MessageAnimation messageAnimation = session.value(
                TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class);

            if (messageAnimation != null) {
                // Compute exact message size and wrap position at right border
                final Font font = Font.font(BaseRenderer.ARCADE_FONT.getFamily(), TS);
                final double width = textWidth(GAME_OVER_MESSAGE_TEXT, font);
                final double wrapX = gameScene().reqCanvasRendering().unscaledWidth() + 0.5 * width;
                messageAnimation.setWidth(width);
                messageAnimation.setWrapX(wrapX);
                Logger.info("Message animation bounds computed: width={}, wrapX={}", width, wrapX);
            }

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
        final GameContext game = gameScene().game();
        gameScene().acceptGameLevel(game.session(), e.level());
    }

    @Override
    default void onLevelStarted(LevelStartedEvent e) {
        final GameContext game = gameScene().game();
        final GameSession session = game.session();
        final ActorSpriteAnimController animController = game.variant().systems().actorSpriteAnimController();

        session.optLevel().ifPresent(level -> resetActorAnimations(animController, session, level));
        gameScene().dynamicCamera().playIntroSequence();
    }

    @Override
    default void onPacDead(PacDeadEvent e) {
        final GameContext game = gameScene().game();
        game.state().triggerTimeout();
    }

    @Override
    default void onPacDying(PacDyingEvent e) {
        gameScene().dynamicCamera().enterManualMode();
        optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    default void onPacEatsFood(PacEatsFoodEvent e) {
        optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(e.tick()));
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

    //TODO This belongs into an animation system class

    default void resetActorAnimations(ActorSpriteAnimController animSystem, GameSession session, GameLevel level) {
        gameScene().resetPacAnimation(animSystem, session, level.entities().pac());
        level.entities().ghosts().forEach(ghost -> gameScene().resetGhostAnimation(animSystem, ghost));
    }

    default void resetPacAnimation(ActorSpriteAnimController animSystem, GameSession session, Pac pac) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) gameScene().game().variant().gamePlay();
        animSystem.select(pac, gamePlay.isBoosterOn(session)
            ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER
            : CommonSpriteAnimationID.PAC_MUNCHING);
        animSystem.resetSelected(pac);
    }

    default void resetGhostAnimation(ActorSpriteAnimController animSystem, Ghost ghost) {
        animSystem.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
        animSystem.resetSelected(ghost);
    }
}
