/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import vilij.components.ErrorDialog;

/**
 *
 * @author omars_000
 */
public class ClusteringRunConfig extends Stage{
    private Label       iteration_label = new Label("Iterations");
    private Label       interval_label  = new Label("Update Interval");
    private Label       cluster_labels   = new Label("Number of labels");
    private TextField   iterations      = new TextField(); 
    private TextField   interval        = new TextField();
    private String      iteration_input; 
    private String      interval_input; 
    private String      cluster_input;
    private Scene       config_scene; 
    private Group       container; 
    private RadioButton continuous_run  = new RadioButton("Continuous Run? ");
    private Button      set             = new Button("Set");
    private TextField   cluster         = new TextField();

    
    public ClusteringRunConfig(){
        container = new Group();
        config_scene = new Scene(container, 300, 400);
        iterations.setLayoutY(20);
        interval_label.setLayoutY(100);
        interval.setLayoutY(120);
        cluster_labels.setLayoutY(200);
        cluster.setLayoutY(220);
        continuous_run.setLayoutY(300);
        set.setLayoutY(350);
        set.setOnAction((ActionEvent e) ->{
            if (!isDigit(iterations.getText()) || !isDigit(interval.getText()) || !isDigit(cluster.getText()))
                ErrorDialog.getDialog().show("Invalid input", "One or more of your input is invalid. Please enter digits only.");
            else{
                iteration_input = iterations.getText();
                interval_input = interval.getText();
                cluster_input = cluster.getText();
                close();
            }
        });
        container.getChildren().addAll(iteration_label, interval_label, cluster_labels, cluster,iterations, interval, continuous_run, set);
        setScene(config_scene);
    }
    
    public int getInterval(){
        return Integer.parseInt(interval_input);
    }
    public int getIterations(){
        return Integer.parseInt(iteration_input);
    }
    public int getClusters(){
        return Integer.parseInt(cluster_input);
    }
    public boolean continuousRun(){
        return continuous_run.isSelected();
    }
    private boolean isDigit(String input){
        for (int i = 0; i < input.length(); i++){
            if (input.charAt(i) < '0' || input.charAt(i) > '9')
                return false;
        }
        return true;
    }
}
