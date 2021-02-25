package de.amr.games.pacman.ui.fx.rendering.standard;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.common.Flap;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;

/**
 * UI of the flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class FlapUI extends Flap {

	public final Animation<?> flapping;

	public FlapUI(int number, String title, FXRendering rendering) {
		super(number, title);
		flapping = rendering.flapFlapping().restart();
	}
}