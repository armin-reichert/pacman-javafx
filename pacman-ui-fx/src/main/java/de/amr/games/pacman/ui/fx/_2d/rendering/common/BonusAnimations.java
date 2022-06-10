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

import java.util.Objects;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationCollection;
import de.amr.games.pacman.lib.animation.StaticGenericAnimation;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusAnimationKey;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class BonusAnimations implements GenericAnimationCollection<Bonus, BonusAnimationKey, Rectangle2D> {

	private BonusAnimationKey selectedKey;
	public final StaticGenericAnimation<Rectangle2D> symbolAnimation;
	public final StaticGenericAnimation<Rectangle2D> valueAnimation;

	public BonusAnimations(Rendering2D r2D) {
		symbolAnimation = r2D.createBonusSymbolAnimation();
		valueAnimation = r2D.createBonusValueAnimation();
		select(BonusAnimationKey.ANIM_NONE);
	}

	@Override
	public Rectangle2D frame(int i) {
		return null;
	}

	@Override
	public void ensureRunning() {
	}

	@Override
	public BonusAnimationKey selectedKey() {
		return selectedKey;
	}

	@Override
	public void select(BonusAnimationKey key) {
		selectedKey = Objects.requireNonNull(key);
	}

	@Override
	public GenericAnimation<Rectangle2D> getByKey(BonusAnimationKey key) {
		return switch (key) {
		case ANIM_NONE -> null;
		case ANIM_SYMBOL -> symbolAnimation;
		case ANIM_VALUE -> valueAnimation;
		};
	}

	@Override
	public Stream<GenericAnimation<Rectangle2D>> all() {
		return Stream.of(symbolAnimation, valueAnimation);
	}

	@Override
	public Rectangle2D currentSprite(Bonus bonus) {
		return switch (selectedKey) {
		case ANIM_NONE -> null;
		case ANIM_SYMBOL -> symbolAnimation.frame(bonus.symbol());
		case ANIM_VALUE -> valueAnimation.frame(bonus.symbol());
		};
	}
}