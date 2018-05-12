//Omar Syed
//110484590

package dataprocessors;

import algorithms.DataSet;
import classification.RandomClassifier;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import ui.AppUI;
import vilij.components.ErrorDialog;
import vilij.templates.ApplicationTemplate;


/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
        
    }
    String   name  = "";
    //added
    public static class DuplicateDataNameException extends Exception{
        private static final String ERR_MSG = "Cannot use the same name more than once.";
        
        public DuplicateDataNameException(String name){
           super(String.format("Duplicate name '%s'. " + ERR_MSG, name)); 
        }
    }
    
    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private ReentrantLock        lock = new ReentrantLock(); 

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try 
                  {
                      name  = checkedname(list.get(0));//changed
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      if (dataLabels.containsKey(name)) // added
                          throw new DuplicateDataNameException(name);
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);   
                  }
                  catch (Exception e) 
                  {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) 
        {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> 
            {
                Point2D point = dataPoints.get(entry.getKey());
                XYChart.Data data = new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(data);
            });
            chart.getData().add(series);
        }
    }

    public void clear(XYChart<Number, Number> chart) {
        dataPoints.clear();
        dataLabels.clear();
        chart.getData().clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
    
    public int returnErrorLine(String[] input){ // return the line that is not formatted correctly
        for (int i = 0; i < input.length; i++){
            String[] line = input[i].split("\t");
            if (line[0].charAt(0) != '@')
                return i;
            else if (line.length != 3)
                return i;
            else if (!isOrderedPair(line[2]))
                return i; 
        }
        return -1;
    }
    
    public boolean isOrderedPair(String s){ //checks whether string is a valid ordered pair
        String[] ordered_pair = s.split(",");
        return ordered_pair.length == 2 && isNumber(ordered_pair[0]) && isNumber(ordered_pair[1]);
    }
    
    public boolean isNumber(String s){ //makes sure that coordinates contain numbers
        String[] decimal; 
        if (s.contains(".")){
            decimal = s.split(".");
            if (decimal.length > 2)
                return false;
            for (int i = 0; i < decimal.length; i++){
                for (int j = 0; j < decimal[i].length(); j++)
                    if (!Character.isDigit(s.charAt(i)))
                        return false;
            }
            return true;
        }
        else{
            for (int i = 0; i < s.length(); i++)
                if (!Character.isDigit(s.charAt(i)))
                    return false;
            return true;
        }
    }
    
    public int isDuplicate(String[] lines){ //checks for duplicate names
        HashMap<String, String> dup_check = new HashMap<>();
        for (int i = 0; i < lines.length; i ++){
            String[] line = lines[i].split("\t");
            if (dup_check.containsKey(line[0]))
                return i;
            dup_check.put(line[0], line[2]);
        }
        return -1;
    }
 
    public ArrayList<Object> getLabels(String[] lines){ //returns both number of labels and label names
        ArrayList<Object> objects = new ArrayList<>(); 
        int num_labels = 0; 
        objects.add(num_labels); 
        ArrayList<String> labels = new ArrayList<>();
        for (String line1 : lines) {
            String[] line = line1.split("\t");
            if (!contains(labels, line[1])){
                num_labels ++;
                objects.set(0, num_labels);
                objects.add(line[1]);
                labels.add(line[1]);
            }
        }
        return objects; 
    }
    
    public String printLabels(ArrayList<Object> labels){
        String list = "";
        for (int i = 2; i < labels.size(); i++)
            list += labels.get(i) + " "; 
        return list; 
    }
    
    public boolean contains(ArrayList<String> labels, String label){
        for (int i = 0; i < labels.size(); i++){
            if (label.equals(labels.get(i)))
                return true;
        }
        return false;
    }
    
    public boolean twoLabels(String text){ //check whether there are exactly two labels in the data
        String[] lines = text.split("\n");
        HashMap<String, String> labels = new HashMap<>();
        for (int i = 0; i < lines.length; i ++)
        {
            String[] line = lines[i].split("\t");
            if (!labels.containsKey(line[1]))
                labels.put(line[1], i+"");
        }
        return labels.size() == 2; 
    }
    
    public ArrayList<Object> returnMetadata(String text){
        int num_labels = 0; 
        String[] lines = text.split("\n");
        int num_instances = lines.length; 
        ArrayList<Object> metadata = new ArrayList<>();
        metadata.add(num_instances);
        metadata.add(num_labels);
        HashMap<String, String> objects = new HashMap<>();
        for (String line:lines){
            String[] line1 = line.split("\t");
            if (!objects.containsKey(line1[1])){
                objects.put(line1[1], line1[2]);
                num_labels++; 
                metadata.set(1, num_labels); //update num_labels
                metadata.add(line1[1]); //add new label
            }
        }
        return metadata; // 0 : num_instances, 1 : num_labels, ...
    }
    
    public void makeLine(ApplicationTemplate appTemplate, XYChart chart, RandomClassifier random, DataSet dataset){         lock.lock();
        try{
            lock.lock();
            clear(chart);
            try {
                processString(((AppUI)appTemplate.getUIComponent()).getTextArea().getText());
            } catch (Exception ex) {
                Logger.getLogger(TSDProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            toChartData(chart);
             Object[] points = dataset.getLocations().values().toArray();
            double max_x = 0;
            double min_x = ((Point2D)points[0]).getX();
            double max_y = ((Point2D)points[0]).getY();
            double min_y = ((Point2D)points[0]).getY();
            for (Object point : points) {
                if (((Point2D)point).getX() > max_x)
                    max_x = ((Point2D)point).getX();
                if (((Point2D)point).getX() < min_x)
                    min_x = ((Point2D)point).getX();
                if (((Point2D)point).getY() > max_y)
                    max_y = ((Point2D)point).getY();
            }
            List<Integer> output = random.getOutput(); // 0 = xCoefficient, 1 = yCoefficient, 2 = constant
            XYChart.Series line = new XYChart.Series<>();
            if (output.get(0) == 0 && output.get(1) == 0)
                ErrorDialog.getDialog().show("Degenerate line", "Degenerate line produced by classification algorithm.");
            else if (output.get(0) == 0){
                double y = (output.get(2))/output.get(1);
                Point2D point1 = new Point2D(min_x, y);
                Point2D point2 = new Point2D(max_x, y);
                XYChart.Data data1 = new XYChart.Data<>(point1.getX(), point1.getY());
                XYChart.Data data2 = new XYChart.Data<>(point2.getX(), point2.getY());
                line.getData().add(data1);
                line.getData().add(data2);
                chart.getData().add(line);
                line.getNode().setId("avg-line");
                data1.getNode().setVisible(false);
                data2.getNode().setVisible(false);
            }
            else if (output.get(1) == 0){
                double x = output.get(2)/output.get(0);
                Point2D point1 = new Point2D(x, min_y);
                Point2D point2 = new Point2D(x, max_y);
                XYChart.Data data1 = new XYChart.Data<>(point1.getX(), point1.getY());
                XYChart.Data data2 = new XYChart.Data<>(point2.getX(), point2.getY());
                line.getData().add(data1);
                line.getData().add(data2);
                chart.getData().add(line);
                line.getNode().setId("avg-line");
                data1.getNode().setVisible(false);
                data2.getNode().setVisible(false);
            }
            else{
                double y1 = (output.get(0)*min_x - output.get(2))/ output.get(1);
                double y2 = (output.get(0)*max_x - output.get(2))/ output.get(1);
                Point2D point1 = new Point2D(min_x, y1);
                Point2D point2 = new Point2D(max_x, y2);
                XYChart.Data data1 = new XYChart.Data<>(point1.getX(),point1.getY());
                XYChart.Data data2 = new XYChart.Data<>(point2.getX(),point2.getY());                    
                line.getData().add(data1);
                line.getData().add(data2);
                chart.getData().add(line);
                line.getNode().setId("avg-line");
                data1.getNode().setVisible(false);
                data2.getNode().setVisible(false);
            }
        }
        finally{
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }
    
    public void makeClusters(ApplicationTemplate appTemplate,XYChart chart, DataSet dataset){
        try{
            lock.lock();
            chart.getData().clear();
            dataLabels = dataset.getLabels();
            dataPoints = dataset.getLocations();
            toChartData(chart);
        }
        finally{
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }
}
