/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.pacmanfx.Globals.TS;

public class GameUI_Preferences extends UIPreferences {

    public GameUI_Preferences() {
        super(GameUI_Preferences.class);
    }

    protected void defineDefaultValues() {
        storeDefaultValue("3d.bonus.symbol.width", 8.0f);
        storeDefaultValue("3d.bonus.points.width", 1.8f * 8.0f);
        storeDefaultValue("3d.energizer.radius", 3.5f);
        storeDefaultValue("3d.energizer.scaling.min", 0.2f);
        storeDefaultValue("3d.energizer.scaling.max", 1.0f);
        storeDefaultValue("3d.floor.padding", 5.0f);
        storeDefaultValue("3d.floor.thickness", 0.5f);
        storeDefaultValue("3d.ghost.size", 15.5f);
        storeDefaultValue("3d.house.base_height", 12.0f);
        storeDefaultValue("3d.house.opacity", 0.4f);
        storeDefaultValue("3d.house.sensitivity", 1.5f * TS);
        storeDefaultValue("3d.house.wall_thickness", 2.5f);
        storeDefaultValue("3d.level_counter.symbol_size", 10.0f);
        storeDefaultValue("3d.level_counter.elevation", 6f);
        storeDefaultValue("3d.lives_counter.capacity", 5);
        storeDefaultColor("3d.lives_counter.pillar_color", Color.grayRgb(120));
        storeDefaultColor("3d.lives_counter.plate_color",  Color.grayRgb(180));
        storeDefaultValue("3d.lives_counter.shape_size", 12.0f);
        storeDefaultValue("3d.obstacle.base_height", 4.0f);
        storeDefaultValue("3d.obstacle.corner_radius", 4.0f);
        storeDefaultValue("3d.obstacle.opacity", 1.0f);
        storeDefaultValue("3d.obstacle.wall_thickness", 2.25f);
        storeDefaultValue("3d.pac.size", 16.0f);
        storeDefaultValue("3d.pellet.radius", 1.0f);

        // "Kornblumenblau, sind die Augen der Frauen beim Weine. Hicks!"
        storeDefaultColor("context_menu.title.fill", Color.CORNFLOWERBLUE);
        storeDefaultFont("context_menu.title.font", Font.font("Dialog", FontWeight.BLACK, 14.0f));

        storeDefaultColor("debug_text.fill", Color.WHITE);
        storeDefaultColor("debug_text.stroke", Color.GRAY);
        storeDefaultFont("debug_text.font", Font.font("Sans", 16.0f));

        storeDefaultValue("scene2d.max_scaling", 5.0f);
    }
}
