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
package de.amr.games.pacman.ui.fx.shell.info;

import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Picture-In-Picture view. Displays 2D game scene in mniature view.
 * 
 * @author Armin Reichert
 */
public class PiPView extends StackPane {

	private final PlayScene2D playScene2D;

	public PiPView(double height) {
		playScene2D = new PlayScene2D(false);
		playScene2D.resize(height);
		playScene2D.getFXSubScene().setFocusTraversable(false);
		getChildren().add(playScene2D.getFXSubScene());
		setBackground(Ufx.colorBackground(Color.BLACK));
	}

	public PlayScene2D getPlayScene2D() {
		return playScene2D;
	}
}