/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Dashboard {

    private static final EnumMap<CommonDashboardID, Boolean> MAXIMIZED = new EnumMap<>(CommonDashboardID.class);
    static {
        MAXIMIZED.put(CommonDashboardID.ABOUT, true);
        MAXIMIZED.put(CommonDashboardID.ACTOR_INFO, true);
        MAXIMIZED.put(CommonDashboardID.ANIMATION_INFO, true);
        MAXIMIZED.put(CommonDashboardID.CUSTOM_MAPS, true);
        MAXIMIZED.put(CommonDashboardID.GENERAL, false);
        MAXIMIZED.put(CommonDashboardID.GAME_CONTROL, false);
        MAXIMIZED.put(CommonDashboardID.GAME_INFO, true);
        MAXIMIZED.put(CommonDashboardID.KEYS_GLOBAL, true);
        MAXIMIZED.put(CommonDashboardID.KEYS_LOCAL, false);
        MAXIMIZED.put(CommonDashboardID.README, false);
        MAXIMIZED.put(CommonDashboardID.SETTINGS_3D, true);
    }

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

    private static boolean isSectionMaximizedByDefault(DashboardID id) {
        requireNonNull(id);
        if (id instanceof CommonDashboardID commonID) {
            return MAXIMIZED.getOrDefault(commonID, false);
        }
        return false;
    }

    private static DashboardSection configure(DashboardSection section, String title, boolean maximized) {
        section.setText(title);
        section.setDisplayedMaximized(maximized);
        return section;
    }

    private final VBox rootPane = new VBox();

    private final Map<DashboardID, DashboardSection> sections = new LinkedHashMap<>();
    private final DashboardConfig config;

    public Dashboard(DashboardConfig config) {
        this.config = requireNonNull(config);
        rootPane.visibleProperty().addListener((_, _, visible) -> {
            if (visible) {
                updateLayout();
            }
        });
        rootPane.setPadding(new Insets(10));
    }

    public Pane rootPane() {
        return rootPane;
    }

    public DashboardConfig config() {
        return config;
    }

    public void update(GameUI ui) {
        requireNonNull(ui);
        sections().filter(DashboardSection::isExpanded).forEach(section -> section.update(ui));
    }

    public void toggleVisibility() {
        rootPane.setVisible(!rootPane.isVisible());
    }

    public Stream<DashboardSection> sections() { return sections.values().stream(); }

    public void removeSection(DashboardID id) {
        requireNonNull(id);
        sections.remove(id);
        updateLayout();
    }

    /**
     * Adds one of the common dashboard sections defined by the given ID.
     *
     * @param translator translator for localized text keys
     * @param id common dashboard section ID
     */
    public void addCommonSection(TranslationManager translator, CommonDashboardID id) {
        requireNonNull(translator);
        requireNonNull(id);
        final DashboardSection section = createCommonSection(this, id);
        final boolean maximized = isSectionMaximizedByDefault(id);
        sections.put(id, configure(section, translator.translate(titleKey(id)), maximized));
    }

    public void addSection(DashboardID id, DashboardSection section, String title, boolean maximized) {
        requireNonNull(id);
        requireNonNull(section);
        requireNonNull(title);
        sections.put(id, configure(section, title, maximized));
    }

    /**
     * Adds all dashboard sections defined by the given ID. The README info box is automatically inserted at the first position.
     *
     * @param translator translator for localized text keys
     * @param ids list of dashboard section IDs
     */
    public void addCommonSections(TranslationManager translator, List<CommonDashboardID> ids) {
        requireNonNull(translator);
        requireNonNull(ids);
        addCommonSection(translator, CommonDashboardID.README);
        for (CommonDashboardID id : ids) {
            if (id != CommonDashboardID.README) addCommonSection(translator, id);
        }
    }

    public Optional<DashboardSection> findSection(DashboardID id) {
        requireNonNull(id);
        return Optional.ofNullable(sections.get(id));
    }

    private void updateLayout() {
        final List<DashboardSection> reorderedSections = new ArrayList<>(sections.entrySet().stream()
            .filter(e -> e.getKey() != CommonDashboardID.README)
            .filter(e -> e.getKey() != CommonDashboardID.ABOUT)
            .map(Map.Entry::getValue)
            .toList());

        if (sections.containsKey(CommonDashboardID.README)) {
            reorderedSections.addFirst(sections.get(CommonDashboardID.README));
        }
        if (sections.containsKey(CommonDashboardID.ABOUT)) {
            reorderedSections.addLast(sections.get(CommonDashboardID.ABOUT));
        }

        rootPane.getChildren().clear();
        rootPane.getChildren().addAll(reorderedSections);
    }

    public void setCompactMode(boolean compactMode) {
        rootPane.getChildren().clear();
        if (compactMode) {
            sections().filter(Node::isVisible).forEach(rootPane.getChildren()::add);
        } else {
            sections().forEach(rootPane.getChildren()::add);
        }
    }
}