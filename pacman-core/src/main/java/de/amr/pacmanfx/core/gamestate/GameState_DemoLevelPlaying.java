/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.rules.GameRules;

import java.util.Optional;

public final class GameState_DemoLevelPlaying extends GameState {

    public GameState_DemoLevelPlaying() {
        super(CommonGameStateID.DEMO_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        session.setLevel(game.variant().gamePlay().buildDemoLevel(game));
        session.hud().showCredit().hideLivesCounter();
        game.eventManager().publishGameEvent(new LevelCreatedEvent(session.level()));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameRules gameRules = game.variant().rules();
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final long tick = timer().tickCount();

        if (tick == 1) {
            prepareLevel(game);
        }
        else if (tick == 2) {
            showActors(level.entities());
        }
        else if (tick == gameRules.demoLevelHuntingStartTick()) {
            startLevel(game);
        }
        else if (tick > gameRules.demoLevelHuntingStartTick()) {
            updateDemoLevel(game);
        }
    }

    private void updateDemoLevel(GameContext game) {
        final GameSession session = game.session();
        final GamePlay gamePlay = game.variant().gamePlay();
        final GameLevel level = session.level();

        game.variant().systems().entityUpdater().updateEntities(game, level);
        gamePlay.updateGamePlay(game, level);
        computeNextState(game, level).ifPresent(nextState -> game.variant().gameFlow().enterState(game, nextState));
    }

    private void prepareLevel(GameContext game) {
        final GameSession session = game.session();
        final GamePlay gamePlay = game.variant().gamePlay();
        final GameLevel level = session.level();

        session.score().data().setEnabled(false);
        session.highScore().data().setEnabled(false);

        gamePlay.prepareLevelForPlaying(game);
        gamePlay.showLevelMessage(game, level, GameLevelMessageType.GAME_OVER);

        // Note: This event is very important because it triggers the creation of the actor animations!
        game.eventManager().publishGameEvent(new LevelStartedEvent(level));
    }

    private void showActors(GameLevelEntitySet entities) {
        entities.pac().show();
        entities.ghosts().forEach(GameEntity::show);
    }

    private void startLevel(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        final GameLevel level = session.level();

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        // Start animating actors
        systems.spriteAnimController().playSelected(level.entities().pac());
        level.entities().ghosts().forEach(systems.spriteAnimController()::playSelected);

        // Clear "READY!" message. The "GAME_OVER" (demo level) and  "TEST LEVEL XX" messages are not cleared!
        session.hud().optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> session.hud().clearMessage());

        // This call fires a game event!
        level.huntingTimerStrategy().startFirstPhase(game, level.number());
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
