package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class LevelCounter3D implements Supplier<Node> {

	public V2i tileRight = V2i.NULL;
	private final List<Image> symbolSprites;
	private final Group root = new Group();

	public LevelCounter3D(GameRendering2D rendering2D) {
		symbolSprites = rendering2D.getSymbolSprites().stream().map(rendering2D::subImage).collect(Collectors.toList());
	}

	public void update(AbstractGameModel game) {
		int x = t(tileRight.x), y = t(tileRight.y);
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		int numberOfEntries = game.currentLevelNumber - firstLevel + 1;
		if (numberOfEntries == root.getChildren().size()) {
			return;
		}
		root.getChildren().clear();
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			Box box = new Box(8, 8, 8);
			PhongMaterial material = new PhongMaterial();
			Image sprite = symbolSprites.get(level);
			material.setDiffuseMap(sprite);
			material.setBumpMap(sprite);
			box.setMaterial(material);
			box.setTranslateX(x);
			box.setTranslateY(y);
			root.getChildren().add(box);
			RotateTransition spinning = new RotateTransition(Duration.seconds(6), box);
			spinning.setAxis(Rotate.X_AXIS);
			spinning.setCycleCount(Transition.INDEFINITE);
			spinning.setFromAngle(0);
			spinning.setToAngle(360);
			spinning.setByAngle(1);
			spinning.setRate(level % 2 == 0 ? 1 : -1);
			spinning.play();
			x -= t(2);
		}
	}

	@Override
	public Node get() {
		return root;
	}
}