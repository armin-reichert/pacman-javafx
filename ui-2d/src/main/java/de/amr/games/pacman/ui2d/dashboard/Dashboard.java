/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Armin Reichert
 */
public class Dashboard extends VBox {

    private final GameSceneContext context;
    private final List<InfoBox> infoBoxes = new ArrayList<>();

    public Dashboard(GameSceneContext context) {
        this.context = context;
        setVisible(false);
    }

    public void addInfoBox(InfoBox infoBox) {
        infoBoxes.add(infoBox);
        getChildren().add(infoBox);
        infoBox.init(context);
    }

    public void addInfoBox(int index, InfoBox infoBox) {
        infoBoxes.add(infoBox);
        getChildren().add(index, infoBox);
        infoBox.init(context);
    }

    public List<InfoBox> getInfoBoxes() {
        return infoBoxes;
    }

    public void update() {
        infoBoxes.forEach(InfoBox::update);
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }
}
