package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controls player movement using cursor keys.
 * 
 * @author Armin Reichert
 */
class ManualPlayerControl {

	private final KeyCode upCode, downCode, leftCode, rightCode;
	private boolean up, down, left, right;

	public ManualPlayerControl(KeyCode upCode, KeyCode downCode, KeyCode leftCode, KeyCode rightCode) {
		this.upCode = upCode;
		this.downCode = downCode;
		this.leftCode = leftCode;
		this.rightCode = rightCode;
	}

	public void steer(Pac player) {
		if (up) {
			player.setWishDir(Direction.UP);
		} else if (down) {
			player.setWishDir(Direction.DOWN);
		} else if (left) {
			player.setWishDir(Direction.LEFT);
		} else if (right) {
			player.setWishDir(Direction.RIGHT);
		}
	}

	public void onKeyPressed(KeyEvent e) {
		if (e.getCode() == upCode) {
			up = true;
		} else if (e.getCode() == downCode) {
			down = true;
		} else if (e.getCode() == leftCode) {
			left = true;
		} else if (e.getCode() == rightCode) {
			right = true;
		}
	}

	public void onKeyReleased(KeyEvent e) {
		if (e.getCode() == upCode) {
			up = false;
		} else if (e.getCode() == downCode) {
			down = false;
		} else if (e.getCode() == leftCode) {
			left = false;
		} else if (e.getCode() == rightCode) {
			right = false;
		}
	}
}