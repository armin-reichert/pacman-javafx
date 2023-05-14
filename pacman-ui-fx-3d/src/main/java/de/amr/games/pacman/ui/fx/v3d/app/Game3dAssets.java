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

import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.entity.GhostModel3D;
import de.amr.games.pacman.ui.fx.v3d.entity.PacModel3D;
import de.amr.games.pacman.ui.fx.v3d.entity.PelletModel3D;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

public class Game3dAssets {

	public static final String KEY_NO_TEXTURE = "No Texture";

	public final PacModel3D pacModel3D;
	public final GhostModel3D ghostModel3D;
	public final PelletModel3D pelletModel3D;
	public final Background wallpaper3D;
	public final Map<String, PhongMaterial> floorTexturesByName = new LinkedHashMap<>();
	public final ResourceBundle messages;
	private final Picker<String> messagePickerCheating;
	private final Picker<String> messagePickerLevelComplete;
	private final Picker<String> messagePickerGameOver;

	Game3dAssets() {
		messages = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.v3d.texts.messages");
		messagePickerCheating = ResourceManager.createPicker(messages, "cheating");
		messagePickerLevelComplete = ResourceManager.createPicker(messages, "level.complete");
		messagePickerGameOver = ResourceManager.createPicker(messages, "game.over");

		wallpaper3D = Game3d.ASSET_MANAGER.imageBackground("graphics/sky.png");
		floorTexturesByName.put("Hexagon", createFloorTexture("hexagon", "jpg"));
		floorTexturesByName.put("Knobs & Bumps", createFloorTexture("knobs", "jpg"));
		floorTexturesByName.put("Plastic", createFloorTexture("plastic", "jpg"));
		floorTexturesByName.put("Wood", createFloorTexture("wood", "jpg"));

		pacModel3D = new PacModel3D();
		ghostModel3D = new GhostModel3D();
		pelletModel3D = new PelletModel3D();
	}

	public String pickCheatingMessage() {
		return messagePickerCheating.next();
	}

	public String pickGameOverMessage() {
		return messagePickerGameOver.next();
	}

	public String pickLevelCompleteMessage(int levelNumber) {
		return "%s%n%n%s".formatted(messagePickerLevelComplete.next(), fmtMessage(messages, "level_complete", levelNumber));
	}

	private PhongMaterial createFloorTexture(String textureBase, String ext) {
		var material = textureMaterial(textureBase, ext, null, null);
		material.diffuseColorProperty().bind(Game3d.d3_floorColorPy);
		return material;
	}

	public PhongMaterial textureMaterial(String textureBase, String ext, Color diffuseColor, Color specularColor) {
		var texture = new PhongMaterial();
		texture.setBumpMap(Game3d.ASSET_MANAGER.image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
		texture.setDiffuseMap(Game3d.ASSET_MANAGER.image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
		texture.setDiffuseColor(diffuseColor);
		texture.setSpecularColor(specularColor);
		return texture;
	}
}