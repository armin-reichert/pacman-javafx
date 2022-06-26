package de.amr.games.pacman.ui.fx._3d.animation;

import java.util.Random;

import de.amr.games.pacman.lib.U;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;

public class FoodEatenAnimation extends Transition {

	private final Shape3D foodShape;
	private final PhongMaterial foodMaterial;
	private final Random rnd = new Random();

	private double scaleFactor;
	private double maxHeight;

	public FoodEatenAnimation(Shape3D foodShape, Color foodColor) {
		this.foodShape = foodShape;
		scaleFactor = 0.25 + 0.25 * rnd.nextDouble();
		maxHeight = -3 - 100 * rnd.nextDouble();
		foodMaterial = new PhongMaterial(foodColor);
		foodMaterial.setDiffuseColor(foodColor.grayscale());
		foodShape.setMaterial(foodMaterial);
		setCycleDuration(Duration.seconds(0.75));
		setOnFinished(e -> {
			foodShape.setVisible(false);
			foodShape.setTranslateZ(-3);
		});
		setInterpolator(Interpolator.EASE_BOTH);
	}

	@Override
	protected void interpolate(double t) {
		var scale = (1 - t) * scaleFactor;
		foodShape.setScaleX(scale);
		foodShape.setScaleY(scale);
		foodShape.setScaleZ(scale);
		foodShape.setTranslateZ(U.lerp(-3, maxHeight, t));
	}
}