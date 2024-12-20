/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.DashboardAssets;
import de.amr.games.pacman.ui2d.dashboard.InfoBox;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.ui2d.GlobalProperties2d.PY_PIP_ON;

/**
 * @author Armin Reichert
 */
public class DashboardLayer extends BorderPane {

    public record DashboardEntry(String title, InfoBox infoBox) {}

    private final GameContext context;
    private final List<DashboardEntry> dashboardEntries = new ArrayList<>();
    private final VBox dashboardContainer = new VBox();
    private final PictureInPictureView pip;

    public DashboardLayer(GameContext context) {
        this.context = context;

        pip = new PictureInPictureView(context);

        setLeft(dashboardContainer);
        setRight(new VBox(pip, new HBox()));

        visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboardContainer.isVisible() || PY_PIP_ON.get(),
            dashboardContainer.visibleProperty(), PY_PIP_ON
        ));
    }

    public List<DashboardEntry> dashboardEntries() {
        return dashboardEntries;
    }

    public PictureInPictureView pipView() {
        return pip;
    }

    private DashboardEntry createEntry(String title, InfoBox infoBox) {
        infoBox.setText(title);
        infoBox.setMinLabelWidth(DashboardAssets.IT.get("infobox.min_label_width"));
        infoBox.setTextColor(DashboardAssets.IT.get("infobox.text_color"));
        infoBox.setContentTextFont(DashboardAssets.IT.get("infobox.text_font"));
        infoBox.setLabelFont(DashboardAssets.IT.get("infobox.label_font"));
        infoBox.init(context);
        return new DashboardEntry(title, infoBox);
    }

    public void addDashboardItem(String title, InfoBox infoBox) {
        dashboardEntries.add(createEntry(title, infoBox));
    }

    public void insertDashboardItem(int index, String title, InfoBox infoBox) {
        dashboardEntries.add(index, createEntry(title, infoBox));
    }

    private void updateDashboardContainer() {
        dashboardContainer.getChildren().setAll(dashboardEntries.stream().map(DashboardEntry::infoBox).toArray(InfoBox[]::new));
    }

    public boolean isDashboardOpen() { return dashboardContainer.isVisible(); }

    public void hideDashboard() {
        dashboardContainer.setVisible(false);
    }

    public void showDashboard() {
        updateDashboardContainer();
        dashboardContainer.setVisible(true);
    }

    public void toggleDashboardVisibility() {
        if (dashboardContainer.isVisible()) {
            hideDashboard();
        } else {
            showDashboard();
        }
    }

    public void update() {
        if (dashboardContainer.isVisible()) {
            dashboardEntries.stream().map(DashboardEntry::infoBox).filter(InfoBox::isExpanded).forEach(InfoBox::update);
        }
        if (pip.isVisible()) {
            pip.draw();
        }
    }
}