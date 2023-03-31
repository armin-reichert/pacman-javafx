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

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.CollapseAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.TurningAnimation;
import javafx.animation.Animation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

/**
 * 3D-representation of Pac-Man or Ms. Pac-Man.
 * 
 * <p>
 * Missing: Specific 3D-model for Ms. Pac-Man, mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D {

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);

	private final Pac pac;
	private final TurningAnimation turningAnimation;
	private final Group root;
	private Color headColor;

	public Pac3D(Pac pac, Group root, Color headColor) {
		this.pac = pac;
		this.root = root;
		this.turningAnimation = new TurningAnimation(root, pac::moveDir);
		this.headColor = headColor;
		Stream.of(PacShape3D.head(root), PacShape3D.eyes(root), PacShape3D.palate(root))
				.forEach(part -> part.drawModeProperty().bind(drawModePy));
		init();
	}

	public Node getRoot() {
		return root;
	}

	public void init() {
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		turningAnimation.init();
		turningAnimation.update();
		headColorPy.set(headColor);
	}

	public void update(GameLevel level) {
		turningAnimation.update();
		if (outsideWorld(level.world())) {
			root.setVisible(false);
		} else {
			root.setVisible(pac.isVisible());
		}
		root.setTranslateX(pac.center().x());
		root.setTranslateY(pac.center().y());
		root.setTranslateZ(-HTS);
	}

	private boolean outsideWorld(World world) {
		double centerX = pac.position().x() + HTS;
		return centerX < HTS || centerX > world.numCols() * TS - HTS;
	}

	/**
	 * TODO Provide different animation for Ms. Pac-Man (rotation like in 2D scene)
	 * 
	 * @return dying animation (must not be longer than time reserved by game controller which is 5 seconds!)
	 */
	public Animation createDyingAnimation() {
		return new CollapseAnimation(root).getAnimation();
	}
}