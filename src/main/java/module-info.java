
module candyjar {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    //requires transitive json.simple;
    
    requires transitive org.glassfish.java.json;
	requires transitive javafx.base;
	requires transitive chartfx.chart;
	requires transitive chartfx.dataset;
	requires transitive org.controlsfx.controls;
	requires java.desktop;
	requires java.image.scaling;
	requires javafx.swing;
	requires commons.io;
	
    
    opens application to javafx.fxml;
    exports application;
    exports data_holders;
    
    

}