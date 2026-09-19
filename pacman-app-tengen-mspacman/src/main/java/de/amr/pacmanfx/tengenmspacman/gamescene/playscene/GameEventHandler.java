/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.event.gameplay.*;
import de.amr.pacmanfx.core.event.ghost.GhostEatenEvent;
import de.amr.pacmanfx.core.event.pac.*;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Extras;
import de.amr.pacmanfx.tengenmspacman.gamestate.Tengen_GameState;
import de.amr.pacmanfx.tengenmspacman.model.MessageAnimation;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptions;

class GameEventHandler implements DefaultGameEventListener {

    private final TengenMsPacMan_PlayScene2D gameScene;

    public GameEventHandler(TengenMsPacMan_PlayScene2D gameScene) {
        this.gameScene = gameScene;
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return gameScene.app().variantManager().currentRuntime().uiConfig().optSoundEffects();
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onStopAllSounds(StopAllSoundsEvent event) {
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
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
        final GameSystems systems = game.playConfig().systems();
        final GameSession session = game.session();
        session.optLevel().ifPresent(level -> {
            resetActorAnimations(systems.actorSpriteAnimController(), session, level);
            gameScene.dynamicCamera().playIntroSequence();
            level.showMessage(MessageType.READY);
        });
    }

    @Override
    public void onGameStarted(GameStartedEvent e) {
        final GameContext game = e.game();
        final GameSession session = game.session();
        final boolean silent = session.isAttractMode() || game.state().id() instanceof TestStateID;
        if (!silent) {
            optSoundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final GameContext game = gameScene.game();
        Logger.info("Enter game state '{}'", e.newState().name());
        final GameSession session = game.session();
        if (e.newState() == Tengen_GameState.GAME_LEVEL_COMPLETE.state()) {
            final GameLevel level = session.level();
            final int numFlashes = game.playConfig().rules().numLevelFlashes(level.number());
            optSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene.playLevelCompleteAnimation(level, numFlashes);
        }
        else if (e.newState() == Tengen_GameState.GAME_OVER.state()) {
            final PlayScene2DCamera camera = gameScene.dynamicCamera();

            optSoundEffects().ifPresent(GameSoundEffects::stopAll);

            final MessageAnimation messageAnimation = session.value(
                TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class);

            //TODO This does not belong here!
            if (messageAnimation != null) {
                // Compute exact message size and wrap position at right border
                final Font font = GlobalFonts.ARCADE.font();
                final String gameOverText = TengenMsPacMan_RenderConfig.MESSAGE_TEXTS.get(MessageType.GAME_OVER);
                final double width = BaseRenderer.textWidth(gameOverText, font);
                final double wrapX = gameScene.reqCanvasRendering().unscaledWidth() + 0.5 * width;
                messageAnimation.setWidth(width);
                messageAnimation.setWrapX(wrapX);
                Logger.info("Message animation bounds computed: width={}, wrapX={}", width, wrapX);
            }

            camera.enterManualMode();
            camera.setToTopPosition();
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
    public void onLevelStarted(LevelStartedEvent e) {
        final GameContext game = gameScene.game();
        final GameSession session = game.session();
        final ActorSpriteAnimController animController = game.playConfig().systems().actorSpriteAnimController();

        session.optLevel().ifPresent(level -> resetActorAnimations(animController, session, level));
        gameScene.dynamicCamera().playIntroSequence();
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        final GameContext game = gameScene.game();
        game.state().triggerTimeout();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        gameScene.dynamicCamera().enterManualMode();
        optSoundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        optSoundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(e.tick()));
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

    //TODO This belongs into an animation system class

    public void resetActorAnimations(ActorSpriteAnimController animSystem, GameSession session, GameLevel level) {
        resetPacAnimation(animSystem, gameOptions(session).boosterEnabled(), level.entities().pac());
        level.entities().ghosts().forEach(ghost -> resetGhostAnimation(animSystem, ghost));
    }

    public void resetPacAnimation(ActorSpriteAnimController animSystem, boolean boosterEnabled, Pac pac) {
        animSystem.select(pac, boosterEnabled
            ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER
            : CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animSystem.resetSelected(pac);
    }

    public void resetGhostAnimation(ActorSpriteAnimController animSystem, Ghost ghost) {
        animSystem.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
        animSystem.resetSelected(ghost);
    }
}
