/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.math.Vector2i;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.translated;

public class SizeInputDialog extends Dialog<Vector2i> {

    public SizeInputDialog(
        int minX, int maxX, int initX,
        int minY, int maxY, int initY)
    {
        setTitle("Enter Size");

        var okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        Spinner<Integer> spinnerX = new Spinner<>(minX, maxX, initX);
        Spinner<Integer> spinnerY = new Spinner<>(minY, maxY, initY);

        spinnerX.setEditable(false);
        spinnerY.setEditable(false);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label(translated("num_columns")), 0, 0);
        grid.add(spinnerX, 1, 0);
        grid.add(new Label(translated("num_rows")), 0, 1);
        grid.add(spinnerY, 1, 1);

        getDialogPane().setContent(grid);

        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Vector2i(spinnerX.getValue(), spinnerY.getValue());
            }
            return null;
        });
    }
}
