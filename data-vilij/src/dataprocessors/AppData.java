//Omar Syed
//110484590
package dataprocessors;


import actions.AppActions;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import vilij.components.ErrorDialog;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        String text = ""; 
        try {
            Scanner scanner = new Scanner(dataFilePath);
            while (scanner.hasNextLine())
                text += scanner.nextLine() + "\n";
            loadData(text);
            ((AppUI) applicationTemplate.getUIComponent()).setDataset(dataFilePath);
        } catch (IOException ex) {
            Logger.getLogger(AppData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadData(String dataString) { //checks whether loaded string is valid
        String[] lines = dataString.split("\n");
        int lineError = processor.returnErrorLine(lines);
        if (lineError != -1)
            ErrorDialog.getDialog().show("Invalid data", "Line " + (lineError+1) + " is not formatted correctly. "
                        + "Please follow the correct format: @name TAB label_name TAB data_point");
        else{
            int duplicate = processor.isDuplicate(lines);
            if (duplicate != -1)
                ErrorDialog.getDialog().show("Invalid data", "Line " + (duplicate+1) + " contains"
                            + " duplicate name");
            else{
                if (lines.length > 10)
                    ((AppUI)applicationTemplate.getUIComponent()).setLoadData(true);
                ((AppUI)applicationTemplate.getUIComponent()).getStack().removeAllElements();
                ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setText(dataString);
                ArrayList<Object> objects = processor.returnMetadata(dataString);
                ((AppUI)applicationTemplate.getUIComponent()).getMetadata().setText("Number of instances: "
                        + lines.length + 
                        "\nNumber of Labels: " +
                        (int)objects.get(1)+ "\nLabels: " + processor.printLabels(objects)+
                        "\nPath: "+ ((AppActions)applicationTemplate.getActionComponent()).getDataFile().toString());
            }   
        }    
    }

    @Override
    public void saveData(Path dataFilePath) {
        try{
            if (dataFilePath != null){ //if the file has already been saved before
                String[] text = ((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText().split("\n");
                File overwrite = new File(dataFilePath.toString());
                FileWriter file_writer = new FileWriter(overwrite);
                try (PrintWriter writer = new PrintWriter(file_writer)) {
                    for (int i = 0; i < text.length; i ++){
                        if (i == text.length-1)
                            writer.write(text[i]);
                        else
                            writer.write(text[i]+ System.getProperty("line.separator"));
                    }
                    writer.close();
                }
            }
            else{
                int lineError = processor.returnErrorLine(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText().split("\n"));
                int duplicateError;
                if (lineError != -1)
                    ErrorDialog.getDialog().show("Invalid data", "Line " + (lineError+1) + " is not formatted correctly. "
                            + "Please follow the correct format: @name TAB label_name TAB data_point");
                else{
                    duplicateError = processor.isDuplicate(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText().split("\n"));
                    if (duplicateError != -1)
                        ErrorDialog.getDialog().show("Invalid data", "Line " + (duplicateError+1) + " contains"
                                + " duplicate name");
                    else{
                        String[] text = ((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText().split("\n");
                        FileChooser save = new FileChooser();
                        save.setTitle("Save work");
                        save.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tab-Separated Data File", ".*.tsd"));
                        save.setInitialFileName("Untitled.tsd");
                        File saveDialog = save.showSaveDialog(new Stage());
                        FileWriter file_writer = new FileWriter(saveDialog);
                        try (PrintWriter writer = new PrintWriter(file_writer)) {
                            for (int i = 0; i < text.length; i ++){
                                if (i == text.length-1)
                                    writer.write(text[i]);
                                else
                                    writer.write(text[i] + System.getProperty("line.separator"));
                            }
                            writer.close();
                        }
                        dataFilePath = saveDialog.toPath();
                        ((AppUI)applicationTemplate.getUIComponent()).disableSaveButton();
                    }
                }
            }
        }
        catch(Exception e){
            ErrorDialog.getDialog().show("Save Error", "There was a problem with saving your file, please try again.");
        }
    }
    
    
    public TSDProcessor getProcessor(){     return processor;   } // added
    
    @Override
    public void clear() {
        processor.clear(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
}
