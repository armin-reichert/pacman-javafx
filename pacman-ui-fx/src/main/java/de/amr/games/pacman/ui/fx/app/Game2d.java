/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;
import static de.amr.games.pacman.ui.fx.util.Ufx.alt;
import static de.amr.games.pacman.ui.fx.util.Ufx.just;
import static de.amr.games.pacman.ui.fx.util.Ufx.shift;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.Spritesheet;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * This is 2D-only version of the Pac-Man and Ms. Pac-Man games.
 * 
 * <p>
 * The application is structured according to the MVC (model-view-controller) design pattern. The model layer consists
 * of the two game models <code> PacManGame</code> and <code> MsPacManGame</code>. The controller is a finite-state
 * machine which is triggered 60 times per second by the game loop. The user interface listens to game events sent from
 * the controller/model layer. The model and controller layers are decoupled from the user interface. This allow to
 * attach different user interfaces without having to change the controller or model.
 * 
 * <p>
 * As a proof of concept I implemented also a (simpler) Swing user interface, see repository
 * <a href="https://github.com/armin-reichert/pacman-ui-swing">Pac-Man Swing UI</a>.
 * 
 * @author Armin Reichert
 */
public class Game2d extends Application {

	public static final ResourceManager RES = new ResourceManager("/de/amr/games/pacman/ui/fx/", Game2d.class);

	//@formatter:off
	public static final ObjectProperty<Color> mainSceneBgColorPy = new SimpleObjectProperty<>(Color.rgb(3,9,66));
	public static final BooleanProperty       showHelpPy         = new SimpleBooleanProperty(false);
	public static final BooleanProperty       showDebugInfoPy    = new SimpleBooleanProperty(false);
	public static final IntegerProperty       simulationStepsPy  = new SimpleIntegerProperty(1);
	//@formatter:on

	public static class Resources {

		public static class PacManGameGraphics {
			public Image icon;
			public Spritesheet spritesheet;
			public Image fullMaze;
			public Image emptyMaze;
			public Image flashingMaze;
		}

		public static class MsPacManGameGraphics {
			public Image icon;
			public Spritesheet spritesheet;
			public Image logo;
			public Image[] emptyFlashingMaze;
		}

		public record Graphics(PacManGameGraphics pacMan, MsPacManGameGraphics msPacMan) {
		}

		public final Font arcadeFont;
		public final Font handwritingFont;

		public final ResourceBundle messages;

		public final AudioClip voiceExplainKeys;
		public final AudioClip voiceAutopilotOff;
		public final AudioClip voiceAutopilotOn;
		public final AudioClip voiceImmunityOff;
		public final AudioClip voiceImmunityOn;

		public final Graphics graphics;

		public final GameSounds gameSoundsMsPacMan;
		public final GameSounds gameSoundsPacMan;

		public Resources() {

			// Fonts
			arcadeFont = RES.font("fonts/emulogic.ttf", 8);
			handwritingFont = RES.font("fonts/RockSalt-Regular.ttf", 8);

			// Graphics
			var gmpm = new MsPacManGameGraphics();
			gmpm.icon = RES.image("graphics/icons/mspacman.png");
			gmpm.spritesheet = new Spritesheet(RES.image("graphics/mspacman/sprites.png"), 16);
			gmpm.emptyFlashingMaze = new Image[6];
			for (int i = 0; i < 6; ++i) {
				var maze = gmpm.spritesheet.subImage(228, 248 * i, 226, 248);
				var mazeColors = ArcadeTheme.MS_PACMAN_MAZE_COLORS[i];
				gmpm.emptyFlashingMaze[i] = Ufx.colorsExchanged(maze, Map.of(//
						mazeColors.wallBaseColor(), Color.WHITE, //
						mazeColors.wallTopColor(), Color.BLACK));
			}
			gmpm.logo = RES.image("graphics/mspacman/midway.png");

			var gpm = new PacManGameGraphics();
			gpm.icon = RES.image("graphics/icons/pacman.png");
			gpm.spritesheet = new Spritesheet(RES.image("graphics/pacman/sprites.png"), 16);
			gpm.fullMaze = RES.image("graphics/pacman/maze_full.png");
			gpm.emptyMaze = RES.image("graphics/pacman/maze_empty.png");
			gpm.flashingMaze = RES.image("graphics/pacman/maze_empty_flashing.png");

			graphics = new Graphics(gpm, gmpm);

			// Texts
			messages = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.texts.messages");

			// Sound
			voiceExplainKeys = RES.audioClip("sound/voice/press-key.mp3");
			voiceAutopilotOff = RES.audioClip("sound/voice/autopilot-off.mp3");
			voiceAutopilotOn = RES.audioClip("sound/voice/autopilot-on.mp3");
			voiceImmunityOff = RES.audioClip("sound/voice/immunity-off.mp3");
			voiceImmunityOn = RES.audioClip("sound/voice/immunity-on.mp3");

			//@formatter:off
			Object[][] audioClipsMsPacman = { 
				{ AudioClipID.BONUS_EATEN,     "sound/mspacman/Fruit.mp3", 1.0 }, 
				{ AudioClipID.CREDIT,          "sound/mspacman/Credit.mp3", 1.0 }, 
				{ AudioClipID.EXTRA_LIFE,      "sound/mspacman/ExtraLife.mp3", 1.0 }, 
				{ AudioClipID.GAME_READY,      "sound/mspacman/Start.mp3", 1.0 }, 
				{ AudioClipID.GAME_OVER,       "sound/common/game-over.mp3", 1.0 }, 
				{ AudioClipID.GHOST_EATEN,     "sound/mspacman/Ghost.mp3", 1.0 }, 
				{ AudioClipID.GHOST_RETURNING, "sound/mspacman/GhostEyes.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_1,  "sound/mspacman/Act1TheyMeet.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_2,  "sound/mspacman/Act2TheChase.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_3,  "sound/mspacman/Act3Junior.mp3", 1.0 }, 
				{ AudioClipID.LEVEL_COMPLETE,  "sound/common/level-complete.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_DEATH,    "sound/mspacman/Died.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_MUNCH,    "sound/mspacman/Pill.wav", 1.0 }, 
				{ AudioClipID.PACMAN_POWER,    "sound/mspacman/ScaredGhost.mp3", 1.0 }, 
				{ AudioClipID.SIREN_1,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SIREN_2,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
				{ AudioClipID.SIREN_3,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
				{ AudioClipID.SIREN_4,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
				{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
			};

			Object[][] audioClipsPacman = { 
				{ AudioClipID.BONUS_EATEN,     "sound/pacman/eat_fruit.mp3", 1.0 }, 
				{ AudioClipID.CREDIT,          "sound/pacman/credit.wav", 1.0 }, 
				{ AudioClipID.EXTRA_LIFE,      "sound/pacman/extend.mp3", 1.0 }, 
				{ AudioClipID.GAME_READY,      "sound/pacman/game_start.mp3", 1.0 }, 
				{ AudioClipID.GAME_OVER,       "sound/common/game-over.mp3", 1.0 }, 
				{ AudioClipID.GHOST_EATEN,     "sound/pacman/eat_ghost.mp3", 1.0 }, 
				{ AudioClipID.GHOST_RETURNING, "sound/pacman/retreating.mp3", 1.0 }, 
				{ AudioClipID.INTERMISSION_1,  "sound/pacman/intermission.mp3", 1.0 }, 
				{ AudioClipID.LEVEL_COMPLETE,  "sound/common/level-complete.mp3", 1.0 }, 
				{ AudioClipID.PACMAN_DEATH,    "sound/pacman/pacman_death.wav", 1.0 }, 
				{ AudioClipID.PACMAN_MUNCH,    "sound/pacman/doublemunch.wav", 1.0 }, 
				{ AudioClipID.PACMAN_POWER,    "sound/pacman/ghost-turn-to-blue.mp3", 1.0 }, 
				{ AudioClipID.SIREN_1,         "sound/pacman/siren_1.mp3", 1.0 }, 
				{ AudioClipID.SIREN_2,         "sound/pacman/siren_2.mp3", 1.0 }, 
				{ AudioClipID.SIREN_3,         "sound/pacman/siren_3.mp3", 1.0 }, 
				{ AudioClipID.SIREN_4,         "sound/pacman/siren_4.mp3", 1.0 }, 
				{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
			};
			//@formatter:on

			gameSoundsMsPacMan = new GameSounds(audioClipsMsPacman, true);
			gameSoundsPacMan = new GameSounds(audioClipsPacman, true);
		}

		public Font font(Font font, double size) {
			return Font.font(font.getFamily(), size);
		}

		/**
		 * @param variant game variant
		 * @return text displayed in READY state
		 */
		public String randomReadyText(GameVariant variant) {
			return "READY!";
		}

		public GameSounds gameSounds(GameVariant variant) {
			return switch (variant) {
			case MS_PACMAN -> gameSoundsMsPacMan;
			case PACMAN -> gameSoundsPacMan;
			default -> throw new IllegalGameVariantException(variant);
			};
		}
	}

	public static class Actions {

		private final ActionContext context;

		public Actions(ActionContext context) {
			this.context = context;
		}

		public void toggleHelp() {
			Ufx.toggle(Game2d.showHelpPy);
			context.ui().updateContextSensitiveHelp();
		}

		public void stopVoiceMessage() {
			context.ui().stopVoiceMessage();
		}

		public void showFlashMessage(String message, Object... args) {
			showFlashMessageSeconds(1, message, args);
		}

		public void showFlashMessageSeconds(double seconds, String message, Object... args) {
			context.ui().flashMessageView().showMessage(String.format(message, args), seconds);
		}

		public void startGame() {
			if (context.game().hasCredit()) {
				context.ui().stopVoiceMessage();
				context.gameController().startPlaying();
			}
		}

		public void startCutscenesTest() {
			context.gameController().startCutscenesTest();
			showFlashMessage("Cut scenes");
		}

		public void restartIntro() {
			context.ui().currentGameScene().end();
			GameEvents.setSoundEventsEnabled(true);
			if (context.game().isPlaying()) {
				context.game().changeCredit(-1);
			}
			context.gameController().restart(INTRO);
		}

		public void reboot() {
			if (context.ui().currentGameScene() != null) {
				context.ui().currentGameScene().end();
			}
			context.ui().playHelpVoiceMessageAfterSeconds(4);
			context.gameController().restart(GameState.BOOT);
		}

		public void addCredit() {
			GameEvents.setSoundEventsEnabled(true);
			context.gameController().addCredit();
		}

		public void enterLevel(int newLevelNumber) {
			if (context.gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
				return;
			}
			context.game().level().ifPresent(level -> {
				if (newLevelNumber > level.number()) {
					for (int n = level.number(); n < newLevelNumber - 1; ++n) {
						context.game().nextLevel();
					}
					context.gameController().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
				} else if (newLevelNumber < level.number()) {
					// not implemented
				}
			});
		}

		public void togglePaused() {
			Ufx.toggle(context.ui().pausedPy);
			// TODO mute and unmute?
			if (context.ui().pausedPy.get()) {
				resources.gameSounds(context.game().variant()).stopAll();
			}
		}

		public void oneSimulationStep() {
			if (context.ui().pausedPy.get()) {
				context.ui().executeSingleStep(true);
			}
		}

		public void tenSimulationSteps() {
			if (context.ui().pausedPy.get()) {
				context.ui().executeSteps(10, true);
			}
		}

		public void changeSimulationSpeed(int delta) {
			int newFramerate = context.ui().targetFrameratePy.get() + delta;
			if (newFramerate > 0 && newFramerate < 120) {
				context.ui().targetFrameratePy.set(newFramerate);
				showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
			}
		}

		public void resetSimulationSpeed() {
			context.ui().targetFrameratePy.set(GameModel.FPS);
			showFlashMessageSeconds(0.75, "%dHz".formatted(context.ui().targetFrameratePy.get()));
		}

		public void selectNextGameVariant() {
			var gameVariant = context.game().variant().next();
			context.gameController().selectGameVariant(gameVariant);
			context.ui().playHelpVoiceMessageAfterSeconds(4);
		}

		public void toggleAutopilot() {
			context.gameController().toggleAutoControlled();
			var auto = context.gameController().isAutoControlled();
			String message = fmtMessage(resources.messages, auto ? "autopilot_on" : "autopilot_off");
			showFlashMessage(message);
			context.ui().updateContextSensitiveHelp();
			context.ui().playVoiceMessage(auto ? resources.voiceAutopilotOn : resources.voiceAutopilotOff);
		}

		public void toggleImmunity() {
			context.game().setImmune(!context.game().isImmune());
			var immune = context.game().isImmune();
			String message = fmtMessage(resources.messages, immune ? "player_immunity_on" : "player_immunity_off");
			showFlashMessage(message);
			context.ui().updateContextSensitiveHelp();
			context.ui().playVoiceMessage(immune ? resources.voiceImmunityOn : resources.voiceImmunityOff);
		}

		public void startLevelTestMode() {
			if (context.gameState() == GameState.INTRO) {
				context.gameController().restart(GameState.LEVEL_TEST);
				showFlashMessage("Level TEST MODE");
			}
		}

		public void cheatAddLives(int numLives) {
			context.game().setLives(numLives + context.game().lives());
			showFlashMessage(fmtMessage(resources.messages, "cheat_add_lives", context.game().lives()));
		}

		public void cheatEatAllPellets() {
			context.gameController().cheatEatAllPellets();
		}

		public void cheatEnterNextLevel() {
			context.gameController().cheatEnterNextLevel();
		}

		public void cheatKillAllEatableGhosts() {
			context.gameController().cheatKillAllEatableGhosts();
		}
	}

	public static class Keys {

		public static final KeyCodeCombination CHEAT_EAT_ALL = alt(KeyCode.E);
		public static final KeyCodeCombination CHEAT_ADD_LIVES = alt(KeyCode.L);
		public static final KeyCodeCombination CHEAT_NEXT_LEVEL = alt(KeyCode.N);
		public static final KeyCodeCombination CHEAT_KILL_GHOSTS = alt(KeyCode.X);

		public static final KeyCodeCombination AUTOPILOT = alt(KeyCode.A);
		public static final KeyCodeCombination DEBUG_INFO = alt(KeyCode.D);
		public static final KeyCodeCombination IMMUNITIY = alt(KeyCode.I);
		public static final KeyCodeCombination MUTE = alt(KeyCode.M);

		public static final KeyCodeCombination PAUSE = just(KeyCode.P);
		public static final KeyCodeCombination PAUSE_STEP = shift(KeyCode.P);
		public static final KeyCodeCombination SINGLE_STEP = just(KeyCode.SPACE);
		public static final KeyCodeCombination TEN_STEPS = shift(KeyCode.SPACE);
		public static final KeyCodeCombination SIMULATION_FASTER = alt(KeyCode.PLUS);
		public static final KeyCodeCombination SIMULATION_SLOWER = alt(KeyCode.MINUS);
		public static final KeyCodeCombination SIMULATION_NORMAL = alt(KeyCode.DIGIT0);

		public static final KeyCodeCombination START_GAME = just(KeyCode.DIGIT1);
		public static final KeyCodeCombination ADD_CREDIT = just(KeyCode.DIGIT5);

		public static final KeyCodeCombination QUIT = just(KeyCode.Q);
		public static final KeyCodeCombination TEST_LEVELS = alt(KeyCode.T);
		public static final KeyCodeCombination SELECT_VARIANT = just(KeyCode.V);
		public static final KeyCodeCombination PLAY_CUTSCENES = alt(KeyCode.Z);

		public static final KeyCodeCombination TOGGLE_HELP = just(KeyCode.F1);
		public static final KeyCodeCombination BOOT = just(KeyCode.F3);
		public static final KeyCodeCombination FULLSCREEN = just(KeyCode.F11);
	}

	/**
	 * Static access to actions of 2D game.
	 */
	public static Actions actions;

	/**
	 * Static access to resources of 2D game.
	 */
	public static Resources resources;

	private GameUI2d ui;

	@Override
	public void start(Stage primaryStage) throws IOException {

		// Convert command-line arguments (if any) into application settings
		var settings = new Settings(getParameters() != null ? getParameters().getNamed() : Collections.emptyMap());

		// Load resources
		long start = System.nanoTime();
		resources = new Resources();
		Logger.info("Loading resources: {} seconds.", (System.nanoTime() - start) / 1e9f);

		ui = new GameUI2d(primaryStage, settings);

		// Some actions operate on on UI, thus must be created after UI
		actions = new Actions(new ActionContext(ui));

		// Initialize game state and start game clock
		actions.reboot();
		ui.start();

		Logger.info("Game started. Locale: {} Clock speed: {} Hz Settings: {}", Locale.getDefault(),
				ui.targetFrameratePy.get(), settings);
	}

	@Override
	public void stop() throws Exception {
		ui.stop();
		Logger.info("Game stopped");
	}
}