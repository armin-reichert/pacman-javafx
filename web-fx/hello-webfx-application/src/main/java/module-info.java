// File managed by WebFX (DO NOT EDIT MANUALLY)

module hello.webfx.application {

    // Direct dependencies modules
    requires javafx.graphics;

    // Exported packages
    exports de.amr;

    // Provided services
    provides javafx.application.Application with de.amr.WebFXApp;

}