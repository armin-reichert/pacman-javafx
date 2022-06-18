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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.FillTransition3D;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of Pac-Man or Ms. Pac-Man.
 * 
 * <p>
 * TODO: Specific 3D-model for Ms. Pac-Man, mouth animation
 * 
 * @author Armin Reichert
 */
public class Pac3D extends Group {

	private final World world;
	private final Pac pac;
	private final Group bodyParts;
	private final Motion motion;
	private final PointLight light = new PointLight(Color.WHITE);
	private Color skullColorImpaled = Color.GHOSTWHITE;

	public Pac3D(World world, Pac pac, PacManModel3D model3D) {
		this.world = world;
		this.pac = pac;
		bodyParts = model3D.createPacMan(Rendering3D.getPacSkullColor(), Rendering3D.getPacEyesColor(),
				Rendering3D.getPacPalateColor());
		motion = new Motion(this);
		light.setTranslateZ(-HTS);
		getChildren().addAll(bodyParts, light);
		reset();
		// only for testing
		skull().setUserData(this);
		eyes().setUserData(this);
		palate().setUserData(this);
	}

	private boolean insideWorld() {
		V2i tile = pac.tile();
		return 0 <= tile.x && tile.x < world.numCols() && 0 <= tile.y && tile.y < world.numRows();
	}

	public String identifyNode(Node node) {
		if (node == eyes()) {
			return String.format("eyes of %s", pac);
		} else if (node == palate()) {
			return String.format("palate of %s", pac);
		} else if (node == skull()) {
			return String.format("skull of %s", pac);
		} else {
			return String.format("part of %s", pac);
		}
	}

	public void reset() {
		bodyParts.setScaleX(1.05);
		bodyParts.setScaleY(1.05);
		bodyParts.setScaleZ(1.05);
		setShapeColor(skull(), Rendering3D.getPacSkullColor());
		update();
	}

	public void update() {
		motion.update(pac);
		setVisible(pac.visible && insideWorld());
	}

	public Shape3D skull() {
		return (Shape3D) bodyParts.getChildren().get(0);
	}

	public Shape3D eyes() {
		return (Shape3D) bodyParts.getChildren().get(1);
	}

	public Shape3D palate() {
		return (Shape3D) bodyParts.getChildren().get(2);
	}

	public Animation dyingAnimation(Color ghostColor, boolean silent) {
		var spin = new RotateTransition(Duration.seconds(0.2), bodyParts);
		spin.setAxis(Rotate.Z_AXIS);
		spin.setByAngle(360);
		spin.setCycleCount(10);

		var shrink = new ScaleTransition(Duration.seconds(2), bodyParts);
		shrink.setToX(0);
		shrink.setToY(0);
		shrink.setToZ(0);

		return new SequentialTransition( //
				new FillTransition3D(Duration.seconds(1), skull(), Rendering3D.getPacSkullColor(), ghostColor), //
				new FillTransition3D(Duration.seconds(1), skull(), ghostColor, skullColorImpaled), //
				new ParallelTransition(spin, shrink));
	}

	private void setShapeColor(Shape3D shape, Color diffuseColor) {
		var material = new PhongMaterial(diffuseColor);
		material.setSpecularColor(diffuseColor.brighter());
		shape.setMaterial(material);
	}
}