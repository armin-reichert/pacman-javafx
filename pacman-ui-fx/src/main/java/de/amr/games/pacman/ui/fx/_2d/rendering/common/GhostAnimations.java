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
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.SimpleThingAnimation;
import de.amr.games.pacman.lib.animation.ThingAnimation;
import de.amr.games.pacman.lib.animation.ThingAnimationCollection;
import de.amr.games.pacman.lib.animation.ThingAnimationMap;
import de.amr.games.pacman.lib.animation.ThingList;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostAnimationKey;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostAnimations extends ThingAnimationCollection<Ghost, GhostAnimationKey, Rectangle2D> {

	public ThingAnimationMap<Direction, Rectangle2D> eyesByDir;
	public SimpleThingAnimation<Rectangle2D> flashing;
	public SimpleThingAnimation<Rectangle2D> blue;
	public ThingAnimationMap<Direction, Rectangle2D> colorByDir;
	public ThingList<Rectangle2D> values;

	public GhostAnimations(int ghostID, Rendering2D r2D) {
		eyesByDir = r2D.createGhostEyesAnimation();
		flashing = r2D.createGhostFlashingAnimation();
		blue = r2D.createGhostBlueAnimation();
		colorByDir = r2D.createGhostColorAnimation(ghostID);
		values = r2D.createGhostValueList();
		select(GhostAnimationKey.ANIM_COLOR);
	}

	@Override
	public ThingAnimation<Rectangle2D> byKey(GhostAnimationKey key) {
		return switch (key) {
		case ANIM_EYES -> eyesByDir;
		case ANIM_FLASHING -> flashing;
		case ANIM_BLUE -> blue;
		case ANIM_COLOR -> colorByDir;
		case ANIM_VALUE -> values;
		};
	}

	@Override
	public Stream<ThingAnimation<Rectangle2D>> all() {
		return Stream.of(eyesByDir, flashing, blue, colorByDir, values);
	}

	@Override
	public Rectangle2D current(Ghost ghost) {
		return switch (selectedKey) {
		case ANIM_EYES -> eyesByDir.get(ghost.wishDir()).animate();
		case ANIM_FLASHING -> flashing.animate();
		case ANIM_BLUE -> blue.animate();
		case ANIM_COLOR -> colorByDir.get(ghost.wishDir()).animate();
		case ANIM_VALUE -> ghost.killIndex >= 0 ? values.frame(ghost.killIndex) : null; // TODO check this
		};
	}
}