/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.InfoBox;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_PIP_ON;

/**
 * @author Armin Reichert
 */
public class DashboardLayer extends BorderPane {

    private record DashboardEntry(String title, InfoBox infoBox) {}

    private final GameContext context;
    private final List<DashboardEntry> dashboardItems = new ArrayList<>();
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

    public PictureInPictureView getPip() {
        return pip;
    }

    private DashboardEntry createEntry(String title, InfoBox infoBox) {
        infoBox.setText(title);
        infoBox.setMinLabelWidth(context.assets().get("infobox.min_label_width"));
        infoBox.setTextColor(context.assets().get("infobox.text_color"));
        infoBox.setTextFont(context.assets().get("infobox.text_font"));
        infoBox.setLabelFont(context.assets().get("infobox.label_font"));
        infoBox.init(context);
        return new DashboardEntry(title, infoBox);
    }

    public void addDashboardItem(String title, InfoBox infoBox) {
        dashboardItems.add(createEntry(title, infoBox));
    }

    private void updateDashboardContainer() {
        dashboardContainer.getChildren().setAll(dashboardItems.stream().map(DashboardEntry::infoBox).toArray(InfoBox[]::new));
    }

    public void addEntry(int index, String title, InfoBox infoBox) {
        dashboardItems.add(index, createEntry(title, infoBox));
    }

    public void hideDashboard() {
        dashboardContainer.setVisible(false);
    }

    public void showDashboard() {
        updateDashboardContainer();
        dashboardContainer.setVisible(true);
        dashboardContainer.getChildren().stream()
                .filter(infoBox -> infoBox instanceof InfoBoxCustomMaps)
                .findFirst()
                .ifPresent(infoBox -> {
                    if (context.gameVariant() != GameVariant.PACMAN_XXL) {
                        dashboardContainer.getChildren().remove(infoBox);
                    }
                });
    }

    public void toggleDashboardVisibility() {
        if (dashboardContainer.isVisible()) {
            hideDashboard();
        } else {
            showDashboard();
        }
    }

    public Stream<InfoBox> getInfoBoxes() {
        return dashboardItems.stream().map(DashboardEntry::infoBox);
    }

    public void update() {
        if (dashboardContainer.isVisible()) {
            dashboardItems.stream().map(DashboardEntry::infoBox).forEach(InfoBox::update);
        }
        if (pip.isVisible()) {
            pip.draw();
        }
    }
}