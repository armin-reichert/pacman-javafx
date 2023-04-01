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
import de.amr.games.pacman.ui.fx._3d.animation.CollapseAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.TurningAnimation;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

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
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private final GameLevel level;
	private final Pac pac;
	private final TurningAnimation turningAnimation;
	private final Group root;
	private final Color headColor;
	private final PointLight light;

	public Pac3D(GameLevel level, Pac pac, Group root, Color headColor) {
		this.level = level;
		this.pac = pac;
		this.root = root;
		this.turningAnimation = new TurningAnimation(root, pac::moveDir);
		this.headColor = headColor;
		Stream.of(PacShape3D.head(root), PacShape3D.eyes(root), PacShape3D.palate(root))
				.forEach(part -> part.drawModeProperty().bind(drawModePy));
		light = createLight();
		init();
	}

	public Node getRoot() {
		return root;
	}

	public PointLight getLight() {
		return light;
	}

	public void init() {
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		turningAnimation.init();
		turningAnimation.update();
		headColorPy.set(headColor);
	}

	public void update() {
		turningAnimation.update();
		if (outsideWorld()) {
			root.setVisible(false);
		} else {
			root.setVisible(pac.isVisible());
		}
		root.setTranslateX(pac.center().x());
		root.setTranslateY(pac.center().y());
		root.setTranslateZ(-HTS);
		updateLight();
	}

	public Animation createDyingAnimation() {
		var variant = level.game().variant();
		return switch (variant) {
		case MS_PACMAN -> createRotatingHeadAnimation();
		case PACMAN -> new SequentialTransition(Ufx.pause(0.25), new CollapseAnimation(root).getAnimation());
		default -> throw new IllegalArgumentException("Unknown game variant: %s".formatted(variant));
		};
	}

	private Animation createRotatingHeadAnimation() {
		var layOnBack = new RotateTransition(Duration.seconds(0.1), root);
		layOnBack.setAxis(Rotate.Y_AXIS);
		layOnBack.setFromAngle(0);
		layOnBack.setToAngle(-90);

		var spin = new RotateTransition(Duration.seconds(0.25), root);
		spin.setAxis(Rotate.Y_AXIS);
		spin.setByAngle(-360);
		spin.setCycleCount(4);
		spin.setDelay(Duration.seconds(0.35));

		return new SequentialTransition(layOnBack, spin, Ufx.pause(2.0));
	}

	private PointLight createLight() {
		var pointLight = new PointLight();
		pointLight.setColor(Color.rgb(255, 255, 0, 0.25));
		pointLight.setMaxRange(2 * TS);
		pointLight.translateXProperty().bind(root.translateXProperty());
		pointLight.translateYProperty().bind(root.translateYProperty());
		pointLight.setTranslateZ(-TS);
		return pointLight;
	}

	private void updateLight() {
		boolean isVisible = pac.isVisible();
		boolean isAlive = !pac.isDead();
		boolean hasPower = pac.powerTimer().isRunning();
		var maxRange = pac.isPowerFading(level) ? 4 : 8;
		light.setLightOn(lightedPy.get() && isVisible && isAlive && hasPower);
		light.setMaxRange(hasPower ? maxRange * TS : 0);
	}

	private boolean outsideWorld() {
		double centerX = pac.position().x() + HTS;
		return centerX < HTS || centerX > level.world().numCols() * TS - HTS;
	}
}