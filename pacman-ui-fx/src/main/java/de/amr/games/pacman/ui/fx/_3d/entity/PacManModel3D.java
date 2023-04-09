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

package de.amr.games.pacman.ui.fx._3d.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx._3d.Model3DException;

/**
 * @author Armin Reichert
 */
public class PacManModel3D {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public static final String MODEL_ID_PAC = "Pac";
	public static final String MESH_ID_EYES = "Sphere.008_Sphere.010_grey_wall";
	public static final String MESH_ID_HEAD = "Sphere_yellow_packman";
	public static final String MESH_ID_PALATE = "Sphere_grey_wall";

	public static final String MODEL_ID_GHOST = "Ghost";
	public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	public static final String MESH_ID_GHOST_EYE_BALLS = "Sphere.009_Sphere.036_white";
	public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	public static final String MODEL_ID_PELLET = "Pellet";
	public static final String MESH_ID_PELLET = "Fruit";

	private static final Map<String, Model3D> MODELS = new HashMap<>();

	/**
	 * Called on start to load all 3D models at once.
	 */
	public static void load() {
		var start = System.nanoTime();
		LOG.info("Loading 3D models...");
		MODELS.put(MODEL_ID_PAC, new Model3D("model3D/pacman.obj"));
		MODELS.put(MODEL_ID_GHOST, new Model3D("model3D/ghost.obj"));
		MODELS.put(MODEL_ID_PELLET, new Model3D("model3D/12206_Fruit_v1_L3.obj"));
		var duration = System.nanoTime() - start;
		LOG.info("Loading 3D models done (%.2f milliseconds).", duration / 1_000_000f);
	}

	public static Model3D model(String id) {
		if (!MODELS.containsKey(id)) {
			throw new Model3DException("Did not find 3D model '%s'", id);
		}
		return MODELS.get(id);
	}
}