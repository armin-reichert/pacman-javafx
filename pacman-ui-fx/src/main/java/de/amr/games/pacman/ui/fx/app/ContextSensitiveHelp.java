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
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * @author Armin Reichert
 */
public class ContextSensitiveHelp {

	record Row(Node leftColumn, Node rightColumn) {
	}

	private class HelpPanel {

		private final List<Row> table = new ArrayList<>();

		public void addRow(String left, String right) {
			table.add(new Row(text(left), text(right)));
		}

		public Pane makePanel() {
			var grid = new GridPane();
			grid.setHgap(20);
			grid.setVgap(10);
			for (int rowIndex = 0; rowIndex < table.size(); ++rowIndex) {
				var row = table.get(rowIndex);
				if (row.rightColumn() != null) {
					grid.add(row.leftColumn(), 0, rowIndex);
					grid.add(row.rightColumn(), 1, rowIndex);
				} else {
					grid.add(row.leftColumn, 0, rowIndex);
					GridPane.setColumnSpan(row.leftColumn, 2);
				}
			}
			int rowIndex = table.size();
			if (gameController.isAutoControlled()) {
				var text = text("AUTOPILOT ON");
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rowIndex);
				++rowIndex;
			}
			if (gameController.game().isImmune()) {
				var text = text("IMMUNITY ON");
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rowIndex);
				++rowIndex;
			}

			var pane = new BorderPane(grid);
			pane.setMaxSize(100, 50);
			pane.setPadding(new Insets(10));
			pane.setBackground(ResourceManager.colorBackground(Color.rgb(200, 200, 200, 0.35)));
			return pane;
		}
	}

	private final GameController gameController;
	private Font font = Font.font("Sans", FontWeight.EXTRA_BOLD, 20);

	public ContextSensitiveHelp(GameController gameController) {
		this.gameController = gameController;
	}

	private GameModel game() {
		return gameController.game();
	}

	private GameVariant variant() {
		return game().variant();
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public Optional<Pane> currentPanel() {
		boolean attractMode = game().level().isPresent() && game().level().get().isDemoLevel();
		var panel = switch (gameController.state()) {
		case BOOT -> null;
		case CREDIT -> helpCredit();
		case INTRO -> helpIntro();
		case READY, HUNTING, PACMAN_DYING, GHOST_DYING -> attractMode ? helpDemoLevel() : helpPlaying();
		default -> null;
		};
		return Optional.ofNullable(panel);
	}

	private Text text(String s) {
		var text = new Text(s);
		text.setFill(Color.YELLOW);
		text.setFont(font);
		return text;
	}

	private Pane helpIntro() {
		var other = variant() == GameVariant.MS_PACMAN ? "PLAY PAC-MAN" : "PLAY MS. PAC-MAN";
		var helpPanel = new HelpPanel();
		if (game().credit() > 0) {
			helpPanel.addRow("START GAME", "1");
		}
		helpPanel.addRow("ADD CREDIT", "5");
		helpPanel.addRow(other, "V");
		return helpPanel.makePanel();
	}

	private Pane helpCredit() {
		var helpPanel = new HelpPanel();
		helpPanel.addRow("ADD CREDIT", "5");
		if (game().credit() > 0) {
			helpPanel.addRow("START GAME", "1");
		}
		helpPanel.addRow("QUIT", "Q");
		return helpPanel.makePanel();
	}

	private Pane helpPlaying() {
		var helpPanel = new HelpPanel();
		helpPanel.addRow("LEFT", "CURSOR LEFT");
		helpPanel.addRow("RIGHT", "CURSOR RIGHT");
		helpPanel.addRow("UP", "CURSOR UP");
		helpPanel.addRow("DOWN", "CURSOR DOWN");
		helpPanel.addRow("QUIT", "Q");
		return helpPanel.makePanel();
	}

	private Pane helpDemoLevel() {
		var helpPanel = new HelpPanel();
		helpPanel.addRow("ADD CREDIT", "5");
		helpPanel.addRow("QUIT", "Q");
		return helpPanel.makePanel();
	}
}