/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl.CommonGameState;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;

import java.util.List;

import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.uilib.Ufx.toggleBoolean;

public final class CheatActions {

    public static final GameAction ACTION_ADD_LIVES = new GameAction("CHEAT_ADD_LIVES") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (!level.isDemoLevel()) {
                game.raiseCheatFlag();
            }
            game.addLives(3);
            ui.showFlashMessage(ui.translate("cheat_add_lives", ui.gameContext().currentGame().lifeCount()));
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.gameContext().currentGame().optGameLevel().isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("CHEAT_EAT_ALL_PELLETS") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            final GameLevel level = game.optGameLevel().orElseThrow();
            level.worldMap().foodLayer().eatPellets();
            game.raiseCheatFlag();
            game.publishGameEvent(new PacEatsFoodEvent(level.pac(), true));
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            return !game.isDemoLevelRunning() && game.control().state().nameMatches(CommonGameState.HUNTING.name());
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("CHEAT_KILL_GHOSTS") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (!level.isDemoLevel()) {
                game.raiseCheatFlag();
            }
            final List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                level.energizerVictims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(ghost -> game.onEatGhost(level, ghost));
                game.control().enterStateWithName(CommonGameState.EATING_GHOST.name());
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            return game.control().state().nameMatches(CommonGameState.HUNTING.name()) && !game.isDemoLevelRunning();
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("CHEAT_ENTER_NEXT_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            final GameLevel level = game.optGameLevel().orElseThrow();
            if (!level.isDemoLevel()) {
                game.raiseCheatFlag();
            }
            game.control().enterStateWithName(CommonGameState.LEVEL_COMPLETE.name());
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            return game.isPlaying()
                && game.control().state().nameMatches(CommonGameState.HUNTING.name())
                && game.optGameLevel().isPresent()
                && game.optGameLevel().get().number() < game.lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("TOGGLE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            if (!game.isDemoLevelRunning()) {
                game.raiseCheatFlag();
            }
            toggleBoolean(game.usingAutopilotProperty());
            final boolean usingAutopilot = game.isUsingAutopilot();
            ui.voicePlayer().playVoice(usingAutopilot ? GameUI_Resources.VOICE_AUTOPILOT_ON : GameUI_Resources.VOICE_AUTOPILOT_OFF);
            ui.showFlashMessage(ui.translate(usingAutopilot ? "autopilot_on" : "autopilot_off"));
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("TOGGLE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            final Game game = ui.gameContext().currentGame();
            if (!game.isDemoLevelRunning()) {
                game.raiseCheatFlag();
            }
            toggleBoolean(game.immuneProperty());
            final boolean immunityOn = game.immuneProperty().get();
            ui.voicePlayer().playVoice(immunityOn ? GameUI_Resources.VOICE_IMMUNITY_ON : GameUI_Resources.VOICE_IMMUNITY_OFF);
            ui.showFlashMessage(ui.translate(immunityOn ? "player_immunity_on" : "player_immunity_off"));
        }
    };
}