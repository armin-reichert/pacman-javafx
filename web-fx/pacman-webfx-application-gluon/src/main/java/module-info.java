// File managed by WebFX (DO NOT EDIT MANUALLY)

module pacman.webfx.application.gluon {

    // Direct dependencies modules
    requires pacman.webfx.application;
    requires webfx.kit.openjfx;
    requires webfx.platform.boot.java;
    requires webfx.platform.console.java;
    requires webfx.platform.resource.gluon;
    requires webfx.platform.scheduler.java;
    requires webfx.platform.shutdown.gluon;

    // Meta Resource package
    opens dev.webfx.platform.meta.exe;

}