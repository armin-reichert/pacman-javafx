/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.Settings;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.beans.property.*;
import javafx.scene.input.*;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.input.Keyboard.alt;
import static de.amr.games.pacman.ui.fx.input.Keyboard.just;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The <strong>play scene</strong> is available in a 2D and a 3D version. All others scenes are 2D only.
 * <p>
 * The picture-in-picture view shows the 2D version of the 3D play scene. It is activated/deactivated by pressing key F2.
 * Size and transparency can be controlled using the dashboard.
 * <p>
 * 
 * @author Armin Reichert
 */
public class PacManGames3dUI extends PacManGames2dUI implements ActionHandler3D {

	public static final ResourceBundle MSG_BUNDLE = ResourceBundle.getBundle(
		"de.amr.games.pacman.ui.fx.v3d.texts.messages", PacManGames3dUI.class.getModule());

	public static final float           PIP_MIN_HEIGHT                = 0.75f * GameModel.TILES_Y * TS;
	public static final float           PIP_MAX_HEIGHT                = 2.00f * GameModel.TILES_Y * TS;

	public static final DoubleProperty  PY_PIP_OPACITY                = new SimpleDoubleProperty(0.66);
	public static final DoubleProperty  PY_PIP_HEIGHT                 = new SimpleDoubleProperty(GameModel.TILES_Y * TS);
	public static final BooleanProperty PY_PIP_ON                     = new SimpleBooleanProperty(false);
	public static final IntegerProperty PY_SIMULATION_STEPS           = new SimpleIntegerProperty(1);
	public static final BooleanProperty PY_3D_AXES_VISIBLE            = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode> PY_3D_DRAW_MODE      = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final BooleanProperty PY_3D_ENABLED                 = new SimpleBooleanProperty(true);
	public static final BooleanProperty PY_3D_ENERGIZER_EXPLODES      = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Color> PY_3D_FLOOR_COLOR       = new SimpleObjectProperty<>(Color.grayRgb(0x33));
	public static final StringProperty  PY_3D_FLOOR_TEXTURE           = new SimpleStringProperty("knobs");
	public static final BooleanProperty PY_3D_FLOOR_TEXTURE_RND       = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Color> PY_3D_LIGHT_COLOR       = new SimpleObjectProperty<>(Color.GHOSTWHITE);
	public static final BooleanProperty PY_3D_NIGHT_MODE              = new SimpleBooleanProperty(false);
	public static final BooleanProperty PY_3D_PAC_LIGHT_ENABLED       = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Perspective> PY_3D_PERSPECTIVE = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final DoubleProperty  PY_3D_WALL_HEIGHT             = new SimpleDoubleProperty(1.75);
	public static final DoubleProperty  PY_3D_WALL_THICKNESS          = new SimpleDoubleProperty(1.25);
	public static final BooleanProperty PY_WOKE_PUSSY                 = new SimpleBooleanProperty(false);

	public static final KeyCodeCombination[] KEYS_TOGGLE_DASHBOARD    = { just(KeyCode.F1), alt(KeyCode.B) };
	public static final KeyCodeCombination   KEY_TOGGLE_PIP_VIEW      = just(KeyCode.F2);
	public static final KeyCodeCombination   KEY_TOGGLE_2D_3D         = alt(KeyCode.DIGIT3);
	public static final KeyCodeCombination   KEY_PREV_PERSPECTIVE     = alt(KeyCode.LEFT);
	public static final KeyCodeCombination   KEY_NEXT_PERSPECTIVE     = alt(KeyCode.RIGHT);

	public static final Picker<String> PICKER_READY_PACMAN            = Picker.fromBundle(MSG_BUNDLE, "pacman.ready");
	public static final Picker<String> PICKER_READY_MS_PACMAN         = Picker.fromBundle(MSG_BUNDLE, "mspacman.ready");
	public static final Picker<String> PICKER_CHEATING                = Picker.fromBundle(MSG_BUNDLE, "cheating");
	public static final Picker<String> PICKER_LEVEL_COMPLETE          = Picker.fromBundle(MSG_BUNDLE, "level.complete");
	public static final Picker<String> PICKER_GAME_OVER               = Picker.fromBundle(MSG_BUNDLE, "game.over");

	public static final String NO_TEXTURE                             = "No Texture";

	private static void loadFloorTextures(ResourceManager rm, String... names) {
		var ext = "jpg";
		for (var name : names) {
			var texture = new PhongMaterial();
			texture.setBumpMap(rm.image("graphics/textures/%s-bump.%s".formatted(name, ext)));
			texture.setDiffuseMap(rm.image("graphics/textures/%s-diffuse.%s".formatted(name, ext)));
			texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
			THEME.set("texture." + name, texture);
		}
		THEME.addAllToArray("texture.names", (Object[]) names);
	}

	static {
		ResourceManager rm = () -> PacManGames3dUI.class;

		THEME.set("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
		THEME.set("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
		THEME.set("model3D.pellet", new Model3D(rm.url("model3D/12206_Fruit_v1_L3.obj")));

		THEME.set("model3D.wallpaper", rm.imageBackground("graphics/sea-wallpaper.jpg",
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER,
				new BackgroundSize(1, 1, true, true, false, true)
		));

		THEME.set("model3D.wallpaper.night", rm.imageBackground("graphics/sea-wallpaper-night.jpg",
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER,
				new BackgroundSize(1, 1, true, true, false, true)
		));

		THEME.set("image.armin1970",                 rm.image("graphics/armin.jpg"));
		THEME.set("icon.play",                       rm.image("graphics/icons/play.png"));
		THEME.set("icon.stop",                       rm.image("graphics/icons/stop.png"));
		THEME.set("icon.step",                       rm.image("graphics/icons/step.png"));

		loadFloorTextures(rm,"hexagon", "knobs", "plastic", "wood");

		THEME.set("ghost.0.color.normal.dress",      THEME.color("palette.red"));
		THEME.set("ghost.0.color.normal.eyeballs",   THEME.color("palette.pale"));
		THEME.set("ghost.0.color.normal.pupils",     THEME.color("palette.blue"));

		THEME.set("ghost.1.color.normal.dress",      THEME.color("palette.pink"));
		THEME.set("ghost.1.color.normal.eyeballs",   THEME.color("palette.pale"));
		THEME.set("ghost.1.color.normal.pupils",     THEME.color("palette.blue"));

		THEME.set("ghost.2.color.normal.dress",      THEME.color("palette.cyan"));
		THEME.set("ghost.2.color.normal.eyeballs",   THEME.color("palette.pale"));
		THEME.set("ghost.2.color.normal.pupils",     THEME.color("palette.blue"));

		THEME.set("ghost.3.color.normal.dress",      THEME.color("palette.orange"));
		THEME.set("ghost.3.color.normal.eyeballs",   THEME.color("palette.pale"));
		THEME.set("ghost.3.color.normal.pupils",     THEME.color("palette.blue"));

		THEME.set("ghost.color.frightened.dress",    THEME.color("palette.blue"));
		THEME.set("ghost.color.frightened.eyeballs", THEME.color("palette.rose"));
		THEME.set("ghost.color.frightened.pupils",   THEME.color("palette.rose"));

		THEME.set("ghost.color.flashing.dress",      THEME.color("palette.pale"));
		THEME.set("ghost.color.flashing.eyeballs",   THEME.color("palette.rose"));
		THEME.set("ghost.color.flashing.pupils",     THEME.color("palette.red"));

		THEME.addAllToArray("mspacman.maze.foodColor",
			Color.rgb(222, 222, 255),
			Color.rgb(255, 255,   0),
			Color.rgb(255,   0,   0),
			Color.rgb(222, 222, 255),
			Color.rgb(  0, 255, 255),
			Color.rgb(222, 222, 255)
		);

		THEME.addAllToArray("mspacman.maze.wallBaseColor",
			Color.rgb(255,   0,   0),
			Color.rgb(222, 222, 255),
			Color.rgb(222, 222, 255),
			Color.rgb(255, 183,  81),
			Color.rgb(255, 255,   0),
			Color.rgb(255,   0,   0)
		);

		THEME.addAllToArray("mspacman.maze.wallTopColor",
			Color.rgb(255, 183, 174),
			Color.rgb( 71, 183, 255),
			Color.rgb(222, 151,  81),
			Color.rgb(222, 151,  81),
			Color.rgb(222, 151,  81),
			Color.rgb(222, 151,  81)
		);

		THEME.set("mspacman.color.head",           Color.rgb(255, 255,   0));
		THEME.set("mspacman.color.palate",         Color.rgb(191,  79,  61));
		THEME.set("mspacman.color.eyes",           Color.rgb( 33,  33,  33));
		THEME.set("mspacman.color.boobs",          Color.rgb(255, 255,   0).deriveColor(0, 1.0, 0.96, 1.0));
		THEME.set("mspacman.color.hairbow",        Color.rgb(255,   0,   0));
		THEME.set("mspacman.color.hairbow.pearls", Color.rgb( 33,  33, 255));

		THEME.set("mspacman.maze.doorColor",       Color.rgb(255, 183, 255));

		THEME.set("pacman.maze.wallBaseColor",     Color.rgb( 33,  33, 255).brighter());
		THEME.set("pacman.maze.wallTopColor",      Color.rgb( 33,  33, 255).darker());
		THEME.set("pacman.maze.doorColor",         Color.rgb(252, 181, 255));

		THEME.set("pacman.color.head",             Color.rgb(255, 255,   0));
		THEME.set("pacman.color.palate",           Color.rgb(191,  79,  61));
		THEME.set("pacman.color.eyes",             Color.rgb( 33,  33,  33));

		// dashboard
		THEME.set("infobox.min_col_width",         100);
		THEME.set("infobox.min_label_width",       120);
		THEME.set("infobox.text_color",            Color.WHITE);
		THEME.set("infobox.label_font",            Font.font("Tahoma", 12));
		THEME.set("infobox.text_font",             Font.font("Tahoma", 12));

		Logger.info("Pac-Man games 3D theme loaded");
	}

	public PacManGames3dUI(Stage stage, Settings settings) {
		super(stage, settings);
		PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateStage());
		PY_3D_ENABLED.addListener((py, ov, nv) -> updateStage());
		int hour = LocalTime.now().getHour();
		PY_3D_NIGHT_MODE.set(hour >= 20 || hour <= 5);
		gamePage().getInfoBoxes().forEach(section -> section.init(this));
	}

	@Override
	public List<ResourceBundle> messageBundles() {
		return List.of(MSG_BUNDLE, PacManGames2dUI.MSG_BUNDLE);
	}


	@Override
	protected void addGameScenes() {
		super.addGameScenes();
		for (var gameVariant : GameVariant.values())
		{
			var playScene3D = new PlayScene3D();
			playScene3D.bindSize(mainScene.widthProperty(), mainScene.heightProperty());
			gameScenes.get(gameVariant).put("play3D", playScene3D);
		}
	}

	@Override
	protected GamePage3D createGamePage() {
		checkNotNull(mainScene);
		var page = new GamePage3D(this, messageBundles());
		page.setSize(mainScene.getWidth(), mainScene.getHeight());
		// register event handler for opening page context menu
		mainScene.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
			currentGameScene().ifPresent(gameScene -> {
				page.contextMenu().hide();
				if (e.getButton() == MouseButton.SECONDARY && isPlayScene(gameScene)) {
					page.contextMenu().rebuild(theme(), actionHandler(), gameScene, messageBundles());
					page.contextMenu().show(mainScene.getRoot(), e.getScreenX(), e.getScreenY());
				}
			})
		);
		gameScenePy.addListener((py, ov, newGameScene) -> page.onGameSceneChanged(newGameScene));
		return page;
	}

	public boolean isPlayScene(GameScene gameScene) {
		var config = sceneConfig();
		return gameScene == config.get("play") || gameScene == config.get("play3D");
	}

	public GamePage3D gamePage() {
		return (GamePage3D) gamePage;
	}

	@Override
	public ActionHandler3D actionHandler() {
		return this;
	}

	@Override
	protected void configurePacSteering() {
		// Enable steering with unmodified and CONTROL + cursor key
		var steering = new KeyboardSteering();
		steering.define(Direction.UP,    KeyCode.UP,    KeyCombination.CONTROL_DOWN);
		steering.define(Direction.DOWN,  KeyCode.DOWN,  KeyCombination.CONTROL_DOWN);
		steering.define(Direction.LEFT,  KeyCode.LEFT,  KeyCombination.CONTROL_DOWN);
		steering.define(Direction.RIGHT, KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
		gameController().setManualPacSteering(steering);
	}

	@Override
	protected void updateStage() {
		var variantKey = gameVariant() == GameVariant.MS_PACMAN ? "mspacman" : "pacman";
		var titleKey = "app.title." + variantKey + (gameClock().isPaused() ? ".paused" : "");
		var dimension = message(messageBundles(), PY_3D_ENABLED.get() ? "threeD" : "twoD");
		stage.setTitle(message(messageBundles(), titleKey, dimension));
		stage.getIcons().setAll(theme().image(variantKey + ".icon"));
		gamePage().updateBackground();
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var gameScene = super.sceneMatchingCurrentGameState();
		if (PY_3D_ENABLED.get() && gameScene == sceneConfig().get("play")) {
			return sceneConfig().getOrDefault("play3D", gameScene);
		}
		return gameScene;
	}

	@Override
	public void toggle2D3D() {
		currentGameScene().ifPresent(gameScene -> {
			Ufx.toggle(PY_3D_ENABLED);
			if (isPlayScene(gameScene)) {
				updateOrReloadGameScene(true);
				gameScene.onSceneVariantSwitch();
			}
			gameController().update();
			showFlashMessage(message(messageBundles(), PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
		});
	}

	@Override
	public void togglePipVisible() {
		Ufx.toggle(PY_PIP_ON);
		showFlashMessage(message(messageBundles(), PY_PIP_ON.get() ? "pip_on" : "pip_off"));
	}

	@Override
	public void selectNextPerspective() {
		PY_3D_PERSPECTIVE.set(PY_3D_PERSPECTIVE.get().next());
		showFlashMessage(message(messageBundles(), "camera_perspective",
			message(messageBundles(), PY_3D_PERSPECTIVE.get().name())));
	}

	@Override
	public void selectPrevPerspective() {
		PY_3D_PERSPECTIVE.set(PY_3D_PERSPECTIVE.get().prev());
		showFlashMessage(message(messageBundles(), "camera_perspective",
			message(messageBundles(), PY_3D_PERSPECTIVE.get().name())));
	}

	@Override
	public void toggleDrawMode() {
		PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}