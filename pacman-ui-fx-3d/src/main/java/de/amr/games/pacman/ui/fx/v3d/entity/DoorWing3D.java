/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui.fx.v3d.animation.ColorChangeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;

/**
 * Left/right wing of ghost house door.
 *
 * @author Armin Reichert
 */
public class DoorWing3D {

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final ObjectProperty<PhongMaterial> barMaterialPy = new SimpleObjectProperty<>(this, "barMaterial",
			new PhongMaterial(Color.GRAY));

	private final Group root = new Group();

	private final Transition doorAnimation;

	private final Color color;

	public DoorWing3D(Vector2i tile, Color color) {
		checkTileNotNull(tile);
		checkNotNull(color);

		this.color = color;
		barMaterialPy.set(new PhongMaterial(color));

		for (int i = 0; i < 2; ++i) {
			var verticalBar = new Cylinder(1, 8);
			verticalBar.materialProperty().bind(barMaterialPy);
			double x = tile.x() * 8 + i * 4 + 2;
			double y = tile.y() * 8 + 4;
			verticalBar.setTranslateX(x);
			verticalBar.setTranslateY(y);
			verticalBar.setTranslateZ(-4);
			verticalBar.setRotationAxis(Rotate.X_AXIS);
			verticalBar.setRotate(90);
			verticalBar.drawModeProperty().bind(drawModePy);
			root.getChildren().add(verticalBar);
		}

		var horizontalBar = new Cylinder(0.5, 9);
		horizontalBar.materialProperty().bind(barMaterialPy);
		horizontalBar.setTranslateX(tile.x() * 8 + 4);
		horizontalBar.setTranslateY(tile.y() * 8 + 4);
		horizontalBar.setTranslateZ(-4);
		horizontalBar.setRotationAxis(Rotate.Z_AXIS);
		horizontalBar.setRotate(90);
		root.getChildren().add(horizontalBar);

		var fadeOut = new ColorChangeTransition(Duration.seconds(0.2),
			color, Color.TRANSPARENT, barMaterialPy.get().diffuseColorProperty()
		);
		var fadeIn = new ColorChangeTransition(Duration.seconds(0.6),
			Color.TRANSPARENT, color, barMaterialPy.get().diffuseColorProperty()
		);
		fadeIn.setDelay(Duration.seconds(0.2));
		doorAnimation = new SequentialTransition(fadeOut, fadeIn);
	}

	public Node getRoot() {
		return root;
	}

	public void playTraversalAnimation() {
		doorAnimation.play(); // if already running, does nothing
	}
}