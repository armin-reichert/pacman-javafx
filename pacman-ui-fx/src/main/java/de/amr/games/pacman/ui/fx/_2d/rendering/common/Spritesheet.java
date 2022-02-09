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
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import javafx.scene.image.Image;

/**
 * A spritesheet.
 * 
 * @author Armin Reichert
 *
 */
public class Spritesheet {

	private final Image image;
	private final int rasterSize;
	private final Map<Direction, Integer> dirIndex;

	public Spritesheet(String imagePath, int rasterSize, Direction... directions) {
		this.image = new Image(getClass().getResource(imagePath).toString());
		this.rasterSize = rasterSize;
		dirIndex = new EnumMap<>(Direction.class);
		for (int i = 0; i < directions.length; ++i) {
			dirIndex.put(directions[i], i);
		}
	}

	public int dirIndex(Direction dir) {
		return dirIndex.get(dir);
	}

	public Image getImage() {
		return image;
	}

	public int getRasterSize() {
		return rasterSize;
	}
}