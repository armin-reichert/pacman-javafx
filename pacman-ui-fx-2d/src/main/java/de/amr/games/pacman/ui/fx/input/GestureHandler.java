/*
MIT License

Copyright (c) 2022 Armin Reichert

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
package de.amr.games.pacman.ui.fx.input;

import de.amr.games.pacman.lib.steering.Direction;
import javafx.scene.Node;
import org.tinylog.Logger;

import java.util.function.Consumer;

/**
 * @author Armin Reichert
 */
public class GestureHandler {

	private boolean dragged;
	private double gestureStartX;
	private double gestureStartY;
	private double gestureEndX;
	private double gestureEndY;
	private Consumer<Direction> dirConsumer = dir -> Logger.info("Move {}", dir);

	public GestureHandler(Node node) {
		node.setOnMousePressed(event -> {
			dragged = false;
			gestureStartX = event.getX();
			gestureStartY = event.getY();

		});
		node.setOnMouseDragged(event -> {
			dragged = true;
		});
		node.setOnMouseReleased(event -> {
			if (dragged) {
				gestureEndX = event.getX();
				gestureEndY = event.getY();
				Direction dir = computeDirection();
				dirConsumer.accept(dir);
			}
		});
	}

	private Direction computeDirection() {
		double dx = Math.abs(gestureEndX - gestureStartX);
		double dy = Math.abs(gestureEndY - gestureStartY);
		if (dx > dy) {
			// horizontal
			return gestureEndX > gestureStartX ? Direction.RIGHT : Direction.LEFT;
		} else {
			// vertical
			return gestureEndY > gestureStartY ? Direction.DOWN : Direction.UP;
		}
	}

	public void setOnDirectionRecognized(Consumer<Direction> dirConsumer) {
		this.dirConsumer = dirConsumer;
	}

}