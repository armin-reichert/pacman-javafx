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

import de.amr.games.pacman.ui.fx.app.Env;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Floor of a building.
 * 
 * @author Armin Reichert
 */
public class Floor3D {

	private boolean textureVisible;
	private Image texture;
	private Color color;
	private final Box root;

	public Floor3D(double width, double height, double depth) {
		root = new Box(width, height, depth);
		root.drawModeProperty().bind(Env.drawMode3D);
		update();
	}

	public Box getRoot() {
		return root;
	}

	public void setTextureVisible(boolean textureVisible) {
		this.textureVisible = textureVisible;
		update();
	}

	public void setTexture(Image texture) {
		this.texture = texture;
		update();
	}

	public Image getTexture() {
		return texture;
	}

	public void setColor(Color color) {
		this.color = color;
		update();
	}

	public Color getColor() {
		return color;
	}

	public void update() {
		root.setMaterial(createMaterial());
	}

	private PhongMaterial createMaterial() {
		var mat = new PhongMaterial();
		if (color != null) {
			mat.setDiffuseColor(color);
			mat.setSpecularColor(color.brighter());
		} else {
			mat.setDiffuseColor(Color.BLACK);
		}
		if (texture != null && textureVisible) {
			mat.setDiffuseMap(texture);
		}
		return mat;
	}
}