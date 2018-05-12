//Omar Syed
//110484590
package ui;

import java.util.*;
import actions.AppActions;
import algorithms.DataSet;
import classification.RandomClassifier;
import clustering.KMeansClusterer;
import clustering.RandomClusterer;
import dataprocessors.AppData;
import static java.io.File.separator;
import java.io.IOException;
import java.nio.file.Path;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import static settings.AppPropertyTypes.SCREENSHOT_ICON;
import static settings.AppPropertyTypes.SCREENSHOT_TOOLTIP;
import vilij.propertymanager.PropertyManager;
import static vilij.settings.PropertyTypes.EXIT_TOOLTIP;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.LOAD_TOOLTIP;
import static vilij.settings.PropertyTypes.NEW_TOOLTIP;
import static vilij.settings.PropertyTypes.SAVE_TOOLTIP;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import vilij.components.ErrorDialog;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;
    //TSDProcessor        processor          ;
    
    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private String                       scrnshotPath;   // path to scrnshotButton
    private LineChart<Number,Number>     chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private Label                        label;          // the display data button
    private RadioButton                  readOnly;       // radio button that grays out text area
    private Stack                        text;           // pushes text that is hidden into view
    private Label                        plot_label;     // graph label
    private boolean                      loadedData = false; // checks whether data has been loaded or not
    private boolean                      started = false;    // checks whether the application has been started up
    private ScrollPane                   metadata_scroll;    // the pane where metadata appears
    private Text                         metadata_text;      // text that conveys all the metadata
    private Label                        choose_algorithm;   // choose algorithm label
    private RadioButton                  is_done;            // done button
    private Button                       clustering;         // clustering option
    private Button                       classification;     // classification option
    private RadioButton                  random_clustering;  
    private RadioButton                  random_classification;
    private RadioButton                  kmeansclustering; 
    private Button                       runtime_config; 
    private GridPane                     container; 
    private Button                       run;
    private int                          change = 0; 
    private DataSet                      dataset; 
    private ClassifierRunConfig          classifier_window = new ClassifierRunConfig();
    private ClusteringRunConfig          cluster_window    = new ClusteringRunConfig(); 
    
    public LineChart<Number,Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        String iconsPath = "/" + String.join(separator,
                                             applicationTemplate.manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             applicationTemplate.manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshotPath = String.join(separator, iconsPath, applicationTemplate.manager.getPropertyValue(SCREENSHOT_ICON.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        newButton = setToolbarButton(newiconPath, manager.getPropertyValue(NEW_TOOLTIP.name()), false);
        saveButton = setToolbarButton(saveiconPath, manager.getPropertyValue(SAVE_TOOLTIP.name()), true);
        loadButton = setToolbarButton(loadiconPath, manager.getPropertyValue(LOAD_TOOLTIP.name()), false);
        exitButton = setToolbarButton(exiticonPath, manager.getPropertyValue(EXIT_TOOLTIP.name()), false);
        scrnshotButton = setToolbarButton(scrnshotPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        toolBar = new ToolBar(newButton, saveButton, loadButton, exitButton, scrnshotButton);
    }
 
    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException ex) {
                ErrorDialog.getDialog().show("Did not capture", "Did not capture image");
            }
        }); 
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        try{
            ((AppData) applicationTemplate.getDataComponent()).clear();
            textArea.clear();
            disableSaveButton();
            disableNewButton();
            scrnshotButton.setDisable(true);
        } 
        catch(Exception e){
            ErrorDialog.getDialog().show("Exception occurred", e.getMessage());
        }
        
    }
    public void setDataset(Path tsdFilePath) throws IOException{
        dataset = DataSet.fromTSDFile(tsdFilePath);
    }
    
    public void setChange(int change){
        this.change = change; 
    }
    
    public Button getDisplayButton(){
        return displayButton; 
    }
    
    public boolean hasText(){
        return hasNewText;
    }
    
    public TextArea getTextArea(){
        return textArea; 
    }
    
    public void setLoadData(boolean value){
        loadedData = value;
    }
    public Stack getStack(){
        return text; // stack that contains extra lines
    }
    public void enableNewButton(){
        newButton.setDisable(false);
    }
    public void disableNewButton(){
        newButton.setDisable(true);
    }
    public void enableSaveButton(){
        saveButton.setDisable(false);
    }
    public void disableSaveButton(){
        saveButton.setDisable(true);
    }
    public void toggleRunButton(boolean disable){
        run.setDisable(disable);
    }
    public void toggleScreenshot(boolean disable){
        scrnshotButton.setDisable(disable);
    }
    public Text getMetadata(){
        return metadata_text; 
    }
    private void layout() {
        // TODO for homework 1
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        primaryScene.getStylesheets().add(getClass().getResource("/properties/style.css").toString());
        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setMaxHeight(600);
        chart.setMaxWidth(600);
        chart.setTranslateX(400);
        chart.setTranslateY(-100);
        textArea = new TextArea(); 
        textArea.setMaxHeight(500);
        textArea.setMaxWidth(300);
        textArea.setTranslateY(-420);
        displayButton = new Button("Display");
        displayButton.setTranslateY(-420);
        label = new Label("Data File");
        label.setTranslateY(-650);
        label.setTranslateX(30);
        label.fontProperty().setValue(new Font(20));
        plot_label = new Label("Plot");
        plot_label.fontProperty().setValue(new Font(20));
        plot_label.setTranslateY(-40);
        plot_label.setTranslateX(700);
        readOnly = new RadioButton();
        readOnly.setText("Read only");
        readOnly.setTranslateX(220);
        readOnly.setTranslateY(-475);
        metadata_scroll = new ScrollPane();
        metadata_text = new Text();
        metadata_scroll.setContent(metadata_text);
        metadata_scroll.setMaxSize(400, 400);
        metadata_scroll.setPrefSize(400, 100);
        metadata_scroll.setTranslateY(250);
        metadata_scroll.setVisible(false);
        choose_algorithm = new Label("Choose an algorithm: ");
        choose_algorithm.fontProperty().setValue(new Font(20));
        choose_algorithm.setTranslateY(300);
        is_done = new RadioButton("Done");
        is_done.setTranslateX(300);
        clustering = new Button("Clustering");
        clustering.setTranslateY(280);
        clustering.setDisable(true);
        classification = new Button("Classification");
        classification.setTranslateY(255);
        classification.setTranslateX(70);
        classification.setDisable(true);
        container = new GridPane(); 
        random_clustering = new RadioButton("Random Clustering");
        random_classification = new RadioButton("Random Classification");
        kmeansclustering = new RadioButton("K Means Clustering");
        container.getChildren().add(random_clustering); //algorithm
        container.getChildren().add(random_classification); //algorithm
        container.getChildren().add(kmeansclustering); //algorithm
        kmeansclustering.setTranslateY(20);
        runtime_config = new Button("Config");
        container.addColumn(1, runtime_config);
        container.setTranslateY(360);
        run = new Button("Run");
        run.setDisable(true);
        run.setTranslateY(600);
        addToContainer(); // add all elements to container
        setInvisible(); // make all elements invisible
        appPane.setMaxHeight(700);
        appPane.setMaxWidth(600);
        primaryStage.setScene(primaryScene);
        text = new Stack();
    }
    
    private void addToContainer(){
        appPane.getChildren().addAll(run);
        appPane.getChildren().addAll(metadata_scroll);
        appPane.getChildren().addAll(plot_label);
        appPane.getChildren().addAll(container);
        appPane.getChildren().addAll(choose_algorithm);
        appPane.getChildren().addAll(is_done);
        appPane.getChildren().addAll(clustering);
        appPane.getChildren().addAll(classification);
        appPane.getChildren().addAll(chart);
        appPane.getChildren().addAll(textArea);
        appPane.getChildren().addAll(displayButton);
        appPane.getChildren().addAll(label);
        appPane.getChildren().addAll(readOnly);
    }
    private void setInvisible(){
        run.setVisible(false);
        metadata_scroll.setVisible(false);
        choose_algorithm.setVisible(false);
        is_done.setVisible(false);
        clustering.setVisible(false);
        classification.setVisible(false);
        chart.setVisible(false);
        textArea.setVisible(false);
        displayButton.setVisible(false);
        label.setVisible(false);
        readOnly.setVisible(false);
        container.setVisible(false);
    }
    private void setWorkspaceActions() { //determines what every component of UI does based on action
        is_done.setOnAction((ActionEvent event) ->{ //if done button is pressed, then error check
            try{
                if (is_done.isSelected()){
                    int errorLine = ((AppData)applicationTemplate.getDataComponent()).getProcessor().returnErrorLine(textArea.getText().split("\n"));
                    if (errorLine != -1)
                        ErrorDialog.getDialog().show("Error on line "+ (errorLine+1), "Please follow the correct format.");
                    else{
                        int dupLine = ((AppData)applicationTemplate.getDataComponent()).getProcessor().isDuplicate(textArea.getText().split("\n"));
                        try{
                            ((AppData) applicationTemplate.getDataComponent()).clear();
                            textArea.setDisable(true);
                            ArrayList<Object> metadata = ((AppData)applicationTemplate.getDataComponent()).getProcessor().returnMetadata(textArea.getText());
                            String labels = ((AppData)applicationTemplate.getDataComponent()).getProcessor().printLabels(metadata);
                            metadata_text.setText("Number of instances: " + metadata.get(0) +
                                                  "\nNumber of labels: " + metadata.get(1) + 
                                                  "\nLabels: " + labels);
                            if (((AppData)applicationTemplate.getDataComponent()).getProcessor().twoLabels(textArea.getText())) //if there are exactly two labels, then enable to classification option
                                classification.setDisable(false); 
                            clustering.setDisable(false); //enable clustering
                        }
                        catch(Exception e){
                            ErrorDialog.getDialog().show("Error on line " +(dupLine+1), e.getMessage());
                        }
                    }
                }
                else{
                    textArea.setDisable(false);
                    clustering.setDisable(true);
                    classification.setDisable(true);
                    container.setVisible(false);
                    clustering.setVisible(true);
                    classification.setVisible(true);
                    run.setDisable(true);
                    run.setVisible(false);
                }
            } catch (Exception e){
                ErrorDialog.getDialog().show("Empty text area", "Please add some content to the text area");
            }
        });
        classification.setOnAction((ActionEvent e) -> {
            classification.setVisible(false); //if algorithm is chosen then allow selection of algorithm type
            clustering.setVisible(false); //same here
            random_clustering.setVisible(false);
            kmeansclustering.setVisible(false);
            random_classification.setVisible(true); //algorithm type appears
            container.setVisible(true);
            run.setVisible(true);
        });
        clustering.setOnAction((ActionEvent e) -> {
            classification.setVisible(false); //if algorithm is chosen then allow selection of algorithm type
            clustering.setVisible(false); //same here
            random_classification.setVisible(false);
            random_clustering.setVisible(true);
            kmeansclustering.setVisible(true);
            container.setVisible(true);
            run.setVisible(true);
        });
        random_classification.setOnAction((ActionEvent e) ->{
            if (random_classification.isSelected())
                run.setDisable(false);
            else
                run.setDisable(true);
        });
        kmeansclustering.setOnAction((ActionEvent e) ->{
            if (kmeansclustering.isSelected())
                run.setDisable(false);
            else
                run.setDisable(true);
        });
        random_clustering.setOnAction((ActionEvent e) ->{
            if (random_clustering.isSelected())
                run.setDisable(false);
            else
                run.setDisable(true);
        });
        runtime_config.setOnAction((ActionEvent e) -> {
            if (random_clustering.isVisible())
                cluster_window.show();
            else
                classifier_window.show();
        });
        run.setOnAction((ActionEvent e) -> {
            if (random_clustering.isSelected() && random_clustering.isVisible()){
                try{
                    RandomClusterer random = new RandomClusterer(cluster_window.getClusters(),applicationTemplate, chart, 
                                                                dataset, 
                                                                cluster_window.getIterations(), 
                                                                cluster_window.getInterval(), 
                                                                cluster_window.continuousRun());
                    Task t = new Task(){
                        @Override
                        protected Object call() throws Exception{
                            random.run();
                            return null;
                        }
                    };
                    new Thread(t).start();
                }
                catch (Exception ex){
                    cluster_window.show();
                }
            }
            else if (kmeansclustering.isVisible() && kmeansclustering.isSelected()){
                try{
                    KMeansClusterer kmeans = new KMeansClusterer(applicationTemplate, chart, dataset, 
                                                                 cluster_window.getIterations(), 
                                                                 cluster_window.getInterval(), 
                                                                 cluster_window.getClusters(),
                                                                 cluster_window.continuousRun());
                    Task t = new Task(){
                        @Override
                        protected Object call() throws Exception{
                            kmeans.run();
                            return null;
                        }
                    };
                    new Thread(t).start();
                }
                catch(Exception ex){
                    cluster_window.show();
                }
            }
            else{
                try{
                    RandomClassifier random = new RandomClassifier(dataset, classifier_window.getIterations(), classifier_window.getInterval(), classifier_window.continuousRun(), applicationTemplate, chart);
                    Task t = new Task(){
                        @Override
                        protected Object call() throws Exception {
                            random.run();
                            return null;
                        }
                    };
                    new Thread(t).start();  
                }
                catch(NumberFormatException ex){
                    classifier_window.show();
                }
            }
        });
        textArea.setOnKeyTyped((KeyEvent event) -> { //if anything is typed, enable new button
            if (event.getCode().equals(KeyCode.BACK_SPACE)){
                change --; 
                if (textArea.getText().isEmpty()){
                    hasNewText = false; 
                    disableSaveButton();
                    disableNewButton();
                }
                else{
                    if (change == 0){
                        hasNewText = false; 
                        disableSaveButton();
                    }
                    else{
                        hasNewText = true;
                        enableSaveButton();
                        enableNewButton();
                    }
                }
            }
            else{
                change ++; 
                if (change == 0){
                    disableSaveButton();
                }
                else{
                    enableSaveButton();
                    enableNewButton();
                }
            }
        });
        
        textArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (textArea.getText().split("\n").length > 10 && loadedData){
                displayButton.fire();
                ErrorDialog.getDialog().show("Too many lines", "Loaded " + textArea.getText().split("\n").length + " lines, showing only 10.");
                for (int i = textArea.getText().split("\n").length-1; i > 9; i--){
                    text.push(textArea.getText().split("\n")[i]);
                }
                deleteLines();
                loadedData = false; //reset loaded data 
            }
            else if (textArea.getText().split("\n").length < 10 ){
                if (!text.empty())
                    textArea.appendText((String) text.pop() + "\n");
            }
        });
        
        displayButton.setOnAction((ActionEvent event) -> { //displays content
            if (textArea.getText().isEmpty()){
                ((AppData) applicationTemplate.getDataComponent()).clear();
                scrnshotButton.setDisable(true);
            }
            else{
                try {
                    ((AppData) applicationTemplate.getDataComponent()).clear();
                    ((AppData) applicationTemplate.getDataComponent()).getProcessor().processString(textArea.getText());
                    ((AppData) applicationTemplate.getDataComponent()).displayData();
                    scrnshotButton.setDisable(false);
                }
                catch (Exception ex) {
                    if (ex.getMessage().contains("ArrayIndexOutOfBoundsException"))
                        ErrorDialog.getDialog().show("Error on line " + (((AppData)applicationTemplate.getDataComponent()).getProcessor().returnErrorLine(textArea.getText().split("\n"))+1), "Please follow correct format.");
                    else if (ex.getMessage().contains("Duplicate name"))
                        ErrorDialog.getDialog().show("Error on line " + (((AppData)applicationTemplate.getDataComponent()).getProcessor().isDuplicate(textArea.getText().split("\n"))+1), ex.getMessage());
                    else
                        ErrorDialog.getDialog().show("Error on line " + (((AppData)applicationTemplate.getDataComponent()).getProcessor().returnErrorLine(textArea.getText().split("\n")) +1), ex.getMessage());
                }      
            }
        });
        readOnly.setOnAction((ActionEvent event) ->{ //is readonly is checked, then disable textarea
            if (readOnly.isSelected())
                textArea.setDisable(true);
            else
                textArea.setDisable(false);
        });
    }

    private void deleteLines() throws ArrayIndexOutOfBoundsException
    {
        StringBuilder str = new StringBuilder();
        String t = textArea.getText(); 
        str.append(t);
        String[] lines = textArea.getText().split("\n");
        str.delete(str.indexOf(lines[10])-1, str.length());
        textArea.setText(str.toString());
    }
    
    public void addRemainingComponents()
    {
        started = true;
        metadata_scroll.setVisible(true);
        choose_algorithm.setVisible(true);
        is_done.setVisible(true);
        clustering.setVisible(true);
        classification.setVisible(true);
        chart.setVisible(true);
        textArea.setVisible(true);
        displayButton.setVisible(true);
        label.setVisible(true);
        readOnly.setVisible(true);
    }
    
}
