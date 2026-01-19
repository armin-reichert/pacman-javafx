/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.assets.Translator;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

public class Dashboard extends VBox {

    public static final int SECTION_MIN_LABEL_WIDTH = 110;
    public static final int SECTION_MIN_WIDTH = 350;

    public static final Color SECTION_CONTENT_BG_COLOR = Color.rgb(0, 0, 50, 1.0);
    public static final Color SECTION_TEXT_COLOR = Color.WHITE;
    public static final Font  SECTION_FONT = Font.font("Sans", 12);

    private static final Map<DashboardID, String> TITLE_KEYS = Map.ofEntries(
        entry(CommonDashboardID.ABOUT         , "infobox.about.title"),
        entry(CommonDashboardID.ACTOR_INFO    , "infobox.actor_info.title"),
        entry(CommonDashboardID.ANIMATION_INFO, "infobox.animation_info.title"),
        entry(CommonDashboardID.CUSTOM_MAPS   , "infobox.custom_maps.title"),
        entry(CommonDashboardID.GENERAL       , "infobox.general.title"),
        entry(CommonDashboardID.GAME_CONTROL  , "infobox.game_control.title"),
        entry(CommonDashboardID.GAME_INFO     , "infobox.game_info.title"),
        entry(CommonDashboardID.KEYS_GLOBAL   , "infobox.keyboard_shortcuts_global.title"),
        entry(CommonDashboardID.KEYS_LOCAL    , "infobox.keyboard_shortcuts_local.title"),
        entry(CommonDashboardID.README        , "infobox.readme.title"),
        entry(CommonDashboardID.SETTINGS_3D   , "infobox.3D_settings.title")
    );

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

    public void addCommonSection(Translator translator, DashboardID id) {
        requireNonNull(translator);
        requireNonNull(id);
        switch (id) {
            case CommonDashboardID.ABOUT          -> addCommonSection(translator, id, new DashboardSectionAbout(this), false);
            case CommonDashboardID.ACTOR_INFO     -> addCommonSection(translator, id, new DashboardSectionActorInfo(this), true);
            case CommonDashboardID.ANIMATION_INFO -> addCommonSection(translator, id, new DashboardSectionGameLevelAnimations(this), true);
            // this dashboard section needs additional configuration to work!
            case CommonDashboardID.CUSTOM_MAPS    -> addCommonSection(translator, id, new DashboardSectionCustomMaps(this), true);
            case CommonDashboardID.GENERAL        -> addCommonSection(translator, id, new DashboardSectionGeneral(this), false);
            case CommonDashboardID.GAME_CONTROL   -> addCommonSection(translator, id, new DashboardSectionGameControl(this), false);
            case CommonDashboardID.GAME_INFO      -> addCommonSection(translator, id, new DashboardSectionGameInfo(this), true);
            case CommonDashboardID.KEYS_GLOBAL    -> addCommonSection(translator, id, new DashboardSectionKeyShortcutsGlobal(this), true);
            case CommonDashboardID.KEYS_LOCAL     -> addCommonSection(translator, id, new DashboardSectionKeyShortcutsLocal(this), false);
            case CommonDashboardID.README         -> addCommonSection(translator, id, new DashboardSectionReadmeFirst(this), false);
            case CommonDashboardID.SETTINGS_3D    -> addCommonSection(translator, id, new DashboardSection3DSettings(this), false);
            default -> Logger.warn("Not so common dashboard ID: {}", id);
        }
        // Initially expand the README section
        sectionsByID.get(CommonDashboardID.README).setExpanded(true);
    }

    public void addSection(DashboardID id, String title, DashboardSection section, boolean maximized) {
        sectionsByID.put(id, configureSection(title, section));
        section.setDisplayedMaximized(maximized);
        section.setMinWidth(SECTION_MIN_WIDTH);
    }

    /**
     * Adds all dashboard sections defined by the given ID. The README info box is automatically inserted at the first position.
     *
     * @param translator translator for localized text keys
     * @param ids list of dashboard section IDs
     */
    public void addSections(Translator translator, List<DashboardID> ids) {
        requireNonNull(translator);
        requireNonNull(ids);
        addCommonSection(translator, CommonDashboardID.README);
        for (DashboardID id : ids) {
            if (id != CommonDashboardID.README) addCommonSection(translator, id);
        }
    }

    public Optional<DashboardSection> findSection(DashboardID id) {
        requireNonNull(id);
        return Optional.ofNullable(sectionsByID.get(id));
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

    private void addCommonSection(Translator translator, DashboardID id, DashboardSection section, boolean maximized) {
        section.setDisplayedMaximized(maximized);
        final String title = translator.translate(TITLE_KEYS.get(id));
        sectionsByID.put(id, configureSection(title, section));
    }

    private DashboardSection configureSection(String title, DashboardSection section) {
        section.setText(title);
        section.setMinLabelWidth(SECTION_MIN_LABEL_WIDTH);
        section.setMinWidth(SECTION_MIN_WIDTH);
        section.setContentBackground(Background.fill(SECTION_CONTENT_BG_COLOR));
        section.setTextColor(SECTION_TEXT_COLOR);
        section.setContentTextFont(SECTION_FONT);
        section.setLabelFont(SECTION_FONT);
        return section;
    }
}