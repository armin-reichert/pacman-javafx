/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.model.Game;
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
            ui.context().cheatUsedProperty().set(true);
            ui.context().currentGame().addLives(3);
            ui.showFlashMessage(ui.assets().translated("cheat_add_lives", ui.context().currentGame().lifeCount()));
        }

        @Override
        public boolean isEnabled(GameUI ui) { return ui.context().currentGame().optGameLevel().isPresent(); }
    };

    public static final GameAction ACTION_EAT_ALL_PELLETS = new GameAction("CHEAT_EAT_ALL_PELLETS") {
        @Override
        public void execute(GameUI ui) {
            ui.context().cheatUsedProperty().set(true);
            ui.context().currentGame().level().worldMap().foodLayer().eatPellets();
            ui.soundManager().pause(SoundID.PAC_MAN_MUNCHING);
            ui.context().currentGame().publishGameEvent(GameEvent.Type.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.context().currentGame().optGameLevel().isPresent()
                    && !ui.context().currentGame().level().isDemoLevel()
                    && ui.context().currentGameState().name().equals("HUNTING");
        }
    };

    public static final GameAction ACTION_KILL_GHOSTS = new GameAction("CHEAT_KILL_GHOSTS") {
        @Override
        public void execute(GameUI ui) {
            ui.context().cheatUsedProperty().set(true);
            GameLevel gameLevel = ui.context().currentGame().level();
            List<Ghost> vulnerableGhosts = gameLevel.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                gameLevel.energizerVictims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(ghost -> gameLevel.game().onGhostKilled(gameLevel, ghost));
                ui.context().currentGame().changeState("GHOST_DYING");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.context().currentGameState().name().equals("HUNTING")
                && ui.context().currentGame().optGameLevel().isPresent() && !ui.context().currentGame().level().isDemoLevel();
        }
    };

    public static final GameAction ACTION_ENTER_NEXT_LEVEL = new GameAction("CHEAT_ENTER_NEXT_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            ui.context().cheatUsedProperty().set(true);
            ui.context().currentGame().changeState("LEVEL_COMPLETE");
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            final Game game = ui.context().currentGame();
            return game.isPlaying()
                    && ui.context().currentGameState().name().equals("HUNTING")
                    && game.optGameLevel().isPresent()
                    && game.level().number() < game.lastLevelNumber();
        }
    };

    public static final GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction("TOGGLE_AUTOPILOT") {
        @Override
        public void execute(GameUI ui) {
            if (ui.context().currentGame().isPlaying()) {
                ui.context().cheatUsedProperty().set(true);
            }
            toggle(ui.context().usingAutopilotProperty());
            boolean autoPilotOn = ui.context().usingAutopilotProperty().get();
            ui.showFlashMessage(ui.assets().translated(autoPilotOn ? "autopilot_on" : "autopilot_off"));
            ui.soundManager().playVoice(autoPilotOn ? SoundID.VOICE_AUTOPILOT_ON : SoundID.VOICE_AUTOPILOT_OFF, 0);
        }
    };

    public static final GameAction ACTION_TOGGLE_IMMUNITY = new GameAction("TOGGLE_IMMUNITY") {
        @Override
        public void execute(GameUI ui) {
            if (ui.context().currentGame().isPlaying()) {
                ui.context().cheatUsedProperty().set(true);
            }
            toggle(ui.context().immunityProperty());
            boolean immunityOn = ui.context().immunityProperty().get();
            ui.showFlashMessage(ui.assets().translated(immunityOn ? "player_immunity_on" : "player_immunity_off"));
            ui.soundManager().playVoice(immunityOn ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF, 0);
        }
    };
}