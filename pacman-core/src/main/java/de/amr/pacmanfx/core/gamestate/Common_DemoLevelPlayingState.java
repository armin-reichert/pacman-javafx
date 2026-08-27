/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
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
        final GameSystems systems = game.variant().systems();
        final GamePlay gamePlay = game.variant().gamePlay();
        final GameSession session = game.session();

        configureHUD(session.hud());
        gamePlay.showMessage(game, MessageType.GAME_OVER);

        final GameLevel level = gamePlay.buildDemoLevel(game);
        session.setLevel(level);
        session.setNumLives(1);

        level.entities().ghosts().forEach(ghost -> {
            systems.worldNavigator().setDisabled(ghost, true);
            systems.ghostAnimation().setDisabled(ghost, true);
        });

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        final GameVariantConfig variantConfig = game.variant();
        final GamePlay gamePlay = variantConfig.gamePlay();
        final GameSystems systems = variantConfig.systems();

        final GameSession session = game.session();
        final GameLevel demoLevel = session.level();

        if (tick == 1) {
            gamePlay.prepareLevelForPlaying(game, demoLevel);
        }
        else if (tick == 2) {
            showActors(demoLevel.entities());
        }
        else if (tick == game.variant().rules().demoLevelHuntingStartTick()) {
            session.hud().clearMessage();
            startEnergizerBlinking(demoLevel);

            final Pac pac = demoLevel.entities().pac();
            systems.pacState().setState(pac, PacState.ACTIVE);

            demoLevel.entities().ghosts().forEach(ghost -> {
                systems.worldNavigator().setDisabled(ghost, false);
                systems.ghostAnimation().setDisabled(ghost, false);
            });

            // This call fires a game event!
            demoLevel.huntingTimerStrategy().startFirstPhase(game, demoLevel.number());
        }
        else if (tick >= game.variant().rules().demoLevelHuntingStartTick()) {
            gamePlay.update(game, demoLevel);
        }

        computeNextState(game, demoLevel).ifPresent(nextState ->
            variantConfig.gameFlow().enterGameState(game, nextState));
    }

    private void configureHUD(HUD hud) {
        hud.gameScore().data().setEnabled(false);
        hud.highScore().data().setEnabled(false);
        hud.levelCounter().hide();
        hud.showCredit();
        hud.show();
    }

    private void startEnergizerBlinking(GameLevel level) {
        final Pulse heartbeat = level.heartbeat();
        heartbeat.setStartState(Pulse.State.ON);
        heartbeat.restart();
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
