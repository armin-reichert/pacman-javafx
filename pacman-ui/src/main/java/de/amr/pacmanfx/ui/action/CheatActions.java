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
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CheatActions {

    private final Game game;
    
    public CheatActions(Game game) {
        this.game = Objects.requireNonNull(game);
    }

    private GameAction actionAddLives;

    public GameAction actionAddLives() {
        if (actionAddLives == null) {
            actionAddLives = new GameAction(game, "cheat_add_lives") {
                @Override
                public void doAction() {
                    final GameModel gameModel = game.currentGameContext().model();
                    gameModel.lives().add(3);
                    gameModel.cheats().notifyCheatUsed();

                    final String msg = game.ui().translations().translate("message.cheat_add_lives", gameModel.lives().count());
                    game.shortMessage(msg);
                }

                @Override
                public boolean isEnabled() { return normalLevel(game).isPresent(); }
            };
        }
        return actionAddLives;
    }


    private GameAction actionEatAllPellets;
    
    public GameAction actionEatAllPellets() {
        if (actionEatAllPellets == null) {
            actionEatAllPellets = new GameAction(game, "cheat_eat_all_pellets") {
                @Override
                public void doAction() {
                    final GameContext gameContext = game.currentGameContext();
                    final GameModel gameModel = gameContext.model();
                    final GameLevel level = gameContext.requireLevel();

                    level.worldMap().foodLayer().eatPellets();
                    gameModel.cheats().notifyCheatUsed();

                    gameContext.flow().publishGameEvent(new PacEatsFoodEvent(gameContext, level.entities().pac(), false, true));
                }

                @Override
                public boolean isEnabled() {
                    final GameState gameState = game.currentGameContext().state();
                    return normalLevel(game).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
                }
            };
        }
        return actionEatAllPellets;
    }
    
    private GameAction actionKillGhosts;

    public GameAction actionKillGhosts() {
        if (actionKillGhosts == null) {
            actionKillGhosts = new GameAction(game, "cheat_kill_ghosts") {
                @Override
                public void doAction() {
                    final GameContext gameContext = game.currentGameContext();
                    final GameModel gameModel = gameContext.model();
                    final GameLevel level = gameContext.requireLevel();

                    gameModel.cheats().notifyCheatUsed();

                    final List<Ghost> killableGhosts = level.entities().ghosts().stream()
                        .filter(ghost -> GhostState.FRIGHTENED == ghost.state() || GhostState.HUNTING_PAC == ghost.state())
                        .toList();

                    if (!killableGhosts.isEmpty()) {
                        level.clearGhostKillChain(); // start again with lowest number for killing ghost
                        killableGhosts.forEach(ghost -> gameModel.onEatGhost(gameContext, level, ghost));
                        gameContext.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST);
                    }
                }

                @Override
                public boolean isEnabled() {
                    final GameState gameState = game.currentGameContext().state();
                    return normalLevel(game).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
                }
            };
        }
        return  actionKillGhosts;
    }

    private GameAction actionEnterNextLevel;

    public GameAction actionEnterNextLevel() {
        if  (actionEnterNextLevel == null) {
            actionEnterNextLevel = new GameAction(game, "cheat_enter_next_level") {
                @Override
                public void doAction() {
                    final GameContext gameContext = game.currentGameContext();
                    final GameModel gameModel = gameContext.model();

                    gameModel.cheats().notifyCheatUsed();
                    gameContext.flow().enterState(GameStateID.GAME_LEVEL_COMPLETE);
                }

                @Override
                public boolean isEnabled() {
                    final GameContext gameContext = game.currentGameContext();
                    final GameState gameState = gameContext.state();
                    final GameLevel normalLevel = normalLevel(game).orElse(null);

                    return normalLevel != null
                        && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState)
                        && normalLevel.number() < gameContext.rules().lastLevelNumber();
                }
            };
        }    
        return actionEnterNextLevel;        
    }
    
    private GameAction actionToggleAutopilot;

    public GameAction actionToggleAutopilot() {
        if (actionToggleAutopilot == null) {
            actionToggleAutopilot = new GameAction(game, "toggle_autopilot") {
                @Override
                public void doAction() {
                    final GameCheats cheats = game.currentGameContext().model().cheats();

                    setAutopilot(game, !cheats.isPacUsingAutopilot());
                }

                @Override
                public boolean isEnabled() {
                    return normalLevel(game).isPresent();
                }
            };
        }
        return actionToggleAutopilot;
    }

    private GameAction actionActivateAutopilot;

    public GameAction actionActivateAutopilot() {
        if (actionActivateAutopilot == null) {
            actionActivateAutopilot =  new GameAction(game, "activate_autopilot") {
                @Override
                public void doAction() {
                    setAutopilot(game, true);
                }

                @Override
                public boolean isEnabled() {
                    return normalLevel(game).isPresent();
                }
            };
        }
        return actionActivateAutopilot;
    }

    private GameAction actionDeactivateAutopilot;

    public GameAction actionDeactivateAutopilot() {
        if (actionDeactivateAutopilot == null) {
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
        }
        return actionDeactivateAutopilot;
    }

    private GameAction actionActivateImmunity;

    public GameAction actionActivateImmunity() {
        if (actionActivateImmunity == null) {
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
        }
        return actionActivateImmunity;
    }

    private GameAction actionDeactivateImmunity;

    public GameAction actionDeactivateImmunity() {
        if (actionDeactivateImmunity == null) {
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
        }
        return actionDeactivateImmunity;
    }

    private GameAction actionToggleImmunity;

    public GameAction actionToggleImmunity() {
        if (actionToggleImmunity == null) {
            actionToggleImmunity = new GameAction(game, "toggle_immunity") {
                @Override
                public void doAction() {
                    final GameCheats cheats = game.currentGameContext().model().cheats();

                    setPacImmune(game, !cheats.isPacImmune());
                }

                @Override
                public boolean isEnabled() {
                    return normalLevel(game).isPresent();
                }
            };
        }
        return actionToggleImmunity;
    }

    // Helpers

    private void setAutopilot(Game game, boolean auto) {
        final GameCheats cheats = game.currentGameContext().model().cheats();

        cheats.pacUsingAutopilotProperty().set(auto);

        game.shortMessage(game.ui().translations().translate(auto ? "autopilot_on" : "autopilot_off"));
        game.ui().sounds().playVoice(auto ? GameUI_Constants.VOICE_AUTOPILOT_ON : GameUI_Constants.VOICE_AUTOPILOT_OFF);
    }

    private void setPacImmune(Game game, boolean immune) {
        final GameCheats cheats = game.currentGameContext().model().cheats();

        cheats.pacImmuneProperty().set(immune);

        game.shortMessage(game.ui().translations().translate(immune ? "player_immunity_on" : "player_immunity_off"));
        game.ui().sounds().playVoice(immune ? GameUI_Constants.VOICE_IMMUNITY_ON : GameUI_Constants.VOICE_IMMUNITY_OFF);
    }

    private Optional<GameLevel> normalLevel(Game game) {
        return game.currentGameContext().optCurrentLevel().filter(level -> !level.isDemoLevel());
    }
}