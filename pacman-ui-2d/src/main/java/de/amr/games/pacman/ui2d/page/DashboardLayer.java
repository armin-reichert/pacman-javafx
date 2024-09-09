/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.dashboard.*;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.ui2d.GameParameters.*;

/**
 * @author Armin Reichert
 */
public class DashboardLayer extends BorderPane {

    private final GameContext context;
    private final List<InfoBox> infoBoxes = new ArrayList<>();
    private final PictureInPictureView pip;
    private final VBox dashboard = new VBox();

    public DashboardLayer(GameContext context) {
        this.context = context;

        dashboard.setVisible(false);
        addInfoBox(context.locText("infobox.general.title"), new InfoBoxGeneral());
        addInfoBox(context.locText("infobox.game_control.title"), new InfoBoxGameControl());
        addInfoBox(context.locText("infobox.game_info.title"), new InfoBoxGameInfo());
        addInfoBox(context.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        addInfoBox(context.locText("infobox.actor_info.title"), new InfoBoxActorInfo());
        addInfoBox(context.locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        addInfoBox(context.locText("infobox.about.title"), new InfoBoxAbout());

        pip = new PictureInPictureView(context);
        pip.heightPy.bind(PY_PIP_HEIGHT);
        pip.opacityPy.bind(PY_PIP_OPACITY_PERCENT.divide(100.0));

        setLeft(dashboard);
        setRight(new VBox(pip.node(), new HBox()));

        visibleProperty().bind(Bindings.createObjectBinding(
            () -> dashboard.isVisible() || PY_PIP_ON.get(),
            dashboard.visibleProperty(), PY_PIP_ON
        ));
    }

    public PictureInPictureView getPip() {
        return pip;
    }

    public void addInfoBox(String title, InfoBox infoBox) {
        addInfoBox(infoBoxes.size(), title, infoBox);
    }

    public void addInfoBox(int index, String title, InfoBox infoBox) {
        infoBoxes.add(index, infoBox);
        infoBox.setText(title);
        infoBox.setMinLabelWidth(context.assets().get("infobox.min_label_width"));
        infoBox.setTextColor(context.assets().get("infobox.text_color"));
        infoBox.setTextFont(context.assets().get("infobox.text_font"));
        infoBox.setLabelFont(context.assets().get("infobox.label_font"));
        infoBox.init(context);
        dashboard.getChildren().add(index, infoBox);
    }

    public void toggleDashboardVisibility() {
        dashboard.setVisible(!isVisible());
    }

    public List<InfoBox> getInfoBoxes() {
        return infoBoxes;
    }

    public void update() {
        infoBoxes.forEach(InfoBox::update);
        pip.setVisible(PY_PIP_ON.get() && context.currentGameSceneIs(GameSceneID.PLAY_SCENE_3D));
        pip.draw();
    }
}