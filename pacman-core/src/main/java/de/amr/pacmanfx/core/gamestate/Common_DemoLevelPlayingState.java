/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantConfig;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;

import java.util.Optional;

public final class Common_DemoLevelPlayingState extends AbstractGameState {

    public Common_DemoLevelPlayingState() {
        super(CommonGameStateID.DEMO_LEVEL_PLAYING);
    }

    @Override
    public void onEnterState(GameContext game) {
        configureHUD(hud);
        gamePlay.showMessage(game, MessageType.GAME_OVER);

        final GameLevel level = gamePlay.buildDemoLevel(game);
        session.setLevel(level);
        session.setNumLives(1);

        level.entities().ghosts().forEach(ghost -> {
            ghost.worldNavigation().setDisabled(true);
            ghost.animation().setStopped(true);
        });

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        final GameVariantConfig variantConfig = game.variant();
        final GameLevel level = session.level();

        if (tick == 1) {
            gamePlay.prepareLevelForPlaying(game, level);
        }
        else if (tick == 2) {
            showPacAndGhosts(level.entities());
        }
        else if (tick == game.variant().rules().demoLevelHuntingStartTick()) {
            startEnergizerBlinking(level);

            final Pac pac = level.entities().pac();
            pac.state().setEnumValue(PacState.ACTIVE);

            level.entities().ghosts().forEach(ghost -> {
                ghost.worldNavigation().setDisabled(false);
                ghost.animation().setStopped(false);
            });

            // This call fires a game event!
            level.huntingTimerStrategy().startFirstPhase(game, level.number());
        }
        else if (tick >= game.variant().rules().demoLevelHuntingStartTick()) {
            gamePlay.update(game, level);
        }

        computeNextState(game, level).ifPresent(nextState ->
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
