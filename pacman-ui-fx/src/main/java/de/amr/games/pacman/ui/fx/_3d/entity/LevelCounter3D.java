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
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
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

	private static final int MAX_ENTRIES = 7;

	private final Rendering2D r2D;
	private final V2d rightPosition;

	public LevelCounter3D(Rendering2D r2D, double x, double y) {
		this.r2D = r2D;
		this.rightPosition = new V2d(x, y);
	}

	public void update(GameModel game) {
		// NOTE: all variables named *Number are starting at 1
		int firstLevelNumber = Math.max(1, game.level.number - MAX_ENTRIES + 1);
		getChildren().clear();
		double x = rightPosition.x, y = rightPosition.y;
		for (int levelNumber = firstLevelNumber; levelNumber <= game.level.number; ++levelNumber) {
			int symbol = game.levelCounter.get(levelNumber - 1);
			Image symbolImage = r2D.spritesheet().image(r2D.getSymbolSprite(symbol));
			Box cube = createSpinningCube(symbolImage, levelNumber % 2 == 0);
			cube.setTranslateX(x);
			cube.setTranslateY(y);
			cube.setTranslateZ(-HTS);
			getChildren().add(cube);
			x -= t(2);
		}
	}

	private Box createSpinningCube(Image symbol, boolean forward) {
		Box cube = new Box(TS, TS, TS);
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseMap(symbol);
		cube.setMaterial(material);
		RotateTransition spinning = new RotateTransition(Duration.seconds(6), cube);
		spinning.setAxis(Rotate.X_AXIS);
		spinning.setCycleCount(Animation.INDEFINITE);
		spinning.setByAngle(360);
		spinning.setRate(forward ? 1 : -1);
		spinning.play();
		return cube;
	}
}