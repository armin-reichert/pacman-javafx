/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.GameSession;
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

    public CheatActions(GameAppContext app) {

        actionAddLives = new GameAction(app, "cheat_add_lives") {
            @Override
            public void doAction() {
                final GameSession session = game().session();
                LivesCounterSystem.addLives(session.livesCounter(), 3);
                game().session().cheats().notifyCheatUsed();
                final String msg = appContext.ui().translations().translate("flash.cheat_add_lives",
                    session.livesCounter().data().numLives());
                appContext.ui().shortMessage(msg);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionEatAllPellets = new GameAction(app, "cheat_eat_all_pellets") {
            @Override
            public void doAction() {
                final GameLevel level = game().session().assertLevel();

                level.worldMap().foodLayer().eatPellets();
                game().session().cheats().notifyCheatUsed();

                game().eventManager().publishGameEvent(new PacEatsFoodEvent(level.entities().pac(), false, true));
            }

            @Override
            public boolean isEnabled() {
                final GameState gameState = game().session().gameState();
                return normalLevel(appContext).isPresent() && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState);
            }
        };

        actionKillGhosts = new GameAction(app, "cheat_kill_ghosts") {
            @Override
            public void doAction() {
                final GameContext game = game();
                final GameLevel level = game.session().assertLevel();

                game.session().cheats().notifyCheatUsed();

                final List<Ghost> killableGhosts = level.entities().ghosts().stream()
                    .filter(ghost -> GhostState.FRIGHTENED == ghost.ghostStateEnum() || GhostState.HUNTING_PAC == ghost.ghostStateEnum())
                    .toList();

                if (!killableGhosts.isEmpty()) {
                    level.clearGhostKillChain(); // start again with lowest number for killing ghost
                    killableGhosts.forEach(ghost -> game.gamePlay().onEatGhost(game, level, ghost));
                    game.session().gameFlow().enterState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
                }
            }

            @Override
            public boolean isEnabled() {
                final GameState gameState = game().session().gameState();
                return normalLevel(appContext).isPresent() && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState);
            }
        };

        actionEnterNextLevel = new GameAction(app, "cheat_enter_next_level") {
            @Override
            public void doAction() {
                game().session().cheats().notifyCheatUsed();
                gameFlow().enterState(game(), CommonGameStateID.GAME_LEVEL_COMPLETE);
            }

            @Override
            public boolean isEnabled() {
                final GameState state = game().session().gameState();
                final GameLevel level = normalLevel(this.appContext).orElse(null);
                return level != null
                    && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(state)
                    && level.number() < game().rules().lastLevelNumber();
            }
        };

        actionToggleAutopilot = new GameAction(app, "toggle_autopilot") {
            @Override
            public void doAction() {
                final GameCheats cheats = game().session().cheats();
                setAutopilot(appContext, !cheats.isPacUsingAutopilot());
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionActivateAutopilot = new GameAction(app, "activate_autopilot") {
            @Override
            public void doAction() {
                setAutopilot(appContext, true);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionDeactivateAutopilot = new GameAction(app, "deactivate_autopilot") {
            @Override
            public void doAction() {
                setAutopilot(appContext, false);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionActivateImmunity = new GameAction(app, "activate_immunity") {
            @Override
            public void doAction() {
                setPacImmune(appContext, true);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionDeactivateImmunity = new GameAction(app, "deactivate_immunity") {
            @Override
            public void doAction() {
                setPacImmune(appContext, false);
            }

            @Override
            public boolean isEnabled() {
                return normalLevel(appContext).isPresent();
            }
        };

        actionToggleImmunity = new GameAction(app, "toggle_immunity") {
            @Override
            public void doAction() {
                final GameCheats cheats = game().session().cheats();
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
        final GameCheats cheats = appContext.currentGame().session().cheats();
        final GameUI ui = appContext.ui();

        cheats.pacUsingAutopilotProperty().set(auto);

        final String message = ui.translations().translate(auto ? "flash.autopilot_on" : "flash.autopilot_off");
        final Media voice = auto ? GlobalAssets.VoiceID.AUTOPILOT_ON.media() : GlobalAssets.VoiceID.AUTOPILOT_OFF.media();

        ui.shortMessage(message);
        ui.sounds().voice().playAfterSec(1, voice);
    }

    private void setPacImmune(GameAppContext appContext, boolean immune) {
        final GameCheats cheats = appContext.currentGame().session().cheats();
        final GameUI ui = appContext.ui();

        cheats.pacImmuneProperty().set(immune);

        final String message = ui.translations().translate(immune ? "flash.player_immunity_on" : "flash.player_immunity_off");
        final Media voice = immune ? GlobalAssets.VoiceID.IMMUNITY_ON.media() : GlobalAssets.VoiceID.IMMUNITY_OFF.media();

        ui.shortMessage(message);
        ui.sounds().voice().playAfterSec(1, voice);
    }

    private Optional<GameLevel> normalLevel(GameAppContext appContext) {
        final GameSession session = appContext.currentGame().session();
        return session.optLevel().filter(_ -> !session.isAttractMode());
    }
}