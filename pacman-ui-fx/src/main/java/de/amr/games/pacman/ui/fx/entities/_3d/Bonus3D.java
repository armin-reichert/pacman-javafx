package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Bonus3D implements Supplier<Node> {

	private final Box box;
	private final List<Image> symbolSprites;
	private final Map<Integer, Image> pointsSprites;
	private final Rendering2D rendering2D;
	private final RotateTransition rotation;
	private final PhongMaterial skin;

	public Bonus3D(GameVariant gameVariant, Rendering2D rendering2D) {
		this.rendering2D = rendering2D;
		symbolSprites = rendering2D.getSymbolSprites().stream().map(rendering2D::subImage).collect(Collectors.toList());
		pointsSprites = getPointsSprites(gameVariant);
		box = new Box(8, 8, 8);
		skin = new PhongMaterial(Color.WHITE);
		rotation = new RotateTransition(Duration.seconds(2), box);
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
			box.setTranslateX(bonus.position.x);
			box.setTranslateY(bonus.position.y);
		}
	}

	public void hide() {
		rotation.stop();
		box.setVisible(false);
	}

	public void showSymbol(PacManBonus bonus) {
		skin.setBumpMap(symbolSprites.get(bonus.symbol));
		skin.setDiffuseMap(symbolSprites.get(bonus.symbol));
		box.setMaterial(skin);
		box.setTranslateX(bonus.position.x);
		box.setTranslateY(bonus.position.y);
		box.setVisible(true);
		rotation.setCycleCount(Transition.INDEFINITE);
		rotation.setRate(1);
		rotation.play();
	}

	public void showPoints(PacManBonus bonus) {
		skin.setBumpMap(pointsSprites.get(bonus.points));
		skin.setDiffuseMap(pointsSprites.get(bonus.points));
		box.setMaterial(skin);
		box.setTranslateX(bonus.position.x);
		box.setTranslateY(bonus.position.y);
		box.setVisible(true);
		rotation.stop();
		rotation.setRate(2);
		rotation.setCycleCount(2);
		rotation.play();
	}

	@Override
	public Node get() {
		return box;
	}
}