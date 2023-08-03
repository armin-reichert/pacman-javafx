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

package de.amr.games.pacman.ui.fx.v3d.animation;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.v3d.entity.Pellet3D;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.util.Duration;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.v2f;

/**
 * @author Armin Reichert
 */
public class FoodOscillation extends Transition {

	private static final Vector2f CENTER = v2f(0.5f * TS * ArcadeWorld.TILES_X, 0.5f * TS * ArcadeWorld.TILES_Y);

	private final Group foodGroup;

	public FoodOscillation(Group foodGroup) {
		this.foodGroup = foodGroup;
		setCycleDuration(Duration.seconds(0.6));
		setCycleCount(INDEFINITE);
		setAutoReverse(true);
		setInterpolator(Interpolator.LINEAR);
	}

	@Override
	protected void interpolate(double t) {
		for (var node : foodGroup.getChildren()) {
			if (node.getUserData() instanceof Pellet3D pellet3D) {
				var position2D = new Vector2f((float) pellet3D.position().getX(), (float) pellet3D.position().getY());
				var centerDistance = position2D.euclideanDistance(CENTER);
				double dz = 2 * Math.sin(2 * centerDistance) * t;
				node.setTranslateZ(-4 + dz);
			}
		}
	}
}