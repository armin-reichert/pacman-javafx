package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
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

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

public class PagePopupMenu {

	private final GameSceneContext sceneContext;
	private final List<Node> column0 = new ArrayList<>();
	private final List<Node> column1 = new ArrayList<>();
	private final Font font;

	public PagePopupMenu(GameSceneContext sceneContext, Font font) {
		checkNotNull(sceneContext);
		checkNotNull(font);
		this.sceneContext = sceneContext;
		this.font = font;
	}

	public void addRow(Node node0, Node node1) {
		column0.add(node0);
		column1.add(node1);
	}

	public int size() {
		return column0.size();
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

	public void addEntry(String messageKey, String keyboardKey) {
		addRow(label(sceneContext.tt(messageKey)), text("[" + keyboardKey + "]", Color.YELLOW));
	}

	public void addDefaultEntries(GridPane grid) {
		int rowIndex = size();
		if (sceneContext.gameController().isAutoControlled()) {
			var text = text(sceneContext.tt("help.autopilot_on"), Color.ORANGE);
			GridPane.setColumnSpan(text, 2);
			grid.add(text, 0, rowIndex);
			++rowIndex;
		}
		if (sceneContext.gameController().isImmune()) {
			var text = text(sceneContext.tt("help.immunity_on"), Color.ORANGE);
			GridPane.setColumnSpan(text, 2);
			grid.add(text, 0, rowIndex);
			++rowIndex;
		}

	}

	public Pane createPane(Color backgroundColor) {
		var grid = new GridPane();
		grid.setHgap(20);
		grid.setVgap(10);
		for (int row = 0; row < column0.size(); ++row) {
			grid.add(column0.get(row), 0, row);
			grid.add(column1.get(row), 1, row);
		}
		addDefaultEntries(grid);
		var pane = new BorderPane(grid);
		pane.setPadding(new Insets(10));
		pane.setBackground(ResourceManager.coloredRoundedBackground(backgroundColor, 10));
		return pane;
	}
}