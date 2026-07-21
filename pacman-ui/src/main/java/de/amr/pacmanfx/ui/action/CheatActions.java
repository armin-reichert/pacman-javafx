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
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;

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

    public CheatActions(GameAppContext appContext) {

        actionAddLives = new GameAction(appContext, "cheat_add_lives") {
            @Override
            public void doAction() {
                final GameModel model = gameContext().model();
                model.addLives(3);
                gameContext().cheats().notifyCheatUsed();

                final String msg = appContext.ui().translations().translate("flash.cheat_add_lives", model.lifeCount());
                appContext.ui().shortMessage(msg);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionEatAllPellets = new GameAction(appContext, "cheat_eat_all_pellets") {
            @Override
            public void doAction() {
                final GameLevel level = gameContext().model().assertLevel();

                level.worldMap().foodLayer().eatPellets();
                gameContext().cheats().notifyCheatUsed();

                gameContext().eventManager().publishGameEvent(new PacEatsFoodEvent(level.entities().pac(), false, true));
            }

            @Override
            public boolean isEnabled() {
                final GameState gameState = gameContext().state();
                return normalLevel(appContext).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
            }
        };

        actionKillGhosts = new GameAction(appContext, "cheat_kill_ghosts") {
            @Override
            public void doAction() {
                final GameContext gameContext = gameContext();
                final GameLevel level = gameContext.assertLevel();

                gameContext.cheats().notifyCheatUsed();

                final List<Ghost> killableGhosts = level.entities().ghosts().stream()
                    .filter(ghost -> GhostState.FRIGHTENED == ghost.state() || GhostState.HUNTING_PAC == ghost.state())
                    .toList();

                if (!killableGhosts.isEmpty()) {
                    level.clearGhostKillChain(); // start again with lowest number for killing ghost
                    killableGhosts.forEach(ghost -> gameContext.gamePlay().onEatGhost(gameContext, ghost));
                    gameContext.flow().enterState(gameContext, GameStateID.GAME_LEVEL_EATING_GHOST);
                }
            }

            @Override
            public boolean isEnabled() {
                final GameState gameState = gameContext().state();
                return normalLevel(appContext).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
            }
        };

        actionEnterNextLevel = new GameAction(appContext, "cheat_enter_next_level") {
            @Override
            public void doAction() {
                final GameContext gameContext = gameContext();
                gameContext.cheats().notifyCheatUsed();
                gameFlow().enterState(gameContext, GameStateID.GAME_LEVEL_COMPLETE);
            }

            @Override
            public boolean isEnabled() {
                final GameState state = gameContext().state();
                final GameLevel level = normalLevel(this.appContext).orElse(null);
                return level != null
                    && GameStateID.GAME_LEVEL_PLAYING.identifies(state)
                    && level.number() < gameContext().model().rules().lastLevelNumber();
            }
        };

        actionToggleAutopilot = new GameAction(appContext, "toggle_autopilot") {
            @Override
            public void doAction() {
                final GameCheats cheats = gameContext().cheats();
                setAutopilot(appContext, !cheats.isPacUsingAutopilot());
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionActivateAutopilot = new GameAction(appContext, "activate_autopilot") {
            @Override
            public void doAction() {
                setAutopilot(appContext, true);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionDeactivateAutopilot = new GameAction(appContext, "deactivate_autopilot") {
            @Override
            public void doAction() {
                setAutopilot(appContext, false);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionActivateImmunity = new GameAction(appContext, "activate_immunity") {
            @Override
            public void doAction() {
                setPacImmune(appContext, true);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionDeactivateImmunity = new GameAction(appContext, "deactivate_immunity") {
            @Override
            public void doAction() {
                setPacImmune(appContext, false);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionToggleImmunity = new GameAction(appContext, "toggle_immunity") {
            @Override
            public void doAction() {
                final GameCheats cheats = gameContext().cheats();
                setPacImmune(appContext, !cheats.isPacImmune());
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
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

    private void setAutopilot(GameAppContext appContext, boolean auto) {
        final GameCheats cheats = appContext.currentGameContext().cheats();
        final GameUI ui = appContext.ui();

        cheats.pacUsingAutopilotProperty().set(auto);

        final String message = ui.translations().translate(auto ? "flash.autopilot_on" : "flash.autopilot_off");
        final Media voice = auto ? GlobalAssets.Voice.AUTOPILOT_ON.media() : GlobalAssets.Voice.AUTOPILOT_OFF.media();

        ui.shortMessage(message);
        ui.sounds().voice().playAfterSec(1, voice);
    }

    private void setPacImmune(GameAppContext appContext, boolean immune) {
        final GameCheats cheats = appContext.currentGameContext().cheats();
        final GameUI ui = appContext.ui();

        cheats.pacImmuneProperty().set(immune);

        final String message = ui.translations().translate(immune ? "flash.player_immunity_on" : "flash.player_immunity_off");
        final Media voice = immune ? GlobalAssets.Voice.IMMUNITY_ON.media() : GlobalAssets.Voice.IMMUNITY_OFF.media();

        ui.shortMessage(message);
        ui.sounds().voice().playAfterSec(1, voice);
    }

    private Optional<GameLevel> normalLevel(GameAppContext appContext) {
        return appContext.currentGameContext().model().optLevel().filter(level -> !level.isDemoLevel());
    }
}