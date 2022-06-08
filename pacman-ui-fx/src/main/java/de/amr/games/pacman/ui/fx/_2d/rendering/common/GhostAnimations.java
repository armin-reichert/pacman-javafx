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
import de.amr.games.pacman.lib.animation.GenericAnimationAPI;
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.lib.animation.CompositeGenericAnimation;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations.Key;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostAnimations implements CompositeGenericAnimation<Ghost, Key, Rectangle2D> {

	public enum Key {
		COLOR, EYES, VALUE, BLUE, FLASHING;
	}

	private Key selectedKey;
	public GenericAnimationMap<Direction, Rectangle2D> eyes;
	public GenericAnimation<Rectangle2D> flashing;
	public GenericAnimation<Rectangle2D> blue;
	public GenericAnimationMap<Direction, Rectangle2D> color;
	public GenericAnimation<Rectangle2D> values;

	public GhostAnimations(int ghostID, Rendering2D r2D) {
		eyes = r2D.createGhostEyesAnimation();
		flashing = r2D.createGhostFlashingAnimation();
		blue = r2D.createGhostBlueAnimation();
		color = r2D.createGhostColorAnimation(ghostID);
		values = r2D.createGhostValueAnimation();
	}

	@Override
	public Key selectedKey() {
		return selectedKey;
	}

	@Override
	public void select(Key key) {
		selectedKey = key;
		selectedAnimation().ensureRunning();
	}

	@Override
	public GenericAnimationAPI animation(Key key) {
		return switch (key) {
		case EYES -> eyes;
		case FLASHING -> flashing;
		case BLUE -> blue;
		case COLOR -> color;
		case VALUE -> values;
		};
	}

	@Override
	public Stream<GenericAnimationAPI> animations() {
		return Stream.of(eyes, flashing, blue, color, values);
	}

	public void startFlashing(int numFlashes, long ticksTotal) {
		long frameTicks = ticksTotal / (numFlashes * flashing.numFrames());
		flashing.frameDuration(frameTicks);
		flashing.repeat(numFlashes);
		flashing.restart();
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
		case EYES -> eyes.get(ghost.wishDir()).frame();
		case FLASHING -> flashing.animate();
		case BLUE -> blue.animate();
		case COLOR -> color.get(ghost.wishDir()).animate();
		case VALUE -> values.frame(frameIndex(ghost.bounty));
		};
	}

	private int frameIndex(int ghostValue) {
		return switch (ghostValue) {
		case 200 -> 0;
		case 400 -> 1;
		case 800 -> 2;
		case 1600 -> 3;
		default -> throw new IllegalArgumentException();
		};
	}
}