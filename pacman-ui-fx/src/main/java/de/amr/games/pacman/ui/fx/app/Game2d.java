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
import static de.amr.games.pacman.lib.Globals.RND;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.rendering2d.GhostColoring;
import de.amr.games.pacman.ui.fx.rendering2d.MazeColoring;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManColoring;
import de.amr.games.pacman.ui.fx.rendering2d.PacManColoring;
import de.amr.games.pacman.ui.fx.rendering2d.Spritesheet;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceMgr;
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
import javafx.scene.input.KeyCombination;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * This is the entry point of the Pac-Man and Ms. Pac-Man games.
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

	public static class Properties {
	//@formatter:off
		public static final ObjectProperty<Color> mainSceneBgColorPy       = new SimpleObjectProperty<>(Color.web("0x334bd3"));
		public static final BooleanProperty       showDebugInfoPy          = new SimpleBooleanProperty(false);
		public static final IntegerProperty       simulationStepsPy        = new SimpleIntegerProperty(1);
	//@formatter:on
	}

	public static final ResourceMgr ResMgr = new ResourceMgr("/de/amr/games/pacman/ui/fx/assets/",
			Game2d.class::getResource);

	public static void loadResources() {
		long start = System.nanoTime();
		runAndMeasureTime("graphics", Graphics::load);
		runAndMeasureTime("sounds", Sounds::load);
		runAndMeasureTime("fonts", Fonts::load);
		runAndMeasureTime("texts", Texts::load);
		Logger.info("Loading application resources took {} seconds.", (System.nanoTime() - start) / 1e9f);
	}

	private static void runAndMeasureTime(String section, Runnable loadingCode) {
		long start = System.nanoTime();
		loadingCode.run();
		Logger.info("Loading {} done ({} seconds).", section, (System.nanoTime() - start) / 1e9f);
	}

	public static class ArcadeTheme {

		public static final Color RED = Color.rgb(255, 0, 0);
		public static final Color YELLOW = Color.rgb(255, 255, 0);
		public static final Color PINK = Color.rgb(252, 181, 255);
		public static final Color CYAN = Color.rgb(0, 255, 255);
		public static final Color ORANGE = Color.rgb(251, 190, 88);
		public static final Color BLACK = Color.rgb(0, 0, 0);
		public static final Color BLUE = Color.rgb(33, 33, 255);
		public static final Color PALE = Color.rgb(222, 222, 255);
		public static final Color ROSE = Color.rgb(252, 187, 179);

		//@formatter:off
		public static final MazeColoring PACMAN_MAZE_COLORS = new MazeColoring(//
			Color.rgb(254, 189, 180), // food color
			Color.rgb(33, 33, 255).darker(), // wall top color
			Color.rgb(33, 33, 255).brighter(), // wall side color
			Color.rgb(252, 181, 255) // ghosthouse door color
		);

		public static final MazeColoring[] MS_PACMAN_MAZE_COLORS = {
			new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(255, 255, 0),   Color.rgb( 71, 183, 255),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(255,   0, 0),   Color.rgb(222, 151,  81),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(222, 222, 255), Color.rgb( 33,  33, 255),  Color.rgb(255, 183,  81), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(0,   255, 255), Color.rgb(255, 183, 255),  Color.rgb(255, 255,   0), Color.rgb(255, 183, 255)),
			new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
		};

		public static final PacManColoring PACMAN_COLORS = new PacManColoring(
			Color.rgb(255, 255, 0), // head
			Color.rgb(191, 79, 61), // palate
			Color.rgb(33, 33, 33)   // eyes
		);

		public static final MsPacManColoring MS_PACMAN_COLORS = new MsPacManColoring(
			Color.rgb(255, 255, 0), // head
			Color.rgb(191, 79, 61), // palate
			Color.rgb(33, 33, 33),  // eyes
			Color.rgb(255, 0, 0),   // hair bow
			Color.rgb(33, 33, 255)  // hair bow pearls
		);
		
		public static final GhostColoring[] GHOST_COLORS = {
			new GhostColoring(
				RED,  PALE, BLUE, // normal
				BLUE, ROSE, ROSE, // frightened
				PALE, ROSE, RED   // flashing
			),
			new GhostColoring(
				PINK, PALE, BLUE, // normal
				BLUE, ROSE, ROSE, // frightened
				PALE, ROSE, RED   // flashing
			),
			new GhostColoring(
				CYAN, PALE, BLUE, // normal
				BLUE, ROSE, ROSE, // frightened
				PALE, ROSE, RED   // flashing
			),
			new GhostColoring(
				ORANGE, PALE, BLUE, // normal
				BLUE,   ROSE, ROSE, // frightened
				PALE,   ROSE, RED   // flashing
			)
		};
		//@formatter:on
	}

	public static class Fonts {

		public static Font arcade;
		public static Font handwriting;

		static void load() {
			arcade = ResMgr.font("fonts/emulogic.ttf", 8);
			handwriting = ResMgr.font("fonts/RockSalt-Regular.ttf", 8);
		}

		public static Font pt(Font font, double size) {
			return Font.font(font.getFamily(), size);
		}

	}

	public static class Texts {

		private static ResourceBundle messageBundle;
		private static Picker<String> messagePickerCheating;
		private static Picker<String> messagePickerLevelComplete;
		private static Picker<String> messagePickerGameOver;

		static void load() {
			messageBundle = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.assets.texts.messages");
			messagePickerCheating = ResMgr.createPicker(messageBundle, "cheating");
			messagePickerLevelComplete = ResMgr.createPicker(messageBundle, "level.complete");
			messagePickerGameOver = ResMgr.createPicker(messageBundle, "game.over");
		}

		/**
		 * Builds a resource key from the given key pattern and arguments and reads the corresponding message from the
		 * messages resource bundle.
		 * 
		 * @param keyPattern message key pattern
		 * @param args       arguments merged into key pattern
		 * @return message text for composed key or string indicating missing text
		 */
		public static String message(String keyPattern, Object... args) {
			try {
				var pattern = messageBundle.getString(keyPattern);
				return MessageFormat.format(pattern, args);
			} catch (Exception x) {
				Logger.error("No text resource found for key '{}'", keyPattern);
				return "missing{%s}".formatted(keyPattern);
			}
		}

		public static String pickCheatingMessage() {
			return messagePickerCheating.next();
		}

		public static String pickGameOverMessage() {
			return messagePickerGameOver.next();
		}

		public static String pickLevelCompleteMessage(int levelNumber) {
			return "%s%n%n%s".formatted(messagePickerLevelComplete.next(), message("level_complete", levelNumber));
		}

		public static String randomReadyText(GameVariant variant) {
			return "READY!";
		}
	}

	public static class Graphics {

		public static class PacManGame {
			public static Image icon;
			public static Spritesheet spritesheet;
			public static Image fullMaze;
			public static Image emptyMaze;
			public static Image flashingMaze;
		}

		public static class MsPacManGame {
			public static Image icon;
			public static Spritesheet spritesheet;
			public static Image logo;
			public static Image[] emptyFlashingMaze;

			private static Image emptyMaze(int i) {
				return spritesheet.subImage(228, 248 * i, 226, 248);
			}

			private static Image emptyMazeFlashing(int i) {
				return Ufx.colorsExchanged(emptyMaze(i), Map.of(//
						ArcadeTheme.MS_PACMAN_MAZE_COLORS[i].wallBaseColor(), Color.WHITE, //
						ArcadeTheme.MS_PACMAN_MAZE_COLORS[i].wallTopColor(), Color.BLACK));
			}
		}

		static void load() {
			PacManGame.icon = ResMgr.image("graphics/icons/pacman.png");
			PacManGame.spritesheet = new Spritesheet(ResMgr.image("graphics/pacman/sprites.png"), 16);
			PacManGame.fullMaze = ResMgr.image("graphics/pacman/maze_full.png");
			PacManGame.emptyMaze = ResMgr.image("graphics/pacman/maze_empty.png");
			PacManGame.flashingMaze = ResMgr.image("graphics/pacman/maze_empty_flashing.png");

			MsPacManGame.icon = ResMgr.image("graphics/icons/mspacman.png");
			MsPacManGame.spritesheet = new Spritesheet(ResMgr.image("graphics/mspacman/sprites.png"), 16);
			MsPacManGame.emptyFlashingMaze = IntStream.range(0, 6).mapToObj(MsPacManGame::emptyMazeFlashing)
					.toArray(Image[]::new);
			MsPacManGame.logo = ResMgr.image("graphics/mspacman/midway.png");
		}
	}

	public static class Sounds {

		public static final AudioClip VOICE_HELP = ResMgr.audioClip("sound/voice/press-key.mp3");
		public static final AudioClip VOICE_AUTOPILOT_OFF = ResMgr.audioClip("sound/voice/autopilot-off.mp3");
		public static final AudioClip VOICE_AUTOPILOT_ON = ResMgr.audioClip("sound/voice/autopilot-on.mp3");
		public static final AudioClip VOICE_IMMUNITY_OFF = ResMgr.audioClip("sound/voice/immunity-off.mp3");
		public static final AudioClip VOICE_IMMUNITY_ON = ResMgr.audioClip("sound/voice/immunity-on.mp3");

		//@formatter:off
		private static final Object[][] MS_PACMAN_AUDIO_CLIP_PATHS = { 
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
				{ AudioClipID.SIREN_2,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SIREN_3,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SIREN_4,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
				{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
		};

		private static final Object[][] PACMAN_AUDIO_CLIP_PATHS = { 
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

		private static GameSounds gameSoundsMsPacMan;
		private static GameSounds gameSoundsPacMan;

		static void load() {
			gameSoundsMsPacMan = new GameSounds(MS_PACMAN_AUDIO_CLIP_PATHS, true);
			gameSoundsPacMan = new GameSounds(PACMAN_AUDIO_CLIP_PATHS, true);
		}

		public static GameSounds gameSounds(GameVariant variant) {
			return switch (variant) {
			case MS_PACMAN -> gameSoundsMsPacMan;
			case PACMAN -> gameSoundsPacMan;
			default -> throw new IllegalGameVariantException(variant);
			};
		}
	}

	public static class Actions {

		private static ActionContext context;
		private static AudioClip currentVoiceMessage;

		public static void setContext(ActionContext context) {
			Actions.context = context;
		}

		public static void playHelpVoiceMessageAfterSeconds(int seconds) {
			Ufx.afterSeconds(seconds, () -> playVoiceMessage(Game2d.Sounds.VOICE_HELP)).play();
		}

		public static void playVoiceMessage(AudioClip voiceMessage) {
			if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
				return; // don't interrupt voice message still playing, maybe enqueue?
			}
			currentVoiceMessage = voiceMessage;
			currentVoiceMessage.play();
		}

		public static void stopVoiceMessage() {
			if (currentVoiceMessage != null) {
				currentVoiceMessage.stop();
			}
		}

		public static void showFlashMessage(String message, Object... args) {
			showFlashMessageSeconds(1, message, args);
		}

		public static void showFlashMessageSeconds(double seconds, String message, Object... args) {
			context.flashMessageView().showMessage(String.format(message, args), seconds);
		}

		public static void startGame() {
			if (context.game().hasCredit()) {
				stopVoiceMessage();
				context.gameController().startPlaying();
			}
		}

		public static void startCutscenesTest() {
			context.gameController().startCutscenesTest();
			showFlashMessage("Cut scenes");
		}

		public static void restartIntro() {
			context.currentGameScene().end();
			GameEvents.setSoundEventsEnabled(true);
			if (context.game().isPlaying()) {
				context.game().changeCredit(-1);
			}
			context.gameController().restart(INTRO);
		}

		public static void reboot() {
			if (context.currentGameScene() != null) {
				context.currentGameScene().end();
			}
			playHelpVoiceMessageAfterSeconds(4);
			context.gameController().restart(GameState.BOOT);
		}

		public static void addCredit() {
			GameEvents.setSoundEventsEnabled(true);
			context.gameController().addCredit();
		}

		public static void enterLevel(int newLevelNumber) {
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

		public static void togglePaused() {
			Ufx.toggle(context.gameLoop().pausedPy);
			// TODO mute and unmute?
			if (context.gameLoop().pausedPy.get()) {
				Game2d.Sounds.gameSounds(context.game().variant()).stopAll();
			}
		}

		public static void oneSimulationStep() {
			if (context.gameLoop().pausedPy.get()) {
				context.gameLoop().executeSingleStep(true);
			}
		}

		public static void tenSimulationSteps() {
			if (context.gameLoop().pausedPy.get()) {
				context.gameLoop().executeSteps(10, true);
			}
		}

		public static void changeSimulationSpeed(int delta) {
			int newFramerate = context.gameLoop().targetFrameratePy.get() + delta;
			if (newFramerate > 0 && newFramerate < 120) {
				context.gameLoop().targetFrameratePy.set(newFramerate);
				showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
			}
		}

		public static void resetSimulationSpeed() {
			context.gameLoop().targetFrameratePy.set(GameModel.FPS);
			showFlashMessageSeconds(0.75, "%dHz".formatted(context.gameLoop().targetFrameratePy.get()));
		}

		public static void selectNextGameVariant() {
			var gameVariant = context.game().variant().next();
			context.gameController().selectGameVariant(gameVariant);
			playHelpVoiceMessageAfterSeconds(4);
		}

		public static void toggleAutopilot() {
			context.gameController().toggleAutoControlled();
			var auto = context.gameController().isAutoControlled();
			String message = Game2d.Texts.message(auto ? "autopilot_on" : "autopilot_off");
			showFlashMessage(message);
			playVoiceMessage(auto ? Game2d.Sounds.VOICE_AUTOPILOT_ON : Game2d.Sounds.VOICE_AUTOPILOT_OFF);
		}

		public static void toggleImmunity() {
			context.game().setImmune(!context.game().isImmune());
			var immune = context.game().isImmune();
			String message = Game2d.Texts.message(immune ? "player_immunity_on" : "player_immunity_off");
			showFlashMessage(message);
			playVoiceMessage(immune ? Game2d.Sounds.VOICE_IMMUNITY_ON : Game2d.Sounds.VOICE_IMMUNITY_OFF);
		}

		public static void startLevelTestMode() {
			if (context.gameState() == GameState.INTRO) {
				context.gameController().restart(GameState.LEVEL_TEST);
				showFlashMessage("Level TEST MODE");
			}
		}

		public static void cheatAddLives(int numLives) {
			context.game().setLives(numLives + context.game().lives());
			showFlashMessage(Game2d.Texts.message("cheat_add_lives", context.game().lives()));
		}

		public static void cheatEatAllPellets() {
			context.gameController().cheatEatAllPellets();
			if (RND.nextDouble() < 0.1) {
				showFlashMessage(Game2d.Texts.pickCheatingMessage());
			}
		}

		public static void cheatEnterNextLevel() {
			context.gameController().cheatEnterNextLevel();
		}

		public static void cheatKillAllEatableGhosts() {
			context.gameController().cheatKillAllEatableGhosts();
			if (RND.nextDouble() < 0.1) {
				showFlashMessage(Game2d.Texts.pickCheatingMessage());
			}
		}
	}

	public static class Keys {

		protected static KeyCodeCombination just(KeyCode code) {
			return new KeyCodeCombination(code);
		}

		protected static KeyCodeCombination alt(KeyCode code) {
			return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
		}

		protected static KeyCodeCombination shift(KeyCode code) {
			return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
		}

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

		public static final KeyCodeCombination BOOT = just(KeyCode.F3);
		public static final KeyCodeCombination FULLSCREEN = just(KeyCode.F11);
	}

	// main

	public static void main(String[] args) {
		launch(args);
	}

	// end of static stuff

	private GameUI2d ui;

	@Override
	public void init() throws Exception {
		loadResources();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		var settings = new Settings(getParameters() != null ? getParameters().getNamed() : Collections.emptyMap());
		var gameController = new GameController(settings.variant);
		ui = new GameUI2d(primaryStage, settings, gameController);
		ui.targetFrameratePy.set(60);
		Actions.setContext(new ActionContext(ui, gameController, ui::currentGameScene, ui.flashMessageView()));
		Actions.reboot();
		ui.start();
		Logger.info("Game started. Locale: {} Framerate: {} Hz Settings: {}", Locale.getDefault(),
				ui.targetFrameratePy.get(), settings);
	}

	@Override
	public void stop() throws Exception {
		ui.stop();
		Logger.info("Game stopped");
	}

}