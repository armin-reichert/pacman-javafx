/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

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
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            realLevel(game).ifPresent(_ -> {
                game.addLives(3);
                game.cheats().raiseFlag();
                ui.showFlashMessage(ui.translationManager().translate(resourceBundleKey(), game.lifeCount()));
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) { return realLevel(ui.gameContext().game()).isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("cheat_eat_all_pellets") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            realLevel(game).ifPresent(level -> {
                level.worldMap().foodLayer().eatPellets();
                game.cheats().raiseFlag();
                game.flow().publishGameEvent(new PacEatsFoodEvent(game, level.entities().pac(), false, true));
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().game();
            return realLevel(game).isPresent()
                && game.flow().state().matchesByName(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("cheat_kill_ghosts") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            realLevel(game).ifPresent(level -> {
                final List<Ghost> killableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
                if (!killableGhosts.isEmpty()) {
                    level.energizerVictims().clear(); // resets value of next killed ghost to 200
                    killableGhosts.forEach(game::onEatGhost);
                    game.flow().enterStateWithName(CanonicalGameState.EATING_GHOST.name());
                }
                game.cheats().raiseFlag();
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().game();
            return realLevel(game).isPresent()
                && game.flow().state().matchesByName(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("cheat_enter_next_level") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            realLevel(game).ifPresent(_ -> {
                game.cheats().raiseFlag();
                game.flow().enterStateWithName(CanonicalGameState.LEVEL_COMPLETE.name());
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().game();
            final Optional<GameLevel> realLevel = realLevel(game);
            return realLevel.isPresent()
                && game.flow().state().matchesByName(CanonicalGameState.LEVEL_PLAYING.name())
                && realLevel.get().number() < game.lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("toggle_autopilot") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            setAutopilot(ui, !game.cheats().isUsingAutopilot());
        }
    };

    public static final GameAction ACTION_ACTIVATE_AUTOPILOT = new GameAction("activate_autopilot") {
        @Override
        public void execute(GameUI ui) {
            setAutopilot(ui, true);
        }
    };

    public static final GameAction ACTION_DEACTIVATE_AUTOPILOT = new GameAction("deactivate_autopilot") {
        @Override
        public void execute(GameUI ui) {
            setAutopilot(ui, false);
        }
    };

    private static void setAutopilot(GameUI ui, boolean on) {
        final Game game = ui.gameContext().game();
        if (on) {
            if (game.isPlayingLevel() && !game.isDemoLevelRunning()) {
                game.cheats().raiseFlag();
            }
        }
        game.cheats().usingAutopilotProperty().set(on);
        ui.voiceManager().playVoice(on ? GameUIConstants.VOICE_AUTOPILOT_ON : GameUIConstants.VOICE_AUTOPILOT_OFF);
        ui.showFlashMessage(ui.translationManager().translate(on ? "autopilot_on" : "autopilot_off"));

    }

    public static final GameAction ACTION_ACTIVATE_IMMUNITY = new GameAction("activate_immunity") {
        @Override
        public void execute(GameUI ui) {
            setImmunity(ui, true);
        }
    };

    public static final GameAction ACTION_DEACTIVATE_IMMUNITY = new GameAction("deactivate_immunity") {
        @Override
        public void execute(GameUI ui) {
            setImmunity(ui, false);
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("toggle_immunity") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            setImmunity(ui, !game.cheats().isImmune());
        }
    };

    public static void setImmunity(GameUI ui, boolean on) {
        final Game game = ui.gameContext().game();
        game.cheats().immuneProperty().set(on);
        ui.voiceManager().playVoice(on ? GameUIConstants.VOICE_IMMUNITY_ON : GameUIConstants.VOICE_IMMUNITY_OFF);
        ui.showFlashMessage(ui.translationManager().translate(on ? "player_immunity_on" : "player_immunity_off"));
    }

    private static Optional<GameLevel> realLevel(Game game) {
        return game.optGameLevel().filter(level -> !level.isDemoLevel());
    }
}