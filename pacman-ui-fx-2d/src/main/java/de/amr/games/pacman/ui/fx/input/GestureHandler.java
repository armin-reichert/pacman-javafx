/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.input;

import java.util.function.Consumer;

import org.tinylog.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import javafx.scene.Node;

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