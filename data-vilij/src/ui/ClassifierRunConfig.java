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
public class ClassifierRunConfig extends Stage{
    private Label       iteration_label = new Label("Iterations");
    private Label       interval_label  = new Label("Update Interval");
    private TextField   iterations      = new TextField(); 
    private TextField   interval        = new TextField();
    private String      iteration_input; 
    private String      interval_input; 
    private Scene       config_scene; 
    private Group       container; 
    private RadioButton continuous_run  = new RadioButton("Continuous Run? ");
    private Button      set             = new Button("Set");
    
    public ClassifierRunConfig(){
        container = new Group();
        config_scene = new Scene(container, 300, 300); 
        iterations.setLayoutY(20);
        interval_label.setLayoutY(100);
        interval.setLayoutY(120);
        continuous_run.setLayoutY(200);
        set.setLayoutY(250);
        set.setOnAction((ActionEvent e) ->{
            if (!isDigit(iterations.getText()) || !isDigit(interval.getText()))
                ErrorDialog.getDialog().show("Invalid input", "One or more of your input is invalid. Please enter digits only.");
            else{
                iteration_input = iterations.getText();
                interval_input = interval.getText();
                close();
            }
        });
        container.getChildren().addAll(iteration_label, interval_label, iterations, interval, continuous_run, set);
        setScene(config_scene);
    }
    
    public int getInterval(){
        return Integer.parseInt(interval_input);
    }
    public int getIterations(){
        return Integer.parseInt(iteration_input);
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
