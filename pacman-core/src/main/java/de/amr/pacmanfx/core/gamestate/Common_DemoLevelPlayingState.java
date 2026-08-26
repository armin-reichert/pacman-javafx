/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameVariantConfig;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.level.MessageType;

import java.util.Optional;

public final class Common_DemoLevelPlayingState extends GameState {

    public Common_DemoLevelPlayingState() {
        super(CommonGameStateID.DEMO_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameLevel level = game.variant().gamePlay().buildDemoLevel(game);
        final GameSession session = game.session();

        session.setLevel(level);
        session.setNumLives(1);

        session.hud().showCredit();
        session.hud().levelCounter().hide();
        session.hud().gameScore().data().setEnabled(false);
        session.hud().highScore().data().setEnabled(false);
        session.hud().show();

        game.eventManager().publishGameEvent(new LevelCreatedEvent(session.level()));
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        final GameVariantConfig variantConfig = game.variant();
        final GamePlay gamePlay = variantConfig.gamePlay();
        final EntityUpdater updater = variantConfig.systems().entityUpdater();

        final GameSession session = game.session();
        final GameLevel demoLevel = session.level();

        if (tick == 1) {
            gamePlay.prepareLevelForPlaying(game, demoLevel);
            gamePlay.showMessage(game, MessageType.GAME_OVER);
        }
        else if (tick == 2) {
            showActors(session.level().entities());
        }
        else if (tick == game.variant().rules().demoLevelHuntingStartTick()) {
            startEnergizerBlinking(session.level());
            startActorAnimations(session.level(), game.variant().systems().actorSpriteAnimController());
            clearReadyMessage(game.session().hud());
            // This call fires a game event!
            session.level().huntingTimerStrategy().startFirstPhase(game, session.level().number());
        }
        else if (tick > game.variant().rules().demoLevelHuntingStartTick()) {
            updater.updateEntities(game, demoLevel);
            gamePlay.updateGamePlay(game, demoLevel);
            computeNextState(game, demoLevel)
                .ifPresent(nextState -> variantConfig.gameFlow().enterGameState(game, nextState));
        }
        updater.updateHUD(game);
    }

    private void startEnergizerBlinking(GameLevel level) {
        final Pulse heartbeat = level.heartbeat();
        heartbeat.setStartState(Pulse.State.ON);
        heartbeat.restart();
    }

    private void startActorAnimations(GameLevel level, ActorSpriteAnimController animController) {
        final GameLevelEntitySet entities = level.entities();
        animController.playSelected(entities.pac());
        entities.ghosts().forEach(animController::playSelected);
    }

    // Clears the "READY!" message. The "GAME_OVER" (demo level) and the "TEST LEVEL XX" messages are left alone.
    private void clearReadyMessage(HUD hud) {
        if (hud.messageView().data().messageType() == MessageType.READY) {
            hud.clearMessage();
        }
    }

    private void showActors(GameLevelEntitySet entities) {
        entities.pac().show();
        entities.ghosts().forEach(GameEntity::show);
    }

    private Optional<CommonGameStateID> computeNextState(GameContext game, GameLevel level) {
        if (game.variant().rules().isLevelCompleted(level)) {
            return Optional.of(CommonGameStateID.GAME_INTRO);
        }
        else if (game.session().thisFrame().gamePlayStep().pacKilled()) {
            return Optional.of(CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        }
        else if (game.session().thisFrame().gamePlayStep().hasGhostBeenKilled()) {
            return Optional.of(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        }
        return Optional.empty(); // keep game state
    }
}
