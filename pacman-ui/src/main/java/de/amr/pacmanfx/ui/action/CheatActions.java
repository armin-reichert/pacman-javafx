/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngineImpl;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.runtime.action.GameAction;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.assets.VoiceID;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public final class CheatActions {

    private final GameAction actionAddLives;
    private final GameAction actionEatAllPellets;
    private final GameAction actionKillGhosts;
    private final GameAction actionEnterNextLevel;
    private final GameAction actionToggleAutopilot;
    private final GameAction actionActivateAutopilot;
    private final GameAction actionDeactivateAutopilot;
    private final GameAction actionActivateImmunity;
    private final GameAction actionDeactivateImmunity;
    private final GameAction actionToggleImmunity;

    private final Set<ActionKeyBinding> bindings;

    public CheatActions() {

        actionAddLives = new GameAction("cheat_add_lives") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                final GameSession session = engine.currentGame().session();
                session.setNumLives(session.numLives() + 3);
                session.cheats().notifyCheatUsed();
                final String msg = engine.translationManager().translate("flash.cheat_add_lives", session.numLives());
                engineImpl.ui().shortMessage(msg);
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return normalLevel(engine).isPresent();
            }
        };

        actionEatAllPellets = new GameAction("cheat_eat_all_pellets") {
            @Override
            public void execute(PacManGamesEngine engine) {
                final GameSession session = engine.currentGame().session();
                final GameLevel level = session.level();
                level.food().eatPellets();
                session.cheats().notifyCheatUsed();
                engine.currentGame().eventManager().publishEvent(
                    new PacEatsFoodEvent(level.entitySet().pac(), false, true, engine.clock().currentTick()));
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                final AbstractGameState gameState = engine.currentGame().state();
                return normalLevel(engine).isPresent() && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState);
            }
        };

        actionKillGhosts = new GameAction("cheat_kill_ghosts") {
            @Override
            public void execute(PacManGamesEngine engine) {
                final GameContext game = engine.currentGame();
                final GameSession session = game.session();
                final GameLevel level = session.level();
                
                session.cheats().notifyCheatUsed();

                final List<Ghost> killableGhosts = level.entitySet().ghosts()
                    .filter(ghost -> GhostState.FRIGHTENED == ghost.state().enumValue()
                        || GhostState.HUNTING_PAC == ghost.state().enumValue())
                    .toList();

                if (!killableGhosts.isEmpty()) {
                    level.setGhostKillCount(0); // start again with lowest number for killing ghost
                    killableGhosts.forEach(ghost -> game.playConfig().gamePlay().pacEatsGhost(game, level, ghost));
                    game.playConfig().gameFlow().enterGameState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
                }
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                final AbstractGameState gameState = engine.currentGame().state();
                return normalLevel(engine).isPresent() && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState);
            }
        };

        actionEnterNextLevel = new GameAction("cheat_enter_next_level") {
            @Override
            public void execute(PacManGamesEngine engine) {
                engine.currentGame().session().cheats().notifyCheatUsed();
                engine.currentGame().playConfig().gameFlow().enterGameState(engine.currentGame(), CommonGameStateID.GAME_LEVEL_COMPLETE);
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                final AbstractGameState state = engine.currentGame().state();
                final GameLevel level = normalLevel(engine).orElse(null);
                return level != null
                    && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(state)
                    && level.number() < engine.currentGame().playConfig().rules().lastLevelNumber();
            }
        };

        actionToggleAutopilot = new GameAction("toggle_autopilot") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                final GameCheats cheats = engine.currentGame().session().cheats();
                setAutopilot(engineImpl, !cheats.isPacUsingAutopilot());
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return normalLevel(engine).isPresent();
            }
        };

        actionActivateAutopilot = new GameAction("activate_autopilot") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                setAutopilot(engineImpl, true);
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return normalLevel(engine).isPresent();
            }
        };

        actionDeactivateAutopilot = new GameAction("deactivate_autopilot") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                setAutopilot(engineImpl, false);
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return normalLevel(engine).isPresent();
            }
        };

        actionActivateImmunity = new GameAction("activate_immunity") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                setPacImmune(engineImpl, true);
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return normalLevel(engine).isPresent();
            }
        };

        actionDeactivateImmunity = new GameAction("deactivate_immunity") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                setPacImmune(engineImpl, false);
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return normalLevel(engine).isPresent();
            }
        };

        actionToggleImmunity = new GameAction("toggle_immunity") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                final GameCheats cheats = engine.currentGame().session().cheats();
                setPacImmune(engineImpl, !cheats.isPacImmune());
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                return normalLevel(engine).isPresent();
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionToggleAutopilot(), combine().alt().key(KeyCode.A)),
            new ActionKeyBinding(actionToggleImmunity(),  combine().alt().key(KeyCode.I)),
            new ActionKeyBinding(actionEatAllPellets(),   combine().alt().key(KeyCode.E)),
            new ActionKeyBinding(actionAddLives(),        combine().alt().key(KeyCode.L)),
            new ActionKeyBinding(actionEnterNextLevel(),  combine().alt().key(KeyCode.N)),
            new ActionKeyBinding(actionKillGhosts(),      combine().alt().key(KeyCode.X))
        );
    }

    public GameAction actionAddLives() {
        return actionAddLives;
    }

    public GameAction actionEatAllPellets() {
        return actionEatAllPellets;
    }

    public GameAction actionKillGhosts() {
        return actionKillGhosts;
    }

    public GameAction actionEnterNextLevel() {
        return actionEnterNextLevel;
    }

    public GameAction actionToggleAutopilot() {
        return actionToggleAutopilot;
    }

    public GameAction actionActivateAutopilot() {
        return actionActivateAutopilot;
    }

    public GameAction actionDeactivateAutopilot() {
        return actionDeactivateAutopilot;
    }

    public GameAction actionActivateImmunity() {
        return actionActivateImmunity;
    }

    public GameAction actionDeactivateImmunity() {
        return actionDeactivateImmunity;
    }

    public GameAction actionToggleImmunity() {
        return actionToggleImmunity;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }

    // Helpers

    private void setAutopilot(PacManGamesEngineImpl engine, boolean auto) {
        final GameCheats cheats = engine.currentGame().session().cheats();
        final GameUI ui = engine.ui();

        cheats.pacUsingAutopilotProperty().set(auto);

        final String message = engine.translationManager().translate(auto ? "flash.autopilot_on" : "flash.autopilot_off");
        final Media voice = auto ? VoiceID.AUTOPILOT_ON.media() : VoiceID.AUTOPILOT_OFF.media();

        ui.shortMessage(message);
        engine.soundManager().voice().playAfterSec(1, voice);
    }

    private void setPacImmune(PacManGamesEngineImpl engine, boolean immune) {
        final GameCheats cheats = engine.currentGame().session().cheats();
        final GameUI ui = engine.ui();

        cheats.pacImmuneProperty().set(immune);

        final String message = engine.translationManager().translate(immune ? "flash.player_immunity_on" : "flash.player_immunity_off");
        final Media voice = immune ? VoiceID.IMMUNITY_ON.media() : VoiceID.IMMUNITY_OFF.media();

        ui.shortMessage(message);
        engine.soundManager().voice().playAfterSec(1, voice);
    }

    private Optional<GameLevel> normalLevel(PacManGamesEngine engine) {
        final GameSession session = engine.currentGame().session();
        return session.optLevel().filter(_ -> !session.isAttractMode());
    }
}