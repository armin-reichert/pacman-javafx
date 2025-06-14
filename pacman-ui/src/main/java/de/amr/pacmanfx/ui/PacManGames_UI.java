/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.GameAction;
import de.amr.pacmanfx.uilib.GameScene;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.isOneOf;
import static de.amr.pacmanfx.controller.GameState.INTRO;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.model.actors.GhostState.HUNTING_PAC;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.uilib.ActionBindingSupport.createBinding;
import static de.amr.pacmanfx.uilib.Ufx.toggle;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;

public interface PacManGames_UI {
    Font DEBUG_TEXT_FONT           = Font.font("Sans", FontWeight.BOLD, 18);
    int LIVES_COUNTER_MAX          = 5;
    double MAX_SCENE_2D_SCALING    = 5;

    double BONUS_3D_SYMBOL_WIDTH  = TS;
    double BONUS_3D_POINTS_WIDTH  = 1.8 * TS;

    float ENERGIZER_3D_RADIUS      = 3.5f;
    float FLOOR_3D_THICKNESS       = 0.5f;
    float GHOST_3D_SIZE            = 16.0f;
    float HOUSE_3D_BASE_HEIGHT     = 12.0f;
    float HOUSE_3D_WALL_TOP_HEIGHT = 0.1f;
    float HOUSE_3D_WALL_THICKNESS  = 1.5f;
    float HOUSE_3D_OPACITY         = 0.4f;
    float HOUSE_3D_SENSITIVITY     = 1.5f * TS;
    float LIVES_COUNTER_3D_SIZE    = 12f;
    float OBSTACLE_3D_BASE_HEIGHT  = 7.0f;
    float OBSTACLE_3D_TOP_HEIGHT   = 0.1f;
    float OBSTACLE_3D_THICKNESS    = 1.25f;
    float PAC_3D_SIZE              = 17.0f;
    float PELLET_3D_RADIUS         = 1.0f;

    // Global key combinations
    KeyCombination KEY_FULLSCREEN  = nude(KeyCode.F11);
    KeyCombination KEY_MUTE        = alt(KeyCode.M);
    KeyCombination KEY_OPEN_EDITOR = alt_shift(KeyCode.E);

    ObjectProperty<Color>         PY_CANVAS_BG_COLOR        = new SimpleObjectProperty<>(Color.BLACK);
    BooleanProperty               PY_CANVAS_FONT_SMOOTHING  = new SimpleBooleanProperty(false);
    BooleanProperty               PY_CANVAS_IMAGE_SMOOTHING = new SimpleBooleanProperty(false);
    BooleanProperty               PY_DEBUG_INFO_VISIBLE     = new SimpleBooleanProperty(false);
    BooleanProperty               PY_IMMUNITY               = new SimpleBooleanProperty(false);
    IntegerProperty               PY_PIP_HEIGHT             = new SimpleIntegerProperty(400);
    BooleanProperty               PY_PIP_ON                 = new SimpleBooleanProperty(false);
    IntegerProperty               PY_PIP_OPACITY_PERCENT    = new SimpleIntegerProperty(100);
    IntegerProperty               PY_SIMULATION_STEPS       = new SimpleIntegerProperty(1);
    BooleanProperty               PY_USING_AUTOPILOT        = new SimpleBooleanProperty(false);
    BooleanProperty               PY_3D_AXES_VISIBLE        = new SimpleBooleanProperty(false);
    ObjectProperty<DrawMode>      PY_3D_DRAW_MODE           = new SimpleObjectProperty<>(DrawMode.FILL);
    BooleanProperty               PY_3D_ENABLED             = new SimpleBooleanProperty(false);
    BooleanProperty               PY_3D_ENERGIZER_EXPLODES  = new SimpleBooleanProperty(true);
    ObjectProperty<Color>         PY_3D_FLOOR_COLOR         = new SimpleObjectProperty<>(Color.rgb(20,20,20));
    ObjectProperty<Color>         PY_3D_LIGHT_COLOR         = new SimpleObjectProperty<>(Color.WHITE);
    BooleanProperty               PY_3D_PAC_LIGHT_ENABLED   = new SimpleBooleanProperty(true);
    ObjectProperty<PerspectiveID> PY_3D_PERSPECTIVE         = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    DoubleProperty                PY_3D_WALL_HEIGHT         = new SimpleDoubleProperty(3.5);
    DoubleProperty                PY_3D_WALL_OPACITY        = new SimpleDoubleProperty(1.0);

    void buildUI(Stage stage, double width, double height, DashboardID... dashboardIDs);
    void restart();
    void selectGameVariant(String variant);
    void show();

    // Configuration
    PacManGames_UIConfig configuration(String gameVariant);
    PacManGames_UIConfig configuration();
    void setConfiguration(String variant, PacManGames_UIConfig configuration);

    // Game scenes
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();
    void setCurrentGameScene(GameScene gameScene);
    boolean currentGameSceneIsPlayScene2D();
    boolean currentGameSceneIsPlayScene3D();
    boolean currentGameSceneIs2D();
    void updateGameScene(boolean reload);

    // Views
    PacManGames_View currentView();
    GameView gameView();
    StartPagesView startPagesView();
    void showEditorView();
    void showGameView();
    void showStartView();

    // Flash messages
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1.5, message, args); }
    void showFlashMessageSec(double seconds, String message, Object... args);

    // Actions

    int SIMULATION_SPEED_DELTA = 2;
    int SIMULATION_SPEED_MIN   = 10;
    int SIMULATION_SPEED_MAX   = 240;

    record SteeringAction(Direction dir) implements GameAction {
        @Override
        public void execute() { theGameLevel().pac().setWishDir(dir); }

        @Override
        public boolean isEnabled() { return optGameLevel().isPresent() && !theGameLevel().pac().isUsingAutopilot(); }

        @Override
        public String toString() {
            return "SteerPlayer_" + dir;
        }
    }

    GameAction ACTION_CHEAT_ADD_LIVES = new GameAction() {
        @Override
        public void execute() {
            theGame().addLives(3);
            theUI().showFlashMessage(theAssets().text("cheat_add_lives", theGame().lifeCount()));
        }

        @Override
        public boolean isEnabled() { return optGameLevel().isPresent(); }
    };

    GameAction ACTION_CHEAT_EAT_ALL_PELLETS = new GameAction() {
        @Override
        public void execute() {
            theGameLevel().eatAllPellets();
            theSound().stopMunchingSound();
            theGameEventManager().publishEvent(theGame(), GameEventType.PAC_FOUND_FOOD);
        }

        @Override
        public boolean isEnabled() {
            return optGameLevel().isPresent()
                    && !theGameLevel().isDemoLevel()
                    && theGameState() == GameState.HUNTING;
        }
    };

    GameAction ACTION_CHEAT_KILL_GHOSTS = new GameAction() {
        @Override
        public void execute() {
            GameLevel level = theGameLevel();
            List<Ghost> vulnerableGhosts = level.ghosts(FRIGHTENED, HUNTING_PAC).toList();
            if (!vulnerableGhosts.isEmpty()) {
                level.victims().clear(); // resets value of next killed ghost to 200
                vulnerableGhosts.forEach(theGame()::onGhostKilled);
                theGameController().changeGameState(GameState.GHOST_DYING);
            }
        }

        @Override
        public boolean isEnabled() {
            return theGameState() == GameState.HUNTING && optGameLevel().isPresent() && !theGameLevel().isDemoLevel();
        }
    };

    GameAction ACTION_CHEAT_ENTER_NEXT_LEVEL = new GameAction() {
        @Override
        public void execute() {
            theGameController().changeGameState(GameState.LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled() {
            return theGame().isPlaying()
                    && theGameState() == GameState.HUNTING
                    && optGameLevel().isPresent()
                    && theGameLevel().number() < theGame().lastLevelNumber();
        }
    };

    /**
     * Adds credit (simulates insertion of a coin) and switches the game state accordingly.
     */
    GameAction ACTION_ARCADE_INSERT_COIN = new GameAction() {
        @Override
        public void execute() {
            if (theCoinMechanism().numCoins() < CoinMechanism.MAX_COINS) {
                theCoinMechanism().insertCoin();
                theSound().enabledProperty().set(true);
                theGameEventManager().publishEvent(theGame(), GameEventType.CREDIT_ADDED);
            }
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public boolean isEnabled() {
            if (theGame().isPlaying()) {
                return false;
            }
            return theGameState() == GameState.SETTING_OPTIONS
                    || theGameState() == INTRO
                    || optGameLevel().isPresent() && optGameLevel().get().isDemoLevel()
                    || theCoinMechanism().isEmpty();
        }
    };

    GameAction ACTION_STEER_UP = new SteeringAction(Direction.UP);

    GameAction ACTION_STEER_DOWN = new SteeringAction(Direction.DOWN);

    GameAction ACTION_STEER_LEFT = new SteeringAction(Direction.LEFT);

    GameAction ACTION_STEER_RIGHT = new SteeringAction(Direction.RIGHT);

    GameAction ACTION_QUIT_GAME_SCENE = new GameAction() {
        @Override
        public void execute() {
            theUI().currentGameScene().ifPresent(GameScene::end);
            theGame().resetEverything();
            if (!theCoinMechanism().isEmpty()) theCoinMechanism().consumeCoin();
            theUI().showStartView();
        }
    };

    GameAction ACTION_RESTART_INTRO = new GameAction() {
        @Override
        public void execute() {
            theSound().stopAll();
            theUI().currentGameScene().ifPresent(GameScene::end);
            if (theGameState() == GameState.TESTING_LEVELS) {
                theGameState().onExit(theGame()); //TODO exit other states too?
            }
            theClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
            theGameController().restart(INTRO);
        }
    };

    GameAction ACTION_BOOT_SHOW_GAME_VIEW = new GameAction() {
        @Override
        public void execute() {
            theUI().showGameView();
            theUI().restart();
        }
    };

    GameAction ACTION_SIMULATION_SLOWER = new GameAction() {
        @Override
        public void execute() {
            double newRate = theClock().targetFrameRate() - SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MIN ? "At minimum speed: " : "";
            theUI().showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    };

    GameAction ACTION_SIMULATION_FASTER = new GameAction() {
        @Override
        public void execute() {
            double newRate = theClock().targetFrameRate() + SIMULATION_SPEED_DELTA;
            newRate = Math.clamp(newRate, SIMULATION_SPEED_MIN, SIMULATION_SPEED_MAX);
            theClock().setTargetFrameRate(newRate);
            String prefix = newRate == SIMULATION_SPEED_MAX ? "At maximum speed: " : "";
            theUI().showFlashMessageSec(0.75, prefix + newRate + "Hz");
        }
    };

    GameAction ACTION_SIMULATION_ONE_STEP = new GameAction() {
        @Override
        public void execute() {
            boolean success = theClock().makeOneStep(true);
            if (!success) {
                theUI().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return theClock().isPaused(); }
    };

    GameAction ACTION_SIMULATION_TEN_STEPS = new GameAction() {
        @Override
        public void execute() {
            boolean success = theClock().makeSteps(10, true);
            if (!success) {
                theUI().showFlashMessage("Simulation step error, clock stopped!");
            }
        }

        @Override
        public boolean isEnabled() { return theClock().isPaused(); }
    };

    GameAction ACTION_SIMULATION_RESET = new GameAction() {
        @Override
        public void execute() {
            theClock().setTargetFrameRate(NUM_TICKS_PER_SEC);
            theUI().showFlashMessageSec(0.75, theClock().targetFrameRate() + "Hz");
        }
    };

    GameAction ACTION_ARCADE_START_GAME = new GameAction() {
        @Override
        public void execute() {
            theSound().stopVoice();
            theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public boolean isEnabled() {
            return !theGameController().selectedGameVariant().equals("MS_PACMAN_TENGEN")
                    && !theCoinMechanism().isEmpty()
                    && (theGameState() == GameState.INTRO || theGameState() == GameState.SETTING_OPTIONS)
                    && theGame().canStartNewGame();
        }
    };

    GameAction ACTION_TEST_CUT_SCENES = new GameAction() {
        @Override
        public void execute() {
            theGameController().changeGameState(GameState.TESTING_CUT_SCENES);
            theUI().showFlashMessage("Cut scenes test"); //TODO localize
        }
    };

    GameAction ACTION_TEST_LEVELS_BONI = new GameAction() {
        @Override
        public void execute() {
            theGameController().restart(GameState.TESTING_LEVELS);
            theUI().showFlashMessageSec(3, "Level TEST MODE");
        }
    };

    GameAction ACTION_TEST_LEVELS_TEASERS = new GameAction() {
        @Override
        public void execute() {
            theGameController().restart(GameState.TESTING_LEVEL_TEASERS);
            theUI().showFlashMessageSec(3, "Level TEST MODE");
        }
    };

    GameAction ACTION_TOGGLE_AUTOPILOT = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_USING_AUTOPILOT);
            boolean auto = PY_USING_AUTOPILOT.get();
            theUI().showFlashMessage(theAssets().text(auto ? "autopilot_on" : "autopilot_off"));
            theSound().playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
        }
    };

    GameAction ACTION_TOGGLE_DEBUG_INFO = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_DEBUG_INFO_VISIBLE);
        }
    };

    GameAction ACTION_TOGGLE_IMMUNITY = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_IMMUNITY);
            theUI().showFlashMessage(theAssets().text(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
            theSound().playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
        }
    };

    GameAction ACTION_TOGGLE_PAUSED = new GameAction() {
        @Override
        public void execute() {
            toggle(theClock().pausedProperty());
            if (theClock().isPaused()) {
                theSound().stopAll();
            }
            Logger.info("Game ({}) {}", theGameController().selectedGameVariant(), theClock().isPaused() ? "paused" : "resumed");
        }
    };

    GameAction ACTION_PERSPECTIVE_NEXT = new GameAction() {
        @Override
        public void execute() {
            PerspectiveID id = PY_3D_PERSPECTIVE.get().next();
            PY_3D_PERSPECTIVE.set(id);
            String msgKey = theAssets().text("camera_perspective", theAssets().text("perspective_id_" + id.name()));
            theUI().showFlashMessage(msgKey);
        }
    };

    GameAction ACTION_PERSPECTIVE_PREVIOUS = new GameAction() {
        @Override
        public void execute() {
            PerspectiveID id = PY_3D_PERSPECTIVE.get().prev();
            PY_3D_PERSPECTIVE.set(id);
            String msgKey = theAssets().text("camera_perspective", theAssets().text("perspective_id_" + id.name()));
            theUI().showFlashMessage(msgKey);
        }
    };

    GameAction ACTION_TOGGLE_DASHBOARD = new GameAction() {
        @Override
        public void execute() {
            theUI().gameView().toggleDashboardVisibility();
        }

        @Override
        public boolean isEnabled() {
            return theUI().currentView().equals(theUI().gameView());
        }
    };

    GameAction ACTION_TOGGLE_DRAW_MODE = new GameAction() {
        @Override
        public void execute() {
            PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    };

    GameAction ACTION_TOGGLE_PLAY_SCENE_2D_3D = new GameAction() {
        @Override
        public void execute() {
            theUI().currentGameScene().ifPresent(gameScene -> {
                toggle(PY_3D_ENABLED);
                if (theUI().currentGameSceneIsPlayScene2D()
                        || theUI().currentGameSceneIsPlayScene3D()) {
                    theUI().updateGameScene(true);
                    theGameController().updateGameState(); //TODO needed?
                }
                if (!theGame().isPlaying()) {
                    theUI().showFlashMessage(theAssets().text(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }

        @Override
        public boolean isEnabled() {
            return isOneOf(theGameState(),
                    GameState.BOOT, GameState.INTRO, GameState.SETTING_OPTIONS, GameState.HUNTING,
                    GameState.TESTING_LEVEL_TEASERS, GameState.TESTING_LEVELS
            );
        }
    };

    GameAction ACTION_TOGGLE_PIP_VISIBILITY = new GameAction() {
        @Override
        public void execute() {
            toggle(PY_PIP_ON);
            if (!theUI().currentGameSceneIsPlayScene3D()) {
                theUI().showFlashMessage(theAssets().text(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };

    // Bindings of game actions to key combinations

    Map<GameAction, Set<KeyCombination>> COMMON_ACTION_BINDINGS = Map.ofEntries(
        createBinding(ACTION_ARCADE_INSERT_COIN,      nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        createBinding(ACTION_ARCADE_START_GAME,       nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        createBinding(ACTION_BOOT_SHOW_GAME_VIEW,     nude(KeyCode.F3)),
        createBinding(ACTION_CHEAT_EAT_ALL_PELLETS,   alt(KeyCode.E)),
        createBinding(ACTION_CHEAT_ADD_LIVES,         alt(KeyCode.L)),
        createBinding(ACTION_CHEAT_ENTER_NEXT_LEVEL,  alt(KeyCode.N)),
        createBinding(ACTION_CHEAT_KILL_GHOSTS,       alt(KeyCode.X)),
        createBinding(ACTION_PERSPECTIVE_PREVIOUS,    alt(KeyCode.LEFT)),
        createBinding(ACTION_PERSPECTIVE_NEXT,        alt(KeyCode.RIGHT)),
        createBinding(ACTION_STEER_UP,                nude(KeyCode.UP), control(KeyCode.UP)),
        createBinding(ACTION_STEER_DOWN,              nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        createBinding(ACTION_STEER_LEFT,              nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        createBinding(ACTION_STEER_RIGHT,             nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        createBinding(ACTION_QUIT_GAME_SCENE,         nude(KeyCode.Q)),
        createBinding(ACTION_SIMULATION_SLOWER,       alt(KeyCode.MINUS)),
        createBinding(ACTION_SIMULATION_FASTER,       alt(KeyCode.PLUS)),
        createBinding(ACTION_SIMULATION_RESET,        alt(KeyCode.DIGIT0)),
        createBinding(ACTION_SIMULATION_ONE_STEP,     shift(KeyCode.P)),
        createBinding(ACTION_SIMULATION_TEN_STEPS,    shift(KeyCode.SPACE)),
        createBinding(ACTION_TEST_CUT_SCENES,         alt(KeyCode.C)),
        createBinding(ACTION_TEST_LEVELS_BONI,        alt(KeyCode.T)),
        createBinding(ACTION_TEST_LEVELS_TEASERS,     alt_shift(KeyCode.T)),
        createBinding(ACTION_TOGGLE_AUTOPILOT,        alt(KeyCode.A)),
        createBinding(ACTION_TOGGLE_DEBUG_INFO,       alt(KeyCode.D)),
        createBinding(ACTION_TOGGLE_PAUSED,           nude(KeyCode.P)),
        createBinding(ACTION_TOGGLE_DASHBOARD,        nude(KeyCode.F1), alt(KeyCode.B)),
        createBinding(ACTION_TOGGLE_IMMUNITY,         alt(KeyCode.I)),
        createBinding(ACTION_TOGGLE_PIP_VISIBILITY,   nude(KeyCode.F2)),
        createBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        createBinding(ACTION_TOGGLE_DRAW_MODE,        alt(KeyCode.W))
    );
}