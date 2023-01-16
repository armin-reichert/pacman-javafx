/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.ui.fx.Actions;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;

/**
 * 3D related settings.
 * 
 * @author Armin Reichert
 */
public class Section3D extends Section {

	private final CheckBox cbSquirting;
	private final ComboBox<Integer> comboResolution;
	private final Slider sliderWallHeight;
	private final Slider sliderWallThickness;
	private final ColorPicker pickerLightColor;
	private final ComboBox<String> comboFloorTexture;
	private final ColorPicker pickerFloorColor;
	private final CheckBox cbAxesVisible;
	private final CheckBox cbWireframeMode;
	private final CheckBox cbPacLighted;

	public Section3D(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);
		pickerLightColor = addColorPicker("Light color", Env.lightColorPy.get());
		pickerLightColor.setOnAction(e -> Env.lightColorPy.set(pickerLightColor.getValue()));
		comboResolution = addComboBox("Maze resolution", 1, 2, 4, 8);
		comboResolution.setOnAction(e -> Env.mazeResolutionPy.set(comboResolution.getValue()));
		sliderWallHeight = addSlider("Wall height", 0.1, 10.0, Env.mazeWallHeightPy.get());
		sliderWallHeight.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.mazeWallHeightPy.set(newVal.doubleValue()));
		sliderWallThickness = addSlider("Wall thickness", 0.1, 2.0, Env.mazeWallThicknessPy.get());
		sliderWallThickness.valueProperty()
				.addListener((obs, oldVal, newVal) -> Env.mazeWallThicknessPy.set(newVal.doubleValue()));
		comboFloorTexture = addComboBox("Floor texture", Env.FLOOR_TEXTURES.toArray(String[]::new));
		comboFloorTexture.setOnAction(e -> Env.floorTexturePy.set(comboFloorTexture.getValue()));
		pickerFloorColor = addColorPicker("Floor color", Env.floorColorPy.get());
		pickerFloorColor.setOnAction(e -> Env.floorColorPy.set(pickerFloorColor.getValue()));
		cbSquirting = addCheckBox("Squirting", () -> Env.toggle(Env.squirtingEffectPy));
		cbPacLighted = addCheckBox("Pac-Man lighted", () -> Env.toggle(Env.pac3DLightedPy));
		cbAxesVisible = addCheckBox("Show axes", () -> Env.toggle(Env.axesVisiblePy));
		cbWireframeMode = addCheckBox("Wireframe mode", Actions::toggleDrawMode);
	}

	@Override
	public void update() {
		super.update();
		comboResolution.setValue(Env.mazeResolutionPy.get());
		comboResolution.setDisable(!gameScene().is3D());
		sliderWallHeight.setValue(Env.mazeWallHeightPy.get());
		sliderWallHeight.setDisable(!gameScene().is3D());
		comboFloorTexture.setValue(Env.floorTexturePy.get());
		comboFloorTexture.setDisable(!gameScene().is3D());
		cbSquirting.setSelected(Env.squirtingEffectPy.get());
		cbPacLighted.setSelected(Env.pac3DLightedPy.get());
		cbAxesVisible.setSelected(Env.axesVisiblePy.get());
		cbAxesVisible.setDisable(!gameScene().is3D());
		cbWireframeMode.setSelected(Env.drawModePy.get() == DrawMode.LINE);
		cbWireframeMode.setDisable(!gameScene().is3D());
	}
}