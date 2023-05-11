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
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Background;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

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
	public static final BooleanProperty showHelpPy        = new SimpleBooleanProperty(false);
	public static final BooleanProperty showDebugInfoPy   = new SimpleBooleanProperty(false);
	public static final IntegerProperty simulationStepsPy = new SimpleIntegerProperty(1);
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

		public final Font arcadeFont;
		public final Font handwritingFont;

		public final ResourceBundle messages;

		public final AudioClip voiceExplainKeys;
		public final AudioClip voiceAutopilotOff;
		public final AudioClip voiceAutopilotOn;
		public final AudioClip voiceImmunityOff;
		public final AudioClip voiceImmunityOn;

		public final Background wallpaper2D;

		public final PacManGameGraphics graphicsPacMan;
		public final MsPacManGameGraphics graphicsMsPacMan;

		public final GameSounds soundsMsPacMan;
		public final GameSounds soundsPacMan;

		public Resources() {

			// Fonts
			arcadeFont = RES.font("fonts/emulogic.ttf", 8);
			handwritingFont = RES.font("fonts/RockSalt-Regular.ttf", 8);

			// Graphics
			wallpaper2D = RES.imageBackground("graphics/pacman_wallpaper_gray.png");

			graphicsMsPacMan = new MsPacManGameGraphics();
			graphicsMsPacMan.icon = RES.image("graphics/icons/mspacman.png");
			graphicsMsPacMan.spritesheet = new Spritesheet(RES.image("graphics/mspacman/sprites.png"), 16);
			graphicsMsPacMan.emptyFlashingMaze = new Image[6];
			for (int i = 0; i < 6; ++i) {
				var maze = graphicsMsPacMan.spritesheet.subImage(228, 248 * i, 226, 248);
				var mazeColors = ArcadeTheme.MS_PACMAN_MAZE_COLORS[i];
				graphicsMsPacMan.emptyFlashingMaze[i] = Ufx.colorsExchanged(maze, Map.of(//
						mazeColors.wallBaseColor(), Color.WHITE, //
						mazeColors.wallTopColor(), Color.BLACK));
			}
			graphicsMsPacMan.logo = RES.image("graphics/mspacman/midway.png");

			graphicsPacMan = new PacManGameGraphics();
			graphicsPacMan.icon = RES.image("graphics/icons/pacman.png");
			graphicsPacMan.spritesheet = new Spritesheet(RES.image("graphics/pacman/sprites.png"), 16);
			graphicsPacMan.fullMaze = RES.image("graphics/pacman/maze_full.png");
			graphicsPacMan.emptyMaze = RES.image("graphics/pacman/maze_empty.png");
			graphicsPacMan.flashingMaze = RES.image("graphics/pacman/maze_empty_flashing.png");

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

			soundsMsPacMan = new GameSounds(audioClipsMsPacman, true);
			soundsPacMan = new GameSounds(audioClipsPacman, true);
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
			case MS_PACMAN -> soundsMsPacMan;
			case PACMAN -> soundsPacMan;
			default -> throw new IllegalGameVariantException(variant);
			};
		}
	}

	public static class Actions {

		private final GameUI2d ui;
		private FadeTransition helpFadingTransition;

		public Actions(GameUI2d ui) {
			this.ui = ui;
		}

		public void toggleHelp() {
			boolean fading = helpFadingTransition != null && helpFadingTransition.getStatus() == Status.RUNNING;
			if (fading) {
				return;
			}
			if (showHelpPy.get()) {
				showHelpPy.set(false);
				return;
			}
			showHelpPy.set(true);
			ui.csHelp.setGameVariant(ui.gameController.game().variant());
			ui.updateContextSensitiveHelp();
			var gameScene = (GameScene2D) ui.currentGameScene;
			gameScene.helpRoot().setOpacity(1);
			helpFadingTransition = new FadeTransition(Duration.seconds(0.5), gameScene.helpRoot());
			helpFadingTransition.setFromValue(1);
			helpFadingTransition.setToValue(0);
			helpFadingTransition.setOnFinished(e -> showHelpPy.set(false));
			helpFadingTransition.setDelay(Duration.seconds(2.0));
			helpFadingTransition.play();
		}

		public void stopVoiceMessage() {
			ui.stopVoice();
		}

		public void showFlashMessage(String message, Object... args) {
			showFlashMessageSeconds(1, message, args);
		}

		public void showFlashMessageSeconds(double seconds, String message, Object... args) {
			ui.flashMessageView().showMessage(String.format(message, args), seconds);
		}

		public void startGame() {
			if (ui.gameController().game().hasCredit()) {
				ui.stopVoice();
				ui.gameController().startPlaying();
			}
		}

		public void startCutscenesTest() {
			ui.gameController().startCutscenesTest();
			showFlashMessage("Cut scenes");
		}

		public void restartIntro() {
			ui.currentGameScene().end();
			GameEvents.setSoundEventsEnabled(true);
			if (ui.gameController().game().isPlaying()) {
				ui.gameController().game().changeCredit(-1);
			}
			ui.gameController().restart(INTRO);
		}

		public void reboot() {
			if (ui.currentGameScene() != null) {
				ui.currentGameScene().end();
			}
			ui.playVoice(Game2d.resources.voiceExplainKeys, 4);
			ui.gameController().restart(GameState.BOOT);
		}

		public void addCredit() {
			GameEvents.setSoundEventsEnabled(true);
			ui.gameController().addCredit();
		}

		public void enterLevel(int newLevelNumber) {
			if (ui.gameController().state() == GameState.CHANGING_TO_NEXT_LEVEL) {
				return;
			}
			ui.gameController().game().level().ifPresent(level -> {
				if (newLevelNumber > level.number()) {
					for (int n = level.number(); n < newLevelNumber - 1; ++n) {
						ui.gameController().game().nextLevel();
					}
					ui.gameController().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
				} else if (newLevelNumber < level.number()) {
					// not implemented
				}
			});
		}

		public void togglePaused() {
			Ufx.toggle(ui.pausedPy);
			// TODO mute and unmute?
			if (ui.pausedPy.get()) {
				resources.gameSounds(ui.gameController().game().variant()).stopAll();
			}
		}

		public void oneSimulationStep() {
			if (ui.pausedPy.get()) {
				ui.executeSingleStep(true);
			}
		}

		public void tenSimulationSteps() {
			if (ui.pausedPy.get()) {
				ui.executeSteps(10, true);
			}
		}

		public void changeSimulationSpeed(int delta) {
			int newFramerate = ui.targetFrameratePy.get() + delta;
			if (newFramerate > 0 && newFramerate < 120) {
				ui.targetFrameratePy.set(newFramerate);
				showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
			}
		}

		public void resetSimulationSpeed() {
			ui.targetFrameratePy.set(GameModel.FPS);
			showFlashMessageSeconds(0.75, "%dHz".formatted(ui.targetFrameratePy.get()));
		}

		public void selectNextGameVariant() {
			var gameVariant = ui.gameController().game().variant().next();
			ui.gameController().selectGameVariant(gameVariant);
			ui.playVoice(Game2d.resources.voiceExplainKeys, 4);
		}

		public void toggleAutopilot() {
			ui.gameController().toggleAutoControlled();
			var auto = ui.gameController().isAutoControlled();
			String message = fmtMessage(resources.messages, auto ? "autopilot_on" : "autopilot_off");
			showFlashMessage(message);
			ui.updateContextSensitiveHelp();
			ui.playVoice(auto ? resources.voiceAutopilotOn : resources.voiceAutopilotOff);
		}

		public void toggleImmunity() {
			ui.gameController().game().setImmune(!ui.gameController().game().isImmune());
			var immune = ui.gameController().game().isImmune();
			String message = fmtMessage(resources.messages, immune ? "player_immunity_on" : "player_immunity_off");
			showFlashMessage(message);
			ui.updateContextSensitiveHelp();
			ui.playVoice(immune ? resources.voiceImmunityOn : resources.voiceImmunityOff);
		}

		public void startLevelTestMode() {
			if (ui.gameController().state() == GameState.INTRO) {
				ui.gameController().restart(GameState.LEVEL_TEST);
				showFlashMessage("Level TEST MODE");
			}
		}

		public void cheatAddLives(int numLives) {
			ui.gameController().game().setLives(numLives + ui.gameController().game().lives());
			showFlashMessage(fmtMessage(resources.messages, "cheat_add_lives", ui.gameController().game().lives()));
		}

		public void cheatEatAllPellets() {
			ui.gameController().cheatEatAllPellets();
		}

		public void cheatEnterNextLevel() {
			ui.gameController().cheatEnterNextLevel();
		}

		public void cheatKillAllEatableGhosts() {
			ui.gameController().cheatKillAllEatableGhosts();
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

		public static final KeyCodeCombination TOGGLE_HELP = just(KeyCode.H);
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
		var settings = new Settings(getParameters().getNamed());

		// Load resources
		long start = System.nanoTime();
		resources = new Resources();
		Logger.info("Loading resources: {} seconds.", (System.nanoTime() - start) / 1e9f);

		ui = new GameUI2d(primaryStage, settings);

		// Some actions operate on on UI, thus must be created after UI
		actions = new Actions(ui);

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

	public static void main(String[] args) {
		launch(args);
	}
}