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

import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class Text3D {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private Box box;
	private double quality = 3;
	private Font font = Font.font(8);
	private Color bgColor = Color.WHITE;
	private Color textColor = Color.RED;
	private String text = "";
	private boolean batchUpdate;

	public Text3D() {
		box = new Box(100, 10, 0.1);
		box.setCache(true); // TODO needed?
	}

	private void updateImage() {
		if (batchUpdate) {
			return;
		}
		box.setWidth(text.length() * font.getSize());
		box.setHeight(font.getSize());
		var canvas = new Canvas(box.getWidth() * quality, box.getHeight() * quality);
		var g = canvas.getGraphicsContext2D();
		var canvasFontSize = font.getSize() * quality;
		g.setFill(bgColor);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setFont(Font.font(font.getFamily(), canvasFontSize));
		g.setFill(textColor);
		g.fillText(text, 0, canvasFontSize);

		var material = ResourceMgr.coloredMaterial(Color.WHITE);
		material.setDiffuseMap(canvas.snapshot(null, null));
		box.setMaterial(material);
		LOG.info("New image produced");
	}

	public Node getRoot() {
		return box;
	}

	public void setTranslate(double x, double y, double z) {
		box.setTranslateX(x);
		box.setTranslateY(y);
		box.setTranslateZ(z);
	}

	public void setRotate(Point3D axis, double angle) {
		box.setRotationAxis(axis);
		box.setRotate(angle);
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

	public void setTextColor(Color color) {
		if (color.equals(this.textColor)) {
			return;
		}
		this.textColor = color;
		updateImage();
	}

	public void setText(String text) {
		if (text.equals(this.text)) {
			return;
		}
		this.text = text;
		updateImage();
	}

	public void setVisible(boolean visible) {
		box.setVisible(visible);
	}
}