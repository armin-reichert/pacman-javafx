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

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;

/**
 * Pac-Man 3D model factory.
 * 
 * @author Armin Reichert
 */
public interface PacModel3D {

	/**
	 * @return transformation group for Pac-Man
	 */
	Group createPacMan(Color skullColor, Color eyesColor, Color palateColor);

	default MeshView skull(Group pac) {
		return (MeshView) pac.getChildren().get(0);
	}

	default MeshView eyes(Group pac) {
		return (MeshView) pac.getChildren().get(1);
	}

	default MeshView palate(Group pac) {
		return (MeshView) pac.getChildren().get(2);
	}

	/**
	 * @return transformation group for a ghost
	 */
	Group createGhost(Color skinColor, Color eyeBallColor, Color pupilColor);
}