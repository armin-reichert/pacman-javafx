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
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Optional;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * 3D pellet.
 * 
 * @author Armin Reichert
 */
public class Pellet3D extends Sphere {

	private Animation animation;

	public Pellet3D(V2i tile, PhongMaterial material) {
		this(tile, material, 1.0);
	}

	public Pellet3D(V2i tile, PhongMaterial material, double radius) {
		setUserData(tile);
		setMaterial(material);
		setRadius(radius);
		setTranslateX(tile.x() * TS + HTS);
		setTranslateY(tile.y() * TS + HTS);
		setTranslateZ(-HTS + 1);
	}

	public V2i tile() {
		return (V2i) getUserData();
	}

	public void eat() {
		var hideAfterDelay = Ufx.pauseSec(0.05, () -> setVisible(false));
		if (animation != null) {
			new SequentialTransition(hideAfterDelay, animation).play();
		} else {
			hideAfterDelay.play();
		}
	}

	public Optional<Animation> getEatenAnimation() {
		return Optional.ofNullable(animation);
	}

	public void setEatenAnimation(Animation animation) {
		this.animation = animation;
	}

	@Override
	public String toString() {
		return String.format("[Pellet, tile: %s]", tile());
	}
}