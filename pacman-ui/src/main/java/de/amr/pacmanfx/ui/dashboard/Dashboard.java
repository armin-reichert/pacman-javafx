/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Dashboard extends VBox {
    public static final int INFOBOX_MIN_LABEL_WIDTH = 110;
    public static final int INFOBOX_MIN_WIDTH = 350;
    public static final Color INFO_BOX_CONTENT_BG_COLOR = Color.rgb(0, 0, 50, 1.0);
    public static final Color INFO_BOX_TEXT_COLOR = Color.WHITE;
    public static final Font INFO_BOX_FONT = Font.font("Sans", 12);

    private GameUI ui;
    private final Map<DashboardID, InfoBox> infoBoxMap = new LinkedHashMap<>();

    public Dashboard() {
        visibleProperty().addListener((py, ov, visible) -> {
            if (visible) {
                updateLayout();
            }
        });
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    public void init(GameUI ui) {
        infoBoxes().forEach(infoBox -> infoBox.init(ui));
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }

    public Stream<InfoBox> infoBoxes() { return infoBoxMap.values().stream(); }

    public void removeInfoBox(DashboardID id) {
        infoBoxMap.remove(id);
        updateLayout();
    }

    public void addInfoBox(DashboardID id) {
        switch (id) {
            case ABOUT          -> addInfoBox(id, "infobox.about.title", new InfoBoxAbout(ui));
            case ACTOR_INFO     -> addInfoBox(id, "infobox.actor_info.title", new InfoBoxActorInfo(ui), true);
            case ANIMATION_INFO -> addInfoBox(id, "infobox.animation_info.title", new InfoBoxGameLevelAnimations(ui), true);
            case CUSTOM_MAPS    -> addInfoBox(id, "infobox.custom_maps.title", new InfoBoxCustomMaps(ui), true);
            case GENERAL        -> addInfoBox(id, "infobox.general.title", new InfoBoxGeneral(ui));
            case GAME_CONTROL   -> addInfoBox(id, "infobox.game_control.title", new InfoBoxGameControl(ui));
            case GAME_INFO      -> addInfoBox(id, "infobox.game_info.title", new InfoBoxGameInfo(ui), true);
            case JOYPAD         -> addInfoBox(id, "infobox.joypad.title", new InfoBoxJoypad(ui));
            case KEYS_GLOBAL    -> addInfoBox(id, "infobox.keyboard_shortcuts_global.title", new InfoBoxKeyShortcutsGlobal(ui), true);
            case KEYS_LOCAL     -> addInfoBox(id, "infobox.keyboard_shortcuts_local.title", new InfoBoxKeyShortcutsLocal(ui));
            case README         -> addInfoBox(id, "infobox.readme.title", new InfoBoxReadmeFirst(ui));
            case SETTINGS_3D    -> addInfoBox(id, "infobox.3D_settings.title", new InfoBox3DSettings(ui));
        }
        infoBoxMap.get(DashboardID.README).setExpanded(true);
    }

    public void configure(List<DashboardID> dashboardIDS) {
        addInfoBox(DashboardID.README);
        for (DashboardID id : dashboardIDS) {
            if (id == DashboardID.README) continue;
            addInfoBox(id);
        }
    }

    public void updateContent() {
        infoBoxes().filter(InfoBox::isExpanded).forEach(InfoBox::update);
    }

    public void updateLayout() {
        getChildren().setAll(infoBoxes().toArray(InfoBox[]::new));
    }

    public void showVisibleInfoBoxesOnly(boolean onlyVisible) {
        getChildren().clear();
        if (onlyVisible) {
            infoBoxes().filter(Node::isVisible).forEach(getChildren()::add);
        } else {
            infoBoxes().forEach(getChildren()::add);
        }
    }

    private void addInfoBox(DashboardID id, String titleKey, InfoBox infoBox, boolean maximized) {
        infoBoxMap.put(id, preconfiguredInfoBox(ui.globalAssets().translated(titleKey), infoBox));
        infoBox.setDashboard(this);
        infoBox.setDisplayedMaximized(maximized);
        infoBox.setMinWidth(INFOBOX_MIN_WIDTH);
    }

    private void addInfoBox(DashboardID id, String titleKey, InfoBox infoBox) {
        addInfoBox(id, titleKey, infoBox, false);
    }

    private InfoBox preconfiguredInfoBox(String title, InfoBox infoBox) {
        infoBox.setText(title);
        infoBox.setMinLabelWidth(INFOBOX_MIN_LABEL_WIDTH);
        infoBox.setContentBackground(Background.fill(INFO_BOX_CONTENT_BG_COLOR));
        infoBox.setTextColor(INFO_BOX_TEXT_COLOR);
        infoBox.setContentTextFont(INFO_BOX_FONT);
        infoBox.setLabelFont(INFO_BOX_FONT);
        return infoBox;
    }
}