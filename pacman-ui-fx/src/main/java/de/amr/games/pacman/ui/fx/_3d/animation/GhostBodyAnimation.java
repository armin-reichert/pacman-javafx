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
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation.Status;
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
	private final Group root3D;
	private final ColorFlashing flashingAnimation;
	private final PortalAppearance portalAppearance;

	private final ObjectProperty<Color> pyDressColor = new SimpleObjectProperty<>();
	private final ObjectProperty<Color> pyEyeBallsColor = new SimpleObjectProperty<>();
	private final ObjectProperty<Color> pyEyePupilsColor = new SimpleObjectProperty<>();

	public GhostBodyAnimation(Ghost ghost, Model3D model3D) {
		this.ghost = ghost;
		this.model3D = model3D;

		var dressColor = faded(Rendering3D.getGhostDressColor(ghost.id));
		var eyeBallColor = Rendering3D.getGhostEyeBallColor();
		var pupilColor = Rendering3D.getGhostPupilColor();

		root3D = model3D.createGhost(dressColor, eyeBallColor, pupilColor);

		flashingAnimation = new ColorFlashing(Rendering3D.getGhostBlueDressColor(),
				Rendering3D.getGhostFlashingDressColor());

		pyDressColor.set(dressColor);
		var dressMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(dressMaterial, pyDressColor);
		dress().setMaterial(dressMaterial);

		pyEyeBallsColor.set(Rendering3D.getGhostEyeBallColor());
		var eyeBallsMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(eyeBallsMaterial, pyEyeBallsColor);
		eyeBalls().setMaterial(eyeBallsMaterial);

		pyEyePupilsColor.set(Rendering3D.getGhostPupilColor());
		var eyePupilsMaterial = new PhongMaterial();
		Ufx.bindMaterialColor(eyePupilsMaterial, pyEyePupilsColor);
		eyePupils().setMaterial(eyePupilsMaterial);

		portalAppearance = new PortalAppearance(pyDressColor, () -> faded(Rendering3D.getGhostDressColor(ghost.id)));
	}

	public void update(World world) {
		portalAppearance.update(root3D, ghost, world);
	}

	public Shape3D dress() {
		return model3D.ghostDress(root3D);
	}

	private Shape3D eyeBalls() {
		return model3D.ghostEyeBalls(root3D);
	}

	private Shape3D eyePupils() {
		return model3D.ghostEyePupils(root3D);
	}

	public Node getRoot() {
		return root3D;
	}

	public boolean isFlashing() {
		return flashingAnimation.getStatus() == Status.RUNNING;
	}

	public void ensureFlashingAnimationRunning() {
		if (!isFlashing()) {
			pyDressColor.bind(flashingAnimation.pyColor);
			flashingAnimation.playFromStart();
		}
	}

	public void ensureFlashingAnimationStopped() {
		if (isFlashing()) {
			pyDressColor.unbind();
			flashingAnimation.stop();
		}
	}

	public void setBlue() {
		pyDressColor.unbind();
		pyDressColor.set(faded(Rendering3D.getGhostBlueDressColor()));
		pyEyeBallsColor.set(Rendering3D.getGhostEyeBallColorFrightened());
		pyEyePupilsColor.set(Rendering3D.getGhostPupilColorFrightened());
	}

	public void setColored() {
		pyDressColor.unbind();
		pyDressColor.set(faded(Rendering3D.getGhostDressColor(ghost.id)));
		pyEyeBallsColor.set(Rendering3D.getGhostEyeBallColor());
		pyEyePupilsColor.set(Rendering3D.getGhostPupilColor());
	}

	private Color faded(Color color) {
		return Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.95);
	}
}