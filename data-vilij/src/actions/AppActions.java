//Omar Syed
//110484590
package actions;


import java.io.File;
import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;
import java.io.IOException;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import ui.AppUI;
import dataprocessors.AppData; 
import java.nio.file.Paths;
import vilij.components.ConfirmationDialog;
import vilij.components.ConfirmationDialog.Option;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;


/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;
    /** Path to the data file currently active. */
    Path dataFilePath;
    /** Stage for new file save and open menu */
    Stage newFileStage;
    /** Checks whether current dataPath was saved once already 
    boolean already_saved = false; */
    boolean started = false;
    
    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        try {
            if (!started){
                started = true;
                ((AppUI)applicationTemplate.getUIComponent()).addRemainingComponents();
                return;
            }
            ((AppUI)applicationTemplate.getUIComponent()).setChange(0);
            boolean option = promptToSave();
            if (option){
                Option choice = ((ConfirmationDialog)applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION)).getSelectedOption();
                if (choice.equals(Option.YES)){
                    try{
                        applicationTemplate.getUIComponent().clear();
                        dataFilePath = null;
                    }
                    catch(Exception e){
                        ErrorDialog exited = ErrorDialog.getDialog();
                        exited.show("Exited save menu", "Your worksheet was not saved.");
                        dataFilePath = null; 
                    }
                }
                else{
                    applicationTemplate.getUIComponent().clear(); //clear the chart
                    dataFilePath = null; 
                }
            }
        } catch (IOException ex) {
            ErrorDialog error = ErrorDialog.getDialog();
            error.show("Invalid option", "Please click cancel if you wish to exit the dialog. Otherwise"
                    + " click no if you wish to leave your work unsaved or yes if you wish to save your"
                    + " work.");
        }
    }

    @Override
    public void handleSaveRequest() {
        AppData data = new AppData(applicationTemplate);
        ((AppUI)applicationTemplate.getUIComponent()).setChange(0);
        data.saveData(dataFilePath);
    }

    @Override
    public void handleLoadRequest() {
        try{
            ((AppUI)applicationTemplate.getUIComponent()).setChange(0);
            if (!started){ //if application was started for the first time, hide all of UI elements
                started = true;
                ((AppUI)applicationTemplate.getUIComponent()).addRemainingComponents();
            }
            FileChooser open = new FileChooser(); 
            Stage fileStage = new Stage();
            File opened = open.showOpenDialog(fileStage);
            dataFilePath = Paths.get(opened.getAbsolutePath());
            AppData data = new AppData(applicationTemplate); 
            data.loadData(dataFilePath);
        } catch (Exception ex){
            ErrorDialog.getDialog().show("Open file error", "There was an error opening the selected file. Please try again.");
        }
    }

    @Override
    public void handleExitRequest() {
        handleNewRequest();
        Platform.exit();
    }


    public void handleScreenshotRequest() throws IOException {
        FileChooser save = new FileChooser();
        save.setTitle("Save Chart");
        save.setInitialFileName("Untitled");
        save.getExtensionFilters().addAll(new ExtensionFilter("PNG", "*.png"));
        File savedImage = save.showSaveDialog(newFileStage);
        if (savedImage != null){
            WritableImage image = ((AppUI)applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(),null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", savedImage);        
        }

    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION).show("Save work?", 
                "Would you like to save your current work?");
        return ((ConfirmationDialog)applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION)).getSelectedOption().equals(Option.YES) 
                || ((ConfirmationDialog)applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION)).getSelectedOption().equals(Option.NO);
    }
    
    public Path getDataFile(){
        return dataFilePath; 
    }

    @Override
    public void handlePrintRequest() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
}

