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
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.runtime.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.GameActionExecutionContext;
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
            public void execute(GameActionExecutionContext context) {
                final GameSession session = context.currentGame().session();
                session.setNumLives(session.numLives() + 3);
                session.cheats().notifyCheatUsed();
                final String msg = context.translationManager().translate("flash.cheat_add_lives", session.numLives());
                context.ui().shortMessage(msg);
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                return normalLevel(context).isPresent();
            }
        };

        actionEatAllPellets = new GameAction("cheat_eat_all_pellets") {
            @Override
            public void execute(GameActionExecutionContext context) {
                final GameSession session = context.currentGame().session();
                final GameLevel level = session.level();
                level.food().eatPellets();
                session.cheats().notifyCheatUsed();
                context.currentGame().eventManager().publishEvent(
                    new PacEatsFoodEvent(level.entitySet().pac(), false, true, context.clock().currentTick()));
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                final AbstractGameState gameState = context.currentGame().state();
                return normalLevel(context).isPresent() && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState);
            }
        };

        actionKillGhosts = new GameAction("cheat_kill_ghosts") {
            @Override
            public void execute(GameActionExecutionContext context) {
                final GameContext game = context.currentGame();
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
            public boolean isEnabled(GameActionExecutionContext context) {
                final AbstractGameState gameState = context.currentGame().state();
                return normalLevel(context).isPresent() && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState);
            }
        };

        actionEnterNextLevel = new GameAction("cheat_enter_next_level") {
            @Override
            public void execute(GameActionExecutionContext context) {
                context.currentGame().session().cheats().notifyCheatUsed();
                context.currentGame().playConfig().gameFlow().enterGameState(context.currentGame(), CommonGameStateID.GAME_LEVEL_COMPLETE);
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                final AbstractGameState state = context.currentGame().state();
                final GameLevel level = normalLevel(context).orElse(null);
                return level != null
                    && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(state)
                    && level.number() < context.currentGame().playConfig().rules().lastLevelNumber();
            }
        };

        actionToggleAutopilot = new GameAction("toggle_autopilot") {
            @Override
            public void execute(GameActionExecutionContext context) {
                final GameCheats cheats = context.currentGame().session().cheats();
                setAutopilot(context, !cheats.isPacUsingAutopilot());
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                return normalLevel(context).isPresent();
            }
        };

        actionActivateAutopilot = new GameAction("activate_autopilot") {
            @Override
            public void execute(GameActionExecutionContext context) {
                setAutopilot(context, true);
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                return normalLevel(context).isPresent();
            }
        };

        actionDeactivateAutopilot = new GameAction("deactivate_autopilot") {
            @Override
            public void execute(GameActionExecutionContext context) {
                setAutopilot(context, false);
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                return normalLevel(context).isPresent();
            }
        };

        actionActivateImmunity = new GameAction("activate_immunity") {
            @Override
            public void execute(GameActionExecutionContext context) {
                setPacImmune(context, true);
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                return normalLevel(context).isPresent();
            }
        };

        actionDeactivateImmunity = new GameAction("deactivate_immunity") {
            @Override
            public void execute(GameActionExecutionContext context) {
                setPacImmune(context, false);
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                return normalLevel(context).isPresent();
            }
        };

        actionToggleImmunity = new GameAction("toggle_immunity") {
            @Override
            public void execute(GameActionExecutionContext context) {
                final GameCheats cheats = context.currentGame().session().cheats();
                setPacImmune(context, !cheats.isPacImmune());
            }

            @Override
            public boolean isEnabled(GameActionExecutionContext context) {
                return normalLevel(context).isPresent();
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

    private void setAutopilot(GameActionExecutionContext context, boolean auto) {
        final GameCheats cheats = context.currentGame().session().cheats();
        final GameUI ui = context.ui();

        cheats.pacUsingAutopilotProperty().set(auto);

        final String message = context.translationManager().translate(auto ? "flash.autopilot_on" : "flash.autopilot_off");
        final Media voice = auto ? VoiceID.AUTOPILOT_ON.media() : VoiceID.AUTOPILOT_OFF.media();

        ui.shortMessage(message);
        context.soundManager().voice().playAfterSec(1, voice);
    }

    private void setPacImmune(GameActionExecutionContext context, boolean immune) {
        final GameCheats cheats = context.currentGame().session().cheats();
        final GameUI ui = context.ui();

        cheats.pacImmuneProperty().set(immune);

        final String message = context.translationManager().translate(immune ? "flash.player_immunity_on" : "flash.player_immunity_off");
        final Media voice = immune ? VoiceID.IMMUNITY_ON.media() : VoiceID.IMMUNITY_OFF.media();

        ui.shortMessage(message);
        context.soundManager().voice().playAfterSec(1, voice);
    }

    private Optional<GameLevel> normalLevel(GameActionExecutionContext context) {
        final GameSession session = context.currentGame().session();
        return session.optLevel().filter(_ -> !session.isAttractMode());
    }
}