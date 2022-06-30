/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.world.World;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class PortalTraversalAnimation {

	private final Logger logger = LogManager.getFormatterLogger();

	private final Creature guy;
	private final World world;
	private final Node node;
	private final ObjectProperty<Color> colorProperty;
	private final Supplier<Color> baseColor;

	public PortalTraversalAnimation(Creature guy, World world, Node node, ObjectProperty<Color> colorProperty,
			Supplier<Color> baseColor) {
		this.guy = guy;
		this.world = world;
		this.node = node;
		this.colorProperty = colorProperty;
		this.baseColor = baseColor;
	}

	public void update() {
		if (colorProperty.isBound()) {
			return;
		}
		colorProperty.set(baseColor.get());
		node.setVisible(guy.isVisible());
		node.setOpacity(1);
		if (outsideWorld()) {
			node.setVisible(true);
			colorProperty.set((Color.color(0.1, 0.1, 0.1, 0.2)));
		} else {
			double fadeStart = 32.0;
			double dist = distFromNearestPortal();
			if (dist <= fadeStart) { // fade into shadow
				node.setVisible(true);
				double opacity = U.lerp(0.2, 1, dist / fadeStart);
				logger.trace("Distance from portal: %.2f Opacity: %.2f", dist, opacity);
				colorProperty.set(Color.color(baseColor.get().getRed() * opacity, baseColor.get().getGreen() * opacity,
						baseColor.get().getBlue() * opacity, opacity));
			}
		}
	}

	private double distFromNearestPortal() {
		double minDist = Double.MAX_VALUE;
		for (var portal : world.portals()) {
			var left = new V2d(portal.left).scaled(World.TS);
			var right = new V2d(portal.right).scaled(World.TS);
			var dist = Math.min(guy.getPosition().euclideanDistance(left), guy.getPosition().euclideanDistance(right));
			if (dist < minDist) {
				minDist = dist;
			}
		}
		return minDist;
	}

	private boolean outsideWorld() {
		double centerX = guy.getPosition().x + World.HTS;
		return centerX < 0 || centerX > world.numCols() * World.TS;
	}
}