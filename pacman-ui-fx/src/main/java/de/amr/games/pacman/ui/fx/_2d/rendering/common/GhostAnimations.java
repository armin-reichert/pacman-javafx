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
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationCollection;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostAnimationKey;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostAnimations implements GenericAnimationCollection<Ghost, GhostAnimationKey, Rectangle2D> {

	private GhostAnimationKey selectedKey;
	public GenericAnimationMap<Direction, Rectangle2D> eyes;
	public SingleGenericAnimation<Rectangle2D> flashing;
	public SingleGenericAnimation<Rectangle2D> blue;
	public GenericAnimationMap<Direction, Rectangle2D> color;
	public SingleGenericAnimation<Rectangle2D> values;

	public GhostAnimations(int ghostID, Rendering2D r2D) {
		eyes = r2D.createGhostEyesAnimation();
		flashing = r2D.createGhostFlashingAnimation();
		blue = r2D.createGhostBlueAnimation();
		color = r2D.createGhostColorAnimation(ghostID);
		values = r2D.createGhostValueAnimation();
		select(GhostAnimationKey.ANIM_COLOR);
	}

	@Override
	public GhostAnimationKey selectedKey() {
		return selectedKey;
	}

	@Override
	public void select(GhostAnimationKey key) {
		selectedKey = key;
	}

	@Override
	public GenericAnimation getByKey(GhostAnimationKey key) {
		return switch (key) {
		case ANIM_EYES -> eyes;
		case ANIM_FLASHING -> flashing;
		case ANIM_BLUE -> blue;
		case ANIM_COLOR -> color;
		case ANIM_VALUE -> values;
		};
	}

	@Override
	public Stream<GenericAnimation> animations() {
		return Stream.of(eyes, flashing, blue, color, values);
	}

	@Override
	public void ensureRunning() {
		eyes.ensureRunning();
		blue.ensureRunning();
		flashing.ensureRunning();
		color.ensureRunning();
	}

	@Override
	public void setFrameIndex(int index) {
		// TODO what?
	}

	@Override
	public Rectangle2D currentSprite(Ghost ghost) {
		return switch (selectedKey) {
		case ANIM_EYES -> eyes.get(ghost.wishDir()).frame();
		case ANIM_FLASHING -> flashing.animate();
		case ANIM_BLUE -> blue.animate();
		case ANIM_COLOR -> color.get(ghost.wishDir()).animate();
		case ANIM_VALUE -> ghost.killIndex >= 0 ? values.frame(ghost.killIndex) : null;// TODO
		};
	}
}