/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.AppConstants;
import de.amr.pacmanfx.ui.AppContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;

public final class CheatActions {

    public static final GameAction ACTION_ADD_LIVES = new GameAction("cheat_add_lives") {
        @Override
        public void doAction(AppContext context) {
            realLevel(context).ifPresent(level -> {
                final GameModel game = level.game();
                game.lives().add(3);
                game.cheats().notifyCheatUsed();
                final String message = context.ui().translations().translate("message.cheat_add_lives", game.lives().count());
                context.shortMessage(message);
            });
        }

        @Override
        public boolean isEnabled(AppContext context) { return realLevel(context).isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("cheat_eat_all_pellets") {
        @Override
        public void doAction(AppContext appContext) {
            realLevel(appContext).ifPresent(level -> {
                final GameContext gameContext = appContext.currentGameContext();
                final GameModel gameModel = gameContext.gameModel();
                level.worldMap().foodLayer().eatPellets();
                gameModel.cheats().cheatUsedProperty().set(true);
                gameContext.gameFlow().publishGameEvent(new PacEatsFoodEvent(gameContext, level.entities().pac(), false, true));
            });
        }

        @Override
        public boolean isEnabled(AppContext context) {
            final State<GameContext> gameState = context.currentGameContext().gameState();
            return realLevel(context).isPresent()
                && gameState.nameIsOneOf(GameStateID.GAME_LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("cheat_kill_ghosts") {
        @Override
        public void doAction(AppContext appContext) {
            realLevel(appContext).ifPresent(level -> {
                final GameContext gameContext = appContext.currentGameContext();
                final GameModel gameModel = gameContext.gameModel();
                final List<Ghost> killableGhosts = level.ghostsInAnyOfStates(Set.of(FRIGHTENED, HUNTING_PAC)).toList();
                if (!killableGhosts.isEmpty()) {
                    level.clearGhostKillChain(); // resets value of next killed ghost to 200
                    killableGhosts.forEach(ghost -> gameModel.onEatGhost(appContext.currentGameContext(), level, ghost));
                    gameContext.gameFlow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST.name());
                }
                gameModel.cheats().cheatUsedProperty().set(true);
            });
        }

        @Override
        public boolean isEnabled(AppContext context) {
            final State<GameContext> gameState = context.currentGameContext().gameState();
            return realLevel(context).isPresent()
                && gameState.nameIsOneOf(GameStateID.GAME_LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("cheat_enter_next_level") {
        @Override
        public void doAction(AppContext appContext) {
            realLevel(appContext).ifPresent(_ -> {
                final GameContext gameContext = appContext.currentGameContext();
                final GameModel gameModel = gameContext.gameModel();
                gameModel.cheats().notifyCheatUsed();
                gameContext.gameFlow().enterState(GameStateID.GAME_LEVEL_COMPLETE.name());
            });
        }

        @Override
        public boolean isEnabled(AppContext context) {
            final GameContext gameContext = context.currentGameContext();
            final State<GameContext> gameState = gameContext.gameState();
            final GameLevel level = realLevel(context).orElse(null);
            return level != null
                && gameState.nameIsOneOf(GameStateID.GAME_LEVEL_PLAYING.name())
                && level.number() < gameContext.gameRules().lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("toggle_autopilot") {
        @Override
        public void doAction(AppContext context) {
            final GameModel game = context.currentGameContext().gameModel();
            setAutopilot(context, !game.cheats().isPacUsingAutopilot());
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return realLevel(context).isPresent();
        }
    };

    public static final GameAction ACTION_ACTIVATE_AUTOPILOT = new GameAction("activate_autopilot") {
        @Override
        public void doAction(AppContext context) {
            setAutopilot(context, true);
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return realLevel(context).isPresent();
        }
    };

    public static final GameAction ACTION_DEACTIVATE_AUTOPILOT = new GameAction("deactivate_autopilot") {
        @Override
        public void doAction(AppContext context) {
            setAutopilot(context, false);
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return realLevel(context).isPresent();
        }
    };

    private static void setAutopilot(AppContext context, boolean auto) {
        final GameModel game = context.currentGameContext().gameModel();
        game.cheats().pacUsingAutopilotProperty().set(auto);
        context.ui().sounds().playVoice(auto ? AppConstants.VOICE_AUTOPILOT_ON : AppConstants.VOICE_AUTOPILOT_OFF);
        context.shortMessage(context.ui().translations().translate(auto ? "autopilot_on" : "autopilot_off"));
    }

    public static final GameAction ACTION_ACTIVATE_IMMUNITY = new GameAction("activate_immunity") {
        @Override
        public void doAction(AppContext context) {
            setPacImmune(context, true);
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return realLevel(context).isPresent();
        }
    };

    public static final GameAction ACTION_DEACTIVATE_IMMUNITY = new GameAction("deactivate_immunity") {
        @Override
        public void doAction(AppContext context) {
            setPacImmune(context, false);
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return realLevel(context).isPresent();
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("toggle_immunity") {
        @Override
        public void doAction(AppContext context) {
            final GameModel game = context.currentGameContext().gameModel();
            setPacImmune(context, !game.cheats().isPacImmune());
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return realLevel(context).isPresent();
        }
    };

    public static void setPacImmune(AppContext context, boolean immune) {
        context.currentGameContext().gameModel().cheats().pacImmuneProperty().set(immune);
        context.ui().sounds().playVoice(immune ? AppConstants.VOICE_IMMUNITY_ON : AppConstants.VOICE_IMMUNITY_OFF);
        context.shortMessage(context.ui().translations().translate(immune ? "player_immunity_on" : "player_immunity_off"));
    }

    private static Optional<GameLevel> realLevel(AppContext context) {
        return context.currentGameContext().optCurrentGameLevel().filter(level -> !level.isDemoLevel());
    }
}