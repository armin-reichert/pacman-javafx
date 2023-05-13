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
package de.amr.games.pacman.ui.fx.v3d.app;

import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;
import static de.amr.games.pacman.ui.fx.util.Ufx.alt;
import static de.amr.games.pacman.ui.fx.util.Ufx.just;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.entity.GhostModel3D;
import de.amr.games.pacman.ui.fx.v3d.entity.PacModel3D;
import de.amr.games.pacman.ui.fx.v3d.entity.PelletModel3D;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class Game3d extends Application {

	public static final ResourceManager ASSET_MANAGER = new ResourceManager("/de/amr/games/pacman/ui/fx/v3d/",
			Game3d.class);

	//@formatter:off
	public static final BooleanProperty             dashboardVisiblePy      = new SimpleBooleanProperty(false);

	public static final BooleanProperty             pipVisiblePy            = new SimpleBooleanProperty(false);
	public static final DoubleProperty              pipOpacityPy            = new SimpleDoubleProperty(0.66);
	public static final DoubleProperty              pipHeightPy             = new SimpleDoubleProperty(World.TILES_Y * Globals.TS);

	public static final BooleanProperty             d3_axesVisiblePy        = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode>    d3_drawModePy           = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final BooleanProperty             d3_enabledPy            = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Color>       d3_floorColorPy         = new SimpleObjectProperty<>(Color.grayRgb(0x60));
	public static final StringProperty              d3_floorTexturePy       = new SimpleStringProperty("Knobs & Bumps");
	public static final BooleanProperty             d3_floorTextureRandomPy = new SimpleBooleanProperty(false);
	public static final BooleanProperty             d3_foodOscillationPy    = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Color>       d3_lightColorPy         = new SimpleObjectProperty<>(Color.GHOSTWHITE);
	public static final DoubleProperty              d3_mazeWallHeightPy     = new SimpleDoubleProperty(1.75);
	public static final DoubleProperty              d3_mazeWallThicknessPy  = new SimpleDoubleProperty(1.25);
	public static final BooleanProperty             d3_pacLightedPy         = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Perspective> d3_perspectivePy        = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final BooleanProperty             d3_energizerExplodesPy  = new SimpleBooleanProperty(true);

	public static final BooleanProperty             wokePussyMode           = new SimpleBooleanProperty(false); 
	//@formatter:on

	public static class Assets {

		public static final String KEY_NO_TEXTURE = "No Texture";

		public final PacModel3D pacModel3D;
		public final GhostModel3D ghostModel3D;
		public final PelletModel3D pelletModel3D;
		public final Background wallpaper3D;
		public final Map<String, PhongMaterial> floorTexturesByName = new LinkedHashMap<>();
		public final ResourceBundle messages;
		private final Picker<String> messagePickerCheating;
		private final Picker<String> messagePickerLevelComplete;
		private final Picker<String> messagePickerGameOver;

		private Assets() {
			messages = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.v3d.texts.messages");
			messagePickerCheating = ResourceManager.createPicker(messages, "cheating");
			messagePickerLevelComplete = ResourceManager.createPicker(messages, "level.complete");
			messagePickerGameOver = ResourceManager.createPicker(messages, "game.over");

			wallpaper3D = ASSET_MANAGER.imageBackground("graphics/sky.png");
			floorTexturesByName.put("Hexagon", createFloorTexture("hexagon", "jpg"));
			floorTexturesByName.put("Knobs & Bumps", createFloorTexture("knobs", "jpg"));
			floorTexturesByName.put("Plastic", createFloorTexture("plastic", "jpg"));
			floorTexturesByName.put("Wood", createFloorTexture("wood", "jpg"));

			pacModel3D = new PacModel3D();
			ghostModel3D = new GhostModel3D();
			pelletModel3D = new PelletModel3D();
		}

		public String pickCheatingMessage() {
			return messagePickerCheating.next();
		}

		public String pickGameOverMessage() {
			return messagePickerGameOver.next();
		}

		public String pickLevelCompleteMessage(int levelNumber) {
			return "%s%n%n%s".formatted(messagePickerLevelComplete.next(),
					fmtMessage(messages, "level_complete", levelNumber));
		}

		private PhongMaterial createFloorTexture(String textureBase, String ext) {
			var material = textureMaterial(textureBase, ext, null, null);
			material.diffuseColorProperty().bind(d3_floorColorPy);
			return material;
		}

		public PhongMaterial textureMaterial(String textureBase, String ext, Color diffuseColor, Color specularColor) {
			var texture = new PhongMaterial();
			texture.setBumpMap(ASSET_MANAGER.image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
			texture.setDiffuseMap(ASSET_MANAGER.image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
			texture.setDiffuseColor(diffuseColor);
			texture.setSpecularColor(specularColor);
			return texture;
		}
	}

	public static class Actions {

		public void togglePipVisibility() {
			Ufx.toggle(pipVisiblePy);
			var key = pipVisiblePy.get() ? "pip_on" : "pip_off";
			Game2d.actions.showFlashMessage(fmtMessage(assets.messages, key));
		}

		public void toggleDashboardVisible() {
			Ufx.toggle(dashboardVisiblePy);
		}

		public void selectNextPerspective() {
			var nextPerspective = d3_perspectivePy.get().next();
			d3_perspectivePy.set(nextPerspective);
			String perspectiveName = fmtMessage(assets.messages, nextPerspective.name());
			Game2d.actions.showFlashMessage(fmtMessage(assets.messages, "camera_perspective", perspectiveName));
		}

		public void selectPrevPerspective() {
			var prevPerspective = d3_perspectivePy.get().prev();
			d3_perspectivePy.set(prevPerspective);
			String perspectiveName = fmtMessage(assets.messages, prevPerspective.name());
			Game2d.actions.showFlashMessage(fmtMessage(assets.messages, "camera_perspective", perspectiveName));
		}

		public void toggleDrawMode() {
			d3_drawModePy.set(d3_drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
		}
	}

	public static class Keys {
		public static final KeyCodeCombination TOGGLE_DASHBOARD_VISIBLE = just(KeyCode.F1);
		public static final KeyCodeCombination TOGGLE_DASHBOARD_VISIBLE_2 = alt(KeyCode.B);
		public static final KeyCodeCombination TOGGLE_PIP_VIEW_VISIBLE = just(KeyCode.F2);
		public static final KeyCodeCombination TOGGLE_3D_ENABLED = alt(KeyCode.DIGIT3);
		public static final KeyCodeCombination PREV_CAMERA = alt(KeyCode.LEFT);
		public static final KeyCodeCombination NEXT_CAMERA = alt(KeyCode.RIGHT);
	}

	/**
	 * Static access to actions of 3D game.
	 */
	public static Actions actions;

	/**
	 * Static access to assets of 3D game.
	 */
	public static Assets assets;

	private GameUI3d ui;

	@Override
	public void start(Stage primaryStage) throws IOException {
		var settings = new Settings(getParameters() != null ? getParameters().getNamed() : Collections.emptyMap());

		long start = System.nanoTime();
		Game2d.assets = new Game2d.Assets();
		Game3d.assets = new Game3d.Assets();
		Logger.info("Loading assets: {} seconds.", (System.nanoTime() - start) / 1e9f);

		Game2d.actions = new Game2d.Actions();
		Game3d.actions = new Game3d.Actions();

		var gameController = new GameController(settings.variant);
		ui = new GameUI3d(gameController, primaryStage, settings);
		ui.init(settings);

		Game2d.actions.setUI(ui);

		// TODO: Check if dashboard initalization used actions
		ui.dashboard().init();

		Game2d.actions.reboot();
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