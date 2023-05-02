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

package de.amr.games.pacman.ui.fx3d.app;

import static de.amr.games.pacman.lib.Globals.randomInt;

import java.util.LinkedHashMap;
import java.util.Map;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.util.ResourceMgr;
import de.amr.games.pacman.ui.fx3d._3d.Model3D;
import de.amr.games.pacman.ui.fx3d._3d.entity.PacModel3D;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 * @author Armin Reichert
 */
public class AppRes3d {

	public static final ResourceMgr Manager = new ResourceMgr("/assets3d/");

	public static void load() {
		long start = System.nanoTime();
		load("3D models", Models3D::load);
		load("3D textures", Textures::load);
		Logger.info("Loading resources took {} seconds.", (System.nanoTime() - start) / 1e9f);
	}

	private static void load(String section, Runnable loadingCode) {
		long start = System.nanoTime();
		loadingCode.run();
		Logger.info("Loading {} done ({} seconds).", section, (System.nanoTime() - start) / 1e9f);
	}

	public static class Models3D {

		public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
		public static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
		public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";
		public static final String MESH_ID_PELLET = "Fruit";

		public static PacModel3D pacModel3D;
		public static Model3D ghostModel3D;
		public static Model3D pelletModel3D;

		static void load() {
			pacModel3D = new PacModel3D(Manager.urlFromRelPath("model3D/pacman.obj"));
			ghostModel3D = new Model3D(Manager.urlFromRelPath("model3D/ghost.obj"));
			pelletModel3D = new Model3D(Manager.urlFromRelPath("model3D/12206_Fruit_v1_L3.obj"));
		}
	}

	public static class Textures {

		public static final String KEY_NO_TEXTURE = "No Texture";

		public static Background backgroundForScene3D;
		private static Map<String, PhongMaterial> floorTexturesByName = new LinkedHashMap<>();

		static void load() {
			backgroundForScene3D = new Background(
					new BackgroundImage(Manager.image("graphics/sky.png"), null, null, null, null));

			loadFloorTexture("Hexagon", "hexagon", "jpg");
			loadFloorTexture("Knobs & Bumps", "knobs", "jpg");
			loadFloorTexture("Plastic", "plastic", "jpg");
			loadFloorTexture("Wood", "wood", "jpg");
		}

		private static void loadFloorTexture(String name, String textureBase, String ext) {
			var material = new PhongMaterial();
			material.setBumpMap(Manager.image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
			material.setDiffuseMap(Manager.image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
			material.diffuseColorProperty().bind(Env3d.d3_floorColorPy);
			floorTexturesByName.put(name, material);
		}

		public static PhongMaterial floorTexture(String name) {
			return floorTexturesByName.get(name);
		}

		public static String[] floorTextureNames() {
			return floorTexturesByName.keySet().toArray(String[]::new);
		}

		public static String randomFloorTextureName() {
			var names = floorTextureNames();
			return names[randomInt(0, names.length)];
		}

		public static PhongMaterial texture(String textureBase, String ext, Color diffuseColor, Color specularColor) {
			var texture = new PhongMaterial();
			texture.setBumpMap(Manager.image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
			texture.setDiffuseMap(Manager.image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
			texture.setDiffuseColor(diffuseColor);
			texture.setSpecularColor(specularColor);
			return texture;
		}
	}
}