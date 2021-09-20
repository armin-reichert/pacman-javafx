package de.amr.games.pacman.ui.fx._3d.entity;

import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.model.pacman.Bonus;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
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

	private final Map<Integer, Image> spritesByValue;
	private final Rendering2D rendering2D;
	private final RotateTransition rotation;
	private final PhongMaterial skin;

	public Bonus3D(Rendering2D rendering2D) {
		super(8, 8, 8);
		this.rendering2D = rendering2D;
		spritesByValue = new HashMap<>();
		rendering2D.getBonusValuesSprites()
				.forEach((points, spriteRegion) -> spritesByValue.put(points, rendering2D.createSubImage(spriteRegion)));
		skin = new PhongMaterial(Color.WHITE);
		rotation = new RotateTransition(Duration.seconds(2), this);
		rotation.setAxis(Rotate.X_AXIS);
		rotation.setByAngle(360);
		rotation.setOnFinished(e -> hide());
		setTranslateZ(-4);
		hide();
	}

	private Image symbolImage(String symbol) {
		return rendering2D.createSubImage(rendering2D.getSymbolSprites().get(symbol));
	}

	public void update(Bonus bonus) {
		if (bonus != null) {
			setTranslateX(bonus.position().x);
			setTranslateY(bonus.position().y);
		}
	}

	public void hide() {
		rotation.stop();
		setVisible(false);
	}

	public void showSymbol(Bonus bonus) {
		skin.setBumpMap(symbolImage(bonus.symbol));
		skin.setDiffuseMap(symbolImage(bonus.symbol));
		setMaterial(skin);
		setTranslateX(bonus.position().x);
		setTranslateY(bonus.position().y);
		setVisible(true);
		rotation.setCycleCount(Transition.INDEFINITE);
		rotation.setRate(1);
		rotation.play();
	}

	public void showPoints(Bonus bonus) {
		if (bonus.points >= 1000) {
			setWidth(10);
		}
		skin.setBumpMap(spritesByValue.get(bonus.points));
		skin.setDiffuseMap(spritesByValue.get(bonus.points));
		setMaterial(skin);
		setTranslateX(bonus.position().x);
		setTranslateY(bonus.position().y);
		setVisible(true);
		rotation.stop();
		rotation.setRate(2);
		rotation.setCycleCount(2);
		rotation.play();
	}
}