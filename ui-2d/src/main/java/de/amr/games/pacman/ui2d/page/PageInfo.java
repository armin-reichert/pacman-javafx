package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PageInfo {

    private final List<Label> column0 = new ArrayList<>();
    private final List<Text> column1 = new ArrayList<>();

    public void addRow(Label label, Text text) {
        column0.add(label);
        column1.add(text);
    }

    public Label label(String s, Color color) {
        var label = new Label(s);
        label.setTextFill(color);
        return label;
    }

    public Text text(String s, Color color) {
        var text = new Text(s);
        text.setFill(color);
        return text;
    }

    public Pane createPane(Color backgroundColor, Font font) {
        var grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        for (int row = 0; row < column0.size(); ++row) {
            grid.add(column0.get(row), 0, row);
            grid.add(column1.get(row), 1, row);
        }
        for (int row = 0; row < column0.size(); ++row) {
            column0.get(row).setFont(font);
            column1.get(row).setFont(font);
        }
        var pane = new BorderPane(grid);
        pane.setPadding(new Insets(10));
        pane.setBackground(Ufx.coloredRoundedBackground(backgroundColor, 10));
        return pane;
    }
}