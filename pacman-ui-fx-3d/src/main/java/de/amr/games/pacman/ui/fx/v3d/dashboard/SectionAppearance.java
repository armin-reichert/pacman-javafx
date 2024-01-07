/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI;
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

	public SectionAppearance(PacManGames3dUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		pickerLightColor = addColorPicker("Light Color", PacManGames3dApp.PY_3D_LIGHT_COLOR.get());
		pickerLightColor.setOnAction(e -> PacManGames3dApp.PY_3D_LIGHT_COLOR.set(pickerLightColor.getValue()));
		pickerFloorColor = addColorPicker("Floor Color", PacManGames3dApp.PY_3D_FLOOR_COLOR.get());
		pickerFloorColor.setOnAction(e -> PacManGames3dApp.PY_3D_FLOOR_COLOR.set(pickerFloorColor.getValue()));
		comboFloorTexture = addComboBox("Floor Texture", floorTextureComboBoxEntries());
		comboFloorTexture.setOnAction(e -> PacManGames3dApp.PY_3D_FLOOR_TEXTURE.set(comboFloorTexture.getValue()));
		cbFloorTextureRandom = addCheckBox("Random Floor Texture", () -> Ufx.toggle(PacManGames3dApp.PY_3D_FLOOR_TEXTURE_RND));
	}

	@Override
	public void update() {
		comboFloorTexture.setValue(PacManGames3dApp.PY_3D_FLOOR_TEXTURE.get());
		cbFloorTextureRandom.setSelected(PacManGames3dApp.PY_3D_FLOOR_TEXTURE_RND.get());
	}

	private String[] floorTextureComboBoxEntries() {
		var names = new String[] { "hexagon", "knobs", "plastic", "wood" };
		var entries = new String[names.length + 1];
		entries[0] = PacManGames3dApp.NO_TEXTURE;
		System.arraycopy(names, 0, entries, 1, names.length);
		return entries;
	}
}