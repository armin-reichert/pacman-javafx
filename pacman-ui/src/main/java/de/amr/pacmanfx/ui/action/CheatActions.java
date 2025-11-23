/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.controller.GameController;
import de.amr.pacmanfx.controller.PacManGamesState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;

import java.util.List;

import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.uilib.Ufx.toggle;

public final class CheatActions {

    public static final GameAction ACTION_ADD_LIVES = new GameAction("CHEAT_ADD_LIVES") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            ui.gameContext().game().addLives(3);
            ui.showFlashMessage(ui.assets().translated("cheat_add_lives", ui.gameContext().game().lifeCount()));
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.gameContext().optGameLevel().isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("CHEAT_EAT_ALL_PELLETS") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            ui.gameContext().gameLevel().worldMap().foodLayer().eatPellets();
            ui.soundManager().pause(SoundID.PAC_MAN_MUNCHING);
            ui.gameContext().eventManager().publishEvent(GameEventType.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().optGameLevel().isPresent()
                    && !ui.gameContext().gameLevel().isDemoLevel()
                    && ui.gameContext().gameState() == PacManGamesState.HUNTING;
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("CHEAT_KILL_GHOSTS") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            GameLevel gameLevel = ui.gameContext().gameLevel();
            List<Ghost> vulnerableGhosts = gameLevel.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                gameLevel.energizerVictims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(ghost -> gameLevel.game().onGhostKilled(gameLevel, ghost));
                ui.gameContext().playStateMachine().changeGameState(PacManGamesState.GHOST_DYING);
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().gameState() == PacManGamesState.HUNTING && ui.gameContext().optGameLevel().isPresent() && !ui.gameContext().gameLevel().isDemoLevel();
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("CHEAT_ENTER_NEXT_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().gameController().cheatUsedProperty().set(true);
            ui.gameContext().playStateMachine().changeGameState(PacManGamesState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().game().isPlaying()
                    && ui.gameContext().gameState() == PacManGamesState.HUNTING
                    && ui.gameContext().optGameLevel().isPresent()
                    && ui.gameContext().gameLevel().number() < ui.gameContext().game().lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("TOGGLE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            final GameController gameController = ui.gameContext().gameController();
            if (gameController.game().isPlaying()) {
                gameController.cheatUsedProperty().set(true);
            }
            toggle(gameController.usingAutopilotProperty());
            boolean autoPilotOn = gameController.usingAutopilotProperty().get();
            ui.showFlashMessage(ui.assets().translated(autoPilotOn ? "autopilot_on" : "autopilot_off"));
            ui.soundManager().playVoice(autoPilotOn ? SoundID.VOICE_AUTOPILOT_ON : SoundID.VOICE_AUTOPILOT_OFF, 0);
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("TOGGLE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            if (ui.gameContext().game().isPlaying()) {
                ui.gameContext().gameController().cheatUsedProperty().set(true);
            }
            toggle(ui.gameContext().gameController().immunityProperty());
            boolean immunityOn = ui.gameContext().gameController().immunityProperty().get();
            ui.showFlashMessage(ui.assets().translated(immunityOn ? "player_immunity_on" : "player_immunity_off"));
            ui.soundManager().playVoice(immunityOn ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF, 0);
        }
    };
}