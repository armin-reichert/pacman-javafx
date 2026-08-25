/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.level.LevelMessageType;

import java.util.Optional;

public final class Common_DemoLevelPlayingState extends GameState {

    public Common_DemoLevelPlayingState() {
        super(CommonGameStateID.DEMO_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        session.setLevel(game.variant().gamePlay().buildDemoLevel(game));
        session.setNumLives(1);

        session.hud().showCredit();
        session.hud().levelCounter().hide();
        session.hud().show();

        game.eventManager().publishGameEvent(new LevelCreatedEvent(session.level()));
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        final GameLevel level = game.session().level();
        final int huntingStartTick = game.variant().rules().demoLevelHuntingStartTick();

        game.variant().systems().entityUpdater().updateHUD(game);

        if (tick == 1) {
            prepareLevel(game);
        }
        else if (tick == 2) {
            showActors(level.entities());
        }
        else if (tick == huntingStartTick) {
            startDemoLevel(game, level);
        }
        else if (tick > huntingStartTick) {
            updateDemoLevel(game);
        }
    }

    private void startDemoLevel(GameContext game, GameLevel level) {
        startEnergizerBlinking(level);
        startActorAnimations(level, game.variant().systems().actorSpriteAnimController());
        clearReadyMessage(game.session().hud());
        // This call fires a game event!
        level.huntingTimerStrategy().startFirstPhase(game, level.number());
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
        if (hud.messageView().data().messageType() == LevelMessageType.READY) {
            hud.clearMessage();
        }
    }

    private void updateDemoLevel(GameContext game) {
        final EntityUpdater updater = game.variant().systems().entityUpdater();
        final GamePlay gamePlay = game.variant().gamePlay();
        final GameLevel level = game.session().level();

        updater.updateEntities(game, level);
        gamePlay.updateGamePlay(game, level);
        computeNextState(game, level)
            .ifPresent(nextState -> game.variant().gameFlow().enterGameState(game, nextState));
    }

    private void prepareLevel(GameContext game) {
        final GameSession session = game.session();
        final GamePlay gamePlay = game.variant().gamePlay();
        final GameLevel level = session.level();

        session.hud().gameScore().data().setEnabled(false);
        session.hud().highScore().data().setEnabled(false);

        gamePlay.prepareLevelForPlaying(game);
        gamePlay.showLevelMessage(game, level, LevelMessageType.GAME_OVER);
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
