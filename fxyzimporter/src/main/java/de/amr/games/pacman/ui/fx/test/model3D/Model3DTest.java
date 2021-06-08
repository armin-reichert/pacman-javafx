package de.amr.games.pacman.ui.fx.test.model3D;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Stream;

import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.importers.Model3D;

import de.amr.games.pacman.ui.fx.model3D.GianmarcosPacManModel3D;
import javafx.scene.paint.Material;

/**
 * Note: To be able to access the private field "materials" of class {@link Model3D} the following
 * VM arg is needed:
 * 
 * <pre>
 --add-opens org.fxyz3d.importers/org.fxyz3d.importers=de.amr.games.pacman.ui.fx.fxyzimporter
 * </pre>
 */
public class Model3DTest {

	private static void log(String fmt, Object... args) {
		System.out.println(String.format(fmt, args));
	}

	public static void main(String[] args) {
		loadUsingInteractiveMeshImporter();
		log("----------------------------");
		loadUsingFxyzImporter();
	}

	private static void loadUsingFxyzImporter() {
		try {
			Model3D model3D = Importer3D.load(Model3DTest.class.getResource("/pacman.obj"));
			log("FXyz OBJ importer:");
			log("MeshViews:");
			model3D.getMeshNames().stream().sorted().forEach(key -> log("%s", key));
			log("");
			log("Materials:");
			getMaterialNames(model3D).sorted().forEach(m -> log("%s", m));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Stream<String> getMaterialNames(Model3D model3D) {
		try {
			Field f = Model3D.class.getDeclaredField("materials");
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, Material> materials = (Map<String, Material>) f.get(model3D);
			return materials.keySet().stream();
		} catch (Exception e) {
			e.printStackTrace();
			return Stream.empty();
		}
	}

	private static void loadUsingInteractiveMeshImporter() {
		GianmarcosPacManModel3D model = new GianmarcosPacManModel3D();
		log("InteractiveMesh OBJ importer:");
		log("MeshViews:");
		model.meshViewsByName.keySet().stream().sorted().forEach(key -> log("%s", key));
		log("");
		log("Materials:");
		model.materialsByName.keySet().stream().sorted().forEach(key -> log("%s", key));
	}
}