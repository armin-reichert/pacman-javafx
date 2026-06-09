/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.game.GameConstants;
import de.amr.pacmanfx.ui.game.Game;

import java.util.List;
import java.util.Optional;

public final class CheatActions {

    public static final GameAction ACTION_ADD_LIVES = new GameAction("cheat_add_lives") {

        @Override
        public void doAction(Game game) {
            normalLevel(game).ifPresent(_ -> {
                final GameModel gameModel = game.currentGameContext().model();

                gameModel.lives().add(3);
                gameModel.cheats().notifyCheatUsed();

                final String message = game.ui().translations().translate("message.cheat_add_lives",
                    gameModel.lives().count());
                game.shortMessage(message);
            });
        }

        @Override
        public boolean isEnabled(Game game) { return normalLevel(game).isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("cheat_eat_all_pellets") {

        @Override
        public void doAction(Game game) {
            normalLevel(game).ifPresent(level -> {
                final GameContext gameContext = game.currentGameContext();
                final GameModel gameModel = gameContext.model();
                level.worldMap().foodLayer().eatPellets();
                gameModel.cheats().notifyCheatUsed();
                gameContext.flow().publishGameEvent(new PacEatsFoodEvent(gameContext, level.entities().pac(), false, true));
            });
        }

        @Override
        public boolean isEnabled(Game game) {
            final GameState gameState = game.currentGameContext().state();
            return normalLevel(game).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("cheat_kill_ghosts") {

        @Override
        public void doAction(Game game) {
            normalLevel(game).ifPresent(level -> {

                final GameContext gameContext = game.currentGameContext();
                final GameModel gameModel = gameContext.model();

                gameModel.cheats().notifyCheatUsed();

                final List<Ghost> killableGhosts = level.entities().ghosts().stream()
                    .filter(ghost -> GhostState.FRIGHTENED == ghost.state() || GhostState.HUNTING_PAC == ghost.state())
                    .toList();

                if (!killableGhosts.isEmpty()) {
                    level.clearGhostKillChain(); // start again with lowest number for killing ghost
                    killableGhosts.forEach(ghost -> gameModel.onEatGhost(gameContext, level, ghost));
                    gameContext.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST);
                }
            });
        }

        @Override
        public boolean isEnabled(Game game) {
            final GameState gameState = game.currentGameContext().state();
            return normalLevel(game).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("cheat_enter_next_level") {

        @Override
        public void doAction(Game game) {
            normalLevel(game).ifPresent(_ -> {
                final GameContext gameContext = game.currentGameContext();
                final GameModel gameModel = gameContext.model();
                gameModel.cheats().notifyCheatUsed();
                gameContext.flow().enterState(GameStateID.GAME_LEVEL_COMPLETE);
            });
        }

        @Override
        public boolean isEnabled(Game game) {
            final GameContext gameContext = game.currentGameContext();
            final GameState gameState = gameContext.state();
            final GameLevel level = normalLevel(game).orElse(null);
            return level != null
                && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState)
                && level.number() < gameContext.rules().lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("toggle_autopilot") {
        @Override
        public void doAction(Game game) {
            final GameModel gameModel = game.currentGameContext().model();
            setAutopilot(game, !gameModel.cheats().isPacUsingAutopilot());
        }

        @Override
        public boolean isEnabled(Game game) {
            return normalLevel(game).isPresent();
        }
    };

    public static final GameAction ACTION_ACTIVATE_AUTOPILOT = new GameAction("activate_autopilot") {
        @Override
        public void doAction(Game game) {
            setAutopilot(game, true);
        }

        @Override
        public boolean isEnabled(Game game) {
            return normalLevel(game).isPresent();
        }
    };

    public static final GameAction ACTION_DEACTIVATE_AUTOPILOT = new GameAction("deactivate_autopilot") {
        @Override
        public void doAction(Game game) {
            setAutopilot(game, false);
        }

        @Override
        public boolean isEnabled(Game game) {
            return normalLevel(game).isPresent();
        }
    };

    private static void setAutopilot(Game game, boolean auto) {
        final GameModel gameModel = game.currentGameContext().model();

        gameModel.cheats().pacUsingAutopilotProperty().set(auto);
        game.shortMessage(game.ui().translations().translate(auto ? "autopilot_on" : "autopilot_off"));

        game.ui().sounds().playVoice(auto ? GameConstants.VOICE_AUTOPILOT_ON : GameConstants.VOICE_AUTOPILOT_OFF);
    }

    public static final GameAction ACTION_ACTIVATE_IMMUNITY = new GameAction("activate_immunity") {
        @Override
        public void doAction(Game game) {
            setPacImmune(game, true);
        }

        @Override
        public boolean isEnabled(Game game) {
            return normalLevel(game).isPresent();
        }
    };

    public static final GameAction ACTION_DEACTIVATE_IMMUNITY = new GameAction("deactivate_immunity") {
        @Override
        public void doAction(Game game) {
            setPacImmune(game, false);
        }

        @Override
        public boolean isEnabled(Game game) {
            return normalLevel(game).isPresent();
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("toggle_immunity") {
        @Override
        public void doAction(Game game) {
            final GameModel gameModel = game.currentGameContext().model();
            setPacImmune(game, !gameModel.cheats().isPacImmune());
        }

        @Override
        public boolean isEnabled(Game game) {
            return normalLevel(game).isPresent();
        }
    };

    private static void setPacImmune(Game game, boolean immune) {
        game.currentGameContext().model().cheats().pacImmuneProperty().set(immune);
        game.ui().sounds().playVoice(immune ? GameConstants.VOICE_IMMUNITY_ON : GameConstants.VOICE_IMMUNITY_OFF);
        game.shortMessage(game.ui().translations().translate(immune ? "player_immunity_on" : "player_immunity_off"));
    }

    private static Optional<GameLevel> normalLevel(Game context) {
        return context.currentGameContext().optCurrentLevel().filter(level -> !level.isDemoLevel());
    }
}