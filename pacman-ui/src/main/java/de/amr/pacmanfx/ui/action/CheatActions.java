/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.GameUI;

import java.util.List;

import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public final class CheatActions {

    public static final GameAction ACTION_ADD_LIVES = new GameAction("CHEAT_ADD_LIVES") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            if (game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
                game.raiseCheatFlag();
            }
            game.addLives(3);
            ui.showFlashMessage(ui.translate("cheat_add_lives", ui.context().currentGame().lifeCount()));
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.context().currentGame().optGameLevel().isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("CHEAT_EAT_ALL_PELLETS") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            game.optGameLevel().ifPresent(level -> {
                level.worldMap().foodLayer().eatPellets();
                game.raiseCheatFlag();
                game.publishGameEvent(new PacEatsFoodEvent(level.pac(), true));
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.context().currentGame();
            return game.optGameLevel().isPresent() && !game.level().isDemoLevel()
                    && game.control().state().matches(GameControl.StateName.HUNTING);
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("CHEAT_KILL_GHOSTS") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            game.optGameLevel().ifPresent(level -> {
                if (!game.level().isDemoLevel()) {
                    game.raiseCheatFlag();
                }
                final List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
                if (!vulnerableGhosts.isEmpty()) {
                    level.energizerVictims().clear(); // resets value of next killed ghost to 200
                    vulnerableGhosts.forEach(ghost -> game.onEatGhost(level, ghost));
                    game.control().enterStateNamed(GameControl.StateName.EATING_GHOST.name());
                }
            });
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.context().currentGame();
            return game.control().state().matches(GameControl.StateName.HUNTING)
                && game.optGameLevel().isPresent() && !game.level().isDemoLevel();
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("CHEAT_ENTER_NEXT_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            if (game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
                game.raiseCheatFlag();
            }
            game.control().enterStateNamed(GameControl.StateName.LEVEL_COMPLETE.name());
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.context().currentGame();
            return game.isPlaying()
                    && game.control().state().matches(GameControl.StateName.HUNTING)
                    && game.optGameLevel().isPresent()
                    && game.level().number() < game.lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("TOGGLE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            if (game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
                game.raiseCheatFlag();
            }
            toggle(game.usingAutopilotProperty());
            boolean usingAutopilot = game.usingAutopilot();
            ui.voicePlayer().play(usingAutopilot ? GameUI.VOICE_AUTOPILOT_ON : GameUI.VOICE_AUTOPILOT_OFF);
            ui.showFlashMessage(ui.translate(usingAutopilot ? "autopilot_on" : "autopilot_off"));
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("TOGGLE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.context().currentGame();
            if (game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
                game.raiseCheatFlag();
            }
            toggle(game.immuneProperty());
            boolean immunityOn = game.immuneProperty().get();
            ui.voicePlayer().play(immunityOn ? GameUI.VOICE_IMMUNITY_ON : GameUI.VOICE_IMMUNITY_OFF);
            ui.showFlashMessage(ui.translate(immunityOn ? "player_immunity_on" : "player_immunity_off"));
        }
    };
}