/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashingTransition;
import de.amr.games.pacman.ui.fx._3d.animation.FadeInTransition3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D representation of a ghost. A ghost is displayed in one of 3 modes: as a full ghost, as eyes only or as a bonus
 * symbol indicating the bounty paid for killing the ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Group {

	private enum DisplayMode {
		COMPLETE_BODY, EYES_ONLY, NUMBER_CUBE
	}

	public final Ghost ghost;
	private final Group bodyParts;
	private final Box numberCube = new Box(8, 8, 8);
	private final Creature3DMotion<Ghost> motion;
	private final Rendering2D r2D;
	private final ColorFlashingTransition flashing = new ColorFlashingTransition(Color.color(0.5, 0.7, 1.0));

	private DisplayMode displayMode;
	private Color frightenedColor = Color.CORNFLOWERBLUE;

	public Ghost3D(Ghost ghost, PacManModel3D model3D, Rendering2D r2D) {
		this.ghost = ghost;
		this.r2D = r2D;
		bodyParts = model3D.createGhost(r2D.getGhostColor(ghost.id), Color.WHITE, Color.BLACK);
		motion = new Creature3DMotion<Ghost>(ghost, this);
		getChildren().addAll(bodyParts, numberCube);
		reset();
	}

	public void reset() {
		setNormalSkinColor();
		update();
	}

	public void update() {
		if (ghost.bounty > 0) {
			if (displayMode != DisplayMode.NUMBER_CUBE) {
				Image texture = r2D.spritesheet().extractRegion(r2D.getBountyNumberSprite(ghost.bounty));
				PhongMaterial material = new PhongMaterial();
				material.setBumpMap(texture);
				material.setDiffuseMap(texture);
				numberCube.setMaterial(material);
				// rotate such that number appears in right orientation
				setRotationAxis(Rotate.X_AXIS);
				setRotate(0);
				setDisplayMode(DisplayMode.NUMBER_CUBE);
			}
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			setDisplayMode(DisplayMode.EYES_ONLY);
			motion.update();
		} else {
			setDisplayMode(DisplayMode.COMPLETE_BODY);
			motion.update();
		}
		boolean insideWorld = ghost.position.x >= 0 && ghost.position.x <= t(ghost.world.numCols() - 1);
		bodyParts.setVisible(insideWorld && ghost.visible);
	}

	public Shape3D skin() {
		return (Shape3D) bodyParts.getChildren().get(0);
	}

	public Node eyes() {
		return bodyParts.getChildren().get(1);
	}

	private void setDisplayMode(DisplayMode mode) {
		if (displayMode != mode) {
			displayMode = mode;
			numberCube.setVisible(displayMode == DisplayMode.NUMBER_CUBE);
			skin().setVisible(displayMode == DisplayMode.COMPLETE_BODY);
			eyes().setVisible(displayMode == DisplayMode.COMPLETE_BODY || displayMode == DisplayMode.EYES_ONLY);
		}
	}

	public void playFlashingAnimation() {
		skin().setMaterial(flashing.getMaterial());
		flashing.playFromStart();
	}

	public void playRevivalAnimation() {
		new FadeInTransition3D(Duration.seconds(1.5), skin(), r2D.getGhostColor(ghost.id)).playFromStart();
	}

	public void setNormalSkinColor() {
		setSkinColor(r2D.getGhostColor(ghost.id));
	}

	public void setFrightenedSkinColor() {
		setSkinColor(frightenedColor);
	}

	private void setSkinColor(Color color) {
		flashing.stop();
		PhongMaterial material = new PhongMaterial(color);
		material.setSpecularColor(color.brighter());
		skin().setMaterial(material);
	}
}