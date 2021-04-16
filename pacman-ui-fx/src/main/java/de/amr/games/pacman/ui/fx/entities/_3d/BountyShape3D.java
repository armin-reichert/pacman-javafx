package de.amr.games.pacman.ui.fx.entities._3d;

import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * 3D shape for dead ghost displayed as bounty value.
 * 
 * @author Armin Reichert
 */
public class BountyShape3D extends Box {

	private final Rendering2D rendering2D;
	private final PhongMaterial skin;

	public BountyShape3D(Rendering2D rendering2D) {
		super(8, 8, 8);
		this.rendering2D = rendering2D;
		skin = new PhongMaterial();
		setMaterial(skin);
	}

	public void setBounty(int value) {
		Image sprite = rendering2D.subImage(rendering2D.getBountyNumberSpritesMap().get(value));
		skin.setBumpMap(sprite);
		skin.setDiffuseMap(sprite);
	}
}