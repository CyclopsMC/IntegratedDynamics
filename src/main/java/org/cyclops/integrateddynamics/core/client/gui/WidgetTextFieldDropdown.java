package org.cyclops.integrateddynamics.core.client.gui;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.lwjgl.glfw.GLFW;

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

    private final Set<IDropdownEntry<T>> possibilities;
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

    public WidgetTextFieldDropdown(FontRenderer fontrenderer, int x, int y, int width, int height,
                                   String narrationMessage, boolean background, Set<IDropdownEntry<T>> possibilities) {
        super(fontrenderer, x, y, width, height, narrationMessage, background);
        this.possibilities = Objects.requireNonNull(possibilities);
    }

    public WidgetTextFieldDropdown(FontRenderer fontrenderer, int x, int y, int width, int height,
                                   String narrationMessage, boolean background) {
        this(fontrenderer, x, y, width, height, narrationMessage, background, Collections.emptySet());
    }

    protected void refreshDropdownList() {
        // Remove all colors and formatting when changing text
        if(getText().contains("§")) {
            setText(getText().replaceAll("§.", ""));
        }
        if (!possibilities.isEmpty()) {
            visiblePossibilities = Lists.newArrayList();
            for (IDropdownEntry<T> possibility : possibilities) {
                if (possibility.getMatchString().toLowerCase().contains(getText().toLowerCase())) {
                    visiblePossibilities.add(possibility);
                }
            }
            visiblePossibilitiesIndex = -1;
            if (visiblePossibilities.size() == 1 && visiblePossibilities.get(0).getMatchString().equals(getText())) {
                selectedDropdownPossibility = visiblePossibilities.get(0);
            }
            if (dropdownEntryListener != null) {
                dropdownEntryListener.onSetDropdownPossiblity(selectedDropdownPossibility);
            }
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

    public void selectPossibility(IDropdownEntry<T> entry) {
        selectedDropdownPossibility = entry;
        setText(selectedDropdownPossibility.getDisplayString());
        visiblePossibilities = Lists.newArrayList();
        visiblePossibilitiesIndex = -1;
        if (dropdownEntryListener != null) {
            dropdownEntryListener.onSetDropdownPossiblity(selectedDropdownPossibility);
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        if (this.getVisible() && isFocused()) {
            FontRenderer fontRenderer = Minecraft.getInstance().getRenderManager().getFontRenderer();
            int yOffset = fontRenderer.FONT_HEIGHT + 3;

            int x = this.x;
            int y = this.y + yOffset;
            int width = this.getWidth() + 9;
            int startIndex = Math.max(0, Math.min(visiblePossibilitiesIndex, visiblePossibilities.size() - getDropdownSize()));
            int endIndex = Math.min(startIndex + getDropdownSize(), visiblePossibilities.size());
            int cy = y;

            // Draw ... if we are not at the first element
            if (startIndex > 0) {
                // Draw background
                fill(x, cy - 1, x + width, cy + 11, -6250336);
                fill(x - 1, cy, x + width - 1, cy + 10, -16777216);

                fontRenderer.drawStringWithShadow("...", (float)x + 1, (float)cy + 2, disabledColor);

                cy += 10;
            }

            for (int i = startIndex; i < endIndex; i++) {
                // Initialize entry
                IDropdownEntry<?> dropdownEntry = visiblePossibilities.get(i);
                String possibility = dropdownEntry.getDisplayString();
                String displayPossibility = fontRenderer.trimStringToWidth(possibility, width);
                boolean active = visiblePossibilitiesIndex == i;
                int entryHeight = yOffset;

                // Optionally initialize tooltip
                boolean addTooltip = (active && MinecraftHelpers.isShifted())
                        || RenderHelpers.isPointInRegion(x, cy, getWidth(), yOffset, mouseX, mouseY);
                List<ITextComponent> tooltipLines = null;
                if (addTooltip) {
                    tooltipLines = dropdownEntry.getTooltip();
                    entryHeight += tooltipLines.size() * yOffset;
                }

                // Draw background
                fill(x, cy - 1, x + width, cy + entryHeight + 1, -6250336);
                fill(x - 1, cy, x + width - 1, cy + entryHeight, -16777216);

                // Draw text
                fontRenderer.drawStringWithShadow(displayPossibility, (float)x + 1, (float)cy + 2, active ? enabledColor : disabledColor);
                if(addTooltip) {
                    int tooltipLineOffsetY = 2;
                    for (ITextComponent tooltipLine : tooltipLines) {
                        tooltipLineOffsetY += yOffset;
                        fontRenderer.drawStringWithShadow(tooltipLine.getFormattedText(), (float)x + 1, (float)cy + tooltipLineOffsetY, enabledColor);
                    }
                }

                cy += entryHeight;
            }

            // Draw ... if we haven't reached the end of the list
            if (endIndex < visiblePossibilities.size()) {
                // Draw background
                fill(x, cy - 1, x + width, cy + 11, -6250336);
                fill(x - 1, cy, x + width - 1, cy + 10, -16777216);

                fontRenderer.drawStringWithShadow("...", (float)x + 1, (float)cy + 2, disabledColor);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.getVisible() && isFocused()) {
            FontRenderer fontRenderer = Minecraft.getInstance().getRenderManager().getFontRenderer();
            int yOffset = fontRenderer.FONT_HEIGHT + 3;

            int x = this.x;
            int y = this.y + yOffset;
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
                    selectVisiblePossibility(i);
                    return true;
                }
                List<ITextComponent> tooltipLines = null;
                if (addTooltip) {
                    tooltipLines = dropdownEntry.getTooltip();
                    entryHeight += tooltipLines.size() * yOffset;
                }

                cy += entryHeight;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
