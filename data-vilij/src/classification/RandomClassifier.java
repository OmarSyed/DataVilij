package classification;


import algorithms.DataSet;
import dataprocessors.AppData;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private ApplicationTemplate applicationTemplate; 
    private XYChart             chart;
    private static final Random RAND = new Random();
    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;
    private static int j = 1; 
    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;
    private ReentrantLock       lock = new ReentrantLock();

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue, ApplicationTemplate appTemplate, XYChart chart) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.chart = chart;
        applicationTemplate = appTemplate; 
    }

    @Override
    public void run() {
        for (int i = 1; i <= maxIterations && tocontinue(); i++) {
            lock.lock();
            int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int constant     = new Double(RAND.nextDouble() * 100).intValue();
            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            if (i % updateInterval == 0 || i == maxIterations)
                Platform.runLater(() -> {
                    ((AppUI) applicationTemplate.getUIComponent()).toggleRunButton(true);
                    ((AppUI) applicationTemplate.getUIComponent()).toggleScreenshot(true);
                    ((AppData) applicationTemplate.getDataComponent()).getProcessor().makeLine(applicationTemplate, chart, this, dataset);
                });
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Platform.runLater(() ->{
            ((AppUI) applicationTemplate.getUIComponent()).toggleRunButton(false);
            ((AppUI) applicationTemplate.getUIComponent()).toggleScreenshot(false);
        });
        if (!tocontinue() && j < maxIterations) {
                int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                int constant     = new Double(RAND.nextDouble() * 100).intValue();
                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);
                j++;
                Platform.runLater(() -> {
                    ((AppUI) applicationTemplate.getUIComponent()).toggleRunButton(true);
                    ((AppUI) applicationTemplate.getUIComponent()).toggleScreenshot(true);
                    ((AppData) applicationTemplate.getDataComponent()).getProcessor().makeLine(applicationTemplate, chart, this, dataset);
                    
                });
                try {
                    Thread.sleep(500);
                } 
                catch (InterruptedException ex) {
                    Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
                }
                Platform.runLater(() ->{
                        ((AppUI) applicationTemplate.getUIComponent()).toggleRunButton(false);
                        ((AppUI) applicationTemplate.getUIComponent()).toggleScreenshot(false);
                });
        }
    }
    

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("/path/to/some-data.tsd"));
        //RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
        //classifier.run(); // no multithreading yet
    }
}