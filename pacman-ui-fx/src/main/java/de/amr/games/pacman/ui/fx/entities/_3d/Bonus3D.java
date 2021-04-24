package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D bonus symbol.
 * 
 * @author Armin Reichert
 */
public class Bonus3D extends Box {

	private final List<Image> symbolSprites;
	private final Map<Integer, Image> pointsSprites;
	private final Rendering2D rendering2D;
	private final RotateTransition rotation;
	private final PhongMaterial skin;

	public Bonus3D(GameVariant gameVariant, Rendering2D rendering2D) {
		super(8, 8, 8);
		this.rendering2D = rendering2D;
		symbolSprites = rendering2D.getSymbolSprites().stream().map(rendering2D::subImage).collect(Collectors.toList());
		pointsSprites = getPointsSprites(gameVariant);
		skin = new PhongMaterial(Color.WHITE);
		rotation = new RotateTransition(Duration.seconds(2), this);
		rotation.setAxis(Rotate.X_AXIS);
		rotation.setByAngle(360);
		rotation.setOnFinished(e -> hide());
		hide();
	}

	private Map<Integer, Image> getPointsSprites(GameVariant gameVariant) {
		Map<Integer, Rectangle2D> spritesMap = rendering2D.getBonusValuesSpritesMap();
		Map<Integer, Image> result = new HashMap<>();
		spritesMap.forEach((points, sprite) -> result.put(points, rendering2D.subImage(sprite)));
		return result;
	}

	public void update(PacManBonus bonus) {
		if (bonus != null) {
			setTranslateX(bonus.position.x);
			setTranslateY(bonus.position.y);
		}
	}

	public void hide() {
		rotation.stop();
		setVisible(false);
	}

	public void showSymbol(PacManBonus bonus) {
		skin.setBumpMap(symbolSprites.get(bonus.symbol));
		skin.setDiffuseMap(symbolSprites.get(bonus.symbol));
		setMaterial(skin);
		setTranslateX(bonus.position.x);
		setTranslateY(bonus.position.y);
		setVisible(true);
		rotation.setCycleCount(Transition.INDEFINITE);
		rotation.setRate(1);
		rotation.play();
	}

	public void showPoints(PacManBonus bonus) {
		if (bonus.points >= 1000) {
			setWidth(10);
		}
		skin.setBumpMap(pointsSprites.get(bonus.points));
		skin.setDiffuseMap(pointsSprites.get(bonus.points));
		setMaterial(skin);
		setTranslateX(bonus.position.x);
		setTranslateY(bonus.position.y);
		setVisible(true);
		rotation.stop();
		rotation.setRate(2);
		rotation.setCycleCount(2);
		rotation.play();
	}
}