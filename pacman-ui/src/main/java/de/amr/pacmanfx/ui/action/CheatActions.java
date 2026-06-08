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
        public void doAction(Game appContext) {
            normalLevel(appContext).ifPresent(_ -> {
                final GameModel gameModel = appContext.currentGameContext().model();

                gameModel.lives().add(3);
                gameModel.cheats().notifyCheatUsed();

                final String message = appContext.ui().translations().translate("message.cheat_add_lives",
                    gameModel.lives().count());
                appContext.shortMessage(message);
            });
        }

        @Override
        public boolean isEnabled(Game appContext) { return normalLevel(appContext).isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("cheat_eat_all_pellets") {

        @Override
        public void doAction(Game appContext) {
            normalLevel(appContext).ifPresent(level -> {
                final GameContext gameContext = appContext.currentGameContext();
                final GameModel gameModel = gameContext.model();
                level.worldMap().foodLayer().eatPellets();
                gameModel.cheats().notifyCheatUsed();
                gameContext.flow().publishGameEvent(new PacEatsFoodEvent(gameContext, level.entities().pac(), false, true));
            });
        }

        @Override
        public boolean isEnabled(Game appContext) {
            final GameState gameState = appContext.currentGameContext().state();
            return normalLevel(appContext).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("cheat_kill_ghosts") {

        @Override
        public void doAction(Game appContext) {
            normalLevel(appContext).ifPresent(level -> {

                final GameContext gameContext = appContext.currentGameContext();
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
        public boolean isEnabled(Game context) {
            final GameState gameState = context.currentGameContext().state();
            return normalLevel(context).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("cheat_enter_next_level") {

        @Override
        public void doAction(Game appContext) {
            normalLevel(appContext).ifPresent(_ -> {
                final GameContext gameContext = appContext.currentGameContext();
                final GameModel gameModel = gameContext.model();
                gameModel.cheats().notifyCheatUsed();
                gameContext.flow().enterState(GameStateID.GAME_LEVEL_COMPLETE);
            });
        }

        @Override
        public boolean isEnabled(Game context) {
            final GameContext gameContext = context.currentGameContext();
            final GameState gameState = gameContext.state();
            final GameLevel level = normalLevel(context).orElse(null);
            return level != null
                && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState)
                && level.number() < gameContext.rules().lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("toggle_autopilot") {
        @Override
        public void doAction(Game appContext) {
            final GameModel gameModel = appContext.currentGameContext().model();
            setAutopilot(appContext, !gameModel.cheats().isPacUsingAutopilot());
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return normalLevel(appContext).isPresent();
        }
    };

    public static final GameAction ACTION_ACTIVATE_AUTOPILOT = new GameAction("activate_autopilot") {
        @Override
        public void doAction(Game appContext) {
            setAutopilot(appContext, true);
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return normalLevel(appContext).isPresent();
        }
    };

    public static final GameAction ACTION_DEACTIVATE_AUTOPILOT = new GameAction("deactivate_autopilot") {
        @Override
        public void doAction(Game appContext) {
            setAutopilot(appContext, false);
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return normalLevel(appContext).isPresent();
        }
    };

    private static void setAutopilot(Game appContext, boolean auto) {
        final GameModel gameModel = appContext.currentGameContext().model();

        gameModel.cheats().pacUsingAutopilotProperty().set(auto);
        appContext.shortMessage(appContext.ui().translations().translate(auto ? "autopilot_on" : "autopilot_off"));

        appContext.ui().sounds().playVoice(auto ? GameConstants.VOICE_AUTOPILOT_ON : GameConstants.VOICE_AUTOPILOT_OFF);
    }

    public static final GameAction ACTION_ACTIVATE_IMMUNITY = new GameAction("activate_immunity") {
        @Override
        public void doAction(Game appContext) {
            setPacImmune(appContext, true);
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return normalLevel(appContext).isPresent();
        }
    };

    public static final GameAction ACTION_DEACTIVATE_IMMUNITY = new GameAction("deactivate_immunity") {
        @Override
        public void doAction(Game appContext) {
            setPacImmune(appContext, false);
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return normalLevel(appContext).isPresent();
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("toggle_immunity") {
        @Override
        public void doAction(Game appContext) {
            final GameModel gameModel = appContext.currentGameContext().model();
            setPacImmune(appContext, !gameModel.cheats().isPacImmune());
        }

        @Override
        public boolean isEnabled(Game appContext) {
            return normalLevel(appContext).isPresent();
        }
    };

    private static void setPacImmune(Game appContext, boolean immune) {
        appContext.currentGameContext().model().cheats().pacImmuneProperty().set(immune);
        appContext.ui().sounds().playVoice(immune ? GameConstants.VOICE_IMMUNITY_ON : GameConstants.VOICE_IMMUNITY_OFF);
        appContext.shortMessage(appContext.ui().translations().translate(immune ? "player_immunity_on" : "player_immunity_off"));
    }

    private static Optional<GameLevel> normalLevel(Game context) {
        return context.currentGameContext().optCurrentLevel().filter(level -> !level.isDemoLevel());
    }
}