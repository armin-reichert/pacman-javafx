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
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;

public final class CheatActions {

    public static final GameAction ACTION_ADD_LIVES = new GameAction("cheat_add_lives") {
        @Override
        public void doAction(GameUI ui) {
            realLevel(ui).ifPresent(level -> {
                final Game game = level.game();
                game.addLives(3);
                game.cheats().cheatUsedProperty().set(true);
                ui.showFlashMessage(ui.translationManager().translate(resourceBundleKey(), game.lifeCount()));
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) { return realLevel(ui.gameContext().game()).isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("cheat_eat_all_pellets") {
        @Override
        public void doAction(GameUI ui) {
            realLevel(ui).ifPresent(level -> {
                final Game game = level.game();
                level.worldMap().foodLayer().eatPellets();
                game.cheats().cheatUsedProperty().set(true);
                game.flow().publishGameEvent(new PacEatsFoodEvent(game, level.entities().pac(), false, true));
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final State<Game> gameState = ui.gameContext().game();
            return realLevel(ui).isPresent()
                && gameState.matchesByName(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("cheat_kill_ghosts") {
        @Override
        public void doAction(GameUI ui) {
            realLevel(ui).ifPresent(level -> {
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
        public boolean isEnabled(GameUI ui) {
            final State<Game> gameState = ui.gameContext().game().flow().state();
            return realLevel(ui).isPresent()
                && gameState.matchesByName(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("cheat_enter_next_level") {
        @Override
        public void doAction(GameUI ui) {
            realLevel(ui).ifPresent(_ -> {
                final Game game = ui.gameContext().game();
                game.cheats().cheatUsedProperty().set(true);
                game.flow().enterStateWithName(CanonicalGameState.LEVEL_COMPLETE.name());
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final State<Game> gameState = ui.gameContext().game().flow().state();
            final GameLevel level = realLevel(ui).orElse(null);
            return level != null
                && gameState.matchesByName(CanonicalGameState.LEVEL_PLAYING.name())
                && level.number() < level.game().lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("toggle_autopilot") {
        @Override
        public void doAction(GameUI ui) {
            final Game game = ui.gameContext().game();
            setAutopilot(ui, !game.cheats().isUsingAutopilot());
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return realLevel(ui).isPresent();
        }
    };

    public static final GameAction ACTION_ACTIVATE_AUTOPILOT = new GameAction("activate_autopilot") {
        @Override
        public void doAction(GameUI ui) {
            setAutopilot(ui, true);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return realLevel(ui).isPresent();
        }
    };

    public static final GameAction ACTION_DEACTIVATE_AUTOPILOT = new GameAction("deactivate_autopilot") {
        @Override
        public void doAction(GameUI ui) {
            setAutopilot(ui, false);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return realLevel(ui).isPresent();
        }
    };

    private static void setAutopilot(GameUI ui, boolean auto) {
        final Game game = ui.gameContext().game();
        game.cheats().usingAutopilotProperty().set(auto);
        ui.soundManager().playVoice(auto ? GameUIConstants.VOICE_AUTOPILOT_ON : GameUIConstants.VOICE_AUTOPILOT_OFF);
        ui.showFlashMessage(ui.translationManager().translate(auto ? "autopilot_on" : "autopilot_off"));
    }

    public static final GameAction ACTION_ACTIVATE_IMMUNITY = new GameAction("activate_immunity") {
        @Override
        public void doAction(GameUI ui) {
            setPacImmune(ui, true);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return realLevel(ui).isPresent();
        }
    };

    public static final GameAction ACTION_DEACTIVATE_IMMUNITY = new GameAction("deactivate_immunity") {
        @Override
        public void doAction(GameUI ui) {
            setPacImmune(ui, false);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return realLevel(ui).isPresent();
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("toggle_immunity") {
        @Override
        public void doAction(GameUI ui) {
            final Game game = ui.gameContext().game();
            setPacImmune(ui, !game.cheats().isImmune());
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return realLevel(ui).isPresent();
        }
    };

    public static void setPacImmune(GameUI ui, boolean immune) {
        final Game game = ui.gameContext().game();
        game.cheats().immuneProperty().set(immune);
        ui.soundManager().playVoice(immune ? GameUIConstants.VOICE_IMMUNITY_ON : GameUIConstants.VOICE_IMMUNITY_OFF);
        ui.showFlashMessage(ui.translationManager().translate(immune ? "player_immunity_on" : "player_immunity_off"));
    }

    private static Optional<GameLevel> realLevel(GameUI ui) {
        return ui.gameContext().game().optGameLevel().filter(level -> !level.isDemoLevel());
    }
}