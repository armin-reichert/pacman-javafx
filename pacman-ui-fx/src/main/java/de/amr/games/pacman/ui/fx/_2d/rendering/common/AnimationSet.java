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

import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class AnimationSet<KEY> {

	private KEY selectedKey;

	public abstract ISpriteAnimation<Rectangle2D> animation(KEY key);

	public abstract Stream<ISpriteAnimation<Rectangle2D>> animations();

	public void select(KEY key) {
		selectedKey = key;
		animation(selectedKey).ensureRunning();
	}

	public KEY selectedKey() {
		return selectedKey;
	}

	public ISpriteAnimation<Rectangle2D> selectedAnimation() {
		return animation(selectedKey);
	}

	public void stop(KEY key) {
		animation(key).stop();
	}

	public void run(KEY key) {
		animation(key).run();
	}

	public void restart(KEY key) {
		animation(key).restart();
	}

	public void reset() {
		animations().forEach(ISpriteAnimation::reset);
	}

	public void stop() {
		animations().forEach(ISpriteAnimation::stop);
	}

	public void run() {
		animations().forEach(ISpriteAnimation::run);
	}

	public void restart() {
		animations().forEach(ISpriteAnimation::restart);
	}
}