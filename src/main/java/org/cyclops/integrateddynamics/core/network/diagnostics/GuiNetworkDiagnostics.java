package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Data;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsSubscribePacket;

import java.util.List;

/**
 * Network diagnostics gui.
 * @author rubensworks
 */
public class GuiNetworkDiagnostics extends Application {

    private static GuiNetworkDiagnostics gui = null;
    private static Stage primaryStage = null;
    private static TableView<ObservablePartData> table = null;

    private static Multimap<Integer, ObservablePartData> networkData = ArrayListMultimap.create();
    private ObservableList<ObservablePartData> partData = FXCollections.observableArrayList(networkData.values());

    public static void setNetworkData(int id, RawNetworkData rawNetworkData) {
        synchronized (networkData) {
            networkData.removeAll(id);
            if (rawNetworkData != null) {
                List<ObservablePartData> parts = Lists.newArrayList();
                for (RawPartData rawPartData : rawNetworkData.getParts()) {
                    parts.add(new ObservablePartData(
                            rawNetworkData.getId(), rawNetworkData.getCables(),
                            rawPartData.getDimension(), rawPartData.getPos(),
                            rawPartData.getSide(), rawPartData.getName(),
                            rawPartData.getLastTickDuration()));
                }
                networkData.putAll(id, parts);
            }
        }
        if (gui != null) {
            gui.updateTable();
        }
    }

    public static void clearNetworkData() {
        networkData.clear();
    }

    public void start() {
        gui = this;

        Platform.setImplicitExit(false);
        if (primaryStage != null) {
            Platform.runLater(() -> {
                try {
                    start(primaryStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            launch();
        }
    }

    protected void updateTable() {
        synchronized (networkData) {
            if (table == null) {
                table = new TableView<>();

                TableColumn<ObservablePartData, Integer> networkCol = new TableColumn<>(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.network"));
                networkCol.prefWidthProperty().bind(table.widthProperty().divide(6));
                networkCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getNetworkId()));
                TableColumn<ObservablePartData, Integer> cablesCol = new TableColumn<>(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.cables"));
                cablesCol.prefWidthProperty().bind(table.widthProperty().divide(12));
                cablesCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getNetworkCables()));
                TableColumn<ObservablePartData, String> partCol = new TableColumn<>(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.part"));
                partCol.prefWidthProperty().bind(table.widthProperty().divide(6));
                partCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getName()));
                TableColumn<ObservablePartData, Long> durationCol = new TableColumn<>(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.ticktime"));
                durationCol.prefWidthProperty().bind(table.widthProperty().divide(6));
                durationCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getLastTickDuration()));
                TableColumn<ObservablePartData, Integer> dimCol = new TableColumn<>(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.dimension"));
                dimCol.prefWidthProperty().bind(table.widthProperty().divide(12));
                dimCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getDimension()));
                TableColumn<ObservablePartData, String> posCol = new TableColumn<>(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.position"));
                posCol.prefWidthProperty().bind(table.widthProperty().divide(6));
                posCol.setCellValueFactory(p -> {
                    BlockPos pos = p.getValue().getPos();
                    return new ReadOnlyObjectWrapper<>(String.format("%s / %s / %s", pos.getX(), pos.getY(), pos.getZ()));
                });
                TableColumn<ObservablePartData, String> sideCol = new TableColumn<>(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.side"));
                sideCol.prefWidthProperty().bind(table.widthProperty().divide(6));
                sideCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getSide().name()));

                table.getColumns().addAll(networkCol, cablesCol, partCol, durationCol, dimCol, posCol, sideCol);
                table.setItems(partData);
            }
            partData.setAll(networkData.values());
            table.sort();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GuiNetworkDiagnostics.primaryStage = primaryStage;
        primaryStage.setTitle(L10NHelpers.localize("gui.integrateddynamics.diagnostics.title"));
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 20, 10, 20));

        updateTable();
        root.setCenter(table);
        table.autosize();

        primaryStage.setScene(new Scene(root, 750, 500));
        primaryStage.setOnCloseRequest(we -> IntegratedDynamics._instance.getPacketHandler()
                .sendToServer(NetworkDiagnosticsSubscribePacket.unsubscribe()));
        primaryStage.show();
    }

    @Data
    public static class ObservablePartData {
        private final int networkId;
        private final int networkCables;
        private final int dimension;
        private final BlockPos pos;
        private final EnumFacing side;
        private final String name;
        private final long lastTickDuration;
    }
}
