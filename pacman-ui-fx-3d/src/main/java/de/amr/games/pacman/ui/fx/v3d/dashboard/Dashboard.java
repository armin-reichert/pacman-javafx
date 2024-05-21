/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.ui.fx.GameSceneContext;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Armin Reichert
 */
public class Dashboard extends VBox {

    private final List<InfoBox> infoBoxes = new ArrayList<>();

    public Dashboard(GameSceneContext sceneContext) {
        infoBoxes.add(new InfoBoxGeneral(sceneContext.theme(), sceneContext.tt("infobox.general.title")));
        infoBoxes.add(new InfoBoxGameControl(sceneContext.theme(), sceneContext.tt("infobox.game_control.title")));
        infoBoxes.add(new InfoBox3D(sceneContext.theme(), sceneContext.tt("infobox.3D_settings.title")));
        infoBoxes.add(new InfoBoxGameInfo(sceneContext.theme(), sceneContext.tt("infobox.game_info.title")));
        infoBoxes.add(new InfoBoxActorInfo(sceneContext.theme(), sceneContext.tt("infobox.actor_info.title")));
        infoBoxes.add(new InfoBoxKeys(sceneContext.theme(), sceneContext.tt("infobox.keyboard_shortcuts.title")));
        infoBoxes.add(new InfoBoxAbout(sceneContext.theme(), sceneContext.tt("infobox.about.title")));
        infoBoxes.forEach(infoBox -> {
            getChildren().add(infoBox.getRoot());
            infoBox.init(sceneContext);
        });
        setVisible(false);
    }

    public void update() {
        infoBoxes.forEach(InfoBox::update);
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }
}
