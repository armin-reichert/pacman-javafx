/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameUI;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

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

    private final Map<DashboardID, DashboardSection> infoBoxMap = new LinkedHashMap<>();

    public Dashboard() {
        visibleProperty().addListener((_, _, visible) -> {
            if (visible) {
                updateLayout();
            }
        });
    }

    public void init(GameUI ui) {
        infoBoxes().forEach(infoBox -> infoBox.init(ui));
    }

    public void update(GameUI ui) {
        infoBoxes().filter(DashboardSection::isExpanded).forEach(section -> section.update(ui));
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }

    public Stream<DashboardSection> infoBoxes() { return infoBoxMap.values().stream(); }

    public void removeInfoBox(DashboardID id) {
        infoBoxMap.remove(id);
        updateLayout();
    }

    public void addCommonInfoBox(GameUI ui, DashboardID id) {
        requireNonNull(id);
        switch (id) {
            case CommonDashboardID.ABOUT          -> addInfoBox(id, ui.translated("infobox.about.title"), new DashboardSectionAbout(this));
            case CommonDashboardID.ACTOR_INFO     -> addInfoBox(id, ui.translated("infobox.actor_info.title"), new InfoBoxActorInfo(this), true);
            case CommonDashboardID.ANIMATION_INFO -> addInfoBox(id, ui.translated("infobox.animation_info.title"), new DashboardSectionGameLevelAnimations(this), true);
            case CommonDashboardID.CUSTOM_MAPS    -> addInfoBox(id, ui.translated("infobox.custom_maps.title"), new DashboardSectionCustomMaps(this), true);
            case CommonDashboardID.GENERAL        -> addInfoBox(id, ui.translated("infobox.general.title"), new DashboardSectionGeneral(this));
            case CommonDashboardID.GAME_CONTROL   -> addInfoBox(id, ui.translated("infobox.game_control.title"), new DashboardSectionGameControl(this));
            case CommonDashboardID.GAME_INFO      -> addInfoBox(id, ui.translated("infobox.game_info.title"), new InfoBoxGameInfo(this), true);
            case CommonDashboardID.KEYS_GLOBAL    -> addInfoBox(id, ui.translated("infobox.keyboard_shortcuts_global.title"), new DashboardSectionKeyShortcutsGlobal(this), true);
            case CommonDashboardID.KEYS_LOCAL     -> addInfoBox(id, ui.translated("infobox.keyboard_shortcuts_local.title"), new DashboardSectionKeyShortcutsLocal(this));
            case CommonDashboardID.README         -> addInfoBox(id, ui.translated("infobox.readme.title"), new DashboardSectionReadmeFirst(this));
            case CommonDashboardID.SETTINGS_3D    -> addInfoBox(id, ui.translated("infobox.3D_settings.title"), new DashboardSection3DSettings(this));
            default -> Logger.warn("Unknown dashboard ID {}", id);
        }
        infoBoxMap.get(CommonDashboardID.README).setExpanded(true);
    }

    public void addInfoBox(DashboardID id, String title, DashboardSection dashboardSection, boolean maximized) {
        infoBoxMap.put(id, preconfiguredInfoBox(title, dashboardSection));
        dashboardSection.setDisplayedMaximized(maximized);
        dashboardSection.setMinWidth(INFOBOX_MIN_WIDTH);
    }

    public void addInfoBox(DashboardID id, String title, DashboardSection dashboardSection) {
        addInfoBox(id, title, dashboardSection, false);
    }

    /**
     * Adds all info boxes defined by the given ID. The README info box is automatically inserted at the first position.
     *
     * @param ui the UI
     * @param ids the dashboard info box IDs
     */
    public void addInfoBoxes(GameUI ui, List<DashboardID> ids) {
        requireNonNull(ui);
        requireNonNull(ids);
        addCommonInfoBox(ui, CommonDashboardID.README);
        for (DashboardID id : ids) {
            if (id != CommonDashboardID.README) addCommonInfoBox(ui, id);
        }
    }

    private void updateLayout() {
        final List<DashboardSection> dashboardSections = new ArrayList<>();
        infoBoxMap.entrySet().stream()
            .filter(e -> e.getKey() != CommonDashboardID.README)
            .filter(e -> e.getKey() != CommonDashboardID.ABOUT)
            .forEach(e -> dashboardSections.add(e.getValue()));
        if (infoBoxMap.containsKey(CommonDashboardID.README)) {
            dashboardSections.addFirst(infoBoxMap.get(CommonDashboardID.README));
        }
        if (infoBoxMap.containsKey(CommonDashboardID.ABOUT)) {
            dashboardSections.addLast(infoBoxMap.get(CommonDashboardID.ABOUT));
        }
        getChildren().setAll(dashboardSections.toArray(DashboardSection[]::new));
    }

    public void showVisibleInfoBoxesOnly(boolean onlyVisible) {
        getChildren().clear();
        if (onlyVisible) {
            infoBoxes().filter(Node::isVisible).forEach(getChildren()::add);
        } else {
            infoBoxes().forEach(getChildren()::add);
        }
    }

    private DashboardSection preconfiguredInfoBox(String title, DashboardSection dashboardSection) {
        dashboardSection.setText(title);
        dashboardSection.setMinLabelWidth(INFOBOX_MIN_LABEL_WIDTH);
        dashboardSection.setContentBackground(Background.fill(INFO_BOX_CONTENT_BG_COLOR));
        dashboardSection.setTextColor(INFO_BOX_TEXT_COLOR);
        dashboardSection.setContentTextFont(INFO_BOX_FONT);
        dashboardSection.setLabelFont(INFO_BOX_FONT);
        return dashboardSection;
    }
}