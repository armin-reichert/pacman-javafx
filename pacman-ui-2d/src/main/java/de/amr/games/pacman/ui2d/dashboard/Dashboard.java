/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Armin Reichert
 */
public class Dashboard extends VBox {

    private final GameContext context;
    private final List<InfoBox> infoBoxes = new ArrayList<>();

    public Dashboard(GameContext context) {
        this.context = context;
        setVisible(false);
    }

    public GameContext getContext() {
        return context;
    }

    public void addInfoBox(String title, InfoBox infoBox) {
        addInfoBox(infoBoxes.size(), title, infoBox);
    }

    public void addInfoBox(int index, String title, InfoBox infoBox) {
        infoBoxes.add(index, infoBox);
        getChildren().add(index, infoBox);
        infoBox.setText(title);
        infoBox.setMinLabelWidth(context.assets().get("infobox.min_label_width"));
        infoBox.setTextColor(context.assets().get("infobox.text_color"));
        infoBox.setTextFont(context.assets().get("infobox.text_font"));
        infoBox.setLabelFont(context.assets().get("infobox.label_font"));
        infoBox.init(context);
    }

    public void update() {
        infoBoxes.forEach(InfoBox::update);
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }
}