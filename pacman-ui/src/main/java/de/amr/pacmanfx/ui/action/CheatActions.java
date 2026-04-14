/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow.CanonicalGameState;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;

public final class CheatActions {

    public static final GameAction ACTION_ADD_LIVES = new GameAction("CHEAT_ADD_LIVES") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            realLevel(game).ifPresent(_ -> {
                game.addLives(3);
                game.cheating().raiseFlag();
                ui.showFlashMessage(ui.translate("cheat_add_lives", game.lifeCount()));
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.gameContext().game().optGameLevel().isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("CHEAT_EAT_ALL_PELLETS") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            realLevel(game).ifPresent(level -> {
                level.worldMap().foodLayer().eatPellets();
                game.cheating().raiseFlag();
                game.flow().publishGameEvent(new PacEatsFoodEvent(game, level.pac(), true));
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().game();
            return !game.isDemoLevelRunning() && game.flow().state().nameMatches(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("CHEAT_KILL_GHOSTS") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            realLevel(game).ifPresent(level -> {
                final List<Ghost> killableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
                if (!killableGhosts.isEmpty()) {
                    level.energizerVictims().clear(); // resets value of next killed ghost to 200
                    killableGhosts.forEach(ghost -> game.onEatGhost(level, ghost));
                    game.flow().enterStateWithName(CanonicalGameState.EATING_GHOST.name());
                }
                game.cheating().raiseFlag();
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().game();
            return !game.isDemoLevelRunning()
                && game.flow().state().nameMatches(CanonicalGameState.LEVEL_PLAYING.name());
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("CHEAT_ENTER_NEXT_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            realLevel(game).ifPresent(_ -> {
                game.cheating().raiseFlag();
                game.flow().enterStateWithName(CanonicalGameState.LEVEL_COMPLETE.name());
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().game();
            final GameLevel level = game.optGameLevel().orElse(null);
            if (level == null) {
                return false;
            }
            return game.isPlaying()
                && game.flow().state().nameMatches(CanonicalGameState.LEVEL_PLAYING.name())
                && level.number() < game.lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("TOGGLE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            setAutopilot(ui, !game.cheating().isUsingAutopilot());
        }
    };

    public static final GameAction ACTION_ACTIVATE_AUTOPILOT = new GameAction("ACTIVATE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            setAutopilot(ui, true);
        }
    };

    public static final GameAction ACTION_DEACTIVATE_AUTOPILOT = new GameAction("DEACTIVATE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            setAutopilot(ui, false);
        }
    };

    private static void setAutopilot(GameUI ui, boolean on) {
        final Game game = ui.gameContext().game();
        if (on) {
            if (game.isPlaying() && !game.isDemoLevelRunning()) {
                game.cheating().raiseFlag();
            }
        }
        game.cheating().usingAutopilotProperty().set(on);
        ui.voicePlayer().playVoice(on ? GameUI_Resources.VOICE_AUTOPILOT_ON : GameUI_Resources.VOICE_AUTOPILOT_OFF);
        ui.showFlashMessage(ui.translate(on ? "autopilot_on" : "autopilot_off"));

    }

    public static final GameAction ACTION_ACTIVATE_IMMUNITY = new GameAction("ACTIVATE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            setImmunity(ui, true);
        }
    };

    public static final GameAction ACTION_DEACTIVATE_IMMUNITY = new GameAction("DEACTIVATE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            setImmunity(ui, false);
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("TOGGLE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().game();
            setImmunity(ui, !game.cheating().isImmune());
        }
    };

    private static void setImmunity(GameUI ui, boolean on) {
        final Game game = ui.gameContext().game();
        if (on) {
            if (game.isPlaying() && !game.isDemoLevelRunning()) {
                game.cheating().raiseFlag();
            }
        }
        game.cheating().immuneProperty().set(on);
        ui.voicePlayer().playVoice(on ? GameUI_Resources.VOICE_IMMUNITY_ON : GameUI_Resources.VOICE_IMMUNITY_OFF);
        ui.showFlashMessage(ui.translate(on ? "player_immunity_on" : "player_immunity_off"));
    }

    private static Optional<GameLevel> realLevel(Game game) {
        return game.optGameLevel().filter(level -> !level.isDemoLevel());
    }
}