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

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.world.World;
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
public class SquirtingAnimation extends Transition {

	private static final Point3D GRAVITY = new Point3D(0, 0, 0.1);

	private final World world;
	private Sphere[] drops;
	private Point3D[] veloc;

	public SquirtingAnimation(World world, Group parent, Shape3D pellet) {
		this.world = world;
		createDrops(pellet, true);
		parent.getChildren().addAll(drops);
		setCycleDuration(Duration.seconds(2));
		setOnFinished(e -> parent.getChildren().removeAll(drops));
	}

	private void createDrops(Shape3D pellet, boolean bigSquirt) {
		var numDrops = bigSquirt ? U.randomInt(20, 30) : U.randomInt(4, 8);
		var color = Color.gray(0.4, 0.25);
		var material = new PhongMaterial(color);
		drops = new Sphere[numDrops];
		veloc = new Point3D[numDrops];
		for (int i = 0; i < numDrops; ++i) {
			var r = bigSquirt ? U.randomDouble(0.1, 1.0) : U.randomDouble(0.1, 0.5);
			var drop = new Sphere(r);
			drop.setMaterial(material);
			drop.setTranslateX(pellet.getTranslateX());
			drop.setTranslateY(pellet.getTranslateY());
			drop.setTranslateZ(-World.TS);
			drop.setVisible(false);
			drops[i] = drop;
			veloc[i] = new Point3D(U.randomDouble(0.05, 0.25), U.randomDouble(0.05, 0.25), -U.randomDouble(1.0, 4.0));
		}
	}

	@Override
	protected void interpolate(double t) {
		for (int i = 0; i < drops.length; ++i) {
			var drop = drops[i];
			if (drop.getTranslateZ() >= -0.5 // reached floor
					&& world.insideMap(drop.getTranslateX(), drop.getTranslateY())) {
				drop.setScaleZ(0.01);
				veloc[i] = Point3D.ZERO;
			} else {
				drop.setVisible(true);
				drop.setTranslateX(drop.getTranslateX() + veloc[i].getX());
				drop.setTranslateY(drop.getTranslateY() + veloc[i].getY());
				drop.setTranslateZ(drop.getTranslateZ() + veloc[i].getZ());
				veloc[i] = veloc[i].add(GRAVITY);
			}
		}
	}
}