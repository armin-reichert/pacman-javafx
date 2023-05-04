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
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.app.GameApp3d;
import de.amr.games.pacman.ui.fx.v3d.app.GameUI3d;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

/**
 * 3D related settings.
 * 
 * @author Armin Reichert
 */
public class Section3D extends Section {

	private final ComboBox<Perspective> comboPerspective;
	private final Slider sliderPiPSceneHeight;
	private final Slider sliderPiPOpacity;
	private final CheckBox cbEnergizerExplodes;
	private final Slider sliderWallHeight;
	private final Slider sliderWallThickness;
	private final CheckBox cbPacLighted;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;

	public Section3D(GameUI3d ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		comboPerspective = addComboBox("Perspective", Perspective.values());
		comboPerspective.setOnAction(e -> GameApp3d.d3_perspectivePy.set(comboPerspective.getValue()));
		addInfo("Camera", () -> (gameScene() instanceof PlayScene3D playScene3D) ? playScene3D.camInfo() : "")
				.available(() -> gameScene().is3D());
		sliderPiPSceneHeight = addSlider("PiP Size", GameUI3d.PIP_MIN_HEIGHT, GameUI3d.PIP_MAX_HEIGHT,
				GameApp3d.pipSceneHeightPy.get());
		sliderPiPSceneHeight.valueProperty()
				.addListener((obs, oldValue, newValue) -> GameApp3d.pipSceneHeightPy.set(newValue.doubleValue()));
		sliderPiPOpacity = addSlider("PiP Transparency", 0.0, 1.0, GameApp3d.pipOpacityPy.get());
		sliderPiPOpacity.valueProperty()
				.addListener((obs, oldValue, newValue) -> GameApp3d.pipOpacityPy.set(newValue.doubleValue()));
		sliderWallHeight = addSlider("Wall Height", 0.1, 8.5, GameApp3d.d3_mazeWallHeightPy.get());
		sliderWallHeight.valueProperty()
				.addListener((obs, oldVal, newVal) -> GameApp3d.d3_mazeWallHeightPy.set(newVal.doubleValue()));
		sliderWallThickness = addSlider("Wall Thickness", 0.1, 2.0, GameApp3d.d3_mazeWallThicknessPy.get());
		sliderWallThickness.valueProperty()
				.addListener((obs, oldVal, newVal) -> GameApp3d.d3_mazeWallThicknessPy.set(newVal.doubleValue()));
		cbEnergizerExplodes = addCheckBox("Energizer Explosion", () -> Ufx.toggle(GameApp3d.d3_energizerExplodesPy));
		cbPacLighted = addCheckBox("Pac-Man Lighted", () -> Ufx.toggle(GameApp3d.d3_pacLightedPy));
		cbAxesVisible = addCheckBox("Show Axes", () -> Ufx.toggle(GameApp3d.d3_axesVisiblePy));
		cbWireframeMode = addCheckBox("Wireframe Mode", GameApp3d.Actions::toggleDrawMode);
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(GameApp3d.d3_perspectivePy.get());
		sliderPiPSceneHeight.setValue(GameApp3d.pipSceneHeightPy.get());
		sliderPiPOpacity.setValue(GameApp3d.pipOpacityPy.get());
		sliderWallHeight.setValue(GameApp3d.d3_mazeWallHeightPy.get());
		cbEnergizerExplodes.setSelected(GameApp3d.d3_energizerExplodesPy.get());
		cbPacLighted.setSelected(GameApp3d.d3_pacLightedPy.get());
		cbAxesVisible.setSelected(GameApp3d.d3_axesVisiblePy.get());
		cbWireframeMode.setSelected(GameApp3d.d3_drawModePy.get() == DrawMode.LINE);
	}
}