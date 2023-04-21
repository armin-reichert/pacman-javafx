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
package de.amr.games.pacman.ui.fx.dashboard;

import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;

/**
 * @author Armin Reichert
 */
public class SectionAppearance extends Section {

	private final ColorPicker pickerBgColor;
	private final ColorPicker pickerLightColor;
	private final ColorPicker pickerFloorColor;
	private final ComboBox<String> comboFloorTexture;
	private final CheckBox cbFloorTextureRandom;

	public SectionAppearance(GameUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		pickerBgColor = addColorPicker("Background color", Env.mainSceneBgColorPy.get());
		pickerBgColor.setOnAction(e -> Env.mainSceneBgColorPy.set(pickerBgColor.getValue()));
		pickerLightColor = addColorPicker("Light Color", Env.d3_lightColorPy.get());
		pickerLightColor.setOnAction(e -> Env.d3_lightColorPy.set(pickerLightColor.getValue()));
		pickerFloorColor = addColorPicker("Floor Color", Env.d3_floorColorPy.get());
		pickerFloorColor.setOnAction(e -> Env.d3_floorColorPy.set(pickerFloorColor.getValue()));
		comboFloorTexture = addComboBox("Floor Texture", textureItems());
		comboFloorTexture.setOnAction(e -> Env.d3_floorTexturePy.set(comboFloorTexture.getValue()));
		cbFloorTextureRandom = addCheckBox("Random Floor Texture", () -> Ufx.toggle(Env.d3_floorTextureRandomPy));
	}

	@Override
	public void update() {
		comboFloorTexture.setValue(Env.d3_floorTexturePy.get());
		cbFloorTextureRandom.setSelected(Env.d3_floorTextureRandomPy.get());
	}

	private String[] textureItems() {
		var textureKeys = AppRes.Graphics.textureNames();
		var items = new String[textureKeys.length + 1];
		items[0] = AppRes.Graphics.KEY_NO_TEXTURE;
		System.arraycopy(textureKeys, 0, items, 1, textureKeys.length);
		return items;
	}
}