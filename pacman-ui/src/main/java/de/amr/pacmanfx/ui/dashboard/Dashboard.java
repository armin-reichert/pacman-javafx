/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.assets.Translator;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Dashboard extends VBox {

    public record DashboardConfig(
        int labelWidth,
        int width,
        Color contentBackground,
        Color textColor,
        Font labelFont,
        Font contentFont) {}

    public static final DashboardConfig DEFAULT_CONFIG = new DashboardConfig(
        110,
        320,
        Color.rgb(0, 0, 50, 1.0),
        Color.WHITE,
        Font.font("Sans", 12),
        Font.font("Sans", 12)
    );

    private static DashboardSection createCommonSection(Dashboard dashboard, DashboardID id) {
        requireNonNull(dashboard);
        requireNonNull(id);
        return switch (id) {
            case CommonDashboardID.ABOUT          -> new DashboardSectionAbout(dashboard);
            case CommonDashboardID.ACTOR_INFO     -> new DashboardSectionActorInfo(dashboard);
            case CommonDashboardID.ANIMATION_INFO -> new DashboardSectionAnimations3D(dashboard);
            // this dashboard section needs additional configuration to work!
            case CommonDashboardID.CUSTOM_MAPS    -> new DashboardSectionCustomMaps(dashboard);
            case CommonDashboardID.GENERAL        -> new DashboardSectionGeneral(dashboard);
            case CommonDashboardID.GAME_CONTROL   -> new DashboardSectionGameControl(dashboard);
            case CommonDashboardID.GAME_INFO      -> new DashboardSectionGameInfo(dashboard);
            case CommonDashboardID.KEYS_GLOBAL    -> new DashboardSectionKeyShortcutsGlobal(dashboard);
            case CommonDashboardID.KEYS_LOCAL     -> new DashboardSectionKeyShortcutsLocal(dashboard);
            case CommonDashboardID.README         -> new DashboardSectionReadmeFirst(dashboard);
            case CommonDashboardID.SETTINGS_3D    -> new DashboardSection3DSettings(dashboard);
            default -> throw new IllegalArgumentException("Illegal dashboard ID: " + id);
        };
    }

    private static String titleKey(DashboardID id) {
        requireNonNull(id);
        return switch (id) {
            case CommonDashboardID.ABOUT          -> "infobox.about.title";
            case CommonDashboardID.ACTOR_INFO     -> "infobox.actor_info.title";
            case CommonDashboardID.ANIMATION_INFO -> "infobox.animation_info.title";
            case CommonDashboardID.CUSTOM_MAPS    -> "infobox.custom_maps.title";
            case CommonDashboardID.GENERAL        -> "infobox.general.title";
            case CommonDashboardID.GAME_CONTROL   -> "infobox.game_control.title";
            case CommonDashboardID.GAME_INFO      -> "infobox.game_info.title";
            case CommonDashboardID.KEYS_GLOBAL    -> "infobox.keyboard_shortcuts_global.title";
            case CommonDashboardID.KEYS_LOCAL     -> "infobox.keyboard_shortcuts_local.title";
            case CommonDashboardID.README         -> "infobox.readme.title";
            case CommonDashboardID.SETTINGS_3D    -> "infobox.3D_settings.title";
            default -> throw new IllegalArgumentException("Illegal dashboard ID: " + id);
        };
    }

    private static boolean isCommonSectionMaximizedByDefault(DashboardID id) {
        requireNonNull(id);
        return switch (id) {
            case CommonDashboardID.ABOUT          -> true;
            case CommonDashboardID.ACTOR_INFO     -> true;
            case CommonDashboardID.ANIMATION_INFO -> true;
            case CommonDashboardID.CUSTOM_MAPS    -> true;
            case CommonDashboardID.GENERAL        -> false;
            case CommonDashboardID.GAME_CONTROL   -> false;
            case CommonDashboardID.GAME_INFO      -> true;
            case CommonDashboardID.KEYS_GLOBAL    -> true;
            case CommonDashboardID.KEYS_LOCAL     -> false;
            case CommonDashboardID.README         -> false;
            case CommonDashboardID.SETTINGS_3D    -> true;
            default -> throw new IllegalArgumentException("Illegal dashboard ID: " + id);
        };
    }

    private final Map<DashboardID, DashboardSection> sectionsByID = new LinkedHashMap<>();
    private final DashboardConfig dashboardConfig;

    public Dashboard(DashboardConfig dashboardConfig) {
        this.dashboardConfig = requireNonNull(dashboardConfig);
        visibleProperty().addListener((_, ignored, visible) -> {
            if (visible) {
                updateLayout();
            }
        });
        setPadding(new Insets(10));
    }

    public Dashboard() {
        this(DEFAULT_CONFIG);
    }

    public DashboardConfig style() {
        return dashboardConfig;
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

    /**
     * Adds one of the common dashboard sections defined by the given ID.
     *
     * @param translator translator for localized text keys
     * @param id common dashboard section ID
     */
    public void addCommonSection(Translator translator, CommonDashboardID id) {
        requireNonNull(translator);
        requireNonNull(id);
        final DashboardSection section = createCommonSection(this, id);
        final boolean maximized = isCommonSectionMaximizedByDefault(id);
        sectionsByID.put(id, configure(section, translator.translate(titleKey(id)), maximized));
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
    public void addCommonSections(Translator translator, List<CommonDashboardID> ids) {
        requireNonNull(translator);
        requireNonNull(ids);
        addCommonSection(translator, CommonDashboardID.README);
        for (CommonDashboardID id : ids) {
            if (id != CommonDashboardID.README) addCommonSection(translator, id);
        }
    }

    public Optional<DashboardSection> findSection(DashboardID id) {
        requireNonNull(id);
        return Optional.ofNullable(sectionsByID.get(id));
    }

    private void updateLayout() {
        final List<DashboardSection> sectionList = new ArrayList<>(sectionsByID.entrySet().stream()
            .filter(e -> e.getKey() != CommonDashboardID.README)
            .filter(e -> e.getKey() != CommonDashboardID.ABOUT)
            .map(Map.Entry::getValue)
            .toList());
        if (sectionsByID.containsKey(CommonDashboardID.README)) {
            sectionList.addFirst(sectionsByID.get(CommonDashboardID.README));
        }
        if (sectionsByID.containsKey(CommonDashboardID.ABOUT)) {
            sectionList.addLast(sectionsByID.get(CommonDashboardID.ABOUT));
        }
        getChildren().setAll(sectionList.toArray(DashboardSection[]::new));
    }

    public void setCompactMode(boolean compactMode) {
        getChildren().clear();
        if (compactMode) {
            sections().filter(Node::isVisible).forEach(getChildren()::add);
        } else {
            sections().forEach(getChildren()::add);
        }
    }

    private DashboardSection configure(DashboardSection section, String title, boolean maximized) {
        section.setText(title);
        section.setDisplayedMaximized(maximized);
        return section;
    }
}