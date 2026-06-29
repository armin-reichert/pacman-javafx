/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import de.amr.basics.Identifier;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import static java.util.Objects.requireNonNull;

public class DashboardSection extends TitledPane {

    protected final Identifier id;
    protected final GridPane grid = new GridPane();
    protected int rowIndex;
    protected boolean displayedStandalone;

    public DashboardSection(Identifier id) {
        this.id = requireNonNull(id);

        getStyleClass().add("dashboard-section");
        grid.getStyleClass().add("dashboard-section-grid");

        setContent(grid);
        setExpanded(false);
        setFocusTraversable(false);
        setDisplayedStandalone(false);
    }

    public Identifier id() {
        return id;
    }

    public boolean isDisplayedStandalone() {
        return displayedStandalone;
    }

    public void setDisplayedStandalone(boolean alone) {
        displayedStandalone = alone;
    }

    public  void clearSection() {
        grid.getChildren().clear();
        rowIndex = 0;
    }

    public  void addRow(Node content) {
        grid.add(content, 0, rowIndex, 2, 1);
        ++rowIndex;
    }

    public  void addRow(Node left, Node right) {
        if (left != null) {
            grid.add(left, 0, rowIndex);
        }
        if (right != null) {
            grid.add(right, 1, rowIndex);
        }
        if (left != null || right != null) {
            ++rowIndex;
        }
    }
}