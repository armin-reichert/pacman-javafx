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

import static de.amr.games.pacman.lib.Globals.randomFloat;
import static de.amr.games.pacman.lib.Globals.randomInt;

import org.tinylog.Logger;

import de.amr.games.pacman.ui.fx.util.Vector3f;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public abstract class Squirting extends Transition {

	public static class Drop extends Sphere {
		private float vx;
		private float vy;
		private float vz;

		private Drop(Squirting squirting, double radius) {
			super(radius);
			setMaterial(squirting.dropMaterial);
			setTranslateX(squirting.origin.x());
			setTranslateY(squirting.origin.y());
			setTranslateZ(squirting.origin.z());
			setVisible(false);
		}

		private void setVelocity(float x, float y, float z) {
			vx = x;
			vy = y;
			vz = z;
		}

		private void move(Vector3f gravity) {
			setTranslateX(getTranslateX() + vx);
			setTranslateY(getTranslateY() + vy);
			setTranslateZ(getTranslateZ() + vz);
			vx += gravity.x();
			vy += gravity.y();
			vz += gravity.z();
		}
	}

	private final Group particleGroup = new Group();
	private PhongMaterial dropMaterial = new PhongMaterial();
	private Vector3f origin = new Vector3f(0, 0, 0);
	private Vector3f gravity = new Vector3f(0, 0, 0.1f);
	private int dropCountMin = 20;
	private int dropCountMax = 40;
	private float dropRadiusMin = 0.1f;
	private float dropRadiusMax = 1.0f;
	private Vector3f dropVelocityMin = new Vector3f(-0.25f, -0.25f, -4.0f);
	private Vector3f dropVelocityMax = new Vector3f(0.25f, 0.25f, -1.0f);

	protected Squirting(Group parent) {
		setCycleDuration(Duration.seconds(2));
		setOnFinished(e -> parent.getChildren().remove(particleGroup));
		parent.getChildren().add(particleGroup);
	}

	public void setDropMaterial(PhongMaterial dropMaterial) {
		this.dropMaterial = dropMaterial;
	}

	public void setOrigin(float x, float y, float z) {
		origin = new Vector3f(x, y, z);
	}

	public void setOrigin(Node node) {
		setOrigin((float) node.getTranslateX(), (float) node.getTranslateY(), (float) node.getTranslateZ());
	}

	public Vector3f getGravity() {
		return gravity;
	}

	public void setGravity(Vector3f gravity) {
		this.gravity = gravity;
	}

	public int getDropCountMin() {
		return dropCountMin;
	}

	public void setDropCountMin(int dropCountMin) {
		this.dropCountMin = dropCountMin;
	}

	public int getDropCountMax() {
		return dropCountMax;
	}

	public void setDropCountMax(int dropCountMax) {
		this.dropCountMax = dropCountMax;
	}

	public float getDropRadiusMin() {
		return dropRadiusMin;
	}

	public void setDropRadiusMin(float dropRadiusMin) {
		this.dropRadiusMin = dropRadiusMin;
	}

	public float getDropRadiusMax() {
		return dropRadiusMax;
	}

	public void setDropRadiusMax(float dropRadiusMax) {
		this.dropRadiusMax = dropRadiusMax;
	}

	public Vector3f getDropVelocityMin() {
		return dropVelocityMin;
	}

	public void setDropVelocityMin(Vector3f dropVelocityMin) {
		this.dropVelocityMin = dropVelocityMin;
	}

	public Vector3f getDropVelocityMax() {
		return dropVelocityMax;
	}

	public void setDropVelocityMax(Vector3f dropVelocityMax) {
		this.dropVelocityMax = dropVelocityMax;
	}

	public PhongMaterial getDropMaterial() {
		return dropMaterial;
	}

	protected abstract boolean reachesEndPosition(Drop drop);

	private void createDrops() {
		for (int i = 0; i < randomInt(dropCountMin, dropCountMax); ++i) {
			var drop = new Drop(this, randomFloat(dropRadiusMin, dropRadiusMax));
			drop.setVisible(true);
			drop.setVelocity(//
					randomFloat(dropVelocityMin.x(), dropVelocityMax.x()), //
					randomFloat(dropVelocityMin.y(), dropVelocityMax.y()), //
					randomFloat(dropVelocityMin.z(), dropVelocityMax.z()));
			particleGroup.getChildren().add(drop);
		}
		Logger.trace("{} drops created", particleGroup.getChildren().size());
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
				drop.move(gravity);
			}
		}
	}
}