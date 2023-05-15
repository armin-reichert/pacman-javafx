/*
MIT License

Copyright (c) 2023 Armin Reichert

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

import java.util.Locale;

import org.tinylog.Logger;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.app.Game2dActions;
import de.amr.games.pacman.ui.fx.app.Settings;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class Game3d extends Application {

	//@formatter:off
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

	public static final Game3dAssets assets = new Game3dAssets();
	public static Game3dActions actions = null;
	public static Game3dUI ui = null;
	private static Settings cfg;

	@Override
	public void init() {
		Game3d.cfg = new Settings(getParameters().getNamed());
		Logger.info("Game configuration: {}", Game3d.cfg);
		long start = System.nanoTime();
		Game2d.assets.load();
		Game3d.assets.load();
		Logger.info("Loading assets: {} seconds.", (System.nanoTime() - start) / 1e9f);
	}

	@Override
	public void start(Stage stage) {
		stage.setFullScreen(Game3d.cfg.fullScreen);
		Game3d.ui = new Game3dUI(Game3d.cfg.variant, stage, Game3d.cfg.zoom * 28 * 8, Game3d.cfg.zoom * 36 * 8);
		Game2d.actions = new Game2dActions(Game3d.ui);
		Game3d.actions = new Game3dActions();
		Game3d.ui.init(Game3d.cfg);
		Game3d.ui.startClockAndShowStage();
		Game2d.actions.reboot();
		Logger.info("Game started. {} Hz language={}", Game3d.ui.clock().targetFrameratePy.get(), Locale.getDefault());
	}

	@Override
	public void stop() {
		Game3d.ui.clock().stop();
		Logger.info("Game stopped.");
	}
}