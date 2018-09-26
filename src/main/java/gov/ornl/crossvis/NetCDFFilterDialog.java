package gov.ornl.crossvis;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.RangeSlider;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.util.List;
import java.util.Optional;

public class NetCDFFilterDialog {

    public static NetCDFFilter getNetCDFFilter(NetcdfFile ncFile) {
        List<Variable> ncVariables = ncFile.getVariables();
        List<Dimension> ncDimensions = ncFile.getDimensions();

        Dialog<NetCDFFilter> dialog = new Dialog<>();
        dialog.setTitle("NetCDF File Filter Dialog");
        dialog.setHeaderText("Specify NetCDF File Filter Paramaters");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ListView<Variable> variableListView = new ListView<>();
        ObservableList<Variable> variables = FXCollections.observableArrayList();
        for (Variable variable : ncVariables) {
            boolean isDimension = false;
            for (Dimension dimension : ncDimensions) {
                if (dimension.getShortName().equals(variable.getShortName())) {
                    isDimension = true;
                    break;
                }
            }

            if (!isDimension) {
                variables.add(variable);
            }
        }
        variableListView.setItems(variables);
        variableListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        gridPane.add(new Label("Select Variables:"), 0, 0, 3, 1);
        gridPane.add(variableListView, 0, 1, 3, 1);

        int rowIndex = 2;
        for (Dimension dimension : ncDimensions) {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(false);

            Label label = new Label(dimension.getShortName());

            RangeSlider rangeSlider = new RangeSlider(0, dimension.getLength(), 0, dimension.getLength());
            rangeSlider.setShowTickMarks(true);
            gridPane.add(checkBox, 0, rowIndex);
            gridPane.add(label, 1, rowIndex);
            gridPane.add(rangeSlider, 2, rowIndex);

            rowIndex++;
        }

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(() -> variableListView.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                NetCDFFilter filter = new NetCDFFilter();
                return filter;
            }
            return null;
        });

        Optional<NetCDFFilter> result = dialog.showAndWait();

        if (result.isPresent()) {
            return result.get();
        }

        return null;
    }
}
