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

import static de.amr.games.pacman.model.common.world.World.TS;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private final ObjectProperty<Color> colorPy;
	private final Color baseColor;

	public PortalTraversalAnimation(ObjectProperty<Color> colorPy, Color baseColor) {
		this.colorPy = colorPy;
		this.baseColor = baseColor;
	}

	public void update(World world, Creature guy, Node guy3D) {
		if (colorPy.isBound()) {
			return;
		}
		guy3D.setVisible(guy.isVisible());
		double dist = distFromNearestPortal(world, guy);
		if (outsideWorld(world, guy)) {
			guy3D.setVisible(false);
		} else if (dist < 4.0) {
			guy3D.setOpacity(1.0);
//			guy3D.setVisible(false);
			LOGGER.trace("Distance from portal: %.2f visible: %s", dist, guy3D.isVisible());
		} else if (dist <= FADING_DISTANCE) {
			double fading = dist / FADING_DISTANCE;
			var color = fade(baseColor, fading);
//			colorPy.set(color);
//			guy3D.setOpacity(fading);
			LOGGER.info("Distance from portal: %.2f fading: %.2f  color: rgb(%.0f,%.0f,%.0f) visible: %s", dist, fading,
					256 * color.getRed(), 256 * color.getGreen(), 256 * color.getBlue(), guy3D.isVisible());
		} else {
			colorPy.set(baseColor);
			guy3D.setOpacity(1.0);
		}
	}

	private Color fade(Color color, double fading) {
		return Color.color(color.getRed() * fading, color.getGreen() * fading, color.getBlue() * fading, fading);
	}

	private double distFromNearestPortal(World world, Creature guy) {
		double minDist = Double.MAX_VALUE;
		for (var portal : world.portals()) {
			if (portal instanceof HorizontalPortal horPortal) {
				var left = horPortal.leftTunnelEnd().minus(1, 0).scaled(TS).toFloatVec();
				var right = horPortal.rightTunnelEnd().plus(1, 0).scaled(TS).toFloatVec();
				var dist = Math.min(guy.position().euclideanDistance(left), guy.position().euclideanDistance(right));
				if (dist < minDist) {
					minDist = dist;
				}
			}
		}
		return minDist;
	}

	private boolean outsideWorld(World world, Creature guy) {
		double centerX = guy.position().x() + World.HTS;
		return centerX < 0 || centerX > world.numCols() * World.TS;
	}
}