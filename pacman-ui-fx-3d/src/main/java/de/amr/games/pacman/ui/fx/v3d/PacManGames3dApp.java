/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.Settings;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.time.LocalTime;
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx.input.Keyboard.alt;
import static de.amr.games.pacman.ui.fx.input.Keyboard.just;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

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
	public static final BooleanProperty PY_3D_NIGHT_MODE         = new SimpleBooleanProperty(false);
	public static final DoubleProperty  PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(1.75);
	public static final DoubleProperty  PY_3D_WALL_THICKNESS     = new SimpleDoubleProperty(1.25);
	public static final BooleanProperty PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Perspective> PY_3D_PERSPECTIVE = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final BooleanProperty PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);
	public static final BooleanProperty PY_WOKE_PUSSY            = new SimpleBooleanProperty(false);

	public static final KeyCodeCombination[] KEYS_TOGGLE_DASHBOARD = { just(KeyCode.F1), alt(KeyCode.B) };
	public static final KeyCodeCombination   KEY_TOGGLE_PIP_VIEW   = just(KeyCode.F2);
	public static final KeyCodeCombination   KEY_TOGGLE_2D_3D      = alt(KeyCode.DIGIT3);
	public static final KeyCodeCombination   KEY_PREV_PERSPECTIVE  = alt(KeyCode.LEFT);
	public static final KeyCodeCombination   KEY_NEXT_PERSPECTIVE  = alt(KeyCode.RIGHT);

	public static final Picker<String> PICKER_READY_PACMAN    = Picker.fromBundle(TEXTS, "pacman.ready");
	public static final Picker<String> PICKER_READY_MS_PACMAN = Picker.fromBundle(TEXTS, "mspacman.ready");
	public static final Picker<String> PICKER_CHEATING        = Picker.fromBundle(TEXTS, "cheating");
	public static final Picker<String> PICKER_LEVEL_COMPLETE  = Picker.fromBundle(TEXTS, "level.complete");
	public static final Picker<String> PICKER_GAME_OVER       = Picker.fromBundle(TEXTS, "game.over");

	public static final String NO_TEXTURE = "No Texture";

	private final Settings settings = new Settings();
	private PacManGames3dUI ui;

	@Override
	public void init() {
		if (getParameters() != null) {
			settings.merge(getParameters().getNamed());
		}
		GameController.create(settings.variant);
		int hour = LocalTime.now().getHour();
		PY_3D_NIGHT_MODE.set(hour >= 20 || hour <= 5);
		Logger.info("Game initialized: {}", settings);
	}

	@Override
	public void start(Stage stage) {
		ui = new PacManGames3dUI(stage, settings);
		GameController.it().addListener(ui);
		ui.showStartPage();
		Logger.info("UI created. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
	}

	@Override
	public void stop() {
		ui.gameClock().stop();
		Logger.info("Game stopped.");
	}
}