/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.NO_TEXTURE;

/**
 * @author Armin Reichert
 */
public class Floor3D extends Box {

    public final ObjectProperty<String> texturePy = new SimpleObjectProperty<>(this, "floorTexture", NO_TEXTURE) {
        @Override
        protected void invalidated() {
            updateMaterial();
        }
    };

    public final ObjectProperty<Color> colorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK) {
        @Override
        protected void invalidated() {
            updateMaterial();
        }
    };

    private final Map<String, PhongMaterial> textures;

    public Floor3D(double sizeX, double sizeY, double thickness, Map<String, PhongMaterial> textures) {
        super(sizeX, sizeY, thickness);
        checkNotNull(textures);
        this.textures = textures;
        updateMaterial();
    }

    private void updateMaterial() {
        var material = textures.getOrDefault("texture." + texturePy.get(), coloredMaterial(colorPy.get()));
        setMaterial(material);
    }
}
