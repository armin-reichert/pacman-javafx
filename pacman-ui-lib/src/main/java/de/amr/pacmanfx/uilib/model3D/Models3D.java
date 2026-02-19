/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.assets.ResourceManager;

import java.net.URL;

public abstract class Models3D {

    private Models3D() {}

    public static Model3D loadModelFromObjFile(String objFilePath) {
        final ResourceManager resourceManager = () -> Models3D.class;
        final URL url = resourceManager.url(objFilePath);
        if (url == null) {
            throw new IllegalArgumentException("Could not access model3D with resource path '%s'".formatted(objFilePath));
        }
        try {
            return new Model3D(url);
        } catch (Exception x) {
            throw new IllegalArgumentException("Could not load 3D model from URL '%s'".formatted(url), x);
        }
    }

    public static final PacManModel3D PAC_MAN_MODEL = new PacManModel3D();
    public static final GhostModel3D GHOST_MODEL = new GhostModel3D();
    public static final PelletModel3D PELLET_MODEL = new PelletModel3D();
}