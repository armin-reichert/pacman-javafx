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
            case DashboardID.ABOUT          -> new DS_About();
            case DashboardID.ACTOR_INFO     -> new DS_ActorInfo();
            case DashboardID.ANIMATION_INFO -> new DS_3DAnimationMonitor();
            // this  section needs additional configuration to work!
            case DashboardID.CUSTOM_MAPS    -> new DS_CustomMapMonitor();
            case DashboardID.GENERAL        -> new DS_General();
            case DashboardID.GAME_CONTROL   -> new DS_GameControl();
            case DashboardID.GAME_INFO      -> new DS_GameInfo();
            case DashboardID.KEYS_GLOBAL    -> new DS_GameViewKeys();
            case DashboardID.KEYS_LOCAL     -> new DS_GameSceneKeys();
            case DashboardID.README         -> new DS_ReadmeFirst(() -> dashboard.removeSection(DashboardID.README));
            case DashboardID.SETTINGS_3D    -> new DS_3DSettings();
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
