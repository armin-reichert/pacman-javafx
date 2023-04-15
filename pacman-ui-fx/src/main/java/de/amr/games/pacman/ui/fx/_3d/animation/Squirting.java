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
package de.amr.games.pacman.ui.fx._3d.animation;

import static de.amr.games.pacman.lib.U.randomInt;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx.util.Vector3f;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public abstract class Squirting extends Transition {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Random RND = new Random();

	private static final short MIN_DROP_COUNT = 20;
	private static final short MAX_DROP_COUNT = 40;

	private static final float MIN_DROP_RADIUS = 0.1f;
	private static final float MAX_DROP_RADIUS = 1.0f;

	private static final Vector3f MIN_VELOCITY = new Vector3f(-0.25f, -0.25f, -4.0f);
	private static final Vector3f MAX_VELOCITY = new Vector3f(0.25f, 0.25f, -1.0f);

	private static final Vector3f GRAVITY = new Vector3f(0, 0, 0.1f);

	private static float randomFloat(double left, double right) {
		float l = (float) left;
		float r = (float) right;
		return l + RND.nextFloat() * (r - l);
	}

	public static class Drop extends Sphere {
		private float vx;
		private float vy;
		private float vz;

		private Drop(double radius, PhongMaterial material, double x, double y, double z) {
			super(radius);
			setMaterial(material);
			setTranslateX(x);
			setTranslateY(y);
			setTranslateZ(z);
			setVisible(false);
		}

		private void setVelocity(float x, float y, float z) {
			vx = x;
			vy = y;
			vz = z;
		}

		private void move() {
			setTranslateX(getTranslateX() + vx);
			setTranslateY(getTranslateY() + vy);
			setTranslateZ(getTranslateZ() + vz);
			vx += GRAVITY.x();
			vy += GRAVITY.y();
			vz += GRAVITY.z();
		}
	}

	private final Group particleGroup = new Group();
	private final PhongMaterial dropMaterial;
	private final Point3D origin;

	protected Squirting(Group parent, double x, double y, double z, PhongMaterial dropMaterial) {
		this.origin = new Point3D(x, y, z);
		this.dropMaterial = dropMaterial;
		setCycleDuration(Duration.seconds(2));
		setOnFinished(e -> parent.getChildren().remove(particleGroup));
		parent.getChildren().add(particleGroup);
	}

	protected abstract boolean reachesEndPosition(Drop drop);

	private void createDrops() {
		for (int i = 0; i < randomInt(MIN_DROP_COUNT, MAX_DROP_COUNT); ++i) {
			var drop = new Drop(randomFloat(MIN_DROP_RADIUS, MAX_DROP_RADIUS), dropMaterial, origin.getX(), origin.getY(),
					origin.getZ());
			drop.setVelocity(//
					randomFloat(MIN_VELOCITY.x(), MAX_VELOCITY.x()), //
					randomFloat(MIN_VELOCITY.y(), MAX_VELOCITY.y()), //
					randomFloat(MIN_VELOCITY.z(), MAX_VELOCITY.z()));
			particleGroup.getChildren().add(drop);
		}
		LOG.info("%d drops created", particleGroup.getChildren().size());
	}

	@Override
	protected void interpolate(double t) {
		if (t == 0) {
			createDrops();
		}
		for (var particle : particleGroup.getChildren()) {
			var drop = (Drop) particle;
			if (reachesEndPosition(drop)) {
				drop.setVelocity(0, 0, 0);
				drop.setScaleZ(0.1);
			} else {
				drop.setVisible(true);
				drop.move();
			}
		}
	}
}