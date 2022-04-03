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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class InfoSection extends TitledPane {

	public final List<InfoText> infos = new ArrayList<>();
	private final GridPane content = new GridPane();
	public Color textColor = Color.WHITE;
	public Font textFont = Font.font("Monospace", 12);
	public Font labelFont = Font.font("Sans", 12);
	private int row;

	public InfoSection(String title) {
		setOpacity(0.7);
		setFocusTraversable(false);
		setText(title);
		setContent(content);
		content.setBackground(U.colorBackground(new Color(0.1, 0.1, 0.1, 0.8)));
		content.setHgap(4);
		content.setVgap(1);
		content.setPadding(new Insets(5));
	}

	public InfoText addInfo(String labelText, Supplier<?> fnValue) {
		Label label = new Label(labelText);
		label.setTextFill(textColor);
		label.setFont(labelFont);
		label.setMinWidth(150);
		content.add(label, 0, row);

		Label separator = new Label(labelText.length() == 0 ? "" : ":");
		separator.setTextFill(textColor);
		separator.setFont(textFont);
		content.add(separator, 1, row);

		InfoText info = new InfoText(fnValue);
		info.setFill(textColor);
		info.setFont(textFont);
		infos.add(info);
		content.add(info, 2, row);

		++row;
		return info;
	}

	public InfoText addInfo(String labelText, String value) {
		return addInfo(labelText, () -> value);
	}
}