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
import de.amr.games.pacman.ui.fx.v3d.app.Game3d;
import de.amr.games.pacman.ui.fx.v3d.app.Game3dUI;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;

/**
 * @author Armin Reichert
 */
public class SectionAppearance extends Section {

	private final ColorPicker pickerLightColor;
	private final ColorPicker pickerFloorColor;
	private final ComboBox<String> comboFloorTexture;
	private final CheckBox cbFloorTextureRandom;

	public SectionAppearance(Game3dUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		pickerLightColor = addColorPicker("Light Color", Game3d.d3_lightColorPy.get());
		pickerLightColor.setOnAction(e -> Game3d.d3_lightColorPy.set(pickerLightColor.getValue()));
		pickerFloorColor = addColorPicker("Floor Color", Game3d.d3_floorColorPy.get());
		pickerFloorColor.setOnAction(e -> Game3d.d3_floorColorPy.set(pickerFloorColor.getValue()));
		comboFloorTexture = addComboBox("Floor Texture", floorTextureComboBoxEntries());
		comboFloorTexture.setOnAction(e -> Game3d.d3_floorTexturePy.set(comboFloorTexture.getValue()));
		cbFloorTextureRandom = addCheckBox("Random Floor Texture", () -> Ufx.toggle(Game3d.d3_floorTextureRandomPy));
	}

	@Override
	public void update() {
		comboFloorTexture.setValue(Game3d.d3_floorTexturePy.get());
		cbFloorTextureRandom.setSelected(Game3d.d3_floorTextureRandomPy.get());
	}

	private String[] floorTextureComboBoxEntries() {
		var names = Game3d.assets.floorTexturesByName.keySet().toArray(String[]::new);
		var entries = new String[names.length + 1];
		entries[0] = Game3d.Assets.KEY_NO_TEXTURE;
		System.arraycopy(names, 0, entries, 1, names.length);
		return entries;
	}
}