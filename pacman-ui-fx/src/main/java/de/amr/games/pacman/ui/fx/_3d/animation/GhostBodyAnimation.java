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
package de.amr.games.pacman.ui.fx._3d.animation;

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import javafx.animation.Animation.Status;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;

/**
 * @author Armin Reichert
 */
public class GhostBodyAnimation {

	private final Ghost ghost;
	private final Model3D model3D;
	private final Group ghostGroup;
	private final ColorFlashingTransition flashingAnimation;

	private final ObjectProperty<Color> skinColorProperty = new SimpleObjectProperty<>();
	private final PhongMaterial skinMaterial = new PhongMaterial();

	private final ObjectProperty<Color> eyeBallsColorProperty = new SimpleObjectProperty<>();
	private final PhongMaterial eyeBallsMaterial = new PhongMaterial();

	private final ObjectProperty<Color> eyePupilsColorProperty = new SimpleObjectProperty<>();
	private final PhongMaterial eyePupilsMaterial = new PhongMaterial();

	private final PortalApproachAnimation portalApproachAnimation;

	private boolean frightened;

	public GhostBodyAnimation(Model3D model3D, Ghost ghost) {
		this.model3D = model3D;
		this.ghost = ghost;

		ghostGroup = model3D.createGhost(//
				faded(Rendering3D.getGhostSkinColor(ghost.id)), //
				Rendering3D.getGhostEyeBallColor(), //
				Rendering3D.getGhostPupilColor());

		flashingAnimation = new ColorFlashingTransition(//
				Rendering3D.getGhostSkinColorFrightened(), //
				Rendering3D.getGhostSkinColorFrightened2());

		skinColorProperty.set(faded(Rendering3D.getGhostSkinColor(ghost.id)));
		skinMaterial.diffuseColorProperty().bind(skinColorProperty);
		skinMaterial.specularColorProperty()
				.bind(Bindings.createObjectBinding(() -> skinColorProperty.get().brighter(), skinColorProperty));

		eyeBallsColorProperty.set(Rendering3D.getGhostEyeBallColor());
		eyeBallsMaterial.diffuseColorProperty().bind(eyeBallsColorProperty);
		eyeBallsMaterial.specularColorProperty()
				.bind(Bindings.createObjectBinding(() -> eyeBallsColorProperty.get().brighter(), eyeBallsColorProperty));

		eyePupilsColorProperty.set(Rendering3D.getGhostPupilColor());
		eyePupilsMaterial.diffuseColorProperty().bind(eyePupilsColorProperty);
		eyePupilsMaterial.specularColorProperty()
				.bind(Bindings.createObjectBinding(() -> eyePupilsColorProperty.get().brighter(), eyePupilsColorProperty));

		skin().setMaterial(skinMaterial);
		eyeBalls().setMaterial(eyeBallsMaterial);
		eyePupils().setMaterial(eyePupilsMaterial);

		portalApproachAnimation = new PortalApproachAnimation(skinColorProperty,
				() -> frightened ? Rendering3D.getGhostSkinColorFrightened() : faded(Rendering3D.getGhostSkinColor(ghost.id)));
	}

	public void update(World world) {
		portalApproachAnimation.update(ghostGroup, ghost, world);
	}

	private Shape3D skin() {
		return model3D.ghostSkin(ghostGroup);
	}

	private Shape3D eyeBalls() {
		return model3D.ghostEyeBalls(ghostGroup);
	}

	private Shape3D eyePupils() {
		return model3D.ghostEyePupils(ghostGroup);
	}

	public Node getRoot() {
		return ghostGroup;
	}

	public void setShowBody(boolean showSkin) {
		model3D.ghostSkin(ghostGroup).setVisible(showSkin);
	}

	public boolean isFlashing() {
		return flashingAnimation.getStatus() == Status.RUNNING;
	}

	public void playFlashingAnimation() {
		if (!isFlashing()) {
			skinColorProperty.bind(flashingAnimation.colorProperty);
			skin().setMaterial(skinMaterial);
			flashingAnimation.playFromStart();
		}
	}

	public void ensureFlashingAnimationStopped() {
		if (isFlashing()) {
			skinColorProperty.unbind();
			skin().setMaterial(skinMaterial);
			flashingAnimation.stop();
		}
	}

	public void setFrightened(boolean frightened) {
		this.frightened = frightened;
		ensureFlashingAnimationStopped();
		if (frightened) {
			skinColorProperty.set(faded(Rendering3D.getGhostSkinColorFrightened()));
			eyeBallsColorProperty.set(Rendering3D.getGhostEyeBallColorFrightened());
			eyePupilsColorProperty.set(Rendering3D.getGhostPupilColorFrightened());
		} else {
			skinColorProperty.set(faded(Rendering3D.getGhostSkinColor(ghost.id)));
			eyeBallsColorProperty.set(Rendering3D.getGhostEyeBallColor());
			eyePupilsColorProperty.set(Rendering3D.getGhostPupilColor());
		}
		skin().setMaterial(skinMaterial);
		eyeBalls().setMaterial(eyeBallsMaterial);
		eyePupils().setMaterial(eyePupilsMaterial);
	}

	private Color faded(Color color) {
		return Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.90);
	}
}