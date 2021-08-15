package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import javafx.scene.input.KeyEvent;

/**
 * Controls player movement using cursor keys.
 * 
 * @author Armin Reichert
 */
class ManualPlayerControl {
	private boolean up, down, left, right;

	public void steer(Pac player) {
		if (up) {
			player.setWishDir(Direction.UP);
		}
		if (down) {
			player.setWishDir(Direction.DOWN);
		}
		if (left) {
			player.setWishDir(Direction.LEFT);
		}
		if (right) {
			player.setWishDir(Direction.RIGHT);
		}
	}

	public void onKeyPressed(KeyEvent e) {
		switch (e.getCode()) {
		case UP:
			up = true;
			break;
		case DOWN:
			down = true;
			break;
		case LEFT:
			left = true;
			break;
		case RIGHT:
			right = true;
			break;
		default:
			break;
		}
	}

	public void onKeyReleased(KeyEvent e) {
		switch (e.getCode()) {
		case UP:
			up = false;
			break;
		case DOWN:
			down = false;
			break;
		case LEFT:
			left = false;
			break;
		case RIGHT:
			right = false;
			break;
		default:
			break;
		}
	}
}