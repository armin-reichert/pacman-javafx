/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell.info;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Section for displaying UI info or editors for configuring the UI.
 * 
 * @author Armin Reichert
 */
public class Section extends TitledPane {

	protected final GameUI ui;
	protected final GameController gc;
	protected final List<InfoText> infoTexts = new ArrayList<>();
	protected final GridPane content = new GridPane();

	private int minLabelWidth;
	private Color textColor;
	private Font textFont;
	private Font labelFont;
	private int row;

	public Section(GameUI ui, GameController gc, String title, int minLabelWidth, Color textColor, Font textFont,
			Font labelFont) {
		this.ui = ui;
		this.gc = gc;
		this.minLabelWidth = minLabelWidth;
		this.textColor = textColor;
		this.textFont = textFont;
		this.labelFont = labelFont;
		setOpacity(0.7);
		setFocusTraversable(false);
		setText(title);
		setContent(content);
		content.setBackground(Ufx.colorBackground(new Color(0.2, 0.2, 0.4, 0.8)));
		content.setHgap(4);
		content.setVgap(3);
		content.setPadding(new Insets(5));
		setExpanded(false);
	}

	protected GameModel game() {
		return gc.game();
	}

	public void init() {
	}

	public void update() {
		infoTexts.forEach(InfoText::update);
	}

	public InfoText addInfo(String labelText, Supplier<?> fnValue) {
		InfoText info = new InfoText(fnValue);
		info.setFill(textColor);
		info.setFont(textFont);
		infoTexts.add(info);
		addRow(labelText, info);
		return info;
	}

	public InfoText addInfo(String labelText, String value) {
		return addInfo(labelText, () -> value);
	}

	public void addRow(String labelText, Node child) {
		Label label = new Label(labelText);
		label.setTextFill(textColor);
		label.setFont(labelFont);
		label.setMinWidth(minLabelWidth);
		content.add(label, 0, row);
		content.add(child, 1, row);
		++row;
	}

	public Button addButton(String labelText, String buttonText, Runnable action) {
		Button button = new Button(buttonText);
		button.setFont(textFont);
		button.setOnAction(e -> action.run());
		addRow(labelText, button);
		return button;
	}

	public Button[] addButtonList(String labelText, String... buttonTexts) {
		HBox hbox = new HBox();
		Button[] buttons = new Button[buttonTexts.length];
		for (int i = 0; i < buttonTexts.length; ++i) {
			buttons[i] = new Button(buttonTexts[i]);
			buttons[i].setFont(textFont);
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
		combo.setStyle(style(textFont));
		addRow(labelText, combo);
		return combo;
	}

	public ColorPicker addColorPicker(String labelText, Color color) {
		var colorPicker = new ColorPicker(color);
		addRow(labelText, colorPicker);
		return colorPicker;
	}

	private static String style(Font font) {
		return String.format("-fx-font: %.0fpx \"%s\";", font.getSize(), font.getFamily());
	}

	public Slider addSlider(String labelText, double min, double max, double value) {
		Slider slider = new Slider(min, max, value);
		slider.setMinWidth(Dashboard.MIN_COL_WIDTH);
		addRow(labelText, slider);
		return slider;
	}

	public Spinner<Integer> addSpinner(String labelText, int min, int max, int initialValue) {
		Spinner<Integer> spinner = new Spinner<>(min, max, initialValue);
		spinner.setStyle(style(textFont));
		addRow(labelText, spinner);
		return spinner;
	}
}