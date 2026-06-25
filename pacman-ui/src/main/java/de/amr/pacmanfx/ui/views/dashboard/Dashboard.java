/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Dashboard {

    private static DashboardSection createCommonSection(Dashboard dashboard, Identifier id) {
        requireNonNull(dashboard);
        requireNonNull(id);
        return switch (id) {
            case DashboardID.ABOUT          -> new DashboardSectionAbout(dashboard);
            case DashboardID.ACTOR_INFO     -> new DashboardSectionActorInfo(dashboard);
            case DashboardID.ANIMATION_INFO -> new DashboardSectionAnimations3D(dashboard);
            // this dashboard section needs additional configuration to work!
            case DashboardID.CUSTOM_MAPS    -> new DashboardSectionCustomMaps(dashboard);
            case DashboardID.GENERAL        -> new DashboardSectionGeneral(dashboard);
            case DashboardID.GAME_CONTROL   -> new DashboardSectionGameControl(dashboard);
            case DashboardID.GAME_INFO      -> new DashboardSectionGameInfo(dashboard);
            case DashboardID.KEYS_GLOBAL    -> new DashboardSectionKeyShortcutsGlobal(dashboard);
            case DashboardID.KEYS_LOCAL     -> new DashboardSectionKeyboardShortcutsCurrentGameScene(dashboard);
            case DashboardID.README         -> new DashboardSectionReadmeFirst(dashboard);
            case DashboardID.SETTINGS_3D    -> new DashboardSection3DSettings(dashboard);
            default -> throw new IllegalArgumentException("Illegal dashboard ID: " + id);
        };
    }

    private static String titleKey(Identifier id) {
        requireNonNull(id);
        return switch (id) {
            case DashboardID.ABOUT          -> "infobox.about.title";
            case DashboardID.ACTOR_INFO     -> "infobox.actor_info.title";
            case DashboardID.ANIMATION_INFO -> "infobox.animation_info.title";
            case DashboardID.CUSTOM_MAPS    -> "infobox.custom_maps.title";
            case DashboardID.GENERAL        -> "infobox.general.title";
            case DashboardID.GAME_CONTROL   -> "infobox.game_control.title";
            case DashboardID.GAME_INFO      -> "infobox.game_info.title";
            case DashboardID.KEYS_GLOBAL    -> "infobox.keyboard_shortcuts_global.title";
            case DashboardID.KEYS_LOCAL     -> "infobox.keyboard_shortcuts_local.title";
            case DashboardID.README         -> "infobox.readme.title";
            case DashboardID.SETTINGS_3D    -> "infobox.3D_settings.title";
            default -> throw new IllegalArgumentException("Illegal dashboard ID: " + id);
        };
    }

    private final VBox rootPane = new VBox();
    private final Map<Identifier, DashboardSection> sections = new LinkedHashMap<>();
    private final DashboardConfig config;

    private Game game;

    private final ChangeListener<Boolean> visibilityChangeHandler = (_, _, _) -> updateLayout();

    public Dashboard(DashboardConfig config) {
        this.config = requireNonNull(config);
        rootPane.visibleProperty().addListener(visibilityChangeHandler);
        rootPane.setPadding(new Insets(10));
    }

    public Pane rootPane() {
        return rootPane;
    }

    public DashboardConfig config() {
        return config;
    }

    public Game game() {
        return game;
    }

    public void connect(Game game) {
        this.game = requireNonNull(game);
        sections.values().forEach(section -> section.connect(game));
    }

    public void update() {
        sections().filter(DashboardSection::isExpanded).forEach(DashboardSection::update);
    }

    public void toggleVisibility() {
        rootPane.setVisible(!rootPane.isVisible());
    }

    public Stream<DashboardSection> sections() { return sections.values().stream(); }

    public void removeSection(Identifier id) {
        requireNonNull(id);
        final DashboardSection section = sections.get(id);
        if (section != null) {
            section.visibleProperty().removeListener(visibilityChangeHandler);
            sections.remove(id);
            updateLayout();
        }
    }

    public void addSection(Identifier id, DashboardSection section) {
        requireNonNull(id);
        requireNonNull(section);
        sections.put(id, section);
        section.visibleProperty().addListener(visibilityChangeHandler);
    }

    public void addSection(Identifier id, DashboardSection section, String title, boolean maximized) {
        section.setText(title);
        section.setDisplayedMaximized(maximized);
        addSection(id, section);
    }

    /**
     * Adds one of the common dashboard sections defined by the given ID.
     *
     * @param translator translator for localized text keys
     * @param id common dashboard section ID
     */
    public DashboardSection addCommonSection(TranslationManager translator, DashboardID id) {
        requireNonNull(translator);
        requireNonNull(id);

        final DashboardSection section = createCommonSection(this, id);
        section.setText(translator.translate(titleKey(id)));
        addSection(id, section);
        return section;
    }

    /**
     * Adds all dashboard sections defined by the given ID. The README info box is automatically inserted at the first position.
     *
     * @param translator translator for localized text keys
     * @param idList list of dashboard section IDs
     */
    public void addCommonSections(TranslationManager translator, List<DashboardID> idList) {
        requireNonNull(idList);

        addCommonSection(translator, DashboardID.README);
        for (DashboardID id : idList) {
            if (id != DashboardID.README) addCommonSection(translator, id);
        }
    }

    public Optional<DashboardSection> findSection(Identifier id) {
        requireNonNull(id);
        return Optional.ofNullable(sections.get(id));
    }

    public void updateLayout() {
        final List<DashboardSection> reorderedSections = new ArrayList<>(sections.entrySet().stream()
            .filter(e -> e.getValue().isVisible())
            .filter(e -> e.getKey() != DashboardID.README)
            .filter(e -> e.getKey() != DashboardID.ABOUT)
            .map(Map.Entry::getValue)
            .toList());

        if (sections.containsKey(DashboardID.README)) {
            reorderedSections.addFirst(sections.get(DashboardID.README));
        }
        if (sections.containsKey(DashboardID.ABOUT)) {
            reorderedSections.addLast(sections.get(DashboardID.ABOUT));
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