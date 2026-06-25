/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.ui.views.dashboard.*;
import de.amr.pacmanfx.uilib.assets.TranslationManager;

import java.util.Optional;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_DashboardID.JOYPAD;

public class TengenDashboardFactory implements DashboardFactory {

    private static class LazyThreadSafeSingletonHolder {
        static final TengenDashboardFactory SINGLETON = new TengenDashboardFactory();
    }

    public static TengenDashboardFactory instance() {
        return TengenDashboardFactory.LazyThreadSafeSingletonHolder.SINGLETON;
    }

    @Override
    public Optional<Identifier> identify(String id) {
        final Optional<Identifier> commonID = CommonDashboardFactory.instance().identify(id);
        if (commonID.isPresent()) {
            return commonID;
        }
        return JOYPAD.name().equals(id) ? Optional.of(JOYPAD) : Optional.empty();
    }

    @Override
    public DashboardSection createSection(Dashboard dashboard, Identifier id, TranslationManager translations) {
        return switch (id) {
            case DashboardID _ -> CommonDashboardFactory.instance().createSection(dashboard, id, translations);
            case TengenMsPacMan_DashboardID tengenID -> {
                if (tengenID == JOYPAD) {
                    yield new DashboardSectionJoypad(dashboard);
                }
                throw new IllegalArgumentException("Illegal Tengen dashboard ID: %s".formatted(tengenID));
            }
            default -> throw new IllegalArgumentException("Illegal dashboard ID: %s".formatted(id));
        };
    }
}
