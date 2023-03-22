module de.amr.objimport {

	requires transitive org.apache.logging.log4j;
	requires transitive javafx.controls;
	requires transitive javafx.fxml;

	exports de.amr.objimport;
	exports de.amr.objimport.obj;
	exports de.amr.objimport.shape3d;
	exports de.amr.objimport.shape3d.symbolic;
}