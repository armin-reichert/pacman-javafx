/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell;

import java.util.function.Supplier;

import javafx.scene.text.Text;

/**
 * Text field whose text is conditionally computed.
 * 
 * @author Armin Reichert
 */
public class InfoText extends Text {

	private Supplier<Boolean> fnEvaluate = () -> true;
	private Supplier<?> fnText = () -> "Value";

	public InfoText(String text) {
		this(() -> text);
	}

	public InfoText(Supplier<?> fnText) {
		this.fnText = fnText;
	}

	public InfoText when(Supplier<Boolean> fnEvaluate) {
		this.fnEvaluate = fnEvaluate;
		return this;
	}

	public void update() {
		if (fnEvaluate.get()) {
			setText(String.valueOf(fnText.get()));
		} else {
			setText("n/a");
		}
	}
}