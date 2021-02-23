package de.amr.games.pacman.ui.fx.mspacman.entities;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.GameEntity;
import javafx.geometry.Rectangle2D;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap extends GameEntity {

	public final int sceneNumber;
	public final String sceneTitle;

	public final Animation<Rectangle2D> flapping = Animation.of( //
			new Rectangle2D(456, 208, 32, 32), //
			new Rectangle2D(488, 208, 32, 32), //
			new Rectangle2D(520, 208, 32, 32), //
			new Rectangle2D(488, 208, 32, 32), //
			new Rectangle2D(456, 208, 32, 32)//
	).repetitions(1).frameDuration(4);

	public Flap(int number, String title) {
		sceneNumber = number;
		sceneTitle = title;
	}
}