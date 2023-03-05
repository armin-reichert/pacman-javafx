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

import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.Creature3DMovement;
import de.amr.games.pacman.ui.fx._3d.animation.PacDyingAnimation;
import javafx.animation.Animation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
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
public class Pac3D extends Group {

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", HEAD_COLOR);
	public final BooleanProperty lightOnPy = new SimpleBooleanProperty(this, "lightOn", true);

	private static final Color HEAD_COLOR = Color.YELLOW;
	private static final Color EYES_COLOR = Color.rgb(33, 33, 33);
	private static final Color PALATE_COLOR = Color.rgb(191, 79, 61);

	private final World world;
	private final Pac pac;
	private final Creature3DMovement movement;
	private final Node root;
	private final PointLight spot;

	public Pac3D(Pac pac, World world) {
		this.pac = pac;
		this.world = world;
		movement = new Creature3DMovement(this, pac);
		root = PacModel3D.createPac3D(EYES_COLOR, PALATE_COLOR);
		Stream.of(PacModel3D.head(root), PacModel3D.eyes(root), PacModel3D.palate(root)).forEach(shape -> {
			shape.drawModeProperty().bind(drawModePy);
		});
		getChildren().add(root);

		spot = new PointLight();
		spot.setColor(Color.rgb(255, 255, 0, 0.25));
		spot.setMaxRange(8 * TS);
		spot.setTranslateZ(0);
		getChildren().add(spot);
	}

	public void init() {
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		root.setTranslateZ(0);
		headColorPy.set(HEAD_COLOR);
		movement.init();
		update();
	}

	public void update() {
		movement.update();
		if (outsideWorld(world, pac)) {
			setVisible(false);
		} else {
			setVisible(pac.isVisible());
		}
		spot.setLightOn(lightOnPy.get() && pac.isVisible() && !pac.isDead());
	}

	private boolean outsideWorld(World world, Creature guy) {
		double centerX = guy.position().x() + HTS;
		return centerX < HTS || centerX > world.numCols() * TS - HTS;
	}

	/**
	 * @param killingGhostColor color of ghost that killed Pac-Man
	 * @return dying animation (must not be longer than time reserved by game controller which is 5 seconds!)
	 */
	public Animation createDyingAnimation(Color killingGhostColor) {
		return new PacDyingAnimation(root, headColorPy, HEAD_COLOR, killingGhostColor).getAnimation();
	}
}