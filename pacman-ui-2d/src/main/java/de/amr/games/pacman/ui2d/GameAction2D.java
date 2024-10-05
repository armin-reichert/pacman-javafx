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
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.controller.GameState.LEVEL_TEST;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.util.KeyInput.*;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public enum GameAction2D implements GameAction {
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
                context.sounds().stopMunchingSound();
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

    DEBUG_INFO(alt(KeyCode.D)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            Ufx.toggle(PY_DEBUG_INFO);
        }
    },

    ENTER_GAME_PAGE(key(KeyCode.SPACE), key(KeyCode.ENTER)),

    FULLSCREEN(key(KeyCode.F11)),

    HELP(key(KeyCode.H)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.gamePage().showHelp();
        }
    },

    MUTE(alt(KeyCode.M)),

    NEXT_FLYER_PAGE(key(KeyCode.DOWN)),

    NEXT_VARIANT(key(KeyCode.V), key(KeyCode.RIGHT)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            List<GameVariant> variantsInOrder = PacManGames2dUI.GAME_VARIANTS_IN_ORDER;
            int nextIndex = variantsInOrder.indexOf(context.game().variant()) + 1;
            context.selectGameVariant(variantsInOrder.get(nextIndex == variantsInOrder.size() ? 0 : nextIndex));
        }
    },

    OPEN_EDITOR(shift_alt(KeyCode.E)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.currentGameScene().ifPresent(GameScene::end);
            context.sounds().stopAll();
            context.gameClock().stop();
            EditorPage editorPage = context.getOrCreateEditorPage();
            editorPage.startEditor(context.game().world().map());
            context.selectPage(editorPage);
        }
    },

    PREV_FLYER_PAGE(key(KeyCode.UP)),

    PREV_VARIANT(key(KeyCode.LEFT)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            List<GameVariant> variantsInOrder = PacManGames2dUI.GAME_VARIANTS_IN_ORDER;
            int prevIndex = variantsInOrder.indexOf(context.game().variant()) - 1;
            context.selectGameVariant(variantsInOrder.get(prevIndex < 0 ? variantsInOrder.size() - 1 : prevIndex));
        }
    },

    SHOW_START_PAGE(key(KeyCode.Q)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.sounds().stopAll();
            context.game().consumeCoin();
            context.selectStartPage();
        }
    },

    RESTART_INTRO(key(KeyCode.Q)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.sounds().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            if (context.gameState() == LEVEL_TEST) {
                context.gameState().onExit(context.game()); //TODO exit other states too?
            }
            context.gameClock().setTargetFrameRate(GameModel.FPS);
            context.gameController().restart(INTRO);
        }
    },

    SIMULATION_FASTER(alt(KeyCode.PLUS)) {
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

    SIMULATION_1_STEP(shift(KeyCode.P)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.gameClock().isPaused()) {
                context.gameClock().makeStep(true);
            }
        }
    },

    SIMULATION_10_STEPS(shift(KeyCode.SPACE)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.gameClock().isPaused()) {
                context.gameClock().makeSteps(10, true);
            }
        }
    },

    //TODO this is not useful in Tengen Ms. Pac-Man
    START_GAME(key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1), key(KeyCode.SPACE)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.game().hasCredit()) {
                context.sounds().stopVoice();
                if (context.gameState() == GameState.INTRO || context.gameState() == GameState.CREDIT) {
                    context.gameController().changeState(GameState.READY);
                } else {
                    Logger.error("Cannot start game play in game state {}", context.gameState());
                }
            }
        }
    },

    TEST_CUT_SCENES(alt(KeyCode.C)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.gameState() == GameState.INTRO) {
                context.gameController().changeState(GameState.INTERMISSION_TEST);
            } else {
                Logger.error("Intermission test can only be started from intro screen");
            }
            context.showFlashMessage("Cut scenes test"); //TODO localize
        }
    },

    TEST_LEVELS(alt(KeyCode.T)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.gameState() == GameState.INTRO) {
                context.gameController().restart(GameState.LEVEL_TEST);
                context.showFlashMessageSeconds(3, "Level TEST MODE");
            }
        }
    },

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

    TOGGLE_IMMUNITY(alt(KeyCode.I)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            toggle(PY_IMMUNITY);
            context.showFlashMessage(context.locText(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
            context.sounds().playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
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
    };

    GameAction2D(KeyCodeCombination... combinations) {
        trigger = KeyInput.register(combinations);
    }

    @Override
    public KeyInput trigger() {
        return trigger;
    }

    @Override
    public boolean called() {
        return Keyboard.pressed(trigger);
    }

    @Override
    public void execute(GameContext context) {
        Logger.info("Execute game action {}", name());
    }

    private final KeyInput trigger;
}