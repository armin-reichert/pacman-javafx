/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.AppConstants;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;

public final class CheatActions {

    public static final GameAction ACTION_ADD_LIVES = new GameAction("cheat_add_lives") {
        @Override
        public void doAction(AppContext context) {
            realLevel(context).ifPresent(level -> {
                final Game game = level.game();
                game.addLives(3);
                game.cheats().cheatUsedProperty().set(true);
                final String message = context.ui().translations().translate("message.cheat_add_lives", game.lifeCount());
                context.shortMessage(message);
            });
        }

        @Override
        public boolean isEnabled(AppContext context) { return realLevel(context).isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("cheat_eat_all_pellets") {
        @Override
        public void doAction(AppContext context) {
            realLevel(context).ifPresent(level -> {
                final Game game = level.game();
                level.worldMap().foodLayer().eatPellets();
                game.cheats().cheatUsedProperty().set(true);
                game.flow().publishGameEvent(new PacEatsFoodEvent(game, level.entities().pac(), false, true));
            });
        }

        @Override
        public boolean isEnabled(AppContext context) {
            final State<Game> gameState = context.currentGameState();
            return realLevel(context).isPresent()
                && gameState.matchesByName(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("cheat_kill_ghosts") {
        @Override
        public void doAction(AppContext context) {
            realLevel(context).ifPresent(level -> {
                final Game game = level.game();
                final List<Ghost> killableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
                if (!killableGhosts.isEmpty()) {
                    level.energizerVictims().clear(); // resets value of next killed ghost to 200
                    killableGhosts.forEach(game::onEatGhost);
                    game.flow().enterStateWithName(CanonicalGameState.EATING_GHOST.name());
                }
                game.cheats().cheatUsedProperty().set(true);
            });
        }

        @Override
        public boolean isEnabled(AppContext context) {
            final State<Game> gameState = context.currentGameState();
            return realLevel(context).isPresent()
                && gameState.matchesByName(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("cheat_enter_next_level") {
        @Override
        public void doAction(AppContext context) {
            realLevel(context).ifPresent(_ -> {
                final Game game = context.currentGame();
                game.cheats().cheatUsedProperty().set(true);
                game.flow().enterStateWithName(CanonicalGameState.LEVEL_COMPLETE.name());
            });
        }

        @Override
        public boolean isEnabled(AppContext context) {
            final State<Game> gameState = context.currentGameState();
            final GameLevel level = realLevel(context).orElse(null);
            return level != null
                && gameState.matchesByName(CanonicalGameState.LEVEL_PLAYING.name())
                && level.number() < level.game().lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("toggle_autopilot") {
        @Override
        public void doAction(AppContext context) {
            final Game game = context.currentGame();
            setAutopilot(context, !game.cheats().isUsingAutopilot());
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
        final Game game = context.currentGame();
        game.cheats().usingAutopilotProperty().set(auto);
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
            final Game game = context.currentGame();
            setPacImmune(context, !game.cheats().isImmune());
        }

        @Override
        public boolean isEnabled(AppContext context) {
            return realLevel(context).isPresent();
        }
    };

    public static void setPacImmune(AppContext context, boolean immune) {
        final Game game = context.currentGame();
        game.cheats().immuneProperty().set(immune);
        context.ui().sounds().playVoice(immune ? AppConstants.VOICE_IMMUNITY_ON : AppConstants.VOICE_IMMUNITY_OFF);
        context.shortMessage(context.ui().translations().translate(immune ? "player_immunity_on" : "player_immunity_off"));
    }

    private static Optional<GameLevel> realLevel(AppContext context) {
        return context.currentGame().optGameLevel().filter(level -> !level.isDemoLevel());
    }
}