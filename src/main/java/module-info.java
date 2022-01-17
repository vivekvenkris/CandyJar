
module candyjar {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    //requires transitive json.simple;
    
    requires transitive org.glassfish.java.json;
	requires transitive javafx.base;
	requires  chartfx.chart;
	requires  chartfx.dataset;
	requires transitive org.controlsfx.controls;
	requires transitive java.desktop;
	requires  java.image.scaling;
	requires transitive javafx.swing;
	requires  commons.io;
	requires  commons.cli;
	requires org.slf4j;
	requires jsky;
	requires net.kurobako.gesturefx;
	requires unirest.java;
	requires javaxt.core;
	requires imgscalr.lib;
	
    
    opens application to javafx.fxml;
    exports application;
    exports data_holders;
    exports utilitites;
    
    

}