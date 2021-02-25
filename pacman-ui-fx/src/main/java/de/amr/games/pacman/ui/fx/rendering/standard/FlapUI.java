package de.amr.games.pacman.ui.fx.rendering.standard;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.Flap;
import javafx.geometry.Rectangle2D;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class FlapUI extends Flap {

	public final Animation<Rectangle2D> flapping = Animation.of( //
			new Rectangle2D(456, 208, 32, 32), //
			new Rectangle2D(488, 208, 32, 32), //
			new Rectangle2D(520, 208, 32, 32), //
			new Rectangle2D(488, 208, 32, 32), //
			new Rectangle2D(456, 208, 32, 32)//
	).repetitions(1).frameDuration(4);

	public FlapUI(int number, String title) {
		super(number, title);
	}
}