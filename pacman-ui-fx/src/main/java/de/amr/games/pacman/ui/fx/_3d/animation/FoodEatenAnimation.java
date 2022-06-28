/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class FoodEatenAnimation extends Transition {

	private static final Logger logger = LogManager.getFormatterLogger();

	private static final Point3D GRAVITY = new Point3D(0, 0, 0.09);

	private final World world;
	private final Shape3D[] particle;
	private Point3D[] velocity;

	public FoodEatenAnimation(World world, Group parent, Shape3D foodShape, Color foodColor) {
		this.world = world;
		boolean energizer = foodShape instanceof Energizer3D;
		int numParticles = energizer ? Ufx.randomInt(10, 30) : Ufx.randomInt(4, 8);
		particle = new Shape3D[numParticles];
		velocity = new Point3D[numParticles];
		var color = Color.gray(0.4, 0.25);
		var material = new PhongMaterial(color);
		for (int i = 0; i < numParticles; ++i) {
			particle[i] = newParticle(foodShape, energizer, material);
			velocity[i] = new Point3D(Ufx.randomDouble(0.05, 0.25), Ufx.randomDouble(0.05, 0.25),
					-Ufx.randomDouble(0.25, 4.0));
		}
		parent.getChildren().addAll(particle);
		setCycleDuration(Duration.seconds(1.5));
		setInterpolator(Interpolator.EASE_OUT);
		setOnFinished(e -> parent.getChildren().removeAll(particle));
	}

	private Shape3D newParticle(Shape3D foodShape, boolean energizer, PhongMaterial material) {
		double r = energizer ? Ufx.randomDouble(0.1, 1.0) : Ufx.randomDouble(0.1, 0.4);
		var p = new Sphere(r);
		p.setMaterial(material);
		p.setTranslateX(foodShape.getTranslateX());
		p.setTranslateY(foodShape.getTranslateY());
		p.setTranslateZ(foodShape.getTranslateZ());
		return p;
	}

	@Override
	protected void interpolate(double t) {
		double dt = t == 0 ? 0 : t - tt;
		logger.info("dt=%f", dt);
		for (int i = 0; i < particle.length; ++i) {
			var p = particle[i];
			if (p.getTranslateZ() >= -0.5 // reached floor
					&& world.insideMap(p.getTranslateX(), p.getTranslateY())) {
				p.setScaleZ(0.01);
				velocity[i] = Point3D.ZERO;
			} else {
				p.setTranslateX(p.getTranslateX() + velocity[i].getX());
				p.setTranslateY(p.getTranslateY() + velocity[i].getY());
				p.setTranslateZ(p.getTranslateZ() + velocity[i].getZ());
				velocity[i] = velocity[i].add(GRAVITY.multiply(60.0 * dt));
			}
		}
		tt = t;
	}

	private double tt;
}