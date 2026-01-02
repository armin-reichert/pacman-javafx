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

import java.util.ArrayList;
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
        visibleProperty().addListener((_, _, visible) -> {
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

    public void addCommonInfoBox(DashboardID id) {
        requireNonNull(id);
        switch (id) {
            case CommonDashboardID.ABOUT          -> addInfoBox(id, ui.translated("infobox.about.title"), new InfoBoxAbout(ui));
            case CommonDashboardID.ACTOR_INFO     -> addInfoBox(id, ui.translated("infobox.actor_info.title"), new InfoBoxActorInfo(ui), true);
            case CommonDashboardID.ANIMATION_INFO -> addInfoBox(id, ui.translated("infobox.animation_info.title"), new InfoBoxGameLevelAnimations(ui), true);
            case CommonDashboardID.CUSTOM_MAPS    -> addInfoBox(id, ui.translated("infobox.custom_maps.title"), new InfoBoxCustomMaps(ui), true);
            case CommonDashboardID.GENERAL        -> addInfoBox(id, ui.translated("infobox.general.title"), new InfoBoxGeneral(ui));
            case CommonDashboardID.GAME_CONTROL   -> addInfoBox(id, ui.translated("infobox.game_control.title"), new InfoBoxGameControl(ui));
            case CommonDashboardID.GAME_INFO      -> addInfoBox(id, ui.translated("infobox.game_info.title"), new InfoBoxGameInfo(ui), true);
            case CommonDashboardID.KEYS_GLOBAL    -> addInfoBox(id, ui.translated("infobox.keyboard_shortcuts_global.title"), new InfoBoxKeyShortcutsGlobal(ui), true);
            case CommonDashboardID.KEYS_LOCAL     -> addInfoBox(id, ui.translated("infobox.keyboard_shortcuts_local.title"), new InfoBoxKeyShortcutsLocal(ui));
            case CommonDashboardID.README         -> addInfoBox(id, ui.translated("infobox.readme.title"), new InfoBoxReadmeFirst(ui));
            case CommonDashboardID.SETTINGS_3D    -> addInfoBox(id, ui.translated("infobox.3D_settings.title"), new InfoBox3DSettings(ui));
            default -> {}
        }
        infoBoxMap.get(CommonDashboardID.README).setExpanded(true);
    }

    public void addInfoBox(DashboardID id, String title, InfoBox infoBox, boolean maximized) {
        infoBoxMap.put(id, preconfiguredInfoBox(title, infoBox));
        infoBox.setDashboard(this);
        infoBox.setDisplayedMaximized(maximized);
        infoBox.setMinWidth(INFOBOX_MIN_WIDTH);
    }

    public void addInfoBox(DashboardID id, String title, InfoBox infoBox) {
        addInfoBox(id, title, infoBox, false);
    }

    public void configure(List<DashboardID> dashboardIDS) {
        addCommonInfoBox(CommonDashboardID.README);
        for (DashboardID id : dashboardIDS) {
            if (id == CommonDashboardID.README) continue;
            addCommonInfoBox(id);
        }
    }

    public void updateContent() {
        infoBoxes().filter(InfoBox::isExpanded).forEach(InfoBox::update);
    }

    public void updateLayout() {
        final List<InfoBox> infoBoxes = new ArrayList<>();
        infoBoxMap.entrySet().stream()
            .filter(e -> e.getKey() != CommonDashboardID.README)
            .filter(e -> e.getKey() != CommonDashboardID.ABOUT)
            .forEach(e -> infoBoxes.add(e.getValue()));
        if (infoBoxMap.containsKey(CommonDashboardID.README)) {
            infoBoxes.addFirst(infoBoxMap.get(CommonDashboardID.README));
        }
        if (infoBoxMap.containsKey(CommonDashboardID.ABOUT)) {
            infoBoxes.addLast(infoBoxMap.get(CommonDashboardID.ABOUT));
        }
        getChildren().setAll(infoBoxes.toArray(InfoBox[]::new));
    }

    public void showVisibleInfoBoxesOnly(boolean onlyVisible) {
        getChildren().clear();
        if (onlyVisible) {
            infoBoxes().filter(Node::isVisible).forEach(getChildren()::add);
        } else {
            infoBoxes().forEach(getChildren()::add);
        }
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