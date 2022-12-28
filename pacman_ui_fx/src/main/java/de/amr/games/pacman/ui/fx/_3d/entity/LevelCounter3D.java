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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.function.IntFunction;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.model.common.LevelCounter;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
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

	/**
	 * @param levelCounter
	 * @param position      right border of the level counter
	 * @param fnSymbolImage
	 */
	public LevelCounter3D(LevelCounter levelCounter, Vector2f position, IntFunction<Image> fnSymbolImage) {
		double x = position.x();
		boolean forward = true;
		for (int symbol : levelCounter) {
			Box cube = createSpinningCube(TS, fnSymbolImage.apply(symbol), forward);
			cube.setTranslateX(x);
			cube.setTranslateY(position.y());
			cube.setTranslateZ(-HTS);
			getChildren().add(cube);
			x -= 2 * TS;
			forward = !forward;
		}
	}

	private Box createSpinningCube(double size, Image texture, boolean forward) {
		var material = new PhongMaterial();
		material.setDiffuseMap(texture);
		Box cube = new Box(size, size, size);
		cube.setMaterial(material);
		var spinning = new RotateTransition(Duration.seconds(6), cube);
		spinning.setAxis(Rotate.X_AXIS);
		spinning.setCycleCount(Animation.INDEFINITE);
		spinning.setByAngle(360);
		spinning.setRate(forward ? 1 : -1);
		spinning.play();
		return cube;
	}
}