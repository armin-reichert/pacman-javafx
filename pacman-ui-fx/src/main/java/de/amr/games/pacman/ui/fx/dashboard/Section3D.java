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
package de.amr.games.pacman.ui.fx.dashboard;

import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.Ufx;
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
	private final CheckBox cbPacWalkingAnimated;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;

	public Section3D(GameUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		comboPerspective = addComboBox("Perspective", Perspective.values());
		comboPerspective.setOnAction(e -> Env.d3_perspectivePy.set(comboPerspective.getValue()));
		addInfo("Camera", () -> (gameScene() instanceof PlayScene3D playScene3D) ? playScene3D.camInfo() : "")
				.available(() -> gameScene().is3D());
		sliderPiPSceneHeight = addSlider("PiP Size", GameUI.PIP_MIN_HEIGHT, GameUI.PIP_MAX_HEIGHT,
				Env.pipSceneHeightPy.get());
		sliderPiPSceneHeight.valueProperty()
				.addListener((obs, oldValue, newValue) -> Env.pipSceneHeightPy.set(newValue.doubleValue()));
		sliderPiPOpacity = addSlider("PiP Transparency", 0.0, 1.0, Env.pipOpacityPy.get());
		sliderPiPOpacity.valueProperty()
				.addListener((obs, oldValue, newValue) -> Env.pipOpacityPy.set(newValue.doubleValue()));
		sliderWallHeight = addSlider("Wall Height", 0.1, 8.5, Env.d3_mazeWallHeightPy.get());
		sliderWallHeight.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.d3_mazeWallHeightPy.set(newVal.doubleValue()));
		sliderWallThickness = addSlider("Wall Thickness", 0.1, 2.0, Env.d3_mazeWallThicknessPy.get());
		sliderWallThickness.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.d3_mazeWallThicknessPy.set(newVal.doubleValue()));
		cbEnergizerExplodes = addCheckBox("Energizer Explosion", () -> Ufx.toggle(Env.d3_energizerExplodesPy));
		cbPacLighted = addCheckBox("Pac-Man Lighted", () -> Ufx.toggle(Env.d3_pacLightedPy));
		cbPacWalkingAnimated = addCheckBox("Pac-Man Animated", () -> Ufx.toggle(Env.d3_pacWalkingAnimatedPy));
		cbAxesVisible = addCheckBox("Show Axes", () -> Ufx.toggle(Env.d3_axesVisiblePy));
		cbWireframeMode = addCheckBox("Wireframe Mode", Actions::toggleDrawMode);
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(Env.d3_perspectivePy.get());
		sliderPiPSceneHeight.setValue(Env.pipSceneHeightPy.get());
		sliderPiPOpacity.setValue(Env.pipOpacityPy.get());
		sliderWallHeight.setValue(Env.d3_mazeWallHeightPy.get());
		cbEnergizerExplodes.setSelected(Env.d3_energizerExplodesPy.get());
		cbPacLighted.setSelected(Env.d3_pacLightedPy.get());
		cbPacWalkingAnimated.setSelected(Env.d3_pacWalkingAnimatedPy.get());
		cbAxesVisible.setSelected(Env.d3_axesVisiblePy.get());
		cbWireframeMode.setSelected(Env.d3_drawModePy.get() == DrawMode.LINE);
	}
}