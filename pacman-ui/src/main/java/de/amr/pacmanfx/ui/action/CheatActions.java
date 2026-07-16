/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.PacEatsFoodEvent;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

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

    public CheatActions(GameActionContext actionContext) {

        actionAddLives = new GameAction(actionContext, "cheat_add_lives") {
            @Override
            public void doAction() {
                final GameModel model = actionContext.currentGameContext().model();
                model.lives().add(3);
                actionContext.currentGameContext().cheats().notifyCheatUsed();

                final String msg = actionContext.ui().translations().translate("flash.cheat_add_lives", model.lives().count());
                actionContext.ui().shortMessage(msg);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(actionContext).isPresent();
            }
        };

        actionEatAllPellets = new GameAction(actionContext, "cheat_eat_all_pellets") {
            @Override
            public void doAction() {
                final GameContext context = this.actionContext.currentGameContext();
                final GameLevel level = context.model().assertLevel();

                level.worldMap().foodLayer().eatPellets();
                context.cheats().notifyCheatUsed();

                context.eventManager().publishGameEvent(new PacEatsFoodEvent(level.entities().pac(), false, true));
            }

            @Override
            public boolean isEnabled() {
                final GameState gameState = actionContext.currentGameContext().state();
                return normalLevel(actionContext).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
            }
        };

        actionKillGhosts = new GameAction(actionContext, "cheat_kill_ghosts") {
            @Override
            public void doAction() {
                final GameContext context = this.actionContext.currentGameContext();
                final GameModel model = context.model();
                final GameLevel level = model.assertLevel();

                context.cheats().notifyCheatUsed();

                final List<Ghost> killableGhosts = level.entities().ghosts().stream()
                    .filter(ghost -> GhostState.FRIGHTENED == ghost.state() || GhostState.HUNTING_PAC == ghost.state())
                    .toList();

                if (!killableGhosts.isEmpty()) {
                    level.clearGhostKillChain(); // start again with lowest number for killing ghost
                    killableGhosts.forEach(ghost -> context.gamePlay().onEatGhost(context, ghost));
                    context.flow().enterState(actionContext.currentGameContext(), GameStateID.GAME_LEVEL_EATING_GHOST);
                }
            }

            @Override
            public boolean isEnabled() {
                final GameState gameState = actionContext.currentGameContext().state();
                return normalLevel(actionContext).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
            }
        };

        actionEnterNextLevel = new GameAction(actionContext, "cheat_enter_next_level") {
            @Override
            public void doAction() {
                final GameContext context = this.actionContext.currentGameContext();

                context.cheats().notifyCheatUsed();
                context.flow().enterState(actionContext.currentGameContext(), GameStateID.GAME_LEVEL_COMPLETE);
            }

            @Override
            public boolean isEnabled() {
                final GameContext context = this.actionContext.currentGameContext();
                final GameState state = context.state();
                final GameLevel level = normalLevel(this.actionContext).orElse(null);

                return level != null
                    && GameStateID.GAME_LEVEL_PLAYING.identifies(state)
                    && level.number() < context.model().rules().lastLevelNumber();
            }
        };

        actionToggleAutopilot = new GameAction(actionContext, "toggle_autopilot") {
            @Override
            public void doAction() {
                final GameCheats cheats = actionContext.currentGameContext().cheats();
                setAutopilot(actionContext, !cheats.isPacUsingAutopilot());
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(actionContext).isPresent();
            }
        };

        actionActivateAutopilot = new GameAction(actionContext, "activate_autopilot") {
            @Override
            public void doAction() {
                setAutopilot(actionContext, true);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(actionContext).isPresent();
            }
        };

        actionDeactivateAutopilot = new GameAction(actionContext, "deactivate_autopilot") {
            @Override
            public void doAction() {
                setAutopilot(actionContext, false);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(actionContext).isPresent();
            }
        };

        actionActivateImmunity = new GameAction(actionContext, "activate_immunity") {
            @Override
            public void doAction() {
                setPacImmune(actionContext, true);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(actionContext).isPresent();
            }
        };

        actionDeactivateImmunity = new GameAction(actionContext, "deactivate_immunity") {
            @Override
            public void doAction() {
                setPacImmune(actionContext, false);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(actionContext).isPresent();
            }
        };

        actionToggleImmunity = new GameAction(actionContext, "toggle_immunity") {
            @Override
            public void doAction() {
                final GameCheats cheats = actionContext.currentGameContext().cheats();
                setPacImmune(actionContext, !cheats.isPacImmune());
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(actionContext).isPresent();
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

    private void setAutopilot(GameActionContext actionContext, boolean auto) {
        final GameCheats cheats = actionContext.currentGameContext().cheats();
        cheats.pacUsingAutopilotProperty().set(auto);

        actionContext.ui().shortMessage(actionContext.ui().translations().translate(auto ? "flash.autopilot_on" : "flash.autopilot_off"));
        actionContext.ui().sounds().playVoice(auto ? GlobalAssets.Voice.AUTOPILOT_ON.media() : GlobalAssets.Voice.AUTOPILOT_OFF.media());
    }

    private void setPacImmune(GameActionContext actionContext, boolean immune) {
        final GameCheats cheats = actionContext.currentGameContext().cheats();
        cheats.pacImmuneProperty().set(immune);

        actionContext.ui().shortMessage(actionContext.ui().translations().translate(immune ? "flash.player_immunity_on" : "flash.player_immunity_off"));
        actionContext.ui().sounds().playVoice(immune ? GlobalAssets.Voice.IMMUNITY_ON.media() : GlobalAssets.Voice.IMMUNITY_OFF.media());
    }

    private Optional<GameLevel> normalLevel(GameActionContext actionContext) {
        return actionContext.currentGameContext().model().optLevel().filter(level -> !level.isDemoLevel());
    }
}