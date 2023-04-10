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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

/**
 * Displays a Pac-Man shape for each live remaining.
 * 
 * @author Armin Reichert
 */
public class LivesCounter3D {

	private static final Color HAIRBOW_COLOR = Color.rgb(255, 0, 0);
	private static final Color HAIRBOW_PEARLS_COLOR = Color.rgb(0, 0, 255);
	private static final Color HEAD_COLOR = Color.YELLOW;
	private static final Color EYES_COLOR = Color.rgb(120, 120, 120);
	private static final Color PALATE_COLOR = Color.rgb(120, 120, 120);

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	private final Group root = new Group();
	private final Group pacGroup = new Group();
	private final Group socketGroup = new Group();
	private final PhongMaterial socketMaterial;

	public LivesCounter3D(GameVariant variant, int maxLives, Model3D model3D) {

		socketGroup.setTranslateZ(0);
		pacGroup.setTranslateZ(-2);

		root.getChildren().addAll(socketGroup, pacGroup);

		socketMaterial = ResourceMgr.coloredMaterial(Color.rgb(180, 180, 180));
		for (int i = 0; i < maxLives; ++i) {
			addSocket(2 * i * TS);
		}

		switch (variant) {
		case MS_PACMAN -> {
			for (int i = 0; i < maxLives; ++i) {
				var pac = PacShape3D.createMsPacManShape(model3D, 9, HEAD_COLOR, EYES_COLOR, PALATE_COLOR, HAIRBOW_COLOR,
						HAIRBOW_PEARLS_COLOR);
				pac.setTranslateX(2.0 * i * TS);
				pac.setTranslateZ(-3.2);
				pac.setRotationAxis(Rotate.Z_AXIS);
				pac.setRotate(180);
				pacGroup.getChildren().add(pac);
			}
		}
		case PACMAN -> {
			for (int i = 0; i < maxLives; ++i) {
				addSocket(2.0 * i * TS);

				var pac = PacShape3D.createPacManShape(model3D, 9, HEAD_COLOR, EYES_COLOR, PALATE_COLOR);
				pac.setTranslateX(2.0 * i * TS);
				pac.setTranslateZ(-3.2);
				pacGroup.getChildren().add(pac);
			}
		}
		default -> throw new IllegalArgumentException();
		}
	}

	private void addSocket(double x) {
		var socket = new Cylinder(4, 1);
		socket.setMaterial(socketMaterial);
		socket.setTranslateX(x);
		socket.setRotationAxis(Rotate.X_AXIS);
		socket.setRotate(90);
		socket.drawModeProperty().bind(Env.d3_drawModePy);
		socketGroup.getChildren().add(socket);

		var pillar = new Cylinder(1, 5);
		pillar.setMaterial(socketMaterial);
		pillar.setTranslateX(x);
		pillar.setTranslateZ(3);
		pillar.setRotationAxis(Rotate.X_AXIS);
		pillar.setRotate(90);
		pillar.drawModeProperty().bind(Env.d3_drawModePy);
		socketGroup.getChildren().add(pillar);
	}

	public Node getRoot() {
		return root;
	}

	public void setPosition(double x, double y, double z) {
		root.setTranslateX(x);
		root.setTranslateY(y);
		root.setTranslateZ(z);
	}

	public void update(int numLives) {
		for (int i = 0; i < pacGroup.getChildren().size(); ++i) {
			var node = pacGroup.getChildren().get(i);
			node.setVisible(i < numLives);
		}
	}
}