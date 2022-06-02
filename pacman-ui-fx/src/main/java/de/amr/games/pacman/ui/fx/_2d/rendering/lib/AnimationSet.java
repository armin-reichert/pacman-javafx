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

package de.amr.games.pacman.ui.fx._2d.rendering.lib;

import java.util.stream.Stream;

/**
 * @author Armin Reichert
 * 
 * @param <K> Key type (enum)
 * @param <S> Sprite type (Rectangle, Image)
 */
public abstract class AnimationSet<K, S> {

	private K selectedKey;

	public abstract ISpriteAnimation animation(K key);

	public abstract Stream<ISpriteAnimation> animations();

	public void select(K key) {
		selectedKey = key;
		animation(selectedKey).ensureRunning();
	}

	public K selectedKey() {
		return selectedKey;
	}

	public ISpriteAnimation selectedAnimation() {
		return animation(selectedKey);
	}

	public void stop(K key) {
		animation(key).stop();
	}

	public void run(K key) {
		animation(key).run();
	}

	public void restart(K key) {
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