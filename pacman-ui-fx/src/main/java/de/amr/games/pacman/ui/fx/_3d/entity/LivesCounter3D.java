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

	private static final Color HAIRBOW_COLOR = Color.rgb(255, 0, 0);
	private static final Color HAIRBOW_PEARLS_COLOR = Color.rgb(0, 0, 255);
	private static final Color HEAD_COLOR = Color.rgb(255, 255, 0);
	private static final Color EYES_COLOR = Color.rgb(120, 120, 120);
	private static final Color PALATE_COLOR = Color.rgb(120, 120, 120);

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	private final Group root = new Group();
	private final int maxLives;

	public LivesCounter3D(GameVariant variant, int maxLives) {
		this.maxLives = maxLives;
		switch (variant) {
		case MS_PACMAN -> {
			for (int i = 0; i < maxLives; ++i) {
				var msPac = PacShape3D.createMsPacManShape(9, HEAD_COLOR, EYES_COLOR, PALATE_COLOR, HAIRBOW_COLOR,
						HAIRBOW_PEARLS_COLOR);
				msPac.setTranslateX(2.0 * i * TS);
				msPac.setRotationAxis(Rotate.Z_AXIS);
				msPac.setRotate(180);
				root.getChildren().add(msPac);
			}
		}
		case PACMAN -> {
			for (int i = 0; i < maxLives; ++i) {
				var pac = PacShape3D.createPacManShape(9, HEAD_COLOR, EYES_COLOR, PALATE_COLOR);
				pac.setTranslateX(2.0 * i * TS);
				root.getChildren().add(pac);
			}
		}
		default -> throw new IllegalArgumentException();
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
		for (int i = 0; i < maxLives; ++i) {
			root.getChildren().get(i).setVisible(i < numLives);
		}
	}
}