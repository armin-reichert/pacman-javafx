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

import de.amr.games.pacman.model.common.world.ArcadeWorld;
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
	private final Shape3D[] p;
	private Point3D[] v;

	public FoodEatenAnimation(Group parent, Shape3D foodShape, Color foodColor) {
		boolean energizer = foodShape instanceof Energizer3D;
		int numParticles = energizer ? Ufx.randomInt(20, 50) : Ufx.randomInt(4, 8);
		p = new Shape3D[numParticles];
		v = new Point3D[numParticles];
		var color = Color.gray(0.4, 0.25);
		var material = new PhongMaterial(color);
		for (int i = 0; i < numParticles; ++i) {
			p[i] = newParticle(foodShape, energizer, material);
			v[i] = new Point3D(Ufx.randomDouble(0.05, 0.25), Ufx.randomDouble(0.05, 0.25), -Ufx.randomDouble(0.5, 4.0));
		}
		parent.getChildren().addAll(p);
		setCycleDuration(Duration.seconds(1.5));
		setOnFinished(e -> parent.getChildren().removeAll(p));
	}

	private Shape3D newParticle(Shape3D foodShape, boolean energizer, PhongMaterial material) {
		double r = energizer ? Ufx.randomDouble(0.1, 1.0) : Ufx.randomDouble(0.1, 0.4);
		var particle = new Sphere(r);
		particle.setMaterial(material);
		particle.setTranslateX(foodShape.getTranslateX());
		particle.setTranslateY(foodShape.getTranslateY());
		particle.setTranslateZ(foodShape.getTranslateZ());
		return particle;
	}

	@Override
	protected void interpolate(double t) {
		for (int i = 0; i < p.length; ++i) {
			var particle = p[i];
			if (v[i].getZ() > 0 && particle.getTranslateZ() >= -0.5 && insideMaze(particle)) {
				// has fallen to ground
				particle.setScaleZ(0.01);
				v[i] = Point3D.ZERO;
			} else {
				particle.setTranslateX(particle.getTranslateX() + v[i].getX());
				particle.setTranslateY(particle.getTranslateY() + v[i].getY());
				particle.setTranslateZ(particle.getTranslateZ() + v[i].getZ());
				v[i] = v[i].add(GRAVITY);
			}
		}
	}

	private boolean insideMaze(Shape3D particle) {
		return 0 <= particle.getTranslateX() && particle.getTranslateX() <= ArcadeWorld.TILES_X * World.TS
				&& 0 <= particle.getTranslateY() && particle.getTranslateY() <= ArcadeWorld.TILES_Y * World.TS;
	}
}