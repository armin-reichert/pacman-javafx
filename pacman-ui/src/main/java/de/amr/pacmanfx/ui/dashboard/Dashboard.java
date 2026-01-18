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

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Dashboard extends VBox {

    public static final int SECTION_MIN_LABEL_WIDTH = 110;
    public static final int SECTION_MIN_WIDTH = 350;

    public static final Color SECTION_CONTENT_BG_COLOR = Color.rgb(0, 0, 50, 1.0);
    public static final Color SECTION_TEXT_COLOR = Color.WHITE;
    public static final Font  SECTION_FONT = Font.font("Sans", 12);

    private final Map<DashboardID, DashboardSection> sectionsByID = new LinkedHashMap<>();

    public Dashboard() {
        visibleProperty().addListener((_, _, visible) -> {
            if (visible) {
                updateLayout();
            }
        });
    }

    public void init(GameUI ui) {
        sections().forEach(infoBox -> infoBox.init(ui));
    }

    public void update(GameUI ui) {
        sections().filter(DashboardSection::isExpanded).forEach(section -> section.update(ui));
    }

    public void toggleVisibility() {
        setVisible(!isVisible());
    }

    public Stream<DashboardSection> sections() { return sectionsByID.values().stream(); }

    public void removeSection(DashboardID id) {
        sectionsByID.remove(id);
        updateLayout();
    }

    public void addCommonSection(GameUI ui, DashboardID id) {
        requireNonNull(id);
        switch (id) {
            case CommonDashboardID.ABOUT          -> addSection(id, ui.translate("infobox.about.title"), new DashboardSectionAbout(this));
            case CommonDashboardID.ACTOR_INFO     -> addSection(id, ui.translate("infobox.actor_info.title"), new InfoBoxActorInfo(this), true);
            case CommonDashboardID.ANIMATION_INFO -> addSection(id, ui.translate("infobox.animation_info.title"), new DashboardSectionGameLevelAnimations(this), true);
            // this dashboard section needs additional configuration to work!
            case CommonDashboardID.CUSTOM_MAPS    -> addSection(id, ui.translate("infobox.custom_maps.title"), new DashboardSectionCustomMaps(this), true);
            case CommonDashboardID.GENERAL        -> addSection(id, ui.translate("infobox.general.title"), new DashboardSectionGeneral(this));
            case CommonDashboardID.GAME_CONTROL   -> addSection(id, ui.translate("infobox.game_control.title"), new DashboardSectionGameControl(this));
            case CommonDashboardID.GAME_INFO      -> addSection(id, ui.translate("infobox.game_info.title"), new InfoBoxGameInfo(this), true);
            case CommonDashboardID.KEYS_GLOBAL    -> addSection(id, ui.translate("infobox.keyboard_shortcuts_global.title"), new DashboardSectionKeyShortcutsGlobal(this), true);
            case CommonDashboardID.KEYS_LOCAL     -> addSection(id, ui.translate("infobox.keyboard_shortcuts_local.title"), new DashboardSectionKeyShortcutsLocal(this));
            case CommonDashboardID.README         -> addSection(id, ui.translate("infobox.readme.title"), new DashboardSectionReadmeFirst(this));
            case CommonDashboardID.SETTINGS_3D    -> addSection(id, ui.translate("infobox.3D_settings.title"), new DashboardSection3DSettings(this));
            default -> Logger.warn("Unknown dashboard ID {}", id);
        }
        sectionsByID.get(CommonDashboardID.README).setExpanded(true);
    }

    public Optional<DashboardSection> findSection(DashboardID id) {
        requireNonNull(id);
        return Optional.ofNullable(sectionsByID.get(id));
    }

    public void addSection(DashboardID id, String title, DashboardSection section, boolean maximized) {
        sectionsByID.put(id, preconfiguredSection(title, section));
        section.setDisplayedMaximized(maximized);
        section.setMinWidth(SECTION_MIN_WIDTH);
    }

    public void addSection(DashboardID id, String title, DashboardSection section) {
        addSection(id, title, section, false);
    }

    /**
     * Adds all info boxes defined by the given ID. The README info box is automatically inserted at the first position.
     *
     * @param ui the UI
     * @param ids the dashboard info box IDs
     */
    public void addSections(GameUI ui, List<DashboardID> ids) {
        requireNonNull(ui);
        requireNonNull(ids);
        addCommonSection(ui, CommonDashboardID.README);
        for (DashboardID id : ids) {
            if (id != CommonDashboardID.README) addCommonSection(ui, id);
        }
    }

    private void updateLayout() {
        final List<DashboardSection> sectionList = new ArrayList<>();
        sectionsByID.entrySet().stream()
            .filter(e -> e.getKey() != CommonDashboardID.README)
            .filter(e -> e.getKey() != CommonDashboardID.ABOUT)
            .forEach(e -> sectionList.add(e.getValue()));
        if (sectionsByID.containsKey(CommonDashboardID.README)) {
            sectionList.addFirst(sectionsByID.get(CommonDashboardID.README));
        }
        if (sectionsByID.containsKey(CommonDashboardID.ABOUT)) {
            sectionList.addLast(sectionsByID.get(CommonDashboardID.ABOUT));
        }
        getChildren().setAll(sectionList.toArray(DashboardSection[]::new));
    }

    public void setIncludeOnlyVisibleSections(boolean onlyVisibleSections) {
        getChildren().clear();
        if (onlyVisibleSections) {
            sections().filter(Node::isVisible).forEach(getChildren()::add);
        } else {
            sections().forEach(getChildren()::add);
        }
    }

    private DashboardSection preconfiguredSection(String title, DashboardSection section) {
        section.setText(title);
        section.setMinLabelWidth(SECTION_MIN_LABEL_WIDTH);
        section.setContentBackground(Background.fill(SECTION_CONTENT_BG_COLOR));
        section.setTextColor(SECTION_TEXT_COLOR);
        section.setContentTextFont(SECTION_FONT);
        section.setLabelFont(SECTION_FONT);
        return section;
    }
}