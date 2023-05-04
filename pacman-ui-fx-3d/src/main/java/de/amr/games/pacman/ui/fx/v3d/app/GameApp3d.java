/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import static de.amr.games.pacman.lib.Globals.randomInt;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.util.ResourceMgr;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.entity.PacModel3D;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
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
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class GameApp3d extends Application {

//@formatter:off
	public static final BooleanProperty             wokePussyMode = new SimpleBooleanProperty(false); 
	public static final BooleanProperty             dashboardVisiblePy = new SimpleBooleanProperty(false);

	public static final BooleanProperty             pipVisiblePy = new SimpleBooleanProperty(false);
	public static final DoubleProperty              pipOpacityPy = new SimpleDoubleProperty(0.66);
	public static final DoubleProperty              pipSceneHeightPy = new SimpleDoubleProperty(World.TILES_Y * Globals.TS);

	public static final BooleanProperty             d3_axesVisiblePy = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode>    d3_drawModePy = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final BooleanProperty             d3_enabledPy = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Color>       d3_floorColorPy = new SimpleObjectProperty<>(Color.grayRgb(0x60));
	public static final StringProperty              d3_floorTexturePy = new SimpleStringProperty("Knobs & Bumps");
	public static final BooleanProperty             d3_floorTextureRandomPy = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Color>       d3_lightColorPy = new SimpleObjectProperty<>(Color.GHOSTWHITE);
	public static final DoubleProperty              d3_mazeWallHeightPy = new SimpleDoubleProperty(1.75);
	public static final DoubleProperty              d3_mazeWallThicknessPy = new SimpleDoubleProperty(1.25);
	public static final BooleanProperty             d3_pacLightedPy = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Perspective> d3_perspectivePy = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final BooleanProperty             d3_energizerExplodesPy = new SimpleBooleanProperty(true);
	// experimental, not used yet 
	public static final BooleanProperty             d3_foodOscillationEnabledPy = new SimpleBooleanProperty(false);
//@formatter:on

	public static class Actions extends de.amr.games.pacman.ui.fx.app.Actions {

		public static void togglePipViewVisible() {
			Ufx.toggle(GameApp3d.pipVisiblePy);
			var msgKey = GameApp3d.pipVisiblePy.get() ? "pip_on" : "pip_off";
			Game2d.ACTIONS.showFlashMessage(Game2d.Texts.message(msgKey));// TODO
		}

		public static void toggleDashboardVisible() {
			Ufx.toggle(GameApp3d.dashboardVisiblePy);
		}

		public static void selectNextPerspective() {
			var nextPerspective = GameApp3d.d3_perspectivePy.get().next();
			GameApp3d.d3_perspectivePy.set(nextPerspective);
			String perspectiveName = Game2d.Texts.message(nextPerspective.name());
			Game2d.ACTIONS.showFlashMessage(Game2d.Texts.message("camera_perspective", perspectiveName));
		}

		public static void selectPrevPerspective() {
			var prevPerspective = GameApp3d.d3_perspectivePy.get().prev();
			GameApp3d.d3_perspectivePy.set(prevPerspective);
			String perspectiveName = Game2d.Texts.message(prevPerspective.name());
			Game2d.ACTIONS.showFlashMessage(Game2d.Texts.message("camera_perspective", perspectiveName));
		}

		public static void toggleDrawMode() {
			GameApp3d.d3_drawModePy.set(GameApp3d.d3_drawModePy.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
		}
	}

	public static final ResourceMgr ResMgr = new ResourceMgr("/de/amr/games/pacman/ui/fx3d/assets/",
			GameApp3d.class::getResource);

	private static void runAndMeasureTime(String section, Runnable loadingCode) {
		long start = System.nanoTime();
		loadingCode.run();
		Logger.info("{} done ({} seconds).", section, (System.nanoTime() - start) / 1e9f);
	}

	public static class Models3D {

		public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
		public static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
		public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";
		public static final String MESH_ID_PELLET = "Fruit";

		public static PacModel3D pacModel3D;
		public static Model3D ghostModel3D;
		public static Model3D pelletModel3D;

		static void load() {
			pacModel3D = new PacModel3D(ResMgr.urlFromRelPath("model3D/pacman.obj"));
			ghostModel3D = new Model3D(ResMgr.urlFromRelPath("model3D/ghost.obj"));
			pelletModel3D = new Model3D(ResMgr.urlFromRelPath("model3D/12206_Fruit_v1_L3.obj"));
		}
	}

	public static class Textures {

		public static final String KEY_NO_TEXTURE = "No Texture";
		public static Background backgroundForScene3D;
		private static Map<String, PhongMaterial> floorTexturesByName = new LinkedHashMap<>();

		static void load() {
			backgroundForScene3D = ResMgr.imageBackground("graphics/sky.png");
			floorTexturesByName.put("Hexagon", createFloorTexture("hexagon", "jpg"));
			floorTexturesByName.put("Knobs & Bumps", createFloorTexture("knobs", "jpg"));
			floorTexturesByName.put("Plastic", createFloorTexture("plastic", "jpg"));
			floorTexturesByName.put("Wood", createFloorTexture("wood", "jpg"));
		}

		private static PhongMaterial createFloorTexture(String textureBase, String ext) {
			var material = textureMaterial(textureBase, ext, null, null);
			material.diffuseColorProperty().bind(GameApp3d.d3_floorColorPy);
			return material;
		}

		public static PhongMaterial textureMaterial(String textureBase, String ext, Color diffuseColor,
				Color specularColor) {
			var texture = new PhongMaterial();
			texture.setBumpMap(ResMgr.image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
			texture.setDiffuseMap(ResMgr.image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
			texture.setDiffuseColor(diffuseColor);
			texture.setSpecularColor(specularColor);
			return texture;
		}

		public static PhongMaterial floorTexture(String name) {
			return floorTexturesByName.get(name);
		}

		public static String[] floorTextureNames() {
			return floorTexturesByName.keySet().toArray(String[]::new);
		}

		public static String randomFloorTextureName() {
			var names = floorTextureNames();
			return names[randomInt(0, names.length)];
		}
	}

	private GameUI3d ui;

	@Override
	public void init() throws Exception {
		Game2d.loadResources();
		long start = System.nanoTime();
		runAndMeasureTime("Loading textures", Textures::load);
		runAndMeasureTime("Loading 3D models", Models3D::load);
		Logger.info("Loading resources took {} seconds.", (System.nanoTime() - start) / 1e9f);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		var settings = new Settings(getParameters() != null ? getParameters().getNamed() : Collections.emptyMap());
		var gameController = new GameController(settings.variant);
		ui = new GameUI3d(primaryStage, settings, gameController);
		ui.start();
		Logger.info("Game started. Locale: {} Framerate: {} Hz Settings: {}", Locale.getDefault(),
				ui.targetFrameratePy.get(), settings);
	}

	@Override
	public void stop() throws Exception {
		ui.stop();
		Logger.info("Game stopped");
	}

	// TODO not sure if we need this main method as we have the "launcher" class Main
	public static void main(String[] args) {
		launch(args);
	}
}