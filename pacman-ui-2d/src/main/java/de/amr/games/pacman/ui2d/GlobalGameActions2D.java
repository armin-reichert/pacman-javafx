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
import de.amr.games.pacman.model.ms_pacman_tengen.BoosterMode;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.util.KeyInput.*;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public enum GlobalGameActions2D implements GameAction {
    /**
     * Adds credit (simulates insertion of a coin) and switches to the credit scene.
     */
    ADD_CREDIT(key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5), key(KeyCode.UP)) {
        @Override
        public void execute(GameContext context) {
            context.sounds().enabledProperty().set(true); // in demo mode, sound is disabled
            context.sounds().playCreditSound();
            if (!context.game().isPlaying()) {
                boolean coinInserted = context.gameController().coinControl().insertCoin();
                if (coinInserted) {
                    context.game().publishGameEvent(GameEventType.CREDIT_ADDED);
                }
                if (context.gameState() != GameState.STARTING) {
                    context.gameController().changeState(GameState.STARTING);
                }
            }
        }
    },

    BOOT(key(KeyCode.F3)) {
        @Override
        public void execute(GameContext context) {
            context.sounds().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            context.game().removeWorld();
            context.gameClock().setTargetFrameRate(GameModel.TICKS_PER_SECOND);
            context.gameController().restart(GameState.BOOT);
        }
    },

    CHEAT_ADD_LIVES(alt(KeyCode.L)) {
        @Override
        public void execute(GameContext context) {
            context.game().addLives(3);
            context.showFlashMessage(context.locText("cheat_add_lives", context.game().lives()));
        }
    },

    CHEAT_EAT_ALL(alt(KeyCode.E)) {
        @Override
        public void execute(GameContext context) {
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                GameWorld world = context.game().world();
                world.map().food().tiles().filter(not(world::isEnergizerPosition)).forEach(world::registerFoodEatenAt);
                context.game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
                context.sounds().stopMunchingSound();
            }
        }
    },

    CHEAT_KILL_GHOSTS(alt(KeyCode.X)) {
        @Override
        public void execute(GameContext context) {
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
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                context.gameController().changeState(GameState.LEVEL_COMPLETE);
            }
        }
    },

    OPEN_EDITOR(shift_alt(KeyCode.E)) {
        @Override
        public void execute(GameContext context) {
            if (context.game().world() == null) {
                Logger.error("Map editor cannot be opened because no world is available");
                return;
            }
            if (context.gameVariant() == GameVariant.PACMAN_XXL) {
                context.currentGameScene().ifPresent(GameScene::end);
                context.sounds().stopAll();
                context.gameClock().stop();
                EditorPage editorPage = context.getOrCreateEditorPage();
                editorPage.startEditor(context.game().world().map());
                context.selectPage(editorPage);
            } else {
                context.showFlashMessageSeconds(2, "This game variant does not support custom maps");
            }
        }
    },

    SHOW_START_PAGE(key(KeyCode.Q)) {
        @Override
        public void execute(GameContext context) {
            context.sounds().stopAll();
            context.game().onGameEnded();
            context.selectStartPage();
        }
    },

    RESTART_INTRO(key(KeyCode.Q)) {
        @Override
        public void execute(GameContext context) {
            context.sounds().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            if (context.gameState() == GameState.TESTING_LEVEL_BONI) {
                context.gameState().onExit(context.game()); //TODO exit other states too?
            }
            context.gameClock().setTargetFrameRate(GameModel.TICKS_PER_SECOND);
            context.gameController().restart(INTRO);
        }
    },

    START_GAME(key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1), key(KeyCode.SPACE)) {
        @Override
        public void execute(GameContext context) {
            if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                context.gameController().changeState(GameState.STARTING);
            } else if (context.game().canStartNewGame()) {
                context.sounds().stopVoice();
                if (context.gameState() == GameState.INTRO || context.gameState() == GameState.STARTING) {
                    context.gameController().changeState(GameState.READY);
                } else {
                    Logger.error("Cannot start game play in game state {}", context.gameState());
                }
            }
        }
    },

    TENGEN_TOGGLE_PAC_BOOSTER(key(KeyCode.A)) {
        @Override
        public void execute(GameContext context) {
            TengenMsPacManGame tengenGame = (TengenMsPacManGame) context.game();
            if (tengenGame.pacBoosterMode() == BoosterMode.ACTIVATED_USING_KEY) {
                if (tengenGame.isBoosterActive()) {
                    tengenGame.deactivateBooster();
                } else {
                    tengenGame.activateBooster();
                }
            }
        }
    },

    TENGEN_QUIT_PLAY_SCENE(key(KeyCode.S)) {
        @Override
        public void execute(GameContext context) {
            context.sounds().stopAll();
            context.currentGameScene().ifPresent(GameScene::end);
            context.game().setPlaying(false);
            TengenMsPacManGame tengenMsPacManGame = (TengenMsPacManGame) context.game();
            tengenMsPacManGame.setCanStartGame(true);
            context.gameController().changeState(GameState.STARTING); // shows Tengen settings scene
        }
    },

    TEST_CUT_SCENES(alt(KeyCode.C)) {
        @Override
        public void execute(GameContext context) {
            context.gameController().changeState(GameState.TESTING_CUT_SCENES);
            context.showFlashMessage("Cut scenes test"); //TODO localize
        }
    },

    TEST_LEVELS_BONI(alt(KeyCode.T)) {
        @Override
        public void execute(GameContext context) {
            context.gameController().restart(GameState.TESTING_LEVEL_BONI);
            context.showFlashMessageSeconds(3, "Level TEST MODE");
        }
    },

    TEST_LEVELS_TEASERS(shift_alt(KeyCode.T)) {
        @Override
        public void execute(GameContext context) {
            context.gameController().restart(GameState.TESTING_LEVEL_TEASERS);
            context.showFlashMessageSeconds(3, "Level TEST MODE");
        }
    },


    TOGGLE_PAUSED(key(KeyCode.P)) {
        @Override
        public void execute(GameContext context) {
            toggle(context.gameClock().pausedPy);
            if (context.gameClock().isPaused()) {
                context.sounds().stopAll();
            }
            Logger.info("Game variant ({}) {}", context.game(), context.gameClock().isPaused() ? "paused" : "resumed");
        }
    };

    GlobalGameActions2D(KeyCodeCombination... combinations) {
        trigger = KeyInput.of(combinations);
    }

    @Override
    public KeyInput trigger() {
        return trigger;
    }

    @Override
    public boolean called(Keyboard keyboard) {
        return keyboard.isRegisteredKeyPressed(trigger);
    }

    @Override
    public abstract void execute(GameContext context);

    private final KeyInput trigger;
}