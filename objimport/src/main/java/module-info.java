module de.amr.objimport {

	requires java.logging;
	requires transitive javafx.controls;
	requires transitive javafx.fxml;

	exports de.amr.objimport;
	exports de.amr.objimport.obj;
	exports de.amr.objimport.shape3d;
	exports de.amr.objimport.shape3d.symbolic;
}