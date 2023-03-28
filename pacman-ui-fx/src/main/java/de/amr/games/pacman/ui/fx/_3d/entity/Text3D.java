/*
MIT License

Copyright (c) 2023 Armin Reichert

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class Text3D extends Box {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private final Canvas canvas;
	private double quality = 3;
	private Font font = Font.font(8);
	private Color bgColor = Color.WHITE;
	private Color color = Color.RED;
	private String text = "";
	private boolean batchUpdate;

	public Text3D(double width, double height) {
		super(width, height, 0.1);
		setCache(true); // TODO needed?
		canvas = new Canvas(width * quality, height * quality);
	}

	public void beginBatch() {
		batchUpdate = true;
	}

	public void endBatch() {
		batchUpdate = false;
		updateImage();
	}

	public void setQuality(double quality) {
		if (quality == this.quality) {
			return;
		}
		this.quality = quality;
		updateImage();
	}

	public void setFont(Font font) {
		if (font.equals(this.font)) {
			return;
		}
		this.font = font;
		updateImage();
	}

	public void setBgColor(Color bgColor) {
		if (bgColor.equals(this.bgColor)) {
			return;
		}
		this.bgColor = bgColor;
		updateImage();
	}

	public void setColor(Color color) {
		if (color.equals(this.color)) {
			return;
		}
		this.color = color;
		updateImage();
	}

	public void setText(String text) {
		if (text.equals(this.text)) {
			return;
		}
		this.text = text;
		updateImage();
	}

	private void updateImage() {
		if (batchUpdate) {
			return;
		}
		var g = canvas.getGraphicsContext2D();
		var fontSize = font.getSize() * quality;
		g.setFill(bgColor);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setFont(Font.font(font.getFamily(), fontSize));
		g.setFill(color);
		g.fillText(text, 0, fontSize);
		var material = new PhongMaterial();
		material.setDiffuseMap(canvas.snapshot(null, null));
		setMaterial(material);
		LOG.info("New image produced");
	}
}