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

import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx.util.Ufx;
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

	private static final Point3D GRAVITY = new Point3D(0, 0, 0.09);

	private final World world;
	private final Shape3D[] particles;
	private Point3D[] velocities;

	public FoodEatenAnimation(World world, Group parent, Shape3D foodShape, Color foodColor) {
		this.world = world;
		boolean energizer = foodShape instanceof Energizer3D;
		int numParticles = energizer ? Ufx.randomInt(20, 50) : Ufx.randomInt(4, 8);
		particles = new Shape3D[numParticles];
		velocities = new Point3D[numParticles];
		var color = Color.gray(0.4, 0.25);
		var material = new PhongMaterial(color);
		for (int i = 0; i < numParticles; ++i) {
			particles[i] = newParticle(foodShape, energizer, material);
			velocities[i] = new Point3D(Ufx.randomDouble(0.05, 0.25), Ufx.randomDouble(0.05, 0.25),
					-Ufx.randomDouble(0.25, 3.0));
		}
		parent.getChildren().addAll(particles);
		setCycleDuration(Duration.seconds(1.5));
		setOnFinished(e -> parent.getChildren().removeAll(particles));
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
		for (int i = 0; i < particles.length; ++i) {
			var p = particles[i];
			if (p.getTranslateZ() >= -0.5 // reached floor
					&& world.insideMap(p.getTranslateX(), p.getTranslateY())) {
				p.setScaleZ(0.01);
				velocities[i] = Point3D.ZERO;
			} else {
				p.setTranslateX(p.getTranslateX() + velocities[i].getX());
				p.setTranslateY(p.getTranslateY() + velocities[i].getY());
				p.setTranslateZ(p.getTranslateZ() + velocities[i].getZ());
				velocities[i] = velocities[i].add(GRAVITY);
			}
		}
	}
}