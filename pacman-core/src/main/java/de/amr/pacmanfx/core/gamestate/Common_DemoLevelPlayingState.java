/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.basics.ui.entities.props.messageview.MessageType;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import org.tinylog.Logger;

import java.util.Optional;

public final class Common_DemoLevelPlayingState extends AbstractGameState {

    public Common_DemoLevelPlayingState() {
        super(CommonGameStateID.DEMO_LEVEL_PLAYING);
    }

    @Override
    public void onEnterState(GameContext game) {
        hud.gameScore().data().setEnabled(false);
        hud.highScore().data().setEnabled(false);
        hud.livesCounter().hide();
        hud.levelCounter().show();
        hud.creditDisplay().show();
        session.store(SessionKey.HUD_VISIBLE, true);

        final GameLevel level = gamePlay.buildDemoLevel(game);
        session.setLevel(level);
        session.setNumLives(1);

        level.entitySet().ghosts().forEach(ghost -> {
            ghost.worldNavigation().setPaused(true);
            ghost.animation().setStopped(true);
        });

        level.showMessage(MessageType.GAME_OVER);

        game.runtime().eventManager().publishEvent(new LevelCreatedEvent(game, level));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        final GameLevel level = session.optLevel().orElseThrow();

        if (stateTick == 1) {
            gamePlay.prepareLevelForPlaying(game, level);
        }
        else if (stateTick == 2) {
            showPacAndGhosts(level.entitySet());
        }
        else if (stateTick == game.runtime().gameRules().demoLevelHuntingStartTick()) {
            startEnergizerBlinking(level);

            final Pac pac = level.entitySet().pac();
            pac.state().setEnumValue(PacState.ACTIVE);

            level.entitySet().ghosts().forEach(ghost -> {
                ghost.worldNavigation().setPaused(false);
                ghost.animation().setStopped(false);
            });

            // This call fires a game event!
            level.huntingTimer().startFirstPhase(game, level.number());
        }
        else if (stateTick >= game.runtime().gameRules().demoLevelHuntingStartTick()) {
            gamePlay.update(game, level);
        }

        computeNextState(game, level).ifPresent(nextState -> {
            if (nextState == CommonGameStateID.GAME_INTRO) {
                clear(game);
            }
            game.runtime().gameFlow().enterGameState(game, nextState);
        });
    }

    private void clear(GameContext game) {
        game.session().optLevel().orElseThrow().entitySet().clear();
        game.session().removeLevel();
        Logger.info("Demo level has been removed");
    }

    private void startEnergizerBlinking(GameLevel level) {
        final Pulse heartbeat = level.heartbeat();
        heartbeat.setStartState(Pulse.State.ON);
        heartbeat.restart();
    }

    private Optional<CommonGameStateID> computeNextState(GameContext game, GameLevel level) {
        final Frame thisFrame = session.thisFrame();
        if (game.runtime().gameRules().isLevelCompleted(level)) {
            return Optional.of(CommonGameStateID.GAME_INTRO);
        }
        else if (thisFrame.pacKilled()) {
            return Optional.of(CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        }
        else if (thisFrame.hasGhostBeenKilled()) {
            return Optional.of(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        }
        return Optional.empty(); // keep game state
    }
}
