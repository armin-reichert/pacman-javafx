/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.MsPacManSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManSpriteSheet;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.fx.input.Keyboard.*;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application implements ResourceManager {
	public static final ResourceBundle TEXTS = ResourceBundle.getBundle(
			"de.amr.games.pacman.ui.fx.texts.messages",
			PacManGames2dApp.class.getModule());

	public static final int CANVAS_WIDTH_UNSCALED  = GameModel.TILES_X * Globals.TS; // 224
	public static final int CANVAS_HEIGHT_UNSCALED = GameModel.TILES_Y * Globals.TS; // 288

	public static final BooleanProperty PY_SHOW_DEBUG_INFO    = new SimpleBooleanProperty(false);

	public static final KeyCodeCombination KEY_CHEAT_EAT_ALL     = alt(KeyCode.E);
	public static final KeyCodeCombination KEY_CHEAT_ADD_LIVES   = alt(KeyCode.L);
	public static final KeyCodeCombination KEY_CHEAT_NEXT_LEVEL  = alt(KeyCode.N);
	public static final KeyCodeCombination KEY_CHEAT_KILL_GHOSTS = alt(KeyCode.X);

	public static final KeyCodeCombination KEY_AUTOPILOT         = alt(KeyCode.A);
	public static final KeyCodeCombination KEY_DEBUG_INFO        = alt(KeyCode.D);
	public static final KeyCodeCombination KEY_IMMUNITY          = alt(KeyCode.I);

	public static final KeyCodeCombination KEY_PAUSE             = just(KeyCode.P);
	public static final KeyCodeCombination[] KEYS_SINGLE_STEP    = { just(KeyCode.SPACE), shift(KeyCode.P) };
	public static final KeyCodeCombination KEY_TEN_STEPS         = shift(KeyCode.SPACE);
	public static final KeyCodeCombination KEY_SIMULATION_FASTER = alt(KeyCode.PLUS);
	public static final KeyCodeCombination KEY_SIMULATION_SLOWER = alt(KeyCode.MINUS);
	public static final KeyCodeCombination KEY_SIMULATION_NORMAL = alt(KeyCode.DIGIT0);

	public static final KeyCodeCombination[] KEYS_START_GAME     = { just(KeyCode.DIGIT1), just(KeyCode.NUMPAD1) };
	public static final KeyCodeCombination[] KEYS_ADD_CREDIT     = { just(KeyCode.DIGIT5), just(KeyCode.NUMPAD5) };

	public static final KeyCodeCombination KEY_QUIT              = just(KeyCode.Q);
	public static final KeyCodeCombination KEY_TEST_LEVELS       = alt(KeyCode.T);
	public static final KeyCodeCombination KEY_SELECT_VARIANT    = just(KeyCode.V);
	public static final KeyCodeCombination KEY_PLAY_CUTSCENES    = alt(KeyCode.C);

	public static final KeyCodeCombination KEY_SHOW_HELP         = just(KeyCode.H);
	public static final KeyCodeCombination KEY_BOOT              = just(KeyCode.F3);
	public static final KeyCodeCombination KEY_FULLSCREEN        = just(KeyCode.F11);

	private final Settings settings = new Settings();
	private final Theme theme = new Theme();
	private PacManGames2dUI ui;

	@Override
	public void init() {
		if (getParameters() != null) {
			settings.merge(getParameters().getNamed());
		}
		GameController.create(settings.variant);
		Logger.info("Game initialized: {}", settings);
	}

	@Override
	public void start(Stage stage) {
		populateTheme(theme);
		Logger.info("Theme created: {}", theme);
		ui = new PacManGames2dUI(stage, settings, theme);
		GameController.it().addListener(ui);
		ui.showStartPage();
		Logger.info("UI initialized. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
	}

	@Override
	public void stop() {
		ui.gameClock().stop();
		Logger.info("Game stopped.");
	}

	public void populateTheme(Theme theme) {
		//
		// Common to both games
		//
		theme.set("canvas.background",               ArcadePalette.BLACK);

		theme.set("ghost.0.color",                   ArcadePalette.RED);
		theme.set("ghost.1.color",                   ArcadePalette.PINK);
		theme.set("ghost.2.color",                   ArcadePalette.CYAN);
		theme.set("ghost.3.color",                   ArcadePalette.ORANGE);

		theme.set("startpage.button.bgColor",        Color.rgb(0, 155, 252, 0.8));
		theme.set("startpage.button.color",          Color.WHITE);
		theme.set("startpage.button.font",           font("fonts/emulogic.ttf", 30));

		theme.set("wallpaper.background",            imageBackground("graphics/pacman_wallpaper.png"));
		theme.set("wallpaper.color",                 Color.rgb(72, 78, 135));

		theme.set("font.arcade",                     font("fonts/emulogic.ttf", 8));
		theme.set("font.handwriting",                font("fonts/Molle-Italic.ttf", 9));
		theme.set("font.monospaced",                 font("fonts/Inconsolata_Condensed-Bold.ttf", 12));

		theme.set("voice.explain",                   audioClip("sound/voice/press-key.mp3"));
		theme.set("voice.autopilot.off",             audioClip("sound/voice/autopilot-off.mp3"));
		theme.set("voice.autopilot.on",              audioClip("sound/voice/autopilot-on.mp3"));
		theme.set("voice.immunity.off",              audioClip("sound/voice/immunity-off.mp3"));
		theme.set("voice.immunity.on",               audioClip("sound/voice/immunity-on.mp3"));

		//
		// Ms. Pac-Man game
		//
		theme.set("mspacman.startpage.image",        image("graphics/mspacman/wallpaper-midway.png"));
		theme.set("mspacman.helpButton.icon",        image("graphics/icons/help-red-64.png"));

		theme.set("mspacman.spritesheet",            new MsPacManSpriteSheet(image("graphics/mspacman/sprites.png")));
		theme.set("mspacman.icon",                   image("graphics/icons/mspacman.png"));
		theme.set("mspacman.logo.midway",            image("graphics/mspacman/midway.png"));
		theme.set("mspacman.flashingMazes",          image("graphics/mspacman/mazes-flashing.png"));

		theme.set("mspacman.audio.bonus_eaten",      audioClip("sound/mspacman/Fruit.mp3"));
		theme.set("mspacman.audio.credit",           audioClip("sound/mspacman/Credit.mp3"));
		theme.set("mspacman.audio.extra_life",       audioClip("sound/mspacman/ExtraLife.mp3"));
		theme.set("mspacman.audio.game_ready",       audioClip("sound/mspacman/Start.mp3"));
		theme.set("mspacman.audio.game_over",        audioClip("sound/common/game-over.mp3"));
		theme.set("mspacman.audio.ghost_eaten",      audioClip("sound/mspacman/Ghost.mp3"));
		theme.set("mspacman.audio.ghost_returning",  audioClip("sound/mspacman/GhostEyes.mp3"));
		theme.set("mspacman.audio.intermission.1",   audioClip("sound/mspacman/Act1TheyMeet.mp3"));
		theme.set("mspacman.audio.intermission.2",   audioClip("sound/mspacman/Act2TheChase.mp3"));
		theme.set("mspacman.audio.intermission.3",   audioClip("sound/mspacman/Act3Junior.mp3"));
		theme.set("mspacman.audio.level_complete",   audioClip("sound/common/level-complete.mp3"));
		theme.set("mspacman.audio.pacman_death",     audioClip("sound/mspacman/Died.mp3"));
		theme.set("mspacman.audio.pacman_munch",     audioClip("sound/mspacman/Pill.wav"));
		theme.set("mspacman.audio.pacman_power",     audioClip("sound/mspacman/ScaredGhost.mp3"));
		theme.set("mspacman.audio.siren.1",          audioClip("sound/mspacman/GhostNoise1.wav"));
		theme.set("mspacman.audio.siren.2",          audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		theme.set("mspacman.audio.siren.3",          audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		theme.set("mspacman.audio.siren.4",          audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		theme.set("mspacman.audio.sweep",            audioClip("sound/common/sweep.mp3"));

		//
		// Pac-Man game
		//
		theme.set("pacman.startpage.image",          image("graphics/pacman/1980-Flyer-USA-Midway-front.jpg"));
		theme.set("pacman.helpButton.icon",          image("graphics/icons/help-blue-64.png"));

		theme.set("pacman.spritesheet",              new PacManSpriteSheet(image("graphics/pacman/sprites.png")));
		theme.set("pacman.icon",                     image("graphics/icons/pacman.png"));
		theme.set("pacman.flashingMaze",             image("graphics/pacman/maze_empty_flashing.png"));
		theme.set("pacman.fullMaze",                 image("graphics/pacman/maze_full.png"));
		theme.set("pacman.emptyMaze",                image("graphics/pacman/maze_empty.png"));
		theme.set("pacman.maze.foodColor",           Color.rgb(254, 189, 180));

		theme.set("pacman.audio.bonus_eaten",        audioClip("sound/pacman/eat_fruit.mp3"));
		theme.set("pacman.audio.credit",             audioClip("sound/pacman/credit.wav"));
		theme.set("pacman.audio.extra_life",         audioClip("sound/pacman/extend.mp3"));
		theme.set("pacman.audio.game_ready",         audioClip("sound/pacman/game_start.mp3"));
		theme.set("pacman.audio.game_over",          audioClip("sound/common/game-over.mp3"));
		theme.set("pacman.audio.ghost_eaten",        audioClip("sound/pacman/eat_ghost.mp3"));
		theme.set("pacman.audio.ghost_returning",    audioClip("sound/pacman/retreating.mp3"));
		theme.set("pacman.audio.intermission",       audioClip("sound/pacman/intermission.mp3"));
		theme.set("pacman.audio.level_complete",     audioClip("sound/common/level-complete.mp3"));
		theme.set("pacman.audio.pacman_death",       audioClip("sound/pacman/pacman_death.wav"));
		theme.set("pacman.audio.pacman_munch",       audioClip("sound/pacman/doublemunch.wav"));
		theme.set("pacman.audio.pacman_power",       audioClip("sound/pacman/ghost-turn-to-blue.mp3"));
		theme.set("pacman.audio.siren.1",            audioClip("sound/pacman/siren_1.mp3"));
		theme.set("pacman.audio.siren.2",            audioClip("sound/pacman/siren_2.mp3"));
		theme.set("pacman.audio.siren.3",            audioClip("sound/pacman/siren_3.mp3"));
		theme.set("pacman.audio.siren.4",            audioClip("sound/pacman/siren_4.mp3"));
		theme.set("pacman.audio.sweep",              audioClip("sound/common/sweep.mp3"));
	}
}