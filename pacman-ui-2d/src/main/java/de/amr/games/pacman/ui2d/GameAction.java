/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.util.KeyInput.*;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public enum GameAction {
    /**
     * Adds credit (simulates insertion of a coin) and switches to the credit scene.
     */
    ADD_CREDIT(key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5), key(KeyCode.UP)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.sounds().enabledProperty().set(true); // in demo mode, sound is disabled
            context.sounds().playCreditSound();
            if (!context.game().isPlaying()) {
                boolean coinInserted = context.game().insertCoin();
                if (coinInserted) {
                    context.game().publishGameEvent(GameEventType.CREDIT_ADDED);
                }
                if (context.gameState() != GameState.CREDIT) {
                    context.gameController().changeState(GameState.CREDIT);
                }
            }
        }
    },

    BOOT(key(KeyCode.F3)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.sounds().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            context.game().removeWorld();
            context.gameClock().setTargetFrameRate(GameModel.FPS);
            context.gameController().restart(GameState.BOOT);
        }
    },

    CHEAT_ADD_LIVES(alt(KeyCode.L)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.game().addLives(3);
            context.showFlashMessage(context.locText("cheat_add_lives", context.game().lives()));
        }
    },

    CHEAT_EAT_ALL(alt(KeyCode.E)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                GameWorld world = context.game().world();
                world.map().food().tiles().filter(not(world::isEnergizerPosition)).forEach(world::eatFoodAt);
                context.game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
            }
        }
    },

    CHEAT_KILL_GHOSTS(alt(KeyCode.X)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                context.game().victims().clear();
                context.game().ghosts(FRIGHTENED, HUNTING_PAC).forEach(context.game()::killGhost);
                context.gameController().changeState(GameState.GHOST_DYING);
            }
        }
    },

    CHEAT_NEXT_LEVEL(alt(KeyCode.N)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                context.gameController().changeState(GameState.LEVEL_COMPLETE);
            }
        }
    },
    CUTSCENES           (alt(KeyCode.C)),
    DEBUG_INFO          (alt(KeyCode.D)),
    ENTER_GAME_PAGE     (key(KeyCode.SPACE), key(KeyCode.ENTER)),
    FULLSCREEN          (key(KeyCode.F11)),
    HELP                (key(KeyCode.H)),
    IMMUNITY            (alt(KeyCode.I)),
    MUTE                (alt(KeyCode.M)),
    NEXT_FLYER_PAGE     (key(KeyCode.DOWN)),

    NEXT_VARIANT(key(KeyCode.V), key(KeyCode.RIGHT)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            List<GameVariant> variantsInOrder = PacManGames2dUI.GAME_VARIANTS_IN_ORDER;
            int nextIndex = variantsInOrder.indexOf(context.game().variant()) + 1;
            context.selectGameVariant(variantsInOrder.get(nextIndex == variantsInOrder.size() ? 0 : nextIndex));
        }
    },

    TOGGLE_PAUSED(key(KeyCode.P)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            toggle(context.gameClock().pausedPy);
            if (context.gameClock().isPaused()) {
                context.sounds().stopAll();
            }
            Logger.info("Game variant ({}) {}", context.game(), context.gameClock().isPaused() ? "paused" : "resumed");
        }
    },

    OPEN_EDITOR         (shift_alt(KeyCode.E)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.game().variant() != GameVariant.PACMAN_XXL) {
                context.showFlashMessageSeconds(3, "Map editor is not available in this game variant"); //TODO localize
                return;
            }
            context.currentGameScene().ifPresent(GameScene::end);
            context.sounds().stopAll();
            context.gameClock().stop();
            EditorPage editorPage = context.getOrCreateEditorPage();
            editorPage.startEditor(context.game().world().map());
            context.selectPage(editorPage);
        }
    },

    PREV_FLYER_PAGE     (key(KeyCode.UP)),

    PREV_VARIANT(key(KeyCode.LEFT)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            List<GameVariant> variantsInOrder = PacManGames2dUI.GAME_VARIANTS_IN_ORDER;
            int prevIndex = variantsInOrder.indexOf(context.game().variant()) - 1;
            context.selectGameVariant(variantsInOrder.get(prevIndex < 0 ? variantsInOrder.size() - 1 : prevIndex));
        }
    },

    QUIT                (key(KeyCode.Q)),

    SIMULATION_FASTER (alt(KeyCode.PLUS)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
                double newRate = context.gameClock().getTargetFrameRate() + 5;
                if (newRate > 0) {
                    context.gameClock().setTargetFrameRate(newRate);
                    context.showFlashMessageSeconds(0.75, newRate + "Hz");
                }
        }
    },

    SIMULATION_NORMAL(alt(KeyCode.DIGIT0)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.gameClock().setTargetFrameRate(GameModel.FPS);
            context.showFlashMessageSeconds(0.75, context.gameClock().getTargetFrameRate() + "Hz");
        }
    },

    SIMULATION_SLOWER(alt(KeyCode.MINUS)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            //TODO dry
            double newRate = context.gameClock().getTargetFrameRate() - 5;
            if (newRate > 0) {
                context.gameClock().setTargetFrameRate(newRate);
                context.showFlashMessageSeconds(0.75, newRate + "Hz");
            }
        }
    },

    SIMULATION_1_STEP(key(KeyCode.SPACE), shift(KeyCode.P)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.gameClock().isPaused()) {
                context.gameClock().makeStep(true);
            }
        }
    },

    SIMULATION_10_STEPS (shift(KeyCode.SPACE)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.gameClock().isPaused()) {
                context.gameClock().makeSteps(10, true);
            }
        }
    },

    START_GAME          (key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1), key(KeyCode.ENTER), key(KeyCode.SPACE)),

    START_TEST_MODE     (alt(KeyCode.T)),

    TOGGLE_AUTOPILOT(alt(KeyCode.A)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            toggle(PY_AUTOPILOT);
            boolean auto = PY_AUTOPILOT.get();
            context.showFlashMessage(context.locText(auto ? "autopilot_on" : "autopilot_off"));
            context.sounds().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
        }
    },

    TOGGLE_DASHBOARD(key(KeyCode.F1), alt(KeyCode.B)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.gamePage().toggleDashboard();
        }
    },

    TOGGLE_PIP_VIEW     (key(KeyCode.F2)),

    TWO_D_THREE_D       (alt(KeyCode.DIGIT3));

    GameAction(KeyCodeCombination... combinations) {
        trigger = KeyInput.register(combinations);
    }

    public KeyInput trigger() {
        return trigger;
    }

    /**
     * @return {@code true} if any key combination defined for this game key is pressed
     */
    public boolean called() {
        return Keyboard.pressed(trigger);
    }

    public void execute(GameContext context) {
        Logger.info("Execute game action {}", name());
    }

    private final KeyInput trigger;
}