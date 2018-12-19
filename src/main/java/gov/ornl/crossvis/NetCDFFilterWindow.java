package gov.ornl.crossvis;

import gov.ornl.datatable.*;
import gov.ornl.geoview.MapView;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NetCDFFilterWindow extends Application {
    private final static Logger log = Logger.getLogger(NetCDFFilterWindow.class.getName());

    private NetcdfFile ncFile;

    private ListView<String> variableListView;

    private MapView mapView;

    private SpinnerValueFactory<String> latMinSpinnerValueFactory;
    private SpinnerValueFactory<String> latMaxSpinnerValueFactory;
    private Spinner<String> latMinSpinner;
    private Spinner<String> latMaxSpinner;
    private ArrayDouble.D1 latValuesArray;
    private ObservableList<String> latValuesList = FXCollections.observableArrayList();
    
    private SpinnerValueFactory<String> lonMinSpinnerValueFactory;
    private SpinnerValueFactory<String> lonMaxSpinnerValueFactory;
    private Spinner<String> lonMinSpinner;
    private Spinner<String> lonMaxSpinner;
    private ArrayDouble.D1 lonValuesArray;
    private ObservableList<String> lonValuesList = FXCollections.observableArrayList();
    
    private SpinnerValueFactory.IntegerSpinnerValueFactory ensembleMinSpinnerValueFactory;
    private SpinnerValueFactory.IntegerSpinnerValueFactory ensembleMaxSpinnerValueFactory;
    private Spinner<Integer> ensembleMinSpinner;
    private Spinner<Integer> ensembleMaxSpinner;

    private SpinnerValueFactory.IntegerSpinnerValueFactory pftMinSpinnerValueFactory;
    private SpinnerValueFactory.IntegerSpinnerValueFactory pftMaxSpinnerValueFactory;
    private Spinner<Integer> pftMinSpinner;
    private Spinner<Integer> pftMaxSpinner;

    private SpinnerValueFactory<String> timeMinSpinnerValueFactory;
    private SpinnerValueFactory<String> timeMaxSpinnerValueFactory;
    private Spinner<String> timeMinSpinner;
    private Spinner<String> timeMaxSpinner;
    private ArrayDouble.D1 timeValuesArray;
    private ObservableList<String> timeValuesList = FXCollections.observableArrayList();
    
    private TextField fileNameTextField;
    private Button fileChooserButton;

    private ObservableList<String> variableNames;

    private DataTable dataTable;

    public NetCDFFilterWindow () { }

    public NetCDFFilterWindow (DataTable dataTable) {
        this.dataTable = dataTable;
    }

    private ListView<String> createVariableListView() {
        variableNames = FXCollections.observableArrayList();
        ListView<String> variableNameListView = new ListView<>(variableNames);
        variableNameListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return variableNameListView;
    }

    private Pane createDimensionFilterPane() {
        GridPane dimensionFilterGridPane = new GridPane();
        dimensionFilterGridPane.setHgap(4);
        dimensionFilterGridPane.setVgap(4);
        ColumnConstraints column1Constraints = new ColumnConstraints(100, 100, 140, Priority.ALWAYS, HPos.RIGHT, true);
        ColumnConstraints column2Constraints = new ColumnConstraints(80, 100, 140, Priority.ALWAYS, HPos.LEFT, true);
        ColumnConstraints column3Constraints = new ColumnConstraints(100, 100, 140, Priority.ALWAYS, HPos.RIGHT, true);
        ColumnConstraints column4Constraints = new ColumnConstraints(80, 100, 140, Priority.ALWAYS, HPos.LEFT, true);
        dimensionFilterGridPane.getColumnConstraints().addAll(column1Constraints, column2Constraints, column3Constraints, column4Constraints);
//
//        latMinSpinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.0);
//        latMaxSpinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 1.0);
//        latMinSpinnerValueFactory.valueProperty().addListener(observable -> {
//            latMaxSpinnerValueFactory.setMin(latMinSpinnerValueFactory.getValue());
//        });
//        latMaxSpinnerValueFactory.valueProperty().addListener(observable -> {
//            latMinSpinnerValueFactory.setMax(latMaxSpinnerValueFactory.getValue());
//        });
//        latMinSpinner = new Spinner<>(latMinSpinnerValueFactory);
//        latMaxSpinner = new Spinner<>(latMaxSpinnerValueFactory);
//        latMinSpinner.setEditable(true);
//        latMaxSpinner.setEditable(true);
//
//        lonMinSpinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.0);
//        lonMaxSpinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 1.0);
//        lonMinSpinnerValueFactory.valueProperty().addListener(observable -> {
//            lonMaxSpinnerValueFactory.setMin(lonMinSpinnerValueFactory.getValue());
//        });
//        lonMaxSpinnerValueFactory.valueProperty().addListener(observable -> {
//            lonMinSpinnerValueFactory.setMax(lonMaxSpinnerValueFactory.getValue());
//        });
//        lonMinSpinner = new Spinner<>(lonMinSpinnerValueFactory);
//        lonMaxSpinner = new Spinner<>(lonMaxSpinnerValueFactory);
//        lonMinSpinner.setEditable(true);
//        lonMaxSpinner.setEditable(true);

        ensembleMinSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,1, 0);
        ensembleMaxSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1, 1);
        ensembleMinSpinnerValueFactory.valueProperty().addListener(observable -> {
            ensembleMaxSpinnerValueFactory.setMin(ensembleMinSpinnerValueFactory.getValue());
        });
        ensembleMaxSpinnerValueFactory.valueProperty().addListener(observable -> {
            ensembleMinSpinnerValueFactory.setMax(ensembleMaxSpinnerValueFactory.getValue());
        });
        ensembleMinSpinner = new Spinner<>(ensembleMinSpinnerValueFactory);
        ensembleMaxSpinner = new Spinner<>(ensembleMaxSpinnerValueFactory);
        ensembleMinSpinner.setEditable(true);
        ensembleMaxSpinner.setEditable(true);

        pftMinSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,1, 0);
        pftMaxSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1, 1);
        pftMinSpinnerValueFactory.valueProperty().addListener(observable -> {
            pftMaxSpinnerValueFactory.setMin(pftMinSpinnerValueFactory.getValue());
        });
        pftMaxSpinnerValueFactory.valueProperty().addListener(observable -> {
            pftMinSpinnerValueFactory.setMax(pftMaxSpinnerValueFactory.getValue());
        });
        pftMinSpinner = new Spinner<>(pftMinSpinnerValueFactory);
        pftMaxSpinner = new Spinner<>(pftMaxSpinnerValueFactory);
        pftMinSpinner.setEditable(true);
        pftMaxSpinner.setEditable(true);

        timeMinSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(timeValuesList);
        timeMaxSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(timeValuesList);
        timeMinSpinnerValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {;
            if (oldValue != null) {
                int minSpinnerValueIdx = timeValuesList.indexOf(newValue);
                int maxSpinnerValueIdx = timeValuesList.indexOf(timeMaxSpinnerValueFactory.getValue());
                if (minSpinnerValueIdx > maxSpinnerValueIdx) {
                    timeMinSpinnerValueFactory.setValue(timeValuesList.get(maxSpinnerValueIdx));
                }
            }
        });
        timeMaxSpinnerValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {;
            if (oldValue != null) {
                int maxSpinnerValueIdx = timeValuesList.indexOf(newValue);
                int minSpinnerValueIdx = timeValuesList.indexOf(timeMinSpinnerValueFactory.getValue());
                if (maxSpinnerValueIdx < minSpinnerValueIdx) {
                    timeMaxSpinnerValueFactory.setValue(timeValuesList.get(minSpinnerValueIdx));
                }
            }
        });
        
        timeMinSpinner = new Spinner<>(timeMinSpinnerValueFactory);
        timeMinSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newValueIndex = timeValuesList.indexOf(newValue);
            if (newValueIndex == -1) {
                // find nearest value
                try {
                    double newValueDouble = Double.parseDouble(newValue);
                    int nearestTimeValueIndex = findNearestTimeValueIndex(newValueDouble);
                    if (nearestTimeValueIndex != -1) {
                        timeMinSpinnerValueFactory.setValue(timeValuesList.get(nearestTimeValueIndex));
                    } else {
                        timeMinSpinnerValueFactory.setValue(oldValue);
                    }
                } catch (NumberFormatException ex) {
                    timeMinSpinnerValueFactory.setValue(oldValue);
                }
            }
        });

        timeMaxSpinner = new Spinner<>(timeMaxSpinnerValueFactory);
        timeMaxSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newValueIndex = timeValuesList.indexOf(newValue);
            if (newValueIndex == -1) {
                // find nearest value
                try {
                    double newValueDouble = Double.parseDouble(newValue);
                    int nearestTimeValueIndex = findNearestTimeValueIndex(newValueDouble);
                    if (nearestTimeValueIndex != -1) {
                        timeMaxSpinnerValueFactory.setValue(timeValuesList.get(nearestTimeValueIndex));
                    } else {
                        timeMaxSpinnerValueFactory.setValue(oldValue);
                    }
                } catch (NumberFormatException ex) {
                    timeMaxSpinnerValueFactory.setValue(oldValue);
                }
            }
        });
        timeMinSpinner.setEditable(true);
        timeMaxSpinner.setEditable(true);

        latMinSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(latValuesList);
        latMaxSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(latValuesList);
        latMinSpinnerValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {;
            if (oldValue != null) {
                int minSpinnerValueIdx = latValuesList.indexOf(newValue);
                int maxSpinnerValueIdx = latValuesList.indexOf(latMaxSpinnerValueFactory.getValue());
                if (minSpinnerValueIdx > maxSpinnerValueIdx) {
                    latMinSpinnerValueFactory.setValue(latValuesList.get(maxSpinnerValueIdx));
                }
                updateMapViewSelection();
            }
        });
        latMaxSpinnerValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {;
            if (oldValue != null) {
                int maxSpinnerValueIdx = latValuesList.indexOf(newValue);
                int minSpinnerValueIdx = latValuesList.indexOf(latMinSpinnerValueFactory.getValue());
                if (maxSpinnerValueIdx < minSpinnerValueIdx) {
                    latMaxSpinnerValueFactory.setValue(latValuesList.get(minSpinnerValueIdx));
                }
                updateMapViewSelection();
            }
        });

        latMinSpinner = new Spinner<>(latMinSpinnerValueFactory);
        latMinSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newValueIndex = latValuesList.indexOf(newValue);
            if (newValueIndex == -1) {
                // find nearest value
                try {
                    double newValueDouble = Double.parseDouble(newValue);
                    int nearestLatValueIndex = findNearestLatValueIndex(newValueDouble);
                    if (nearestLatValueIndex != -1) {
                        latMinSpinnerValueFactory.setValue(latValuesList.get(nearestLatValueIndex));
//                        updateMapViewSelection();
                    } else {
                        latMinSpinnerValueFactory.setValue(oldValue);
                    }
                } catch (NumberFormatException ex) {
                    latMinSpinnerValueFactory.setValue(oldValue);
                }
            }
        });

        latMaxSpinner = new Spinner<>(latMaxSpinnerValueFactory);
        latMaxSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newValueIndex = latValuesList.indexOf(newValue);
            if (newValueIndex == -1) {
                // find nearest value
                try {
                    double newValueDouble = Double.parseDouble(newValue);
                    int nearestLatValueIndex = findNearestLatValueIndex(newValueDouble);
                    if (nearestLatValueIndex != -1) {
                        latMaxSpinnerValueFactory.setValue(latValuesList.get(nearestLatValueIndex));
//                        updateMapViewSelection();
                    } else {
                        latMaxSpinnerValueFactory.setValue(oldValue);
                    }
                } catch (NumberFormatException ex) {
                    latMaxSpinnerValueFactory.setValue(oldValue);
                }
            }
        });
        latMinSpinner.setEditable(true);
        latMaxSpinner.setEditable(true);

        lonMinSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(lonValuesList);
        lonMaxSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<String>(lonValuesList);
        lonMinSpinnerValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {;
            if (oldValue != null) {
                int minSpinnerValueIdx = lonValuesList.indexOf(newValue);
                int maxSpinnerValueIdx = lonValuesList.indexOf(lonMaxSpinnerValueFactory.getValue());
                if (minSpinnerValueIdx > maxSpinnerValueIdx) {
                    lonMinSpinnerValueFactory.setValue(lonValuesList.get(maxSpinnerValueIdx));
                }
                updateMapViewSelection();
            }
        });
        lonMaxSpinnerValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {;
            if (oldValue != null) {
                int maxSpinnerValueIdx = lonValuesList.indexOf(newValue);
                int minSpinnerValueIdx = lonValuesList.indexOf(lonMinSpinnerValueFactory.getValue());
                if (maxSpinnerValueIdx < minSpinnerValueIdx) {
                    lonMaxSpinnerValueFactory.setValue(lonValuesList.get(minSpinnerValueIdx));
                }
                updateMapViewSelection();
            }
        });

        lonMinSpinner = new Spinner<>(lonMinSpinnerValueFactory);
        lonMinSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newValueIndex = lonValuesList.indexOf(newValue);
            if (newValueIndex == -1) {
                // find nearest value
                try {
                    double newValueDouble = Double.parseDouble(newValue);
                    int nearestLonValueIndex = findNearestLonValueIndex(newValueDouble);
                    if (nearestLonValueIndex != -1) {
                        lonMinSpinnerValueFactory.setValue(lonValuesList.get(nearestLonValueIndex));
//                        updateMapViewSelection();
                    } else {
                        lonMinSpinnerValueFactory.setValue(oldValue);
                    }
                } catch (NumberFormatException ex) {
                    lonMinSpinnerValueFactory.setValue(oldValue);
                }
            }
        });

        lonMaxSpinner = new Spinner<>(lonMaxSpinnerValueFactory);
        lonMaxSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newValueIndex = lonValuesList.indexOf(newValue);
            if (newValueIndex == -1) {
                // find nearest value
                try {
                    double newValueDouble = Double.parseDouble(newValue);
                    int nearestLonValueIndex = findNearestLonValueIndex(newValueDouble);
                    if (nearestLonValueIndex != -1) {
                        lonMaxSpinnerValueFactory.setValue(lonValuesList.get(nearestLonValueIndex));
//                        updateMapViewSelection();
                    } else {
                        lonMaxSpinnerValueFactory.setValue(oldValue);
                    }
                } catch (NumberFormatException ex) {
                    lonMaxSpinnerValueFactory.setValue(oldValue);
                }
            }
        });
        lonMinSpinner.setEditable(true);
        lonMaxSpinner.setEditable(true);

        dimensionFilterGridPane.add(new Label(" Ensemble Min: "), 0, 0, 1, 1);
        dimensionFilterGridPane.add(ensembleMinSpinner, 1, 0, 1, 1);
        dimensionFilterGridPane.add(new Label(" Ensemble Max: "), 2, 0, 1, 1);
        dimensionFilterGridPane.add(ensembleMaxSpinner, 3, 0, 1, 1);

        dimensionFilterGridPane.add(new Label(" PFT Min: "), 0, 1, 1, 1);
        dimensionFilterGridPane.add(pftMinSpinner, 1, 1, 1, 1);
        dimensionFilterGridPane.add(new Label(" PFT Max: "), 2, 1, 1, 1);
        dimensionFilterGridPane.add(pftMaxSpinner, 3, 1, 1, 1);

        dimensionFilterGridPane.add(new Label(" Time Start: "), 0, 2, 1, 1);
        dimensionFilterGridPane.add(timeMinSpinner, 1, 2, 1, 1);
        dimensionFilterGridPane.add(new Label(" Time End: "), 2, 2, 1, 1);
        dimensionFilterGridPane.add(timeMaxSpinner, 3, 2, 1, 1);

        dimensionFilterGridPane.add(new Label(" Latitude Min: "), 0, 3, 1, 1);
        dimensionFilterGridPane.add(latMinSpinner, 1, 3, 1, 1);
        dimensionFilterGridPane.add(new Label(" Latitude Max: "), 2, 3, 1, 1);
        dimensionFilterGridPane.add(latMaxSpinner, 3, 3, 1, 1);

        dimensionFilterGridPane.add(new Label(" Longitude Min: "), 0, 4, 1, 1);
        dimensionFilterGridPane.add(lonMinSpinner, 1, 4, 1, 1);
        dimensionFilterGridPane.add(new Label(" Longitude Max: "), 2, 4, 1, 1);
        dimensionFilterGridPane.add(lonMaxSpinner, 3, 4, 1, 1);

        return dimensionFilterGridPane;
    }

    private void updateMapViewSelection() {
        if (lonValuesArray == null || latValuesArray == null) { return; }

        double west = lonValuesArray.get(lonValuesList.indexOf(lonMinSpinner.getValue()));
        double east = lonValuesArray.get(lonValuesList.indexOf(lonMaxSpinner.getValue()));
        double south = latValuesArray.get(latValuesList.indexOf(latMinSpinner.getValue()));
        double north = latValuesArray.get(latValuesList.indexOf(latMaxSpinner.getValue()));

        mapView.setSelection(west - 360, east - 360, south, north);
    }

    private int findNearestTimeValueIndex(double value) {
        int nearestIndex = -1;

        if (value <= timeValuesArray.get(0)) {
            nearestIndex = 0;
        } else if (value >= timeValuesArray.get(timeValuesArray.getShape()[0]-1)) {
            nearestIndex = timeValuesArray.getShape()[0] - 1;
        } else {
            for (int i = 1; i < timeValuesArray.getShape()[0]; i++) {
                if (value == timeValuesArray.get(i)) {
                    nearestIndex = i;
                    break;
                } else if (value >= timeValuesArray.get(i-1) && value <= timeValuesArray.get(i)){
                    if ((value - timeValuesArray.get(i-1)) < (timeValuesArray.get(i) - value)) {
                        nearestIndex = i - 1;
                    } else {
                        nearestIndex = i;
                    }
                    break;
                }
            }
        }

        return nearestIndex;
    }

    private int findNearestLonValueIndex(double lon) {
        int nearestIdx = -1;

        if (lon <= lonValuesArray.get(0)) {
            nearestIdx = 0;
        } else if (lon >= lonValuesArray.get(lonValuesArray.getShape()[0] - 1)) {
            nearestIdx = lonValuesArray.getShape()[0] - 1;
        } else {
            for (int i = 1; i < lonValuesArray.getShape()[0]; i++) {
                if (lon == lonValuesArray.get(i)) {
                    nearestIdx = i;
                    break;
                } else if (lon >= lonValuesArray.get(i - 1) && lon <= lonValuesArray.get(i)) {
                    if ((lon - lonValuesArray.get(i - 1)) < (lonValuesArray.get(i) - lon)) {
                        nearestIdx = i - 1;
                    } else {
                        nearestIdx = i;
                    }
                    break;
                }
            }
        }

        return nearestIdx;
    }
    
    private int findNearestLatValueIndex(double lat) {
        int nearestIdx = -1;
        
        if (lat <= latValuesArray.get(0)) {
            nearestIdx = 0;
        } else if (lat >= latValuesArray.get(latValuesArray.getShape()[0] - 1)) {
            nearestIdx = latValuesArray.getShape()[0] - 1;
        } else {
            for (int i = 1; i < latValuesArray.getShape()[0]; i++) {
                if (lat == latValuesArray.get(i)) {
                    nearestIdx = i;
                    break;
                } else if (lat >= latValuesArray.get(i - 1) && lat <= latValuesArray.get(i)) {
                    if ((lat - latValuesArray.get(i - 1)) < (latValuesArray.get(i) - lat)) {
                        nearestIdx = i - 1;
                    } else {
                        nearestIdx = i;
                    }
                    break;
                }
            }
        }
        
        return nearestIdx;
    }

    private void openNCFile(Stage stage, File f) throws IOException {
        File file = f;

        if (file == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open NetCDF File");

            file = fileChooser.showOpenDialog(stage);
            if (file == null) {
                return;
            }
        }

        this.ncFile = NetcdfFile.open(file.getAbsolutePath());

        variableNames.clear();
        mapView.clearSelectionMask();
        mapView.clearSelection();

        for (Variable variable : ncFile.getVariables()) {
            if (ncFile.findDimension(variable.getFullName()) == null) {
                variableNames.add(variable.getFullName());
            }
        }

        Dimension ensembleDimension = ncFile.findDimension("ensemble");
        int ensembleMin = 0;
        int ensembleMax = ensembleDimension.getLength() - 1;
        ensembleMinSpinnerValueFactory.setMin(ensembleMin);
        ensembleMinSpinnerValueFactory.setMax(ensembleMax);
        ensembleMinSpinnerValueFactory.setValue(ensembleMin);
        ensembleMaxSpinnerValueFactory.setMin(ensembleMin);
        ensembleMaxSpinnerValueFactory.setMax(ensembleMax);
        ensembleMaxSpinnerValueFactory.setValue(ensembleMax);

        Dimension pftDimension = ncFile.findDimension("pft");
        int pftMin = 0;
        int pftMax = pftDimension.getLength() - 1;
        pftMinSpinnerValueFactory.setMin(pftMin);
        pftMinSpinnerValueFactory.setMax(pftMax);
        pftMinSpinnerValueFactory.setValue(pftMin);
        pftMaxSpinnerValueFactory.setMin(pftMin);
        pftMaxSpinnerValueFactory.setMax(pftMax);
        pftMaxSpinnerValueFactory.setValue(pftMax);

        Variable timeVariable = ncFile.findVariable("time");
        int timeVariableShape[] = timeVariable.getShape();
        timeValuesArray = (ArrayDouble.D1) timeVariable.read();
        timeValuesList.clear();
        for (int i = 0; i < timeVariableShape[0]; i++) {
            timeValuesList.add(String.valueOf(timeValuesArray.get(i)));
        }
        timeMinSpinnerValueFactory.setValue(timeValuesList.get(0));
        timeMaxSpinnerValueFactory.setValue(timeValuesList.get(3));

        Variable latVariable = ncFile.findVariable("lat");
        int latVariableShape[] = latVariable.getShape();
        latValuesArray = (ArrayDouble.D1)latVariable.read();
        latValuesList.clear();
        for (int i = 0; i < latVariableShape[0]; i++) {
            latValuesList.add(String.valueOf(latValuesArray.get(i)));
        }
        latMinSpinnerValueFactory.setValue(latValuesList.get(0));
        latMaxSpinnerValueFactory.setValue(latValuesList.get(latValuesList.size() - 1));

        Variable lonVariable = ncFile.findVariable("lon");
        int lonVariableShape[] = lonVariable.getShape();
        lonValuesArray = (ArrayDouble.D1)lonVariable.read();
        lonValuesList.clear();
        for (int i = 0; i < lonVariableShape[0]; i++) {
            lonValuesList.add(String.valueOf(lonValuesArray.get(i)));
        }
        lonMinSpinnerValueFactory.setValue(lonValuesList.get(0));
        lonMaxSpinnerValueFactory.setValue(lonValuesList.get(lonValuesList.size() - 1));

        mapView.setSelectionMask(lonValuesArray.get(0) - 360.,
                lonValuesArray.get(lonVariableShape[0]-1) - 360.,
                latValuesArray.get(0), latValuesArray.get(latVariableShape[0]-1));
        mapView.setSelection(lonValuesArray.get(0) - 360.,
                lonValuesArray.get(lonVariableShape[0]-1) - 360.,
                latValuesArray.get(0), latValuesArray.get(latVariableShape[0]-1));

        fileNameTextField.setText(file.getAbsolutePath());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        fileNameTextField = new TextField();
        fileNameTextField.setPrefWidth(140);
        fileNameTextField.setEditable(true);
        fileNameTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                File f = new File(fileNameTextField.getText());
                try {
                    openNCFile(primaryStage, f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        fileChooserButton = new Button(" Open... ");
        fileChooserButton.setOnAction(event -> {
            try {
                openNCFile(primaryStage, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        HBox fileBox = new HBox(new Label("File: "), fileNameTextField, fileChooserButton);
        fileBox.setSpacing(4);
        HBox.setHgrow(fileNameTextField, Priority.ALWAYS);

        variableListView = createVariableListView();

        ButtonBar varSelectionButtonBar = new ButtonBar();
        Button selectAllButton = new Button("Select All");
        selectAllButton.setOnAction(event -> {
            variableListView.getSelectionModel().selectAll();
        });
        Button selectNoneButton = new Button ("Unselect All");
        selectNoneButton.setOnAction(event -> {
            variableListView.getSelectionModel().clearSelection();
        });
        varSelectionButtonBar.getButtons().addAll(selectAllButton, selectNoneButton);

        VBox variableListBox = new VBox();
        variableListBox.setSpacing(4);
        variableListBox.getChildren().addAll(variableListView, varSelectionButtonBar);
        variableListBox.setMaxHeight(250);
        variableListBox.setMinHeight(150);

        Pane dimensionFilterPane = createDimensionFilterPane();
        dimensionFilterPane.setPrefWidth(400);

        mapView = new MapView();
        mapView.setPrefSize(400, 200);
        mapView.selectionGeoRectangleProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == newValue) { return; }

            if (newValue != null) {
                // update lat / lon range spinners based on selection
                int nearestValueIdx = findNearestLatValueIndex(newValue.topLatitude);
                if (nearestValueIdx != -1) {
                    latMaxSpinnerValueFactory.setValue(latValuesList.get(nearestValueIdx));
                }

                nearestValueIdx = findNearestLatValueIndex(newValue.bottomLatitude);
                if (nearestValueIdx != -1) {
                    latMinSpinnerValueFactory.setValue(latValuesList.get(nearestValueIdx));
                }

                nearestValueIdx = findNearestLonValueIndex(newValue.leftLongitude + 360.);
                if (nearestValueIdx != -1) {
                    lonMinSpinnerValueFactory.setValue(lonValuesList.get(nearestValueIdx));
                }

                nearestValueIdx = findNearestLonValueIndex(newValue.rightLongitude + 360.);
                if (nearestValueIdx != -1) {
                    lonMaxSpinnerValueFactory.setValue(lonValuesList.get(nearestValueIdx));
                }
            } else {
                // reset lat / lon range spinners to full range
                latMinSpinnerValueFactory.setValue(latValuesList.get(0));
                latMaxSpinnerValueFactory.setValue(latValuesList.get(latValuesList.size() - 1));
                lonMinSpinnerValueFactory.setValue(lonValuesList.get(0));
                lonMaxSpinnerValueFactory.setValue(lonValuesList.get(lonValuesList.size() - 1));
            }
        });
        VBox dimensionFilterPaneBox = new VBox();
        dimensionFilterPaneBox.setSpacing(4);
        Label note = new Label("NOTE:  If you type a value below, press enter to commit the change.");
        dimensionFilterPaneBox.getChildren().addAll(note, dimensionFilterPane, mapView);

        Button readButton = new Button("Read and Visualize Data");
        readButton.setDisable(dataTable == null);
        readButton.setOnAction(event -> {
            try {
                Pair<ArrayList<Column>,ArrayList<Tuple>> data = readData(dataTable);
                if (dataTable != null) {
                    dataTable.setData(data.getValue(), data.getKey());
                }
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Button exportButton = new Button("Read and Export Data...");
        exportButton.setOnAction(event -> {
            try {
                Pair<ArrayList<Column>, ArrayList<Tuple>> data = readData(null);
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Data to CSV File");
                File csvFile = fileChooser.showSaveDialog(primaryStage);
                if (csvFile != null) {
                    int numTuplesWritten = IOUtilities.exportDataToCSVFile(csvFile, data.getKey(), data.getValue());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("CSV File Export");
                    alert.setContentText(numTuplesWritten + " tuples successfully written to ' " + csvFile.getName());
                    alert.showAndWait();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }
        });
//        exportButton.setDisable(true);
        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setPadding(new Insets(20, 10, 10, 10));
        buttonBar.getButtons().addAll(readButton, exportButton);

        TitledPane variablesTitledPane = new TitledPane("Variables", variableListBox);
        variablesTitledPane.setCollapsible(false);
        TitledPane filterTitledPane = new TitledPane("Dimension Filters", dimensionFilterPaneBox);
        filterTitledPane.setCollapsible(false);
        VBox inputBox = new VBox(fileBox, variablesTitledPane, filterTitledPane);
        inputBox.setPadding(new Insets(10));
        inputBox.setSpacing(4);
        VBox.setVgrow(variablesTitledPane, Priority.NEVER);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(inputBox);
        rootNode.setBottom(buttonBar);

        Scene scene = new Scene(rootNode, 500, 720, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("NetCDF Dataset Filter Window");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        mapView.selectionGeoRectangleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                
            } else {
                // set selection to full range
            }
        });
    }


    private Pair<ArrayList<Column>, ArrayList<Tuple>> readData(DataTable dataTable) throws InvalidRangeException, IOException {
        // get selected parameter and output values from variables
        List<String> selectedVariableNames = variableListView.getSelectionModel().getSelectedItems();
        ArrayList<Variable> selectedParameters = new ArrayList<>();
        ArrayList<Variable> selectedOutputs = new ArrayList<>();

        Variable pftFracVariable = null;

        for (String variableName : selectedVariableNames) {
            Variable variable = ncFile.findVariable(variableName);
            if (variable.getRank() == 1) {
                // model parameters only have one dimension (ensemble number)
                selectedParameters.add(variable);
            } else if (variable.getRank() == 5) {
                // model outputs have all 5 dimensions (ensember, pft, time, lat, lon)
                selectedOutputs.add(variable);
            } else if (variable.getRank() == 3) {
                // pft frac is a parameter? that has 3 dimensions (pft, lat, lon)
                pftFracVariable = variable;
            }
        }

        if (pftFracVariable == null) {
            pftFracVariable = ncFile.findVariable("pft_frac");
        }

        // setup ensemble range
        Range ensembleRange = new Range(ensembleMinSpinner.getValue(), ensembleMaxSpinner.getValue(), 1);
        ArrayList<Range> parameterRangeList = new ArrayList<>();
        parameterRangeList.add(ensembleRange);

        // get selected parameter value arrays
        ArrayList<ArrayDouble.D1> parameterArrays = new ArrayList<>();
        for (Variable selectedParameter : selectedParameters) {
            ArrayDouble.D1 parameterArray = (ArrayDouble.D1) selectedParameter.read(parameterRangeList);
            parameterArrays.add(parameterArray);
        }

        // get selected variable value arrays
        Range pftRange = new Range(pftMinSpinner.getValue(), pftMaxSpinner.getValue(), 1);

        // setup time range
        int startTimeIdx = timeValuesList.indexOf(timeMinSpinner.getValue());
        int endTimeIdx = timeValuesList.indexOf(timeMaxSpinner.getValue());
        Range timeRange = new Range(startTimeIdx, endTimeIdx, 1);
//        int timeStart = (int)(timeLowValue / timeRangeSlider.getBlockIncrement());
//        int timeEnd = (int)(timeHighValue / timeRangeSlider.getBlockIncrement());
//        Range timeRange = new Range(timeStart, timeEnd, 1);

        // setup lat range
        int minLatIdx = latValuesList.indexOf(latMinSpinner.getValue());
        int maxLatIdx = latValuesList.indexOf(latMaxSpinner.getValue());
        Variable latVariable = ncFile.findVariable("lat");
        Range latRange = new Range(minLatIdx, maxLatIdx, 1);

        // setup lon range
        int minLonIdx = lonValuesList.indexOf(lonMinSpinner.getValue());
        int maxLonIdx = lonValuesList.indexOf(lonMaxSpinner.getValue());
        Variable lonVariable = ncFile.findVariable("lon");
        Range lonRange = new Range(minLonIdx, maxLatIdx, 1);
//        Range lonRange = new Range(0, lonVariable.getShape()[0] - 1, 1);

        // make a list of all dimension range selections
        ArrayList<Range> outputRangeList = new ArrayList();
        outputRangeList.add(ensembleRange);
        outputRangeList.add(pftRange);
        outputRangeList.add(timeRange);
        outputRangeList.add(latRange);
        outputRangeList.add(lonRange);

        // read the model output variable arrays for selection
        ArrayList<ArrayFloat.D5> outputArrays = new ArrayList<>();
        for (Variable output : selectedOutputs) {
            ArrayFloat.D5 outputArray = (ArrayFloat.D5) output.read(outputRangeList);
            outputArrays.add(outputArray);
        }

        // read pftFrac variable array for selection
        ArrayFloat.D3 pftFracArray = null;
        if (pftFracVariable != null) {
            ArrayList<Range> pftFracRanges = new ArrayList<>();
            pftFracRanges.add(latRange);
            pftFracRanges.add(lonRange);
            pftFracRanges.add(pftRange);
            pftFracArray = (ArrayFloat.D3) pftFracVariable.read(pftFracRanges);
        }

//        Variable timeVariable = ncFile.findVariable("time");
//        ArrayDouble.D1 timeArray = (ArrayDouble.D1) timeVariable.read();
//        ArrayDouble.D1 latArray = (ArrayDouble.D1) latVariable.read();
//        ArrayDouble.D1 lonArray = (ArrayDouble.D1) lonVariable.read();

        // populate columns array with dimension, parameters, and model outputs
        ArrayList<Column> columns = new ArrayList<>();
        columns.add(new DoubleColumn("ensemble"));
        columns.add(new DoubleColumn("pft"));
        columns.add(new DoubleColumn("time"));
        columns.add(new DoubleColumn("lat"));
        columns.add(new DoubleColumn("lon"));

        for (Variable parameter : selectedParameters) {
            columns.add(new DoubleColumn(parameter.getFullName()));
        }

        for (Variable modelOutput : selectedOutputs) {
            columns.add(new DoubleColumn(modelOutput.getFullName()));
        }

        ArrayList<Tuple> tuples = new ArrayList<>();
//        for (int i = ensembleRange.first(); i <= ensembleRange.last(); i++) {
        for (int i = 0; i < ensembleRange.length(); i++) {
            int ensemble = ensembleRange.element(i);

            double parameterValues[] = new double[selectedParameters.size()];
            for (int iparameter = 0; iparameter < selectedParameters.size(); iparameter++) {
                double value = parameterArrays.get(iparameter).get(i);
                parameterValues[iparameter] = value;
            }

//            for (int j = pftRange.first(); j <= pftRange.last(); j++) {
            for (int j = 0; j < pftRange.length(); j++) {
                int pft = pftRange.element(j);
//                int pft = j;
//                for (int k = timeRange.first(); k <= timeRange.last(); k++) {
                for (int k = 0; k < timeRange.length(); k++) {
                    int time_idx = timeRange.element(k);
                    double time_value = timeValuesArray.get(time_idx);
//                    double time = timeValuesArray.get(k);

//                    for (int l = latRange.first(); l <= latRange.last(); l++) {
                    for (int l = 0; l < latRange.length(); l++) {
                        int lat_idx = latRange.element(l);
                        double lat = latValuesArray.get(lat_idx);
//                        double lat = latValuesArray.get(l);

                        for (int m = 0; m < lonRange.length(); m++) {
//                        for (int m = lonRange.first(); m < lonRange.last(); m++) {
                            int lon_idx = lonRange.element(m);

                            Tuple tuple = new Tuple(dataTable);

//                            double lon = lonValuesArray.get(m);
                            double lon = lonValuesArray.get(lon_idx);

                            // add dimension values to tuple
                            tuple.addElement((double)ensemble);
//                            log.info("ensemble: " + ensemble);

                            if (pftFracArray != null) {
                                float pftFracValue = pftFracArray.get(l, m, j);
                                tuple.addElement((double)pftFracValue);
                            }
                            tuple.addElement(time_value);
                            tuple.addElement(lat);
                            tuple.addElement(lon);

                            // add parameters to tuple
                            for (double parameterValue : parameterValues) {
                                tuple.addElement(parameterValue);
                            }

                            for (int ioutput = 0; ioutput < selectedOutputs.size(); ioutput++) {
                                float outputValue = outputArrays.get(ioutput).get(i, j, k, l, m);
                                tuple.addElement((double)outputValue);
                            }

                            if (tuple.getElementCount() != columns.size()) {
                                log.info("this tuple doesn't have enough elements");
                            }

                            tuples.add(tuple);
                        }
                    }
                }
            }
        }

        log.info("Read " + columns.size() + " columns and " + tuples.size() + " tuples");

        return new Pair<>(columns, tuples);
//
//        if (dataTable != null) {
//            dataTable.setData(tuples, columns);
//        }
        // prompt user for action (add to View, clear and add to view, write data to csv file)
    }

}
