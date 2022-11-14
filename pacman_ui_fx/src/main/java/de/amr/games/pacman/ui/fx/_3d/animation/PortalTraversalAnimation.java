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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.world.HorizontalPortal;
import de.amr.games.pacman.model.common.world.World;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class PortalTraversalAnimation {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final double FADING_DISTANCE = 32.0;

	private final Creature guy;
	private final Node node;
	private final ObjectProperty<Color> colorPy;
	private final Color baseColor;

	public PortalTraversalAnimation(Creature guy, Node node, ObjectProperty<Color> colorPy, Color baseColor) {
		this.guy = guy;
		this.node = node;
		this.colorPy = colorPy;
		this.baseColor = baseColor;
	}

	public void update(World world) {
		if (colorPy.isBound()) {
			return;
		}
		node.setVisible(guy.isVisible());
		double dist = distFromNearestPortal(world);
		if (dist < 4.0 || outsideWorld(world)) {
			node.setOpacity(1.0);
			node.setVisible(false);
			LOGGER.trace("Distance from portal: %.2f visible: %s", dist, node.isVisible());
		} else if (dist <= FADING_DISTANCE) {
			double fading = dist / FADING_DISTANCE;
			var color = fade(baseColor, fading);
			colorPy.set(color);
			LOGGER.trace("Distance from portal: %.2f color: rgb(%.0f,%.0f,%.0f) visible: %s", dist, 256 * color.getRed(),
					256 * color.getGreen(), 256 * color.getBlue(), node.isVisible());
		} else {
			colorPy.set(baseColor);
			node.setOpacity(1.0);
		}
	}

	private Color fade(Color color, double fading) {
		return Color.color(color.getRed() * fading, color.getGreen() * fading, color.getBlue() * fading, fading);
	}

	private double distFromNearestPortal(World world) {
		double minDist = Double.MAX_VALUE;
		for (var portal : world.portals()) {
			if (portal instanceof HorizontalPortal horPortal) {
				var left = new V2d(horPortal.leftTunnelEnd().minus(1, 0)).scaled(World.TS);
				var right = new V2d(horPortal.rightTunnelEnd().plus(1, 0)).scaled(World.TS);
				var dist = Math.min(guy.position().euclideanDistance(left), guy.position().euclideanDistance(right));
				if (dist < minDist) {
					minDist = dist;
				}
			}
		}
		return minDist;
	}

	private boolean outsideWorld(World world) {
		double centerX = guy.position().x() + World.HTS;
		return centerX < 0 || centerX > world.numCols() * World.TS;
	}
}