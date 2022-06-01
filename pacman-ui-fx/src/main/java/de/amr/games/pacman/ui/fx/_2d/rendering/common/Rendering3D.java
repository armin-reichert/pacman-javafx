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

package de.amr.games.pacman.ui.fx._2d.rendering.common;

import de.amr.games.pacman.model.common.actors.Ghost;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public interface Rendering3D {

	default Color getPlayerSkullColor() {
		return Color.YELLOW;
	}

	default Color getPlayerEyesColor() {
		return Color.rgb(33, 33, 33);
	}

	default Color getPlayerPalateColor() {
		return Color.CORAL;
	}

	/**
	 * @param ghostID 0=Blinky, 1=Pinky, 2=Inky, 3=Clyde/Sue
	 * @return color of ghost
	 */
	default Color getGhostSkinColor(int ghostID) {
		return switch (ghostID) {
		case Ghost.RED_GHOST -> Color.RED;
		case Ghost.PINK_GHOST -> Color.rgb(252, 181, 255);
		case Ghost.CYAN_GHOST -> Color.CYAN;
		case Ghost.ORANGE_GHOST -> Color.rgb(253, 192, 90);
		default -> Color.WHITE; // should not happen
		};
	}

	default Color getGhostSkinColorFrightened() {
		return Color.rgb(33, 33, 255);
	}

	default Color getGhostSkinColorFrightened2() {
		return Color.rgb(224, 221, 255);
	}

	default Color getGhostEyeBallColor() {
		return Color.GHOSTWHITE;
	}

	default Color getGhostEyeBallColorFrightened() {
		return Color.rgb(245, 189, 180);
	}

	default Color getGhostPupilColor() {
		return Color.rgb(33, 33, 255);
	}

	default Color getGhostPupilColorFrightened() {
		return Color.RED;
	}
}