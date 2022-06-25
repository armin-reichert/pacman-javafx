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
package de.amr.games.pacman.ui.fx._3d.model;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Pac-Man 3D model factory.
 * 
 * @author Armin Reichert
 */
public interface PacManModel3D {

	/**
	 * @return transformation group for Pac-Man
	 */
	Group createPacMan(Color skullColor, Color eyesColor, Color palateColor);

	/**
	 * @return transformation group for a ghost
	 */
	Group createGhost(Color skinColor, Color eyeBallColor, Color pupilColor);

	public static void scale(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		double s1 = size / bounds.getWidth();
		double s2 = size / bounds.getHeight();
		double s3 = size / bounds.getDepth();
		node.getTransforms().add(new Scale(s1, s2, s3));
	}
}