/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.Settings;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.*;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Map;
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.input.Keyboard.alt;
import static de.amr.games.pacman.ui.fx.input.Keyboard.just;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application implements ResourceManager {
	private static final ResourceBundle TEXTS = ResourceBundle.getBundle(
			"de.amr.games.pacman.ui.fx.v3d.texts.messages",
			PacManGames3dApp.class.getModule());

	public static String message(String key, Object... args) {
		return ResourceManager.message(TEXTS, key, args);
	}

	public static final float           PIP_MIN_HEIGHT           = 0.75f * GameModel.TILES_Y * TS;
	public static final float           PIP_MAX_HEIGHT           = 2.00f * GameModel.TILES_Y * TS;

	public static final DoubleProperty  PY_PIP_OPACITY           = new SimpleDoubleProperty(0.66);
	public static final DoubleProperty  PY_PIP_HEIGHT            = new SimpleDoubleProperty(GameModel.TILES_Y * TS);
	public static final BooleanProperty PY_PIP_ON                = new SimpleBooleanProperty(false);

	public static final IntegerProperty PY_SIMULATION_STEPS      = new SimpleIntegerProperty(1);

	public static final BooleanProperty PY_3D_AXES_VISIBLE       = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode> PY_3D_DRAW_MODE = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final BooleanProperty PY_3D_ENABLED            = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Color> PY_3D_FLOOR_COLOR  = new SimpleObjectProperty<>(Color.grayRgb(0x33));
	public static final StringProperty  PY_3D_FLOOR_TEXTURE      = new SimpleStringProperty("knobs");
	public static final BooleanProperty PY_3D_FLOOR_TEXTURE_RND  = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Color> PY_3D_LIGHT_COLOR  = new SimpleObjectProperty<>(Color.GHOSTWHITE);
	public static final DoubleProperty  PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(1.75);
	public static final DoubleProperty  PY_3D_WALL_THICKNESS     = new SimpleDoubleProperty(1.25);
	public static final BooleanProperty PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Perspective> PY_3D_PERSPECTIVE = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final BooleanProperty PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);

	public static final BooleanProperty PY_WOKE_PUSSY            = new SimpleBooleanProperty(false);

	public static final KeyCodeCombination[] KEYS_TOGGLE_DASHBOARD  = { just(KeyCode.F1), alt(KeyCode.B) };
	public static final KeyCodeCombination   KEY_TOGGLE_PIP_VIEW    = just(KeyCode.F2);
	public static final KeyCodeCombination   KEY_TOGGLE_2D_3D       = alt(KeyCode.DIGIT3);
	public static final KeyCodeCombination   KEY_PREV_PERSPECTIVE   = alt(KeyCode.LEFT);
	public static final KeyCodeCombination   KEY_NEXT_PERSPECTIVE   = alt(KeyCode.RIGHT);

	public static final Picker<String> PICKER_READY_PACMAN    = Picker.fromBundle(TEXTS, "pacman.ready");
	public static final Picker<String> PICKER_READY_MS_PACMAN = Picker.fromBundle(TEXTS, "mspacman.ready");
	public static final Picker<String> PICKER_CHEATING        = Picker.fromBundle(TEXTS, "cheating");
	public static final Picker<String> PICKER_LEVEL_COMPLETE  = Picker.fromBundle(TEXTS, "level.complete");
	public static final Picker<String> PICKER_GAME_OVER       = Picker.fromBundle(TEXTS, "game.over");

	public static final String KEY_NO_TEXTURE = "No Texture";

	private final Settings settings = new Settings();
	private PacManGames3dUI ui;

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
		var theme = createTheme();
		Logger.info("Theme created: {}", theme);

		ui = new PacManGames3dUI(stage, settings, theme);
		GameController.it().addListener(ui);
		ui.showStartPage();
		Logger.info("UI created. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
	}

	@Override
	public void stop() {
		ui.clock().stop();
		Logger.info("Game stopped.");
	}

	private Theme createTheme() {
		var theme = new PacManGames2dApp().createTheme();

		theme.set("model3D.pacman",                  new Model3D(url("model3D/pacman.obj")));
		theme.set("model3D.ghost",                   new Model3D(url("model3D/ghost.obj")));
		theme.set("model3D.pellet",                  new Model3D(url("model3D/12206_Fruit_v1_L3.obj")));

		theme.set("model3D.wallpaper",  imageBackground("graphics/sea-wallpaper.jpg",
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER,
				new BackgroundSize(1, 1, true, true, false, true)
		));
		theme.set("model3D.wallpaper.night",  imageBackground("graphics/sea-wallpaper-night.jpg",
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER,
				new BackgroundSize(1, 1, true, true, false, true)
		));

		theme.set("image.armin1970",                 image("graphics/armin.jpg"));
		theme.set("icon.play",                       image("graphics/icons/play.png"));
		theme.set("icon.stop",                       image("graphics/icons/stop.png"));
		theme.set("icon.step",                       image("graphics/icons/step.png"));

		theme.set("texture.hexagon",                 createFloorTexture("hexagon", "jpg"));
		theme.set("texture.knobs",                   createFloorTexture("knobs", "jpg"));
		theme.set("texture.plastic",                 createFloorTexture("plastic", "jpg"));
		theme.set("texture.wood",                    createFloorTexture("wood", "jpg"));

		theme.set("ghost.0.color.normal.dress",      ArcadePalette.RED);
		theme.set("ghost.0.color.normal.eyeballs",   ArcadePalette.PALE);
		theme.set("ghost.0.color.normal.pupils",     ArcadePalette.BLUE);

		theme.set("ghost.1.color.normal.dress",      ArcadePalette.PINK);
		theme.set("ghost.1.color.normal.eyeballs",   ArcadePalette.PALE);
		theme.set("ghost.1.color.normal.pupils",     ArcadePalette.BLUE);

		theme.set("ghost.2.color.normal.dress",      ArcadePalette.CYAN);
		theme.set("ghost.2.color.normal.eyeballs",   ArcadePalette.PALE);
		theme.set("ghost.2.color.normal.pupils",     ArcadePalette.BLUE);

		theme.set("ghost.3.color.normal.dress",      ArcadePalette.ORANGE);
		theme.set("ghost.3.color.normal.eyeballs",   ArcadePalette.PALE);
		theme.set("ghost.3.color.normal.pupils",     ArcadePalette.BLUE);

		theme.set("ghost.color.frightened.dress",    ArcadePalette.BLUE);
		theme.set("ghost.color.frightened.eyeballs", ArcadePalette.ROSE);
		theme.set("ghost.color.frightened.pupils",   ArcadePalette.ROSE);

		theme.set("ghost.color.flashing.dress",      ArcadePalette.PALE);
		theme.set("ghost.color.flashing.eyeballs",   ArcadePalette.ROSE);
		theme.set("ghost.color.flashing.pupils",     ArcadePalette.RED);

		theme.addAllToArray("mspacman.maze.foodColor",
				Color.rgb(222, 222, 255),
				Color.rgb(255, 255, 0),
				Color.rgb(255, 0, 0),
				Color.rgb(222, 222, 255),
				Color.rgb(0, 255, 255),
				Color.rgb(222, 222, 255)
		);

		theme.addAllToArray("mspacman.maze.wallBaseColor",
				Color.rgb(255, 0, 0),
				Color.rgb(222, 222, 255),
				Color.rgb(222, 222, 255),
				Color.rgb(255, 183, 81),
				Color.rgb(255, 255, 0),
				Color.rgb(255, 0, 0)
		);

		theme.addAllToArray("mspacman.maze.wallTopColor",
				Color.rgb(255, 183, 174),
				Color.rgb(71, 183, 255),
				Color.rgb(222, 151, 81),
				Color.rgb(222, 151, 81),
				Color.rgb(222, 151, 81),
				Color.rgb(222, 151, 81)
		);

		theme.set("mspacman.color.head",             Color.rgb(255, 255, 0));
		theme.set("mspacman.color.palate",           Color.rgb(191, 79, 61));
		theme.set("mspacman.color.eyes",             Color.rgb(33, 33, 33));
		theme.set("mspacman.color.boobs",            Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
		theme.set("mspacman.color.hairbow",          Color.rgb(255, 0, 0));
		theme.set("mspacman.color.hairbow.pearls",   Color.rgb(33, 33, 255));

		theme.set("mspacman.maze.doorColor",         Color.rgb(255, 183, 255));

		theme.set("pacman.maze.wallBaseColor",       Color.rgb(33, 33, 255).brighter());
		theme.set("pacman.maze.wallTopColor",        Color.rgb(33, 33, 255).darker());
		theme.set("pacman.maze.doorColor",           Color.rgb(252, 181, 255));

		theme.set("pacman.color.head",               Color.rgb(255, 255, 0));
		theme.set("pacman.color.palate",             Color.rgb(191, 79, 61));
		theme.set("pacman.color.eyes",               Color.rgb(33, 33, 33));

		return theme;
	}

	private PhongMaterial createFloorTexture(String baseName, String ext) {
		var texture = new PhongMaterial();
		texture.setBumpMap(image("graphics/textures/%s-bump.%s".formatted(baseName, ext)));
		texture.setDiffuseMap(image("graphics/textures/%s-diffuse.%s".formatted(baseName, ext)));
		texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
		return texture;
	}
}