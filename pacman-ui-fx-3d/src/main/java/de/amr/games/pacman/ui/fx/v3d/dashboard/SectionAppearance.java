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
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.app.GameApp3d;
import de.amr.games.pacman.ui.fx.v3d.app.GameUI3d;
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

	public SectionAppearance(GameUI3d ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		pickerBgColor = addColorPicker("Background color", Env.mainSceneBgColorPy.get());
		pickerBgColor.setOnAction(e -> Env.mainSceneBgColorPy.set(pickerBgColor.getValue()));
		pickerLightColor = addColorPicker("Light Color", GameApp3d.d3_lightColorPy.get());
		pickerLightColor.setOnAction(e -> GameApp3d.d3_lightColorPy.set(pickerLightColor.getValue()));
		pickerFloorColor = addColorPicker("Floor Color", GameApp3d.d3_floorColorPy.get());
		pickerFloorColor.setOnAction(e -> GameApp3d.d3_floorColorPy.set(pickerFloorColor.getValue()));
		comboFloorTexture = addComboBox("Floor Texture", textureItems());
		comboFloorTexture.setOnAction(e -> GameApp3d.d3_floorTexturePy.set(comboFloorTexture.getValue()));
		cbFloorTextureRandom = addCheckBox("Random Floor Texture", () -> Ufx.toggle(GameApp3d.d3_floorTextureRandomPy));
	}

	@Override
	public void update() {
		comboFloorTexture.setValue(GameApp3d.d3_floorTexturePy.get());
		cbFloorTextureRandom.setSelected(GameApp3d.d3_floorTextureRandomPy.get());
	}

	private String[] textureItems() {
		var textureKeys = GameApp3d.Textures.floorTextureNames();
		var items = new String[textureKeys.length + 1];
		items[0] = GameApp3d.Textures.KEY_NO_TEXTURE;
		System.arraycopy(textureKeys, 0, items, 1, textureKeys.length);
		return items;
	}
}