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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 * @author Armin Reichert
 */
public class Game3dAssets extends ResourceManager {

	public static final String KEY_NO_TEXTURE = "No Texture";

	//@formatter:off
	public final ResourceBundle messages              = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.v3d.texts.messages");

	private final Picker<String> pickerReadyPacMan    = ResourceManager.createPicker(messages, "pacman.ready");
	private final Picker<String> pickerReadyMsPacMan  = ResourceManager.createPicker(messages, "mspacman.ready");
	private final Picker<String> pickerCheating       = ResourceManager.createPicker(messages, "cheating");
	private final Picker<String> pickerLevelComplete  = ResourceManager.createPicker(messages, "level.complete");
	private final Picker<String> pickerGameOver       = ResourceManager.createPicker(messages, "game.over");

	public final Model3D pacModel3D                   = new Model3D(urlFromRelPath("model3D/pacman.obj"));
	public final Model3D ghostModel3D                 = new Model3D(urlFromRelPath("model3D/ghost.obj"));
	public final Model3D pelletModel3D                = new Model3D(urlFromRelPath("model3D/12206_Fruit_v1_L3.obj"));

	public final Background wallpaper3D               = imageBackground("graphics/sky.png");
	
	public final Map<String, PhongMaterial> floorTexturesByName = new LinkedHashMap<>();
	{
		floorTexturesByName.put("Hexagon", createFloorTexture("hexagon", "jpg"));
		floorTexturesByName.put("Knobs & Bumps", createFloorTexture("knobs", "jpg"));
		floorTexturesByName.put("Plastic", createFloorTexture("plastic", "jpg"));
		floorTexturesByName.put("Wood", createFloorTexture("wood", "jpg"));
	}
	//@formatter:on

	public Game3dAssets() {
		super("/de/amr/games/pacman/ui/fx/v3d/", Game3dAssets.class);
	}

	public String pickFunnyReadyMessage(GameVariant gameVariant) {
		return switch (gameVariant) {
		case MS_PACMAN -> pickerReadyMsPacMan.next();
		case PACMAN -> pickerReadyPacMan.next();
		default -> throw new IllegalGameVariantException(gameVariant);
		};
	}

	public String pickCheatingMessage() {
		return pickerCheating.next();
	}

	public String pickGameOverMessage() {
		return pickerGameOver.next();
	}

	public String pickLevelCompleteMessage(int levelNumber) {
		return "%s%n%n%s".formatted(pickerLevelComplete.next(), fmtMessage(messages, "level_complete", levelNumber));
	}

	private PhongMaterial createFloorTexture(String textureBase, String ext) {
		var material = textureMaterial(textureBase, ext, null, null);
		material.diffuseColorProperty().bind(Game3d.d3_floorColorPy);
		return material;
	}

	public PhongMaterial textureMaterial(String textureBase, String ext, Color diffuseColor, Color specularColor) {
		var texture = new PhongMaterial();
		texture.setBumpMap(image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
		texture.setDiffuseMap(image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
		texture.setDiffuseColor(diffuseColor);
		texture.setSpecularColor(specularColor);
		return texture;
	}
}