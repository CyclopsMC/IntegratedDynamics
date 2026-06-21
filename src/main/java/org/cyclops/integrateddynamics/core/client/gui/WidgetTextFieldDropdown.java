package org.cyclops.integrateddynamics.core.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A text field that can show a dropdown for autocomplete.
 * @param <T> The dropdown entry type.
 * @author rubensworks
 */
public class WidgetTextFieldDropdown<T> extends WidgetTextFieldExtended {

    private Set<IDropdownEntry<T>> possibilities;
    private List<IDropdownEntry<T>> visiblePossibilities = Collections.emptyList();
    private int visiblePossibilitiesIndex = -1;
    private IDropdownEntry<T> selectedDropdownPossibility = null;
    private int dropdownSize = 5;
    private IDropdownEntryListener<T> dropdownEntryListener;

    private int enabledColor = ARGB.opaque(14737632);
    private int disabledColor = ARGB.opaque(7368816);

    public IDropdownEntry<T> getSelectedDropdownPossibility() {
        return selectedDropdownPossibility;
    }

    public int getDropdownSize() {
        return dropdownSize;
    }

    public void setDropdownSize(int dropdownSize) {
        this.dropdownSize = dropdownSize;
    }

    public IDropdownEntryListener<T> getDropdownEntryListener() {
        return dropdownEntryListener;
    }

    public void setDropdownEntryListener(IDropdownEntryListener<T> dropdownEntryListener) {
        this.dropdownEntryListener = dropdownEntryListener;
    }

    public WidgetTextFieldDropdown(Font fontrenderer, int x, int y, int width, int height,
                                   Component narrationMessage, boolean background, Set<IDropdownEntry<T>> possibilities) {
        super(fontrenderer, x, y, width, height, narrationMessage, background);
        setPossibilities(Objects.requireNonNull(possibilities));
    }

    public WidgetTextFieldDropdown(Font fontrenderer, int x, int y, int width, int height,
                                   Component narrationMessage, boolean background) {
        this(fontrenderer, x, y, width, height, narrationMessage, background, Collections.emptySet());
    }

    public void setPossibilities(Set<IDropdownEntry<T>> possibilities) {
        this.possibilities = possibilities;
        this.visiblePossibilities = Collections.emptyList();
    }

    public int getPossibilitiesCount() {
        return possibilities.size();
    }

    @Nullable
    public IDropdownEntry<T> getVisiblePossibility(int index) {
        return visiblePossibilities.get(index);
    }

    public void refreshDropdownList() {
        // Remove all colors and formatting when changing text
        if(getValue().contains("§")) {
            setValue(getValue().replaceAll("§.", ""));
        }
        if (!possibilities.isEmpty()) {
            visiblePossibilities = Lists.newArrayList();
            for (IDropdownEntry<T> possibility : possibilities) {
                if (possibility.getMatchString().toLowerCase().contains(getValue().toLowerCase())) {
                    visiblePossibilities.add(possibility);
                }
            }
            visiblePossibilitiesIndex = -1;
            if (!visiblePossibilities.isEmpty()) {
                selectedDropdownPossibility = visiblePossibilities.stream()
                        .filter(e -> e.getMatchString().equals(getValue()))
                        .findFirst()
                        .orElse(null);
            }
            if (dropdownEntryListener != null) {
                dropdownEntryListener.onSetDropdownPossiblity(selectedDropdownPossibility);
            }
        }
    }
    @Override
    public void setFocused(boolean isFocusedIn) {
        super.setFocused(isFocusedIn);
        if (isFocusedIn) {
            refreshDropdownList();
        }
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (super.charTyped(evt)) {
            refreshDropdownList();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        IDropdownEntry<T> oldPossibility = selectedDropdownPossibility;
        selectedDropdownPossibility = null;
        if (!possibilities.isEmpty()) {
            switch (evt.key()) {
                case GLFW.GLFW_KEY_UP:
                    if (visiblePossibilitiesIndex >= 0) {
                        visiblePossibilitiesIndex--;
                    } else {
                        visiblePossibilitiesIndex = visiblePossibilities.size() - 1;
                    }
                    return true;
                case GLFW.GLFW_KEY_TAB:
                case GLFW.GLFW_KEY_DOWN:
                    if (visiblePossibilitiesIndex < visiblePossibilities.size() - 1) {
                        visiblePossibilitiesIndex++;
                    } else {
                        visiblePossibilitiesIndex = 0;
                    }
                    return true;
                case GLFW.GLFW_KEY_KP_ENTER:
                case GLFW.GLFW_KEY_ENTER:
                case GLFW.GLFW_KEY_RIGHT:
                    if (visiblePossibilitiesIndex >= 0
                            && visiblePossibilitiesIndex < visiblePossibilities.size()) {
                        selectVisiblePossibility(visiblePossibilitiesIndex);
                        return true;
                    }
            }
        }
        if (super.keyPressed(evt)) {
            refreshDropdownList();
            return true;
        }
        selectedDropdownPossibility = oldPossibility;
        return false;
    }

    protected void selectVisiblePossibility(int index) {
        visiblePossibilitiesIndex = index;
        selectPossibility(visiblePossibilities.get(visiblePossibilitiesIndex));
    }

    public void selectPossibility(@Nullable IDropdownEntry<T> entry) {
        selectedDropdownPossibility = entry;
        setValue(selectedDropdownPossibility != null ? selectedDropdownPossibility.getDisplayString().getString() : "");
        visiblePossibilities = Lists.newArrayList();
        visiblePossibilitiesIndex = -1;
        if (dropdownEntryListener != null) {
            dropdownEntryListener.onSetDropdownPossiblity(selectedDropdownPossibility);
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Display text red that is in an "invalid" state (no valid dropdrown entry selected)
        this.setTextColor(this.selectedDropdownPossibility == null ? IModHelpers.get().getBaseHelpers().RGBAToInt(220, 10, 10, 255) : ARGB.opaque(14737632));

        super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.isVisible() && isFocused()) {
            Font fontRenderer = Minecraft.getInstance().font;
            int yOffset = fontRenderer.lineHeight + 3;

            int x = this.getX();
            int y = this.getY() + yOffset;
            int width = this.getWidth() + 9;
            int startIndex = Math.max(0, Math.min(visiblePossibilitiesIndex, visiblePossibilities.size() - getDropdownSize()));
            int endIndex = Math.min(startIndex + getDropdownSize(), visiblePossibilities.size());
            int cy = y;

            // Draw ... if we are not at the first element
            if (startIndex > 0) {
                // Draw background
                guiGraphics.fill(x, cy - 1, x + width, cy + 11, -6250336);
                guiGraphics.fill(x - 1, cy, x + width - 1, cy + 10, -16777216);

                guiGraphics.text(fontRenderer, "...", x + 1, cy + 2, disabledColor, true);

                cy += 10;
            }

            for (int i = startIndex; i < endIndex; i++) {
                // Initialize entry
                IDropdownEntry<?> dropdownEntry = visiblePossibilities.get(i);
                MutableComponent possibility = dropdownEntry.getDisplayString();
                List<FormattedCharSequence> displayPossibility = fontRenderer.split(possibility, width);
                boolean active = visiblePossibilitiesIndex == i;
                int entryHeight = yOffset;

                // Optionally initialize tooltip
                boolean addTooltip = (active && IModHelpers.get().getMinecraftClientHelpers().isShifted())
                        || IModHelpers.get().getRenderHelpers().isPointInRegion(x, cy, getWidth(), yOffset, mouseX, mouseY);
                List<MutableComponent> tooltipLines = null;
                if (addTooltip) {
                    tooltipLines = dropdownEntry.getTooltip();
                    entryHeight += tooltipLines.size() * yOffset;
                }

                // Draw background
                guiGraphics.fill(x, cy - 1, x + width, cy + entryHeight + 1, -6250336);
                guiGraphics.fill(x - 1, cy, x + width - 1, cy + entryHeight, -16777216);

                // Draw text
                guiGraphics.text(fontRenderer, displayPossibility.get(0), x + 1, cy + 2, active ? enabledColor : disabledColor, true);
                if(addTooltip) {
                    int tooltipLineOffsetY = 2;
                    for (Component tooltipLine : tooltipLines) {
                        tooltipLineOffsetY += yOffset;
                        guiGraphics.text(fontRenderer, tooltipLine.getString(), x + 1, cy + tooltipLineOffsetY, enabledColor, true);
                    }
                }

                cy += entryHeight;
            }

            // Draw ... if we haven't reached the end of the list
            if (endIndex < visiblePossibilities.size()) {
                // Draw background
                guiGraphics.fill(x, cy - 1, x + width, cy + 11, -6250336);
                guiGraphics.fill(x - 1, cy, x + width - 1, cy + 10, -16777216);

                guiGraphics.text(fontRenderer, "...", x + 1, cy + 2, disabledColor, true);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        if (this.isVisible() && isFocused()) {
            int i = getHoveredVisiblePossibility(evt.x(), evt.y());
            if (i >= 0) {
                selectVisiblePossibility(i);
                return true;
            }
        }
        return super.mouseClicked(evt, isDoubleClick);
    }

    public int getHoveredVisiblePossibility(double mouseX, double mouseY) {
        Font fontRenderer = Minecraft.getInstance().gui.hud.getFont();
        int yOffset = fontRenderer.lineHeight + 3;

        int x = this.getX();
        int y = this.getY() + yOffset;
        int startIndex = Math.max(0, Math.min(visiblePossibilitiesIndex, visiblePossibilities.size() - getDropdownSize()));
        int endIndex = Math.min(startIndex + getDropdownSize(), visiblePossibilities.size());
        int cy = y;

        // Draw ... if we are not at the first element
        if (startIndex > 0) {
            cy += 10;
        }

        for (int i = startIndex; i < endIndex; i++) {
            // Initialize entry
            IDropdownEntry<?> dropdownEntry = visiblePossibilities.get(i);
            boolean active = visiblePossibilitiesIndex == i;
            int entryHeight = yOffset;

            // Optionally initialize tooltip
            boolean addTooltip = (active && IModHelpers.get().getMinecraftClientHelpers().isShifted())
                    || IModHelpers.get().getRenderHelpers().isPointInRegion(x, cy, getWidth(), yOffset, mouseX, mouseY);
            if (IModHelpers.get().getRenderHelpers().isPointInRegion(x, cy, getWidth(), yOffset, mouseX, mouseY)) {
                return i;
            }
            List<MutableComponent> tooltipLines = null;
            if (addTooltip) {
                tooltipLines = dropdownEntry.getTooltip();
                entryHeight += tooltipLines.size() * yOffset;
            }

            cy += entryHeight;
        }

        return -1;
    }

}
