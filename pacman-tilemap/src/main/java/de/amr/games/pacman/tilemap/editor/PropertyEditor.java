package de.amr.games.pacman.tilemap.editor;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.Properties;

public class PropertyEditor extends BorderPane {

    private Properties editedProperties;
    private GridPane grid = new GridPane();
    private int nameColumnMinWidth = 100;
    private String title;

    public PropertyEditor(String title) {
        this.title = title;
        var lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Sans", FontWeight.BOLD, 14));
        setTop(lblTitle);
        setCenter(grid);
    }

    public Properties getEditedProperties() {
        return editedProperties;
    }

    public void setEditedProperties(Properties editedProperties) {
        this.editedProperties = editedProperties;
        grid.getChildren().clear();
        int row = 0;
        for (var entry : editedProperties.entrySet()) {
            Label lblPropertyName = new Label(String.valueOf(entry.getKey()));
            lblPropertyName.setPadding(new Insets(0,5,0,0));
            lblPropertyName.setMinWidth(nameColumnMinWidth);
            TextField editor = new TextField(String.valueOf(entry.getValue()));
            editor.setOnAction(e -> {
                editedProperties.put(entry.getKey(), editor.getText());
            });
            grid.add(lblPropertyName, 0, row);
            grid.add(editor, 1, row);
            ++row;
        }
    }

}
