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

import de.amr.games.pacman.lib.animation.AnimationMethods;
import de.amr.games.pacman.lib.animation.CompositeGenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.ui.fx._2d.rendering.common.BonusAnimations.Key;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class BonusAnimations implements CompositeGenericAnimation<Bonus, Key, Rectangle2D> {

	public enum Key {
		SYMBOL, VALUE;
	}

	private Key selectedKey;
	public final GenericAnimation<Rectangle2D> symbolAnimation;
	public final GenericAnimation<Rectangle2D> valueAnimation;
	public final GenericAnimation<Integer> jumpAnimation;

	public BonusAnimations(Rendering2D r2D) {
		symbolAnimation = r2D.createBonusSymbolAnimation();
		valueAnimation = r2D.createBonusValueAnimation();
		jumpAnimation = new GenericAnimation<>(2, -2);
		jumpAnimation.frameDuration(10);
		jumpAnimation.repeatForever();
	}

	@Override
	public void ensureRunning() {
	}

	@Override
	public void setFrameIndex(int index) {
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
	public AnimationMethods animation(Key key) {
		return switch (key) {
		case SYMBOL -> symbolAnimation;
		case VALUE -> valueAnimation;
		};
	}

	@Override
	public Stream<AnimationMethods> animations() {
		return Stream.of(symbolAnimation, valueAnimation);
	}

	@Override
	public Rectangle2D currentSprite(Bonus bonus) {
		return switch (selectedKey) {
		case SYMBOL -> symbolAnimation.frame(bonus.symbol());
		case VALUE -> valueAnimation.frame(bonus.symbol());
		};
	}
}