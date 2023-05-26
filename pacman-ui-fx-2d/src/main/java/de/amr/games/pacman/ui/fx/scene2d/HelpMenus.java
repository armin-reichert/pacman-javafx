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

package de.amr.games.pacman.ui.fx.scene2d;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * @author Armin Reichert
 */
public class HelpMenus {

	private static class Menu {
		private final List<Node> column0 = new ArrayList<>();
		private final List<Node> column1 = new ArrayList<>();

		public void addRow(Node node0, Node node1) {
			column0.add(node0);
			column1.add(node1);
		}

		public int size() {
			return column0.size();
		}
	}

	private final ResourceBundle translations;
	private Font font;

	public HelpMenus(ResourceBundle translations) {
		this.translations = translations;
		this.font = Font.font("Sans", 12);
	}

	public Pane menuIntro(GameController gameController) {
		var menu = new Menu();
		if (gameController.game().credit() > 0) {
			addEntry(menu, "help.start_game", "1");
		}
		addEntry(menu, "help.add_credit", "5");
		addEntry(menu, gameController.game().variant() == GameVariant.MS_PACMAN ? "help.pacman" : "help.ms_pacman", "V");
		return createPane(gameController, menu);
	}

	public Pane menuCredit(GameController gameController) {
		var menu = new Menu();
		if (gameController.game().credit() > 0) {
			addEntry(menu, "help.start_game", "1");
		}
		addEntry(menu, "help.add_credit", "5");
		addEntry(menu, "help.show_intro", "Q");
		return createPane(gameController, menu);
	}

	public Pane menuPlaying(GameController gameController) {
		var menu = new Menu();
		addEntry(menu, "help.move_left", tt("help.cursor_left"));
		addEntry(menu, "help.move_right", tt("help.cursor_right"));
		addEntry(menu, "help.move_up", tt("help.cursor_up"));
		addEntry(menu, "help.move_down", tt("help.cursor_down"));
		addEntry(menu, "help.show_intro", "Q");
		return createPane(gameController, menu);
	}

	public Pane menuDemoLevel(GameController gameController) {
		var menu = new Menu();
		addEntry(menu, "help.add_credit", "5");
		addEntry(menu, "help.show_intro", "Q");
		return createPane(gameController, menu);
	}

	private Pane createPane(GameController gameController, Menu menu) {
		var grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		for (int row = 0; row < menu.column0.size(); ++row) {
			grid.add(menu.column0.get(row), 0, row);
			grid.add(menu.column1.get(row), 1, row);
		}
		int rowIndex = menu.size();
		if (gameController.isAutoControlled()) {
			var text = text(tt("help.autopilot_on"), Color.ORANGE);
			GridPane.setColumnSpan(text, 2);
			grid.add(text, 0, rowIndex++);
		}
		if (gameController.game().isImmune()) {
			var text = text(tt("help.immunity_on"), Color.ORANGE);
			GridPane.setColumnSpan(text, 2);
			grid.add(text, 0, rowIndex++);
		}

		var pane = new BorderPane(grid);
		pane.setPadding(new Insets(10));
		switch (gameController.game().variant()) {
		case MS_PACMAN:
			pane.setBackground(ResourceManager.coloredBackground(Color.rgb(255, 0, 0, 0.9)));
			break;
		case PACMAN:
			pane.setBackground(ResourceManager.coloredBackground(Color.rgb(33, 33, 255, 0.9)));
			break;
		default:
			throw new IllegalGameVariantException(gameController.game().variant());
		}
		return pane;
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

	private void addEntry(Menu menu, String rbKey, String kbKey) {
		menu.addRow(label(tt(rbKey)), text("[" + kbKey + "]"));
	}
}