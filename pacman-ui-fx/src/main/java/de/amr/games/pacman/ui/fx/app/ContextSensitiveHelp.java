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
import de.amr.games.pacman.model.GameVariant;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author Armin Reichert
 */
public class ContextSensitiveHelp {

	record Row(Node leftColumn, Node rightColumn) {
	}

	private final GameController gameController;

	public ContextSensitiveHelp(GameController gameController) {
		this.gameController = gameController;
	}

	public Optional<Pane> panel() {
		var game = gameController.game();
		var panel = switch (gameController.state()) {
		case BOOT -> null;
		case CREDIT -> helpCredit();
		case INTRO -> helpIntro();
		case HUNTING -> game.isPlaying() ? helpPlaying() : helpDemoLevel();
		default -> helpGeneral();
		};
		return Optional.ofNullable(panel);
	}

	private Pane helpIntro() {
		var game = gameController.game();
		var variant = game.variant();
		var other = variant == GameVariant.MS_PACMAN ? "Pac-Man" : "Ms. Pac-Man";
		List<Row> table = new ArrayList<>();
		if (game.credit() > 0) {
			table.add(new Row(text("PLAY"), text("1")));
		}
		table.add(new Row(text("ADD CREDIT"), text("5")));
		table.add(new Row(text(other), text("V")));
		return grid(table);
	}

	private Pane helpCredit() {
		var game = gameController.game();
		List<Row> table = new ArrayList<>();
		if (game.credit() > 0) {
			table.add(new Row(text("PLAY"), text("1")));
		}
		table.add(new Row(text("ADD CREDIT"), text("5")));
		table.add(new Row(text("QUIT"), text("Q")));
		return grid(table);
	}

	private Pane helpPlaying() {
		List<Row> table = new ArrayList<>();
		table.add(new Row(text("QUIT"), text("Q")));
		table.add(new Row(text("LEFT"), text("CURSOR LEFT")));
		table.add(new Row(text("RIGHT"), text("CURSOR RIGHT")));
		table.add(new Row(text("UP"), text("CURSOR UP")));
		table.add(new Row(text("DOWN"), text("CURSOR DOWN")));
		return grid(table);
	}

	private Pane helpDemoLevel() {
		List<Row> table = new ArrayList<>();
		table.add(new Row(text("QUIT"), text("Q")));
		table.add(new Row(text("ADD CREDIT"), text("5")));
		return grid(table);
	}

	private Pane helpGeneral() {
		List<Row> table = new ArrayList<>();
		table.add(new Row(text("ALWAYS AT YOU SIDE!"), null));
		return grid(table);
	}

	private GridPane grid(List<Row> table) {
		var grid = new GridPane();
		grid.setHgap(40);
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
		return grid;
	}

	private Text text(String s) {
		var text = new Text(s);
		text.setFill(Color.YELLOW);
		text.setFont(Game2d.resources.font(Game2d.resources.arcadeFont, 16));
		return text;
	}
}