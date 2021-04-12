package de.amr.games.pacman.ui.fx.entities._3d;

import java.util.function.Supplier;

import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * 3D shape for dead ghost displayed as bounty value.
 * 
 * @author Armin Reichert
 */
public class BountyShape3D implements Supplier<Node> {

	private final Box root;
	private final GameRendering2D rendering2D;
	private final PhongMaterial skin;

	public BountyShape3D(GameRendering2D rendering2D) {
		this.rendering2D = rendering2D;
		root = new Box(8, 8, 8);
		skin = new PhongMaterial();
		root.setMaterial(skin);
	}

	public void setBounty(int value) {
		Image sprite = rendering2D.subImage(rendering2D.getBountyNumberSpritesMap().get(value));
		skin.setBumpMap(sprite);
		skin.setDiffuseMap(sprite);
	}

	@Override
	public Node get() {
		return root;
	}
}