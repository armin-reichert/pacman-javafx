/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

/**
 * Play scene perspectives.
 *
 * @author Armin Reichert
 */
public enum Perspective {
    DRONE(new CamDrone()),
    TOTAL(new CamTotal()),
    FOLLOWING_PLAYER(new CamFollowingPlayer()),
    NEAR_PLAYER(new CamNearPlayer());

    private final CameraController camController;

    Perspective(CameraController camController) {
        this.camController = camController;
    }

    public CameraController getCamController() {
        return camController;
    }

    public static Perspective succ(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() < n - 1 ? p.ordinal() + 1 : 0];
    }

    public static Perspective pred(Perspective p) {
        int n = Perspective.values().length;
        return Perspective.values()[p.ordinal() > 0 ? p.ordinal() - 1 : n - 1];
    }

}