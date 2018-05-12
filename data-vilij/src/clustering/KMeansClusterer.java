/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

/**
 *
 * @author omars_000
 */
import algorithms.DataSet;
import classification.RandomClassifier;
import dataprocessors.AppData;
import javafx.geometry.Point2D;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {
    private ApplicationTemplate appTemplate;
    private XYChart             chart;
    private DataSet       dataset;
    private List<Point2D> centroids;
    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;


    public KMeansClusterer(ApplicationTemplate appTemplate, XYChart chart, DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean tocontinue) {
        super(numberOfClusters);
        this.appTemplate = appTemplate;
        this.chart = chart;
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    @Override
    public void run() {
        initializeCentroids();
        int iteration = 0;
        while (iteration++ < maxIterations & tocontinue.get()) {
            assignLabels();
            recomputeCentroids();
            Platform.runLater(()->{
                ((AppUI) appTemplate.getUIComponent()).toggleRunButton(true);
                ((AppUI) appTemplate.getUIComponent()).toggleScreenshot(true);
                ((AppData)appTemplate.getDataComponent()).getProcessor().makeClusters(appTemplate, chart, dataset);
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Platform.runLater(()->{
                ((AppUI) appTemplate.getUIComponent()).toggleRunButton(false);
                ((AppUI) appTemplate.getUIComponent()).toggleScreenshot(false);
        });
        //add noncontinuous run 
        if (iteration < maxIterations & !tocontinue.get()){
            assignLabels();
            recomputeCentroids();
            Platform.runLater(()->{
            //apply physical changes to the chart
                ((AppUI) appTemplate.getUIComponent()).toggleRunButton(true);
                ((AppUI) appTemplate.getUIComponent()).toggleScreenshot(true);
                ((AppData)appTemplate.getDataComponent()).getProcessor().makeClusters(appTemplate, chart, dataset);
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            Platform.runLater(()->{
                ((AppUI) appTemplate.getUIComponent()).toggleRunButton(false);
                ((AppUI) appTemplate.getUIComponent()).toggleScreenshot(false);
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
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
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