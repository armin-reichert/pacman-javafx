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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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

	public final BooleanProperty textureVisible = new SimpleBooleanProperty(false);
	public final ObjectProperty<Image> texture = new SimpleObjectProperty<>(null);
	public final ObjectProperty<Color> textureColor = new SimpleObjectProperty<>(Color.BLACK);
	public final ObjectProperty<Color> solidColor = new SimpleObjectProperty<>(Color.GREEN);

	private final Box root;

	public Floor3D(double width, double height, double depth) {
		root = new Box(width, height, depth);
		root.drawModeProperty().bind(Env.drawMode3D);
		texture.addListener((x, y, z) -> update());
		textureColor.addListener((x, y, z) -> update());
		solidColor.addListener((x, y, z) -> update());
		textureVisible.addListener((x, y, z) -> update());
		update();
	}

	public Box getRoot() {
		return root;
	}

	private void update() {
		if (textureVisible.get()) {
			displayTexture();
		} else {
			displaySolid();
		}
	}

	private void displayTexture() {
		var material = new PhongMaterial();
		material.setDiffuseColor(textureColor.get());
		material.setSpecularColor(textureColor.get().brighter());
		material.setDiffuseMap(texture.get());
		root.setMaterial(material);
	}

	private void displaySolid() {
		var material = new PhongMaterial();
		material.setDiffuseColor(solidColor.get());
		material.setSpecularColor(solidColor.get().brighter());
		root.setMaterial(material);
	}
}