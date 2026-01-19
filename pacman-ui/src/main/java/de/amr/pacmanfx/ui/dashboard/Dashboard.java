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

import java.util.*;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

public class Dashboard extends VBox {

    public record Style(
        int minLabelWidth,
        int minWidth,
        Color contentBackground,
        Color textColor,
        Font labelFont,
        Font contentFont) {}

    public static final Style DEFAULT_STYLE = new Style(
        110,
        350,
        Color.rgb(0, 0, 50, 1.0),
        Color.WHITE,
        Font.font("Sans", 12),
        Font.font("Sans", 12)
    );

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
    private final Style style;

    public Dashboard(Style style) {
        this.style = requireNonNull(style);
        visibleProperty().addListener((_, _, visible) -> {
            if (visible) {
                updateLayout();
            }
        });
    }

    public Dashboard() {
        this(DEFAULT_STYLE);
    }

    public Style style() {
        return style;
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

    private DashboardSection createCommonSection(DashboardID id) {
        requireNonNull(id);
        return switch (id) {
            case CommonDashboardID.ABOUT          -> new DashboardSectionAbout(this);
            case CommonDashboardID.ACTOR_INFO     -> new DashboardSectionActorInfo(this);
            case CommonDashboardID.ANIMATION_INFO -> new DashboardSectionGameLevelAnimations(this);
            // this dashboard section needs additional configuration to work!
            case CommonDashboardID.CUSTOM_MAPS    -> new DashboardSectionCustomMaps(this);
            case CommonDashboardID.GENERAL        -> new DashboardSectionGeneral(this);
            case CommonDashboardID.GAME_CONTROL   -> new DashboardSectionGameControl(this);
            case CommonDashboardID.GAME_INFO      -> new DashboardSectionGameInfo(this);
            case CommonDashboardID.KEYS_GLOBAL    -> new DashboardSectionKeyShortcutsGlobal(this);
            case CommonDashboardID.KEYS_LOCAL     -> new DashboardSectionKeyShortcutsLocal(this);
            case CommonDashboardID.README         -> new DashboardSectionReadmeFirst(this);
            case CommonDashboardID.SETTINGS_3D    -> new DashboardSection3DSettings(this);
            default -> throw new IllegalArgumentException("Illegal dashboard ID: " + id);
        };
    }

    /**
     * Adds one of the common dashboard sections defined by the given ID.
     *
     * @param translator translator for localized text keys
     * @param id common dashboard section ID
     */
    public void addCommonSection(Translator translator, DashboardID id) {
        requireNonNull(translator);
        requireNonNull(id);
        final DashboardSection section = createCommonSection(id);
        sectionsByID.put(id, configure(section, translator.translate(TITLE_KEYS.get(id)), false));
    }

    public void addSection(DashboardID id, DashboardSection section, String title, boolean maximized) {
        requireNonNull(id);
        requireNonNull(title);
        requireNonNull(section);
        sectionsByID.put(id, configure(section, title, maximized));
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

    private DashboardSection configure(DashboardSection section, String title, boolean maximized) {
        section.setText(title);
        section.setDisplayedMaximized(maximized);
        section.setMinLabelWidth(style.minLabelWidth());
        section.setMinWidth(style.minWidth());
        section.setContentBackground(Background.fill(style.contentBackground()));
        section.setTextColor(style.textColor());
        return section;
    }
}