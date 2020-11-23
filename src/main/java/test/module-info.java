
module candyjar {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    //requires transitive json.simple;
    
    requires transitive org.glassfish.java.json;
	requires javafx.base;
    
    opens application to javafx.fxml;
    exports application;
    exports data_holders;

}