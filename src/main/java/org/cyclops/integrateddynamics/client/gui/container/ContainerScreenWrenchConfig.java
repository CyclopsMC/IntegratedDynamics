package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonCheckbox;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;
import org.cyclops.cyclopscore.client.gui.image.IImage;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.client.gui.image.Images;
import org.cyclops.integrateddynamics.core.part.PartConfigEntry;
import org.cyclops.integrateddynamics.inventory.container.ContainerWrenchConfig;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gui for looking at the configuration inside a Wrench,
 * and switching off the parts of it that should not be pasted.
 * @author rubensworks
 */
public class ContainerScreenWrenchConfig extends ContainerScreenScrolling<ContainerWrenchConfig> {

    private static final int BOX_WIDTH = 160;
    private static final int BOX_HEIGHT = 18;
    private static final int BOX_X = 9;
    private static final int BOX_Y = 18;
    private static final int BUTTON_X = 12;
    private static final int TITLE_X = 8;
    /**
     * Where the search box sits in the gui texture.
     */
    private static final int SEARCH_BOX_X = 79;
    /**
     * Up to the search box, keeping as much room next to it as the title has on its other side.
     * The width of a string counts the gap after its last letter, which is not part of what is seen.
     */
    private static final int TITLE_WIDTH = SEARCH_BOX_X - TITLE_X * 2 + 1;
    private static final int ICON_X = 25;
    private static final int ICON_SIZE = 16;
    private static final int LABEL_X = 43;
    private static final int VALUE_X = 128;
    /**
     * Where the name of a row ends, which is the same for every row,
     * so that the names stay under each other whatever a row happens to show on its right.
     */
    private static final int LABEL_END = VALUE_X - 3;
    private static final int VALUE_WIDTH = 38;
    private static final int VALUE_HEIGHT = 10;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_X = VALUE_X + VALUE_WIDTH - SLOT_SIZE;
    /**
     * A slot is a pixel taller than the row that holds it, so it hangs over the line above rather than the one below.
     */
    private static final int SLOT_Y = -1;
    /**
     * Where an empty slot sits inside the gui texture, which is where the player inventory starts.
     */
    private static final int SLOT_TEXTURE_X = 8;
    private static final int SLOT_TEXTURE_Y = 139;

    /**
     * What the row of an entry that belongs to no value type is tinted in, which is nothing at all.
     */
    private static final int COLOR_NEUTRAL = IModHelpers.get().getBaseHelpers().RGBToInt(255, 255, 255);

    private final Map<String, ButtonCheckbox> entryButtons = Maps.newHashMap();
    /**
     * The item of every aspect that is shown, as it stays the same for as long as this gui is open.
     */
    private final Map<String, ItemStack> aspectIcons = Maps.newHashMap();

    public ContainerScreenWrenchConfig(ContainerWrenchConfig container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected Rectangle getScrollRegion() {
        return new Rectangle(this.leftPos + BOX_X, this.topPos + BOX_Y, BOX_WIDTH, 105);
    }

    @Override
    public void init() {
        clearWidgets();
        this.entryButtons.clear();
        this.aspectIcons.clear();
        super.init();

        for (PartConfigEntry entry : getMenu().getEntries()) {
            // Parked outside of the gui until it turns out to be on the page that is shown
            ButtonCheckbox button = new ButtonCheckbox(-20, -20, 12, 12,
                    Component.translatable("gui.integrateddynamics.wrench_config.entry"),
                    createServerPressable(entry.id(), b -> {}));
            this.entryButtons.put(entry.id(), button);
            addRenderableWidget(button);
        }

        // Next to the gui, where this mod puts the buttons of a part as well
        ButtonImage toggleAll = new ButtonImage(this.leftPos - 20, this.topPos, 18, 18,
                Component.translatable("gui.integrateddynamics.wrench_config.toggle_all"),
                createServerPressable(ContainerWrenchConfig.BUTTON_TOGGLE_ALL, b -> {}),
                new IImage[]{Images.BUTTON_BACKGROUND_INACTIVE, Images.BUTTON_MIDDLE_CHECK_ALL},
                false, 0, 0);
        toggleAll.setTooltip(Tooltip.create(Component.translatable(
                "gui.integrateddynamics.wrench_config.toggle_all")));
        addRenderableWidget(toggleAll);
    }

    @Override
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/wrench_config.png");
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);

        for (ButtonCheckbox button : this.entryButtons.values()) {
            button.setX(-20);
            button.setY(-20);
        }

        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            PartConfigEntry entry = container.getVisibleElement(i);
            int x = this.leftPos + offsetX;
            int y = this.topPos + offsetY + BOX_Y + BOX_HEIGHT * i;

            // The same colour that a part gui gives the value type of an aspect
            Triple<Float, Float, Float> rgb = IModHelpers.get().getBaseHelpers().intToRGB(entry.color().orElse(COLOR_NEUTRAL));
            int color = ARGB.colorFromFloat(1F, colorSmoothener(rgb.getLeft()), colorSmoothener(rgb.getMiddle()),
                    colorSmoothener(rgb.getRight()));
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + BOX_X, y, 0, getBaseYSize(),
                    BOX_WIDTH, BOX_HEIGHT - 1, 256, 256, color);

            // The same item that a part gui shows for an aspect
            ItemStack aspectIcon = getAspectIcon(entry);
            if (!aspectIcon.isEmpty()) {
                guiGraphics.item(aspectIcon, x + ICON_X, y);
            }

            // In a slot of its own, so that a card looks the same here as it does inside a part
            if (!entry.icon().isEmpty()) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + SLOT_X, y + SLOT_Y,
                        SLOT_TEXTURE_X, SLOT_TEXTURE_Y, SLOT_SIZE, SLOT_SIZE, 256, 256);
                guiGraphics.item(entry.icon(), x + SLOT_X + 1, y + SLOT_Y + 1);
                guiGraphics.itemDecorations(font, entry.icon(), x + SLOT_X + 1, y + SLOT_Y + 1);
            }

            ButtonCheckbox button = this.entryButtons.get(entry.id());
            if (button != null) {
                button.setChecked(container.isEntryEnabled(entry.id()));
                button.setX(x + BUTTON_X);
                button.setY(y + 3);
            }
        }
    }

    // Everything that has to end up on top of the buttons is drawn here, as the buttons are drawn after the background
    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // The gui has no room for the usual labels, what it holds goes in the bar at the top instead,
        // shrunk when needed so that it never runs into the search field next to it
        IModHelpers.get().getRenderHelpers().drawScaledString(guiGraphics, font, this.title.getString(),
                offsetX + TITLE_X, offsetY + 6, Math.min(1F, (float) TITLE_WIDTH / font.width(this.title)),
                IModHelpers.get().getBaseHelpers().RGBAToInt(64, 64, 64, 255), false, Font.DisplayMode.NORMAL);

        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            PartConfigEntry entry = container.getVisibleElement(i);
            int y = offsetY + BOX_Y + BOX_HEIGHT * i;
            String value = entry.value().getString();
            // Only what belongs to an aspect is indented, to leave room for what says which aspect that is
            int labelX = belongsToAspect(entry) ? LABEL_X : ICON_X + 2;
            int labelWidth = LABEL_END - labelX;

            // The mark that a part gui puts on the button that opens the settings of an aspect,
            // without the button around it, as there is nothing to open here
            if (isPartOfAspect(entry)) {
                IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, font,
                        "+", offsetX + ICON_X, y + 9, ICON_SIZE,
                        IModHelpers.get().getBaseHelpers().RGBAToInt(90, 90, 90, 255), false, Font.DisplayMode.NORMAL);
            }

            // What an entry belongs to is told by the tooltip, as a row only has room for its name
            IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, font,
                    entry.label().getString(), offsetX + labelX, y + 9, labelWidth,
                    IModHelpers.get().getBaseHelpers().RGBAToInt(40, 40, 40, 255), false, Font.DisplayMode.NORMAL);

            if (!value.isEmpty()) {
                // A row that also holds a slot leaves room for it
                int valueWidth = entry.icon().isEmpty() ? VALUE_WIDTH : SLOT_X - 2 - VALUE_X;
                drawValueBox(guiGraphics, offsetX + VALUE_X, y + 3, valueWidth);
                // In the colour of its value type, the same way that the part gui shows a property value
                IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, font,
                        value, offsetX + VALUE_X + 2, y + 9, valueWidth - 4, 1F, valueWidth - 4,
                        ARGB.opaque(getColor(entry.value(), IModHelpers.get().getBaseHelpers().RGBToInt(255, 255, 255))),
                        false, Font.DisplayMode.NORMAL);
            }

        }
    }

    // Tooltips are drawn here rather than with the rows,
    // as inside the gui they would be kept away from the wrong edge of the screen
    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(guiGraphics, mouseX, mouseY);

        ContainerWrenchConfig container = getMenu();
        for (int i = 0; i < container.getPageSize(); i++) {
            if (!container.isElementVisible(i)) {
                continue;
            }
            PartConfigEntry entry = container.getVisibleElement(i);
            int y = offsetY + BOX_Y + BOX_HEIGHT * i;
            if (entry.aspect().isPresent()
                    && isHovering(offsetX + ICON_X, y, ICON_SIZE, ICON_SIZE, mouseX, mouseY)) {
                // What a part gui tells about an aspect when its item is hovered
                List<Component> lines = Lists.newArrayList();
                IAspect<?, ?> aspect = Aspects.REGISTRY.getAspect(entry.aspect().get());
                if (aspect != null) {
                    aspect.loadTooltip(lines::add, true);
                    guiGraphics.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
                }
                return;
            }
            if (!entry.icon().isEmpty()
                    && isHovering(offsetX + SLOT_X, y + SLOT_Y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
                // The card itself is shown, so it tells the player what it holds just like it does anywhere else
                guiGraphics.setTooltipForNextFrame(font, getTooltipFromItem(this.minecraft, entry.icon()),
                        entry.icon().getTooltipImage(), entry.icon(), mouseX, mouseY);
                return;
            }
            // An aspect is told about by its item, so its name needs no tooltip of its own
            if (entry.aspect().isEmpty()
                    && isHovering(offsetX + BOX_X, y, BOX_WIDTH, BOX_HEIGHT - 1, mouseX, mouseY)) {
                guiGraphics.setComponentTooltipForNextFrame(font, getEntryTooltip(entry), mouseX, mouseY);
                return;
            }
        }
    }

    /**
     * @param entry An entry that is shown.
     * @return What that entry holds, in full, as the row itself has no room for all of it.
     */
    protected List<Component> getEntryTooltip(PartConfigEntry entry) {
        List<Component> lines = Lists.newArrayList();
        lines.add(entry.label().copy().withStyle(ChatFormatting.WHITE));
        if (isPartOfAspect(entry) && !entry.group().getString().isEmpty()) {
            // The aspect of a property says more about it than the section that it is in
            lines.add(Component.translatable("gui.integrateddynamics.wrench_config.entry.aspect",
                    entry.group().copy()).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable(entry.section().getGuiTranslationKey())
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        if (!entry.value().getString().isEmpty()) {
            lines.add(Component.translatable("gui.integrateddynamics.wrench_config.entry.value",
                    entry.value().copy()).withStyle(ChatFormatting.GRAY));
        }
        getEntryDescription(entry).ifPresent(description ->
                lines.add(description.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        return lines;
    }

    /**
     * @param entry An entry that is shown.
     * @return What the rest of this mod says about the thing that the entry holds, if it says anything.
     */
    protected Optional<MutableComponent> getEntryDescription(PartConfigEntry entry) {
        if (!(entry.label().getContents() instanceof TranslatableContents contents)) {
            return Optional.empty();
        }
        // The same key that a part gui shows the description of an aspect property under
        String key = contents.getKey() + ".info";
        return I18n.exists(key) ? Optional.of(Component.translatable(key)) : Optional.empty();
    }

    /**
     * An inset box in the style of the rest of this gui, to set a value apart from the name next to it.
     */
    protected void drawValueBox(GuiGraphicsExtractor guiGraphics, int x, int y, int width) {
        guiGraphics.fill(x, y, x + width, y + VALUE_HEIGHT,
                IModHelpers.get().getBaseHelpers().RGBAToInt(55, 55, 55, 255));
        guiGraphics.fill(x + 1, y + 1, x + width + 1, y + VALUE_HEIGHT + 1,
                IModHelpers.get().getBaseHelpers().RGBAToInt(255, 255, 255, 255));
        guiGraphics.fill(x + 1, y + 1, x + width, y + VALUE_HEIGHT,
                IModHelpers.get().getBaseHelpers().RGBAToInt(139, 139, 139, 255));
    }

    /**
     * @param entry An entry that is shown.
     * @return The item that a part gui shows for the aspect of that entry, if it stands for one.
     */
    protected ItemStack getAspectIcon(PartConfigEntry entry) {
        if (entry.aspect().isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.aspectIcons.computeIfAbsent(entry.id(), id -> {
            IAspect<?, ?> aspect = Aspects.REGISTRY.getAspect(entry.aspect().get());
            if (aspect == null) {
                return ItemStack.EMPTY;
            }
            // A variable card that reads from the aspect, which is what makes its item show that aspect.
            // The part that this was copied from is not known anymore, so it gets the first part id.
            return IntegratedDynamics._instance.getRegistryManager()
                    .getRegistry(IVariableFacadeHandlerRegistry.class)
                    .writeVariableFacadeItem(new ItemStack(RegistryEntries.ITEM_VARIABLE.get()),
                            new AspectVariableFacade(false, 0, aspect), Aspects.REGISTRY);
        });
    }

    /**
     * @param entry An entry that is shown.
     * @return If that entry is part of an aspect rather than being one itself.
     */
    protected boolean isPartOfAspect(PartConfigEntry entry) {
        return entry.aspect().isEmpty() && entry.color().isPresent();
    }

    /**
     * @param entry An entry that is shown.
     * @return If that entry is an aspect or part of one, which is what is shown left of its name.
     */
    protected boolean belongsToAspect(PartConfigEntry entry) {
        return entry.aspect().isPresent() || isPartOfAspect(entry);
    }

    protected int getColor(Component component, int fallback) {
        TextColor color = component.getStyle().getColor();
        return color == null ? fallback : color.getValue();
    }

    protected float colorSmoothener(float color) {
        return 1F - ((1F - color) / 4F);
    }

    @Override
    protected int getBaseXSize() {
        return 195;
    }

    @Override
    protected int getBaseYSize() {
        return 222;
    }

}
