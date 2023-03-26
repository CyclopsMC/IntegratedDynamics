package org.cyclops.integrateddynamics.core.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
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
    @Getter
    private IDropdownEntry<T> selectedDropdownPossibility = null;
    @Getter
    @Setter
    private int dropdownSize = 5;
    @Getter
    @Setter
    private IDropdownEntryListener<T> dropdownEntryListener;

    private int enabledColor = 14737632;
    private int disabledColor = 7368816;

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

    protected void refreshDropdownList() {
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
    public boolean charTyped(char typedChar, int keyCode) {
        if (super.charTyped(typedChar, keyCode)) {
            refreshDropdownList();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        selectedDropdownPossibility = null;
        if (!possibilities.isEmpty()) {
            switch (typedChar) {
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
        if (super.keyPressed(typedChar, keyCode, modifiers)) {
            refreshDropdownList();
            return true;
        }
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
    public void renderWidget(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Display text red that is in an "invalid" state (no valid dropdrown entry selected)
        this.setTextColor(this.selectedDropdownPossibility == null ? Helpers.RGBToInt(220, 10, 10) : 14737632);

        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
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
                fill(matrixStack, x, cy - 1, x + width, cy + 11, -6250336);
                fill(matrixStack, x - 1, cy, x + width - 1, cy + 10, -16777216);

                fontRenderer.drawShadow(matrixStack, "...", (float)x + 1, (float)cy + 2, disabledColor);

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
                boolean addTooltip = (active && MinecraftHelpers.isShifted())
                        || RenderHelpers.isPointInRegion(x, cy, getWidth(), yOffset, mouseX, mouseY);
                List<MutableComponent> tooltipLines = null;
                if (addTooltip) {
                    tooltipLines = dropdownEntry.getTooltip();
                    entryHeight += tooltipLines.size() * yOffset;
                }

                // Draw background
                fill(matrixStack, x, cy - 1, x + width, cy + entryHeight + 1, -6250336);
                fill(matrixStack, x - 1, cy, x + width - 1, cy + entryHeight, -16777216);

                // Draw text
                // MCP: drawStringWithShadow
                fontRenderer.drawShadow(matrixStack, displayPossibility.get(0), (float)x + 1, (float)cy + 2, active ? enabledColor : disabledColor);
                if(addTooltip) {
                    int tooltipLineOffsetY = 2;
                    for (Component tooltipLine : tooltipLines) {
                        tooltipLineOffsetY += yOffset;
                        fontRenderer.drawShadow(matrixStack, tooltipLine.getString(), (float)x + 1, (float)cy + tooltipLineOffsetY, enabledColor);
                    }
                }

                cy += entryHeight;
            }

            // Draw ... if we haven't reached the end of the list
            if (endIndex < visiblePossibilities.size()) {
                // Draw background
                fill(matrixStack, x, cy - 1, x + width, cy + 11, -6250336);
                fill(matrixStack, x - 1, cy, x + width - 1, cy + 10, -16777216);

                fontRenderer.drawShadow(matrixStack, "...", (float)x + 1, (float)cy + 2, disabledColor);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.isVisible() && isFocused()) {
            int i = getHoveredVisiblePossibility(mouseX, mouseY);
            if (i >= 0) {
                selectVisiblePossibility(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public int getHoveredVisiblePossibility(double mouseX, double mouseY) {
        Font fontRenderer = Minecraft.getInstance().gui.getFont();
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
            boolean addTooltip = (active && MinecraftHelpers.isShifted())
                    || RenderHelpers.isPointInRegion(x, cy, getWidth(), yOffset, mouseX, mouseY);
            if (RenderHelpers.isPointInRegion(x, cy, getWidth(), yOffset, mouseX, mouseY)) {
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
