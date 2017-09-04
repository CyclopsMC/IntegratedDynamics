package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.network.packet.NetworkDiagnosticsSubscribePacket;
import org.cyclops.integrateddynamics.network.packet.PlayerTeleportPacket;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
            Collection<ObservablePartData> previous = networkData.removeAll(id);

            // The positions that were being rendered previously
            Set<PartPos> previousPositionsWithRender = Sets.newHashSet();
            for (ObservablePartData partData : previous) {
                PartPos pos = partData.toPartPos();
                if (pos != null && NetworkDiagnosticsPartOverlayRenderer.getInstance().hasPartPos(pos)) {
                    previousPositionsWithRender.add(pos);
                }
            }

            if (rawNetworkData != null) {
                List<ObservablePartData> parts = Lists.newArrayList();
                for (RawPartData rawPartData : rawNetworkData.getParts()) {
                    ObservablePartData partData = new ObservablePartData(
                            rawNetworkData.getId(), rawNetworkData.getCables(),
                            rawPartData.getDimension(), rawPartData.getPos(),
                            rawPartData.getSide(), rawPartData.getName(),
                            rawPartData.getLast20TicksDurationNs());
                    parts.add(partData);

                    // Remove this position from the previously rendered list
                    PartPos pos = partData.toPartPos();
                    if (pos != null) {
                        previousPositionsWithRender.remove(pos);
                    }
                }

                // Remove all remaining positions from the renderlist,
                // because those do not exist anymore.
                for (PartPos partPos : previousPositionsWithRender) {
                    NetworkDiagnosticsPartOverlayRenderer.getInstance().removePos(partPos);
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
                    onCloseGui();
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
                        columnNames.addElement("_id");

                        data.clear();
                        int i = 0;
                        for (ObservablePartData observablePartData : networkData.values()) {
                            Vector<Object> row = new Vector<>();
                            row.add(observablePartData.getNetworkId());
                            row.add(observablePartData.getNetworkCables());
                            row.add(observablePartData.getName());
                            row.add(String.format("%.6f", ((double) observablePartData.getLast20TicksDurationNs()) / MinecraftHelpers.SECOND_IN_TICKS / 1000000));
                            row.add(observablePartData.getDimension());
                            BlockPos pos = observablePartData.getPos();
                            row.add(String.format("%s / %s / %s", pos.getX(), pos.getY(), pos.getZ()));
                            row.add(observablePartData.getSide().name());
                            row.add(i++);
                            data.addElement(row);
                        }

                        if (table == null) {
                            table = new JTable();
                            model = new DefaultTableModel(data, columnNames) {
                                @Override
                                public Class<?> getColumnClass(int column) {
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
                            table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
                                @Override
                                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                    ObservablePartData partData = getPartDataFromRow(row);
                                    if (partData != null && NetworkDiagnosticsPartOverlayRenderer.getInstance().hasPartPos(partData.toPartPos())) {
                                        c.setBackground(Color.CYAN);
                                    } else {
                                        c.setBackground(isSelected ? Color.BLUE : Color.WHITE);
                                    }
                                    return c;
                                }
                            });
                            table.getColumnModel().removeColumn(table.getColumn("_id"));
                            table.setAutoCreateRowSorter(true);
                            table.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    super.mouseClicked(e);
                                    if (e.getClickCount() == 1) {
                                        JTable target = (JTable)e.getSource();
                                        int row = target.rowAtPoint(e.getPoint());
                                        ObservablePartData partData = getPartDataFromRow(row);
                                        if (partData != null) {
                                            PartPos pos = partData.toPartPos();
                                            if (e.getButton() == MouseEvent.BUTTON1) {
                                                if (NetworkDiagnosticsPartOverlayRenderer.getInstance().hasPartPos(pos)) {
                                                    NetworkDiagnosticsPartOverlayRenderer.getInstance().removePos(pos);
                                                } else {
                                                    NetworkDiagnosticsPartOverlayRenderer.getInstance().addPos(pos);
                                                }
                                            } else if (e.getButton() == MouseEvent.BUTTON3) {
                                                BlockPos blockPos = pos.getPos().getBlockPos().offset(pos.getSide());
                                                float yaw = pos.getSide().getOpposite().getHorizontalAngle();
                                                IntegratedDynamics._instance.getPacketHandler().sendToServer(new PlayerTeleportPacket(
                                                        pos.getPos().getDimensionId(),
                                                        blockPos.getX(),
                                                        blockPos.getY() - 1,
                                                        blockPos.getZ(),
                                                        yaw,
                                                        0
                                                ));
                                            }
                                        }
                                    }
                                }
                            });
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

    protected static ObservablePartData getPartDataFromRow(int row) {
        if (row < 0) {
            return null;
        }
        Object[] data;
        synchronized (networkData) {
            data = networkData.values().toArray();
        }
        int internalId = (int) table.getModel().getValueAt(table.convertRowIndexToModel(row), 7);
        if (internalId < data.length) {
            return (ObservablePartData) data[internalId];
        }
        return null;
    }

    protected static void onCloseGui() {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(NetworkDiagnosticsSubscribePacket.unsubscribe());
        NetworkDiagnosticsPartOverlayRenderer.getInstance().clearPositions();
    }

    @Data
    public static class ObservablePartData {
        private final int networkId;
        private final int networkCables;
        private final int dimension;
        private final BlockPos pos;
        private final EnumFacing side;
        private final String name;
        private final long last20TicksDurationNs;

        public PartPos toPartPos() {
            World world = Minecraft.getMinecraft().world;
            if (getDimension() == world.provider.getDimension()) {
                return PartPos.of(DimPos.of(world, getPos()), getSide());
            }
            return null;
        }
    }
}
