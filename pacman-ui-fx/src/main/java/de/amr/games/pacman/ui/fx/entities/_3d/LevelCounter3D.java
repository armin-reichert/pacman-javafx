package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
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

	public void update(GameModel game) {
		int x = t(tileRight.x), y = t(tileRight.y);
		// all *Number variables are starting with 1!
		final int maxItems = 7;
		int firstLevelNumber = Math.max(1, game.currentLevel().number - maxItems + 1);
		getChildren().clear();
		for (int levelNumber = firstLevelNumber; levelNumber <= game.currentLevel().number; ++levelNumber) {
			Image sprite = symbolImage(game.levelSymbol(levelNumber));
			Box indicator = createLevelIndicator(levelNumber, sprite, x, y);
			getChildren().add(indicator);
			x -= t(2);
		}
	}

	private Box createLevelIndicator(int levelNumber, Image sprite, int x, int y) {
		Box box = new Box(8, 8, 8);
		box.setTranslateX(x);
		box.setTranslateY(y);
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseMap(sprite);
		box.setMaterial(material);
		RotateTransition spinning = new RotateTransition(Duration.seconds(6), box);
		spinning.setAxis(Rotate.X_AXIS);
		spinning.setCycleCount(Transition.INDEFINITE);
		spinning.setFromAngle(0);
		spinning.setToAngle(360);
		spinning.setByAngle(1);
		spinning.setRate(levelNumber % 2 == 0 ? 1 : -1);
		spinning.play();
		return box;
	}
}