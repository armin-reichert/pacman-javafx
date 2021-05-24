package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
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

	private final Rendering2D rendering2D;
	private V2i tileRight;

	public LevelCounter3D(V2i tileRight, Rendering2D rendering2D) {
		this.tileRight = tileRight;
		this.rendering2D = rendering2D;
	}

	private Image symbolImage(String symbol) {
		return rendering2D.subImage(rendering2D.getSymbolSprites().get(symbol));
	}

	public void update(PacManGameModel game) {
		int x = t(tileRight.x), y = t(tileRight.y);
		// all *Number variables are starting with 1!
		final int maxItems = 7;
		int firstLevelNumber = Math.max(1, game.currentLevel().number - maxItems + 1);
		getChildren().clear();
		for (int levelNumber = firstLevelNumber; levelNumber <= game.currentLevel().number; ++levelNumber) {
			Image symbol = symbolImage(game.levelSymbol(levelNumber));
			Box cube = createSpinningCube(symbol, levelNumber % 2 == 0);
			cube.setTranslateX(x);
			cube.setTranslateY(y);
			getChildren().add(cube);
			x -= t(2);
		}
	}

	private Box createSpinningCube(Image symbol, boolean forward) {
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
}