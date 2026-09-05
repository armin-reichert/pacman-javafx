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
import org.tinylog.Logger;

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
            ghost.worldNavigation().setPaused(true);
            ghost.animation().setStopped(true);
        });

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        final GameVariantConfig variantConfig = game.variant();
        final GameLevel level = session.level();

        if (stateTick == 1) {
            gamePlay.prepareLevelForPlaying(game, level);
        }
        else if (stateTick == 2) {
            showPacAndGhosts(level.entities());
        }
        else if (stateTick == game.variant().rules().demoLevelHuntingStartTick()) {
            startEnergizerBlinking(level);

            final Pac pac = level.entities().pac();
            pac.state().setEnumValue(PacState.ACTIVE);

            level.entities().ghosts().forEach(ghost -> {
                ghost.worldNavigation().setPaused(false);
                ghost.animation().setStopped(false);
            });

            // This call fires a game event!
            level.huntingTimer().startFirstPhase(game, level.number());
        }
        else if (stateTick >= game.variant().rules().demoLevelHuntingStartTick()) {
            gamePlay.update(game, level);
        }

        computeNextState(game, level).ifPresent(nextState -> {
            if (nextState == CommonGameStateID.GAME_INTRO) {
                clear(game);
            }
            variantConfig.gameFlow().enterGameState(game, nextState);
        });
    }

    private void clear(GameContext game) {
        game.session().hud().clearMessage();
        game.session().level().entities().removeAll();
        game.session().setLevel(null);
        Logger.info("Demo level has been removed");
    }

    private void configureHUD(HUD hud) {
        hud.gameScore().data().setEnabled(false);
        hud.highScore().data().setEnabled(false);
        hud.levelCounter().hide();
        hud.creditDisplay().show();
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
        else if (game.session().thisFrame().pacKilled()) {
            return Optional.of(CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        }
        else if (game.session().thisFrame().hasGhostBeenKilled()) {
            return Optional.of(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        }
        return Optional.empty(); // keep game state
    }
}
