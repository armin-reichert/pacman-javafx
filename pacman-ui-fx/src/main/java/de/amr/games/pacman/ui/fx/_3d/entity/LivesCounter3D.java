/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.world.World.TS;

import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * Displays a Pac-Man for each live remaining.
 * 
 * @author Armin Reichert
 */
public class LivesCounter3D extends Group {

	static final int max = 5;

	public LivesCounter3D(PacManModel3D model3D) {
		for (int i = 0; i < max; ++i) {
			Node indicator = model3D.createPacMan(Color.YELLOW, Color.rgb(20, 20, 20), Color.CHOCOLATE);
			indicator.setTranslateX(2 * i * TS);
			getChildren().add(indicator);
		}
		setVisibleItems(max);
	}

	public void setVisibleItems(int n) {
		for (int i = 0; i < max; ++i) {
			getChildren().get(i).setVisible(i < n);
		}
	}
}