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

import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Animated cube displaying a number. Used for eaten ghosts.
 * 
 * @author Armin Reichert
 */
public class NumberAnimation3D extends Box {

	private final Image[] valueImages;

	public NumberAnimation3D(Rendering2D r2D) {
		super(8, 8, 8);
		valueImages = r2D.createGhostValueList().frames().map(r2D::getSpriteImage).toArray(Image[]::new);
	}

	public void selectNumberAtIndex(int index) {
		var texture = valueImages[index];
		var material = new PhongMaterial();
		material.setBumpMap(texture);
		material.setDiffuseMap(texture);
		setMaterial(material);
	}
}