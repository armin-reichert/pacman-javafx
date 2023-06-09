/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Section for displaying UI info or editors for configuring the UI.
 * 
 * @author Armin Reichert
 */
public abstract class Section {

	public static String fmtSpeed(float fraction) {
		return String.format("%.2f px/sec", GameModel.SPEED_PX_100_PERCENT * fraction);
	}

	protected final PacManGames2dUI ui;
	protected final List<InfoText> infoTexts = new ArrayList<>();
	protected final TitledPane root = new TitledPane();
	protected final GridPane content = new GridPane();

	private int minLabelWidth;
	private Color textColor;
	private Font textFont;
	private Font labelFont;

	private int row;

	protected Section(PacManGames2dUI ui, String title, int minLabelWidth, Color textColor, Font textFont,
			Font labelFont) {
		this.ui = ui;
		this.minLabelWidth = minLabelWidth;
		this.textColor = textColor;
		this.textFont = textFont;
		this.labelFont = labelFont;
		content.setBackground(ResourceManager.coloredBackground(new Color(0.2, 0.2, 0.4, 0.8)));
		content.setHgap(4);
		content.setVgap(3);
		content.setPadding(new Insets(5));
		root.setExpanded(false);
		root.setOpacity(0.7);
		root.setFocusTraversable(false);
		root.setText(title);
		root.setContent(content);
	}

	public TitledPane getRoot() {
		return root;
	}

	public void update() {
		infoTexts.forEach(InfoText::update);
	}

	protected GameModel game() {
		return GameController.it().game();
	}

	protected GameScene gameScene() {
		return ui.currentGameScene();
	}

	protected Supplier<String> ifLevelExists(Function<GameLevel, String> infoSupplier) {
		return () -> {
			if (game().level().isEmpty()) {
				return InfoText.NO_INFO;
			}
			return infoSupplier.apply(game().level().get());
		};
	}

	private void addRow(String labelText, Node child) {
		Label label = new Label(labelText);
		label.setTextFill(textColor);
		label.setFont(labelFont);
		label.setMinWidth(minLabelWidth);
		content.add(label, 0, row);
		content.add(child, 1, row);
		++row;
	}

	protected InfoText addInfo(String labelText, Supplier<?> fnValue) {
		InfoText info = new InfoText(fnValue);
		info.setFill(textColor);
		info.setFont(textFont);
		infoTexts.add(info);
		addRow(labelText, info);
		return info;
	}

	protected void addEmptyLine() {
		addInfo("", "");
	}

	protected InfoText addInfo(String labelText, String value) {
		return addInfo(labelText, () -> value);
	}

	protected Button addButton(String labelText, String buttonText, Runnable action) {
		Button button = new Button(buttonText);
		button.setFont(textFont);
		button.setOnAction(e -> action.run());
		addRow(labelText, button);
		return button;
	}

	protected Button[] addButtonList(String labelText, String... buttonTexts) {
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

	protected CheckBox addCheckBox(String labelText, Runnable callback) {
		CheckBox cb = new CheckBox();
		cb.setTextFill(textColor);
		cb.setFont(textFont);
		cb.setOnAction(e -> callback.run());
		addRow(labelText, cb);
		return cb;
	}

	@SuppressWarnings("unchecked")
	protected <T> ComboBox<T> addComboBox(String labelText, T... items) {
		var combo = new ComboBox<T>(FXCollections.observableArrayList(items));
		combo.setStyle(style(textFont));
		addRow(labelText, combo);
		return combo;
	}

	protected ColorPicker addColorPicker(String labelText, Color color) {
		var colorPicker = new ColorPicker(color);
		addRow(labelText, colorPicker);
		return colorPicker;
	}

	protected Slider addSlider(String labelText, double min, double max, double initialValue) {
		Slider slider = new Slider(min, max, initialValue);
		slider.setMinWidth(Dashboard.MIN_COL_WIDTH);
		slider.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			if (e.getClickCount() == 2) {
				slider.setValue(initialValue);
			}
		});
		addRow(labelText, slider);
		return slider;
	}

	protected Spinner<Integer> addSpinner(String labelText, int min, int max, int initialValue) {
		Spinner<Integer> spinner = new Spinner<>(min, max, initialValue);
		spinner.setStyle(style(textFont));
		addRow(labelText, spinner);
		return spinner;
	}

	private static String style(Font font) {
		return String.format("-fx-font: %.0fpx \"%s\";", font.getSize(), font.getFamily());
	}
}