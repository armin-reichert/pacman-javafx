/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class ContextSensitiveHelp {

	public static class Menu {
		final List<List<Node>> rows = new ArrayList<>();
	}

	private final ResourceBundle translations;
	private final GameController gameController;
	private Color backgroundColor = Color.WHITE;
	private Font font = Font.font("Helvetica", 8);
	private FadeTransition closeAnimation;

	public ContextSensitiveHelp(GameController gameController, ResourceBundle translations) {
		this.gameController = gameController;
		this.translations = translations;
		closeAnimation = new FadeTransition(Duration.seconds(0.5));
		closeAnimation.setFromValue(1);
		closeAnimation.setToValue(0);
	}

	public void show(Node anchor, Duration openDuration) {
		if (closeAnimation.getStatus() == Status.RUNNING) {
			return;
		}
		anchor.setOpacity(1);
		closeAnimation.setNode(anchor);
		closeAnimation.setDelay(openDuration);
		closeAnimation.play();
	}

	public void addRow(Menu menu, String labelText, String keySpec) {
		menu.rows.add(Arrays.asList(label(labelText), key(keySpec)));
	}

	public Pane createPane(Menu menu) {
		var grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		for (int rowIndex = 0; rowIndex < menu.rows.size(); ++rowIndex) {
			var row = menu.rows.get(rowIndex);
			grid.add(row.get(0), 0, rowIndex);
			grid.add(row.get(1), 1, rowIndex);
		}
		if (gameController.isAutoControlled()) {
			var text = text(tt("help.autopilot_on"), Color.ORANGE);
			GridPane.setColumnSpan(text, 2);
			grid.add(text, 0, menu.rows.size());
		}
		if (gameController.game().isImmune()) {
			var text = text(tt("help.immunity_on"), Color.ORANGE);
			GridPane.setColumnSpan(text, 2);
			grid.add(text, 0, menu.rows.size() + 1);
		}

		var pane = new BorderPane(grid);
		pane.setPadding(new Insets(10));
		pane.setBackground(ResourceManager.colorBackground(backgroundColor));
		return pane;
	}

	public Optional<Pane> current() {
		var variant = gameController.game().variant();
		switch (variant) {
		case MS_PACMAN -> backgroundColor = (Color.rgb(255, 0, 0, 0.9));
		case PACMAN -> backgroundColor = (Color.rgb(33, 33, 255, 0.9));
		default -> throw new IllegalGameVariantException(variant);
		}
		var pane = switch (gameController.state()) {
		case CREDIT -> menuCredit();
		case INTRO -> menuIntro();
		case READY, HUNTING, PACMAN_DYING, GHOST_DYING -> attractMode() ? menuDemoLevel() : menuPlaying();
		default -> null;
		};
		return Optional.ofNullable(pane);
	}

	public void setFont(Font font) {
		this.font = font;
	}

	private String tt(String key) {
		return translations.getString(key);
	}

	private Label label(String s) {
		var label = new Label(s);
		label.setTextFill(Color.gray(0.9));
		label.setFont(font);
		return label;
	}

	private Text text(String s, Color color) {
		var text = new Text(s);
		text.setFill(color);
		text.setFont(font);
		return text;
	}

	private Text text(String s) {
		return text(s, Color.YELLOW);
	}

	private Text key(String key) {
		return text("[" + key + "]");
	}

	private GameModel game() {
		return gameController.game();
	}

	private boolean attractMode() {
		var gameLevel = game().level();
		return gameLevel.isPresent() && gameLevel.get().isDemoLevel();
	}

	private Pane menuIntro() {
		var menu = new Menu();
		if (game().credit() > 0) {
			addRow(menu, tt("help.start_game"), "1");
		}
		addRow(menu, tt("help.add_credit"), "5");
		addRow(menu, tt(game().variant() == GameVariant.MS_PACMAN ? "help.pacman" : "help.ms_pacman"), "V");
		return createPane(menu);
	}

	private Pane menuCredit() {
		var menu = new Menu();
		if (game().credit() > 0) {
			addRow(menu, tt("help.start_game"), "1");
		}
		addRow(menu, tt("help.add_credit"), "5");
		addRow(menu, tt("help.show_intro"), "Q");
		return createPane(menu);
	}

	private Menu menuPlaying;

	private Pane menuPlaying() {
		if (menuPlaying == null) {
			var menu = new Menu();
			addRow(menu, tt("help.move_left"), tt("help.cursor_left"));
			addRow(menu, tt("help.move_right"), tt("help.cursor_right"));
			addRow(menu, tt("help.move_up"), tt("help.cursor_up"));
			addRow(menu, tt("help.move_down"), tt("help.cursor_down"));
			addRow(menu, tt("help.show_intro"), "Q");
			menuPlaying = menu;
		}
		return createPane(menuPlaying);
	}

	private Menu menuDemoLevel;

	private Pane menuDemoLevel() {
		if (menuDemoLevel == null) {
			var menu = new Menu();
			addRow(menu, tt("help.start_game"), "5");
			addRow(menu, tt("help.show_intro"), "Q");
			menuDemoLevel = menu;
		}
		return createPane(menuDemoLevel);
	}
}