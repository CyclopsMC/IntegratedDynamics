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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Network diagnostics gui.
 * @author rubensworks
 */
public class GuiNetworkDiagnostics extends JFrame {

    private static final int IDX_PARTS_NETWORK = 0;
    private static final int IDX_PARTS_CABLES = 1;
    private static final int IDX_PARTS_PART = 2;
    private static final int IDX_PARTS_TICKTIME = 3;
    private static final int IDX_PARTS_DIMENSION = 4;
    private static final int IDX_PARTS_POSITION = 5;
    private static final int IDX_PARTS_SIDE = 6;
    private static final int IDX_PARTS_ID = 7;

    private static final int IDX_OBSERVERS_NETWORK = 0;
    private static final int IDX_OBSERVERS_PART = 1;
    private static final int IDX_OBSERVERS_TICKTIME = 2;
    private static final int IDX_OBSERVERS_DIMENSION = 3;
    private static final int IDX_OBSERVERS_POSITION = 4;
    private static final int IDX_OBSERVERS_SIDE = 5;
    private static final int IDX_OBSERVERS_ID = 6;

    private static GuiNetworkDiagnostics gui = null;

    private static JTable tableParts = null;
    private static final Vector<String> columnNamesParts = new Vector<>();
    private static final Vector<Vector<Object>> dataParts = new Vector<>();
    private static final Multimap<Integer, ObservablePartData> networkDataParts = ArrayListMultimap.create();

    private static JTable tableObservers = null;
    private static final Vector<String> columnNamesObservers = new Vector<>();
    private static final Vector<Vector<Object>> dataObservers = new Vector<>();
    private static final Multimap<Integer, ObservableObserverData> networkDataObservers = ArrayListMultimap.create();

    public static void setNetworkData(int id, RawNetworkData rawNetworkData) {
        synchronized (networkDataParts) {
            Collection<ObservablePartData> previous = networkDataParts.removeAll(id);

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

                networkDataParts.putAll(id, parts);
            }

            Collection<ObservableObserverData> previousObservers = networkDataObservers.removeAll(id);

            // The positions that were being rendered previously
            Set<PartPos> previousPositionsWithRenderObservers = Sets.newHashSet();
            for (ObservableObserverData partData : previousObservers) {
                PartPos pos = partData.toPartPos();
                if (pos != null && NetworkDiagnosticsPartOverlayRenderer.getInstance().hasPartPos(pos)) {
                    previousPositionsWithRenderObservers.add(pos);
                }
            }

            if (rawNetworkData != null) {
                List<ObservableObserverData> observers = Lists.newArrayList();
                for (RawObserverData rawPartData : rawNetworkData.getObservers()) {
                    ObservableObserverData partData = new ObservableObserverData(
                            rawNetworkData.getId(),
                            rawPartData.getDimension(), rawPartData.getPos(),
                            rawPartData.getSide(), rawPartData.getName(),
                            rawPartData.getLast20TicksDurationNs());
                    observers.add(partData);

                    // Remove this position from the previously rendered list
                    PartPos pos = partData.toPartPos();
                    if (pos != null) {
                        previousPositionsWithRenderObservers.remove(pos);
                    }
                }

                // Remove all remaining positions from the renderlist,
                // because those do not exist anymore.
                for (PartPos partPos : previousPositionsWithRenderObservers) {
                    NetworkDiagnosticsPartOverlayRenderer.getInstance().removePos(partPos);
                }

                networkDataObservers.putAll(id, observers);
            }
        }
        if (gui != null) {
            gui.updateTables();
        }
    }

    public static void clearNetworkData() {
        networkDataParts.clear();
        networkDataObservers.clear();
    }

    public static void start() {
        if (gui == null) {
            gui = new GuiNetworkDiagnostics();

            gui.setTitle(L10NHelpers.localize("gui.integrateddynamics.diagnostics.title"));
            gui.updateTables();
            gui.setSize(750, 500);
            gui.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            gui.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    onCloseGui();
                    super.windowClosing(e);
                }
            });
            gui.setLocationRelativeTo(null);
        }
        gui.setVisible(true);
    }

    private void updateTables() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    synchronized (networkDataParts) {
                        dataParts.clear();
                        int i = 0;
                        for (ObservablePartData observablePartData : networkDataParts.values()) {
                            Vector<Object> row = new Vector<>();
                            row.add(observablePartData.getNetworkId());
                            row.add(observablePartData.getNetworkCables());
                            row.add(observablePartData.getName());
                            row.add(((double) observablePartData.getLast20TicksDurationNs()) / MinecraftHelpers.SECOND_IN_TICKS / 1000000);
                            row.add(observablePartData.getDimension());
                            row.add(observablePartData.getPos());
                            row.add(observablePartData.getSide().name());
                            row.add(i++);
                            dataParts.addElement(row);
                        }

                        dataObservers.clear();
                        i = 0;
                        for (ObservableObserverData observableObserverData : networkDataObservers.values()) {
                            Vector<Object> row = new Vector<>();
                            row.add(observableObserverData.getNetworkId());
                            row.add(observableObserverData.getName());
                            row.add(((double) observableObserverData.getLast20TicksDurationNs()) / MinecraftHelpers.SECOND_IN_TICKS / 1000000);
                            row.add(observableObserverData.getDimension());
                            row.add(observableObserverData.getPos());
                            row.add(observableObserverData.getSide() == null ? "null" : observableObserverData.getSide().name());
                            row.add(i++);
                            dataObservers.addElement(row);
                        }

                        if (tableParts == null) {
                            /* ----- ----- ----- Init parts table ----- ----- ----- */
                            columnNamesParts.clear();
                            columnNamesParts.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.network"));
                            columnNamesParts.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.cables"));
                            columnNamesParts.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.part"));
                            columnNamesParts.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.ticktime"));
                            columnNamesParts.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.dimension"));
                            columnNamesParts.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.position"));
                            columnNamesParts.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.side"));
                            columnNamesParts.addElement("_id");

                            tableParts = new JTable(new DefaultTableModel(dataParts, columnNamesParts) {
                                @Override
                                public Class<?> getColumnClass(int column) {
                                    // My eyes are bleeding as I write this...
                                    // I'm terribly sorry, I must be going to hell now.
                                    switch (column) {
                                        case IDX_PARTS_NETWORK:
                                        case IDX_PARTS_CABLES:
                                        case IDX_PARTS_DIMENSION:
                                            return Integer.class;

                                        case IDX_PARTS_TICKTIME:
                                            return Double.class;

                                        case IDX_PARTS_POSITION:
                                            return BlockPos.class;

                                        case IDX_PARTS_PART:
                                        case IDX_PARTS_SIDE:
                                        default:
                                            return String.class;
                                    }
                                }
                            }) {
                                @Override
                                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                                    Component c = super.prepareRenderer(renderer, row, column);
                                    ObservablePartData partData = getPartDataFromRow(row);
                                    if (partData != null && NetworkDiagnosticsPartOverlayRenderer.getInstance().hasPartPos(partData.toPartPos())) {
                                        c.setBackground(Color.CYAN);
                                    } else {
                                        c.setBackground(isCellSelected(row, column) ? getSelectionBackground() : null);
                                    }
                                    return c;
                                }
                            };

                            tableParts.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    super.mouseClicked(e);
                                    if (e.getClickCount() == 1) {
                                        JTable target = (JTable)e.getSource();
                                        int row = target.rowAtPoint(e.getPoint());
                                        ObservablePartData partData = getPartDataFromRow(row);
                                        if (partData != null) {
                                            teleportPlayer(e, partData.toPartPos());
                                        }
                                    }
                                }
                            });

                            /* ----- ----- ----- Init observers table ----- ----- ----- */
                            columnNamesObservers.clear();
                            columnNamesObservers.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.network"));
                            columnNamesObservers.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.part"));
                            columnNamesObservers.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.ticktime"));
                            columnNamesObservers.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.dimension"));
                            columnNamesObservers.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.position"));
                            columnNamesObservers.addElement(L10NHelpers.localize("gui.integrateddynamics.diagnostics.table.side"));
                            columnNamesObservers.addElement("_id");

                            tableObservers = new JTable(new DefaultTableModel(dataObservers, columnNamesObservers) {
                                @Override
                                public Class<?> getColumnClass(int column) {
                                    // My eyes are bleeding as I write this...
                                    // I'm terribly sorry, I must be going to hell now.
                                    switch (column) {
                                        case IDX_OBSERVERS_NETWORK:
                                        case IDX_OBSERVERS_DIMENSION:
                                            return Integer.class;

                                        case IDX_OBSERVERS_TICKTIME:
                                            return Double.class;

                                        case IDX_OBSERVERS_POSITION:
                                            return BlockPos.class;

                                        case IDX_OBSERVERS_PART:
                                        case IDX_OBSERVERS_SIDE:
                                        default:
                                            return String.class;
                                    }
                                }
                            }) {
                                @Override
                                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                                    Component c = super.prepareRenderer(renderer, row, column);
                                    ObservableObserverData observerData = getObserverDataFromRow(row);
                                    if (observerData != null && NetworkDiagnosticsPartOverlayRenderer.getInstance().hasPartPos(observerData.toPartPos())) {
                                        c.setBackground(Color.CYAN);
                                    } else {
                                        c.setBackground(isCellSelected(row, column) ? getSelectionBackground() : null);
                                    }
                                    return c;
                                }
                            };

                            tableObservers.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    super.mouseClicked(e);
                                    if (e.getClickCount() == 1) {
                                        JTable target = (JTable)e.getSource();
                                        int row = target.rowAtPoint(e.getPoint());
                                        ObservableObserverData observerData = getObserverDataFromRow(row);
                                        if (observerData != null) {
                                            teleportPlayer(e, observerData.toPartPos());
                                        }
                                    }
                                }
                            });

                            for (JTable table : new JTable[] { tableParts, tableObservers }) {
                                table.getColumnModel().removeColumn(table.getColumn("_id"));
                                table.setAutoCreateRowSorter(true);

                                table.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
                                    {
                                        setHorizontalAlignment(JLabel.RIGHT);
                                    }

                                    @Override
                                    protected void setValue(Object value) {
                                        Double doubleValue = (Double) value;
                                        setText(String.format("%.6f", doubleValue));
                                    }
                                });

                                table.setDefaultRenderer(BlockPos.class, new DefaultTableCellRenderer() {
                                    @Override
                                    protected void setValue(Object value) {
                                        BlockPos pos = (BlockPos) value;
                                        setText(String.format("%s / %s / %s", pos.getX(), pos.getY(), pos.getZ()));
                                    }
                                });
                            }

                            // Set custom comparators for the Position columns
                            ((TableRowSorter<?>) tableParts.getRowSorter()).setComparator(IDX_PARTS_POSITION, new BlockPosComparator());
                            ((TableRowSorter<?>) tableObservers.getRowSorter()).setComparator(IDX_OBSERVERS_POSITION, new BlockPosComparator());

                            // Pack GUI
                            JPanel panelMain = new JPanel(new GridLayout(2, 1));
                            JPanel panelParts = new JPanel(new BorderLayout());
                            JPanel panelObservers = new JPanel(new BorderLayout());
                            panelParts.add(BorderLayout.NORTH, new JLabel(L10NHelpers.localize("gui.integrateddynamics.diagnostics.parts")));
                            panelParts.add(BorderLayout.CENTER, new JScrollPane(tableParts));
                            panelObservers.add(BorderLayout.NORTH, new JLabel(L10NHelpers.localize("gui.integrateddynamics.diagnostics.observers")));
                            panelObservers.add(BorderLayout.CENTER, new JScrollPane(tableObservers));
                            panelMain.add(panelParts);
                            panelMain.add(panelObservers);
                            add(panelMain);
                            pack();
                        } else {
                            tableParts.getRowSorter().allRowsChanged();
                            ((DefaultTableModel) tableParts.getModel()).fireTableDataChanged();
                            tableObservers.getRowSorter().allRowsChanged();
                            ((DefaultTableModel) tableObservers.getModel()).fireTableDataChanged();
                        }
                        repaint();
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static ObservablePartData getPartDataFromRow(int row) {
        if (row < 0) {
            return null;
        }
        Object[] data;
        synchronized (networkDataParts) {
            data = networkDataParts.values().toArray();
        }
        int internalId = (int) tableParts.getModel().getValueAt(tableParts.convertRowIndexToModel(row), IDX_PARTS_ID);
        if (internalId < data.length) {
            return (ObservablePartData) data[internalId];
        }
        return null;
    }

    private static ObservableObserverData getObserverDataFromRow(int row) {
        if (row < 0) {
            return null;
        }
        Object[] data;
        synchronized (networkDataParts) {
            data = networkDataObservers.values().toArray();
        }
        int internalId = (int) tableObservers.getModel().getValueAt(tableObservers.convertRowIndexToModel(row), IDX_OBSERVERS_ID);
        if (internalId < data.length) {
            return (ObservableObserverData) data[internalId];
        }
        return null;
    }

    private static void onCloseGui() {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(NetworkDiagnosticsSubscribePacket.unsubscribe());
        NetworkDiagnosticsPartOverlayRenderer.getInstance().clearPositions();
    }

    private static void teleportPlayer(MouseEvent e, PartPos pos) {
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

    @Data
    private static class ObservablePartData {
        private final int networkId;
        private final int networkCables;
        private final int dimension;
        private final BlockPos pos;
        private final EnumFacing side;
        private final String name;
        private final long last20TicksDurationNs;

        private PartPos toPartPos() {
            World world = Minecraft.getMinecraft().world;
            if (getDimension() == world.provider.getDimension()) {
                return PartPos.of(DimPos.of(world, getPos()), getSide());
            }
            return null;
        }
    }

    @Data
    private static class ObservableObserverData {
        private final int networkId;
        private final int dimension;
        private final BlockPos pos;
        private final EnumFacing side;
        private final String name;
        private final long last20TicksDurationNs;

        private PartPos toPartPos() {
            World world = Minecraft.getMinecraft().world;
            if (getDimension() == world.provider.getDimension()) {
                return PartPos.of(DimPos.of(world, getPos()), getSide());
            }
            return null;
        }
    }

    private static class BlockPosComparator implements Comparator<BlockPos> {
        @Override
        public int compare(BlockPos o1, BlockPos o2) {
            if (o1.getX() != o2.getX()) {
                return o1.getX() - o2.getX();
            }
            if (o1.getY() != o2.getY()) {
                return o1.getY() - o2.getY();
            }
            return o1.getZ() - o2.getZ();
        }
    }
}
