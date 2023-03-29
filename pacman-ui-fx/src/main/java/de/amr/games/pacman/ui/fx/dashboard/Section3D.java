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

import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

/**
 * 3D related settings.
 * 
 * @author Armin Reichert
 */
public class Section3D extends Section {

	private final CheckBox cbSquirting;
	private final Slider sliderWallHeight;
	private final Slider sliderWallThickness;
	private final ColorPicker pickerLightColor;
	private final ComboBox<String> comboFloorTexture;
	private final ColorPicker pickerFloorColor;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;
	private final CheckBox cbPacLighted;

	public Section3D(GameUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		pickerLightColor = addColorPicker("Light color", Env.d3_lightColorPy.get());
		pickerLightColor.setOnAction(e -> Env.d3_lightColorPy.set(pickerLightColor.getValue()));
		sliderWallHeight = addSlider("Wall height", 0.1, 10.0, Env.d3_mazeWallHeightPy.get());
		sliderWallHeight.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.d3_mazeWallHeightPy.set(newVal.doubleValue()));
		sliderWallThickness = addSlider("Wall thickness", 0.1, 2.0, Env.d3_mazeWallThicknessPy.get());
		sliderWallThickness.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.d3_mazeWallThicknessPy.set(newVal.doubleValue()));
		comboFloorTexture = addComboBox("Floor texture", ResourceMgr.floorTextureKeys());
		comboFloorTexture.setOnAction(e -> Env.d3_floorTexturePy.set(comboFloorTexture.getValue()));
		pickerFloorColor = addColorPicker("Floor color", Env.d3_floorColorPy.get());
		pickerFloorColor.setOnAction(e -> Env.d3_floorColorPy.set(pickerFloorColor.getValue()));
		cbSquirting = addCheckBox("Energizer Eaten Animation", () -> Ufx.toggle(Env.d3_eatingEnergizerAnimatedPy));
		cbPacLighted = addCheckBox("Pac-Man lighted", () -> Ufx.toggle(Env.d3_pacLightedPy));
		cbAxesVisible = addCheckBox("Show axes", () -> Ufx.toggle(Env.d3_axesVisiblePy));
		cbWireframeMode = addCheckBox("Wireframe mode", Actions::toggleDrawMode);
	}

	@Override
	public void update() {
		super.update();
		var no3D = !gameScene().is3D();
		pickerLightColor.setDisable(no3D);
		sliderWallHeight.setDisable(no3D);
		sliderWallHeight.setValue(Env.d3_mazeWallHeightPy.get());
		sliderWallThickness.setDisable(no3D);
		comboFloorTexture.setDisable(no3D);
		comboFloorTexture.setValue(Env.d3_floorTexturePy.get());
		pickerFloorColor.setDisable(no3D);
		cbSquirting.setDisable(no3D);
		cbSquirting.setSelected(Env.d3_eatingEnergizerAnimatedPy.get());
		cbPacLighted.setDisable(no3D);
		cbPacLighted.setSelected(Env.d3_pacLightedPy.get());
		cbAxesVisible.setDisable(no3D);
		cbAxesVisible.setSelected(Env.d3_axesVisiblePy.get());
		cbWireframeMode.setDisable(no3D);
		cbWireframeMode.setSelected(Env.d3_drawModePy.get() == DrawMode.LINE);
	}
}