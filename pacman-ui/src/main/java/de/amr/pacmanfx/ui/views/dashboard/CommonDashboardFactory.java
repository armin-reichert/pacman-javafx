/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;


import de.amr.basics.Identifier;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class CommonDashboardFactory implements DashboardFactory {

    private CommonDashboardFactory() {}

    private static class LazyThreadSafeSingletonHolder {
        static final CommonDashboardFactory SINGLETON = new CommonDashboardFactory();
    }

    public static CommonDashboardFactory instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    @Override
    public Optional<Identifier> identify(String id) {
        try {
            return Optional.of(DashboardID.valueOf(id));
        }
        catch (IllegalArgumentException x) {
            return Optional.empty();
        }
    }

    @Override
    public DashboardSection createSection(Dashboard dashboard, Identifier id, TranslationManager translations) {
        requireNonNull(dashboard);
        requireNonNull(id);
        requireNonNull(translations);

        final DashboardSection section = switch (id) {
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
        section.setText(translations.translate(titleKey(id)));
        return section;
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
}
