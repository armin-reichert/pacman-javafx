/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell.info;

import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
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
	private ComboBox<Perspective> comboPerspective;
	private ComboBox<Integer> comboResolution;
	private Slider sliderWallHeight;
	private CheckBox cbUseFloorTexture;
	private CheckBox cbAxesVisible;
	private CheckBox cbWireframeMode;

	public Section3D(GameUI ui) {
		super(ui, "3D Settings");
		comboPerspective = addComboBox("Perspective", Perspective.values());
		comboPerspective.setOnAction(e -> Env.$perspective.set(comboPerspective.getValue()));
		comboResolution = addComboBox("Maze resolution", 1, 2, 4, 8);
		addInfo("Camera", () -> scene3D().getCamController().info()).when(() -> gameScene().is3D());
		comboResolution.setOnAction(e -> Env.$mazeResolution.set(comboResolution.getValue()));
		sliderWallHeight = addSlider("Maze wall height", 0, 10, 8);
		sliderWallHeight.valueProperty().addListener(($value, _old, _new) -> Env.$mazeWallHeight.set(_new.doubleValue()));
		cbUseFloorTexture = addCheckBox("Maze floor texture", ui::toggleUseMazeFloorTexture);
		cbAxesVisible = addCheckBox("Show axes", ui::toggleAxesVisible);
		cbWireframeMode = addCheckBox("Wireframe mode", ui::toggleDrawMode);
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(Env.$perspective.get());
		comboPerspective.setDisable(!ui.getCurrentGameScene().is3D());
		comboResolution.setValue(Env.$mazeResolution.get());
		comboResolution.setDisable(!ui.getCurrentGameScene().is3D());
		sliderWallHeight.setValue(Env.$mazeWallHeight.get());
		sliderWallHeight.setDisable(!ui.getCurrentGameScene().is3D());
		cbUseFloorTexture.setSelected(Env.$useMazeFloorTexture.get());
		cbUseFloorTexture.setDisable(!ui.getCurrentGameScene().is3D());
		cbAxesVisible.setSelected(Env.$axesVisible.get());
		cbAxesVisible.setDisable(!ui.getCurrentGameScene().is3D());
		cbWireframeMode.setSelected(Env.$drawMode3D.get() == DrawMode.LINE);
		cbWireframeMode.setDisable(!ui.getCurrentGameScene().is3D());
	}
}