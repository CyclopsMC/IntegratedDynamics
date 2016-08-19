package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.Data;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsSubscribePacket;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;

/**
 * Network diagnostics gui.
 * @author rubensworks
 */
public class GuiNetworkDiagnostics extends JFrame {

    private static GuiNetworkDiagnostics gui = null;
    private static JTable table = null;
    private static Vector<String> columnNames = new Vector<>();
    private static Vector<Vector<Object>> data = new Vector<>();
    private static DefaultTableModel model;

    private static Multimap<Integer, ObservablePartData> networkData = ArrayListMultimap.create();

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

    public static void start() {
        if (gui == null) {
            gui = new GuiNetworkDiagnostics();

            gui.setTitle(L10NHelpers.localize("gui.integrateddynamics.diagnostics.title"));
            gui.updateTable();
            gui.setSize(750, 500);
            gui.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            gui.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    IntegratedDynamics._instance.getPacketHandler().sendToServer(NetworkDiagnosticsSubscribePacket.unsubscribe());
                    super.windowClosing(e);
                }
            });
            gui.setLocationRelativeTo((Component) null);
        }
        gui.setVisible(true);
    }

    protected void updateTable() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    synchronized (networkData) {
                        columnNames.clear();
                        columnNames.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.network"));
                        columnNames.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.cables"));
                        columnNames.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.part"));
                        columnNames.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.ticktime"));
                        columnNames.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.dimension"));
                        columnNames.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.position"));
                        columnNames.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.side"));

                        data.clear();
                        for (ObservablePartData observablePartData : networkData.values()) {
                            Vector<Object> row = new Vector<>();
                            row.add(observablePartData.getNetworkId());
                            row.add(observablePartData.getNetworkCables());
                            row.add(observablePartData.getName());
                            row.add(observablePartData.getLastTickDuration());
                            row.add(observablePartData.getDimension());
                            BlockPos pos = observablePartData.getPos();
                            row.add(String.format("%s / %s / %s", pos.getX(), pos.getY(), pos.getZ()));
                            row.add(observablePartData.getSide().name());
                            data.addElement(row);
                        }

                        if (table == null) {
                            table = new JTable();
                            model = new DefaultTableModel(data, columnNames) {
                                @Override
                                public Class getColumnClass(int column) {
                                    // My eyes are bleeding as I write this...
                                    // I'm terribly sorry, I must be going to hell now.
                                    if (column == 0 || column == 1 || column == 4) {
                                        return Integer.class;
                                    }
                                    if (column == 3) {
                                        return Long.class;
                                    }
                                    return String.class;
                                }
                            };
                            table.setModel(model);
                            table.setAutoCreateRowSorter(true);
                            add(new JScrollPane(table));
                            pack();
                        } else {
                            table.getRowSorter().allRowsChanged();
                            model.fireTableDataChanged();
                        }
                        repaint();
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
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
