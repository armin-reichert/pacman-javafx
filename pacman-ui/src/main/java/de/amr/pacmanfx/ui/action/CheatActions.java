/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
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

    public CheatActions(Game game) {

        actionAddLives = new GameAction(game, "cheat_add_lives") {
            @Override
            public void doAction() {
                final GameModel model = game.context().model();
                model.lives().add(3);
                game.context().cheats().notifyCheatUsed();

                final String msg = game.ui().translations().translate("flash.cheat_add_lives", model.lives().count());
                game.ui().shortMessage(msg);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(game).isPresent();
            }
        };

        actionEatAllPellets = new GameAction(game, "cheat_eat_all_pellets") {
            @Override
            public void doAction() {
                final GameContext context = game.context();
                final GameLevel level = context.model().assertLevel();

                level.worldMap().foodLayer().eatPellets();
                context.cheats().notifyCheatUsed();

                context.flow().publishGameEvent(new PacEatsFoodEvent(context, level.entities().pac(), false, true));
            }

            @Override
            public boolean isEnabled() {
                final GameState gameState = game.context().state();
                return normalLevel(game).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
            }
        };

        actionKillGhosts = new GameAction(game, "cheat_kill_ghosts") {
            @Override
            public void doAction() {
                final GameContext context = game.context();
                final GameModel model = context.model();
                final GameLevel level = model.assertLevel();

                context.cheats().notifyCheatUsed();

                final List<Ghost> killableGhosts = level.entities().ghosts().stream()
                    .filter(ghost -> GhostState.FRIGHTENED == ghost.state() || GhostState.HUNTING_PAC == ghost.state())
                    .toList();

                if (!killableGhosts.isEmpty()) {
                    level.clearGhostKillChain(); // start again with lowest number for killing ghost
                    killableGhosts.forEach(ghost -> model.onEatGhost(context, level, ghost));
                    context.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST);
                }
            }

            @Override
            public boolean isEnabled() {
                final GameState gameState = game.context().state();
                return normalLevel(game).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
            }
        };

        actionEnterNextLevel = new GameAction(game, "cheat_enter_next_level") {
            @Override
            public void doAction() {
                final GameContext context = game.context();

                context.cheats().notifyCheatUsed();
                context.flow().enterState(GameStateID.GAME_LEVEL_COMPLETE);
            }

            @Override
            public boolean isEnabled() {
                final GameContext context = game.context();
                final GameState state = context.state();
                final GameLevel level = normalLevel(game).orElse(null);

                return level != null
                    && GameStateID.GAME_LEVEL_PLAYING.identifies(state)
                    && level.number() < context.model().rules().lastLevelNumber();
            }
        };

        actionToggleAutopilot = new GameAction(game, "toggle_autopilot") {
            @Override
            public void doAction() {
                final GameCheats cheats = game.context().cheats();
                setAutopilot(game, !cheats.isPacUsingAutopilot());
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(game).isPresent();
            }
        };

        actionActivateAutopilot = new GameAction(game, "activate_autopilot") {
            @Override
            public void doAction() {
                setAutopilot(game, true);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(game).isPresent();
            }
        };

        actionDeactivateAutopilot = new GameAction(game, "deactivate_autopilot") {
            @Override
            public void doAction() {
                setAutopilot(game, false);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(game).isPresent();
            }
        };

        actionActivateImmunity = new GameAction(game, "activate_immunity") {
            @Override
            public void doAction() {
                setPacImmune(game, true);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(game).isPresent();
            }
        };

        actionDeactivateImmunity = new GameAction(game, "deactivate_immunity") {
            @Override
            public void doAction() {
                setPacImmune(game, false);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(game).isPresent();
            }
        };

        actionToggleImmunity = new GameAction(game, "toggle_immunity") {
            @Override
            public void doAction() {
                final GameCheats cheats = game.context().cheats();
                setPacImmune(game, !cheats.isPacImmune());
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(game).isPresent();
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

    private void setAutopilot(Game game, boolean auto) {
        final GameCheats cheats = game.context().cheats();
        cheats.pacUsingAutopilotProperty().set(auto);

        game.ui().shortMessage(game.ui().translations().translate(auto ? "flash.autopilot_on" : "flash.autopilot_off"));
        game.ui().sounds().playVoice(auto ? GlobalAssets.Voice.AUTOPILOT_ON.media() : GlobalAssets.Voice.AUTOPILOT_OFF.media());
    }

    private void setPacImmune(Game game, boolean immune) {
        final GameCheats cheats = game.context().cheats();
        cheats.pacImmuneProperty().set(immune);

        game.ui().shortMessage(game.ui().translations().translate(immune ? "flash.player_immunity_on" : "flash.player_immunity_off"));
        game.ui().sounds().playVoice(immune ? GlobalAssets.Voice.IMMUNITY_ON.media() : GlobalAssets.Voice.IMMUNITY_OFF.media());
    }

    private Optional<GameLevel> normalLevel(Game game) {
        return game.context().model().optGameLevel().filter(level -> !level.isDemoLevel());
    }
}