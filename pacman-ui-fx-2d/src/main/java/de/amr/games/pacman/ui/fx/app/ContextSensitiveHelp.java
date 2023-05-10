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
public class ContextSensitiveHelp {

	private final GameController gameController;
	private Font font = Font.font("Helvetica", 8);

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

	private class Help {

		private final List<List<Node>> rows = new ArrayList<>();

		public void addRow(Node left, String key) {
			rows.add(List.of(left, key(key)));
		}

		public Pane createPane() {
			var grid = new GridPane();
			grid.setHgap(20);
			grid.setVgap(10);
			for (int rowIndex = 0; rowIndex < rows.size(); ++rowIndex) {
				var row = rows.get(rowIndex);
				if (row.get(1) != null) {
					grid.add(row.get(0), 0, rowIndex);
					grid.add(row.get(1), 1, rowIndex);
				} else {
					grid.add(row.get(0), 0, rowIndex);
					GridPane.setColumnSpan(row.get(0), 2);
				}
			}
			if (gameController.isAutoControlled()) {
				var text = text("AUTOPILOT ON", Color.ORANGE);
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rows.size());
			}
			if (gameController.game().isImmune()) {
				var text = text("IMMUNITY ON", Color.ORANGE);
				GridPane.setColumnSpan(text, 2);
				grid.add(text, 0, rows.size() + 1);
			}

			var pane = new BorderPane(grid);
			pane.setMaxSize(200, 50);
			pane.setPadding(new Insets(10));
			pane.setBackground(ResourceManager.colorBackground(Color.rgb(200, 200, 200, 0.66)));
			return pane;
		}
	}

	public ContextSensitiveHelp(GameController gameController) {
		this.gameController = gameController;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	private GameModel game() {
		return gameController.game();
	}

	private GameVariant variant() {
		return game().variant();
	}

	private boolean attractMode() {
		return game().level().isPresent() && game().level().get().isDemoLevel();
	}

	public Optional<Pane> current() {
		var pane = switch (gameController.state()) {
		case CREDIT -> helpCredit();
		case INTRO -> helpIntro();
		case READY, HUNTING, PACMAN_DYING, GHOST_DYING -> attractMode() ? helpDemoLevel() : helpPlaying();
		default -> null;
		};
		return Optional.ofNullable(pane);
	}

	private Pane helpIntro() {
		var help = new Help();
		if (game().credit() > 0) {
			help.addRow(label("START GAME"), "1");
		}
		help.addRow(label("ADD CREDIT"), "5");
		help.addRow(variant() == GameVariant.MS_PACMAN ? label("PAC-MAN") : label("MS.PAC-MAN"), "V");
		return help.createPane();
	}

	private Pane helpCredit() {
		var help = new Help();
		if (game().credit() > 0) {
			help.addRow(label("START GAME"), "1");
		}
		help.addRow(label("ADD CREDIT"), "5");
		help.addRow(label("QUIT"), "Q");
		return help.createPane();
	}

	private Pane helpPlaying() {
		var help = new Help();
		help.addRow(label("LEFT"), "CURSOR LEFT");
		help.addRow(label("RIGHT"), "CURSOR RIGHT");
		help.addRow(label("UP"), "CURSOR UP");
		help.addRow(label("DOWN"), "CURSOR DOWN");
		help.addRow(label("QUIT"), "Q");
		return help.createPane();
	}

	private Pane helpDemoLevel() {
		var help = new Help();
		help.addRow(label("ADD CREDIT"), "5");
		help.addRow(label("QUIT"), "Q");
		return help.createPane();
	}
}