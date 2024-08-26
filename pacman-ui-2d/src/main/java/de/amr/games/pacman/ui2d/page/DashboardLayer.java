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

import static de.amr.games.pacman.ui2d.GameParameters.*;

/**
 * @author Armin Reichert
 */
public class DashboardLayer extends BorderPane {

    private final GameContext context;
    private final Dashboard dashboard;
    private final PictureInPictureView pip;

    public DashboardLayer(GameContext context) {
        this.context = context;

        dashboard = new Dashboard(context);
        dashboard.addInfoBox(context.locText("infobox.general.title"), new InfoBoxGeneral());
        dashboard.addInfoBox(context.locText("infobox.game_control.title"), new InfoBoxGameControl());
        dashboard.addInfoBox(context.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        dashboard.addInfoBox(context.locText("infobox.game_info.title"), new InfoBoxGameInfo());
        dashboard.addInfoBox(context.locText("infobox.actor_info.title"), new InfoBoxActorInfo());
        dashboard.addInfoBox(context.locText("infobox.keyboard_shortcuts.title"), new InfoBoxKeys());
        dashboard.addInfoBox(context.locText("infobox.about.title"), new InfoBoxAbout());

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

    public Dashboard dashboard() {
        return dashboard;
    }

    public void update() {
        dashboard.update();
        pip.setVisible(PY_PIP_ON.get() && context.currentGameSceneIs(GameSceneID.PLAY_SCENE_3D));
        pip.draw();
    }
}