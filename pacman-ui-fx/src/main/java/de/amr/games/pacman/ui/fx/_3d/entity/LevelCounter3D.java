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

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D level counter.
 * 
 * @author Armin Reichert
 */
public class LevelCounter3D extends Group {

	static final int MAX_ENTRIES = 7;

	private static Box createSpinningCube(Image symbol, boolean forward) {
		Box box = new Box(TS, TS, TS);
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseMap(symbol);
		box.setMaterial(material);
		RotateTransition spinning = new RotateTransition(Duration.seconds(6), box);
		spinning.setAxis(Rotate.X_AXIS);
		spinning.setCycleCount(Transition.INDEFINITE);
		spinning.setByAngle(360);
		spinning.setRate(forward ? 1 : -1);
		spinning.play();
		return box;
	}

	private final Rendering2D rendering2D;
	private V2d rightPosition;

	public LevelCounter3D(Rendering2D rendering2D) {
		this.rendering2D = rendering2D;
	}

	public void setRightPosition(double x, double y) {
		rightPosition = new V2d(x, y);
	}

	public void rebuild(PacManGameModel game) {
		// NOTE: all variables named ...Number are starting at 1
		int firstLevelNumber = Math.max(1, game.level().number - MAX_ENTRIES + 1);
		getChildren().clear();
		double x = rightPosition.x, y = rightPosition.y;
		for (int levelNumber = firstLevelNumber; levelNumber <= game.level().number; ++levelNumber) {
			Image symbol = symbolImage(game.levelSymbol(levelNumber));
			Box cube = createSpinningCube(symbol, levelNumber % 2 == 0);
			cube.setTranslateX(x);
			cube.setTranslateY(y);
			getChildren().add(cube);
			x -= t(2);
		}
	}

	private Image symbolImage(String symbol) {
		return rendering2D.createSubImage(rendering2D.getSymbolSprites().get(symbol));
	}
}