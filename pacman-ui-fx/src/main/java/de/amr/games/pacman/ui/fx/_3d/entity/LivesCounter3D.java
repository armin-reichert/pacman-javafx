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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

/**
 * Displays a Pac-Man shape for each live remaining.
 * 
 * @author Armin Reichert
 */
public class LivesCounter3D {

	private static final Color EYES_COLOR = Color.rgb(60, 60, 60);
	private static final Color PALATE_COLOR = Color.rgb(60, 60, 60);
	private static final Color HEAD_COLOR = Color.rgb(255, 255, 0);
	private static final int MAX_LIVES_DISPLAYED = 5;

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	private final Group root = new Group();

	public LivesCounter3D(boolean facingRight) {
		for (int i = 0; i < MAX_LIVES_DISPLAYED; ++i) {
			var pacRoot = Pac3D.createTG(HEAD_COLOR, EYES_COLOR, PALATE_COLOR);
			pacRoot.setTranslateX(2.0 * i * TS);
			if (facingRight) {
				pacRoot.setRotationAxis(Rotate.Z_AXIS);
				pacRoot.setRotate(180);
			}
			Pac3D.head(pacRoot).drawModeProperty().bind(drawModePy);
			root.getChildren().add(pacRoot);
		}
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
		for (int i = 0; i < MAX_LIVES_DISPLAYED; ++i) {
			root.getChildren().get(i).setVisible(i < numLives);
		}
	}
}