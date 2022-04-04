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
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class InfoSection extends TitledPane {

	public final List<InfoText> infos = new ArrayList<>();
	private final GridPane content = new GridPane();
	public Color textColor = Color.WHITE;
	public Font textFont = Font.font("Monospace", 12);
	public Font labelFont = Font.font("Sans", 12);
	public Color headerColor = Color.YELLOW;
	public Font headerFont = Font.font("Arial", FontWeight.BOLD, 16);
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

	public void addRow(String labelText, Node child) {
		Label label = new Label(labelText);
		label.setTextFill(textColor);
		label.setFont(textFont);
		content.add(label, 0, row);
		content.add(child, 1, row);
		++row;
	}

	public void addSectionHeader(String title) {
		Text header = new Text(title);
		header.setFill(headerColor);
		header.setFont(headerFont);
		header.setTextAlignment(TextAlignment.CENTER);
		GridPane.setConstraints(header, 0, row, 2, 1, HPos.CENTER, VPos.CENTER, Priority.NEVER, Priority.NEVER,
				new Insets(12, 0, 8, 0));
		content.getChildren().add(header);
		++row;
	}

	public Button addButton(String labelText, String buttonText, Runnable action) {
		Button button = new Button(buttonText);
		button.setOnAction(e -> action.run());
		addRow(labelText, button);
		return button;
	}

	public Button[] addButtons(String labelText, String... buttonTexts) {
		HBox hbox = new HBox();
		Button[] buttons = new Button[buttonTexts.length];
		for (int i = 0; i < buttonTexts.length; ++i) {
			buttons[i] = new Button(buttonTexts[i]);
			hbox.getChildren().add(buttons[i]);
		}
		addRow(labelText, hbox);
		return buttons;
	}

	public CheckBox addCheckBox(String labelText, Runnable callback) {
		CheckBox cb = new CheckBox();
		cb.setTextFill(textColor);
		cb.setFont(textFont);
		cb.setOnAction(e -> callback.run());
		addRow(labelText, cb);
		return cb;
	}

	@SuppressWarnings("unchecked")
	public <T> ComboBox<T> addComboBox(String labelText, T... items) {
		var combo = new ComboBox<T>(FXCollections.observableArrayList(items));
		addRow(labelText, combo);
		return combo;
	}

	public Slider addSlider(String labelText, double min, double max, double value) {
		Slider slider = new Slider(min, max, value);
		slider.setMinWidth(200);
		addRow(labelText, slider);
		return slider;
	}

	public Spinner<Integer> addSpinner(String labelText, int min, int max, int initialValue) {
		Spinner<Integer> spinner = new Spinner<>(min, max, initialValue);
		addRow(labelText, spinner);
		return spinner;
	}
}