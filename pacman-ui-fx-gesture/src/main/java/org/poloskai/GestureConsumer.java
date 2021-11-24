package org.poloskai;

import de.amr.games.pacman.controller.PlayerControl;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.common.Pac;

/**
 * Consumes gestures and steers player accordingly.
 *
 */
public class GestureConsumer implements PlayerControl {

	private final GestureProducer gp;

	public GestureConsumer(GestureProducer gp) {
		this.gp = gp;
	}

	@Override
	public void steer(Pac player) {
		if (gp.gestureQ.isEmpty()) {
			return;
		}
		String gesture = gp.gestureQ.poll();
		switch (gesture) {
		case "UP":
			player.setWishDir(Direction.UP);
			break;
		case "DOWN":
			player.setWishDir(Direction.DOWN);
			break;
		case "LEFT":
			player.setWishDir(Direction.LEFT);
			break;
		case "RIGHT":
			player.setWishDir(Direction.RIGHT);
			break;
		default:
			Logging.log("Unknown gesture %s", gesture);
		}
	}
}