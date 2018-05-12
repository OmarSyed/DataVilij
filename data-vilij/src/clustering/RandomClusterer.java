/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

import algorithms.DataSet;
import classification.RandomClassifier;
import dataprocessors.AppData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author omars_000
 */
public class RandomClusterer extends Clusterer{
    private ApplicationTemplate applicationTemplate;
    private XYChart             chart;        
    private static final Random RAND = new Random();
    private List<Point2D> centroids;
    @SuppressWarnings("FieldCanBeLocal")
    private DataSet             dataset;
    private final  int          maxIterations; 
    private final  int          updateInterval;
    private final  AtomicBoolean tocontinue;
    private ReentrantLock        lock = new ReentrantLock(); 
    
    
    
    public RandomClusterer(int k, ApplicationTemplate applicationTemplate, XYChart chart,
            DataSet dataset, int maxIterations, int updateInterval,
            boolean tocontinue) {
        super(k);
        this.applicationTemplate = applicationTemplate;
        this.chart = chart;
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

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

    @Override
    public void run() {
        initializeCentroids();
        int iteration = 0;
        while (iteration++ < maxIterations & tocontinue.get()) {
            assignLabels();
            recomputeCentroids();
            Platform.runLater(()->{
                ((AppUI) applicationTemplate.getUIComponent()).toggleRunButton(true);
                ((AppUI) applicationTemplate.getUIComponent()).toggleScreenshot(true);
                ((AppData)applicationTemplate.getDataComponent()).getProcessor().makeClusters(applicationTemplate, chart, dataset);
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Platform.runLater(()->{
                ((AppUI) applicationTemplate.getUIComponent()).toggleRunButton(false);
                ((AppUI) applicationTemplate.getUIComponent()).toggleScreenshot(false);
        });
        //add noncontinuous run 
        if (iteration < maxIterations & !tocontinue.get()){
            assignLabels();
            recomputeCentroids();
            Platform.runLater(()->{
            //apply physical changes to the chart
                ((AppUI) applicationTemplate.getUIComponent()).toggleRunButton(true);
                ((AppUI) applicationTemplate.getUIComponent()).toggleScreenshot(true);
                ((AppData)applicationTemplate.getDataComponent()).getProcessor().makeClusters(applicationTemplate, chart, dataset);
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            Platform.runLater(()->{
                ((AppUI) applicationTemplate.getUIComponent()).toggleRunButton(false);
                ((AppUI) applicationTemplate.getUIComponent()).toggleScreenshot(false);
            });
        }
    }
    
    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                i = (++i % instanceNames.size());
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            int random = new Double(RAND.nextDouble() * numberOfClusters).intValue();
            dataset.getLabels().put(instanceName, Integer.toString(random));
        });
    }

    private void recomputeCentroids() {
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                                 .entrySet()
                                 .stream()
                                 .filter(entry -> i == Integer.parseInt(entry.getValue()))
                                 .map(entry -> dataset.getLocations().get(entry.getKey()))
                                 .reduce(new Point2D(0, 0), (p, q) -> {
                                     clusterSize.incrementAndGet();
                                     return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                                 });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }
    
}
