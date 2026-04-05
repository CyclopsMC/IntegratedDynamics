package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonCheckbox;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.WidgetTextFieldDropdown;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.ingredient.ItemMatchProperties;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.RenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Selection panel for the list element value type.
 */
public abstract class ValueTypeRecipeAdapterLPElementPropertiesSubGui<E extends IGuiInputElement> extends RenderPattern<E, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    protected final int slotId;
    protected ButtonCheckbox inputNbt;
    protected ButtonCheckbox inputTags;
    protected ButtonCheckbox inputReusable;
    protected WidgetTextFieldDropdown<Identifier> inputTagsDropdown;
    protected ButtonImage inputSave;

    public ValueTypeRecipeAdapterLPElementPropertiesSubGui(E element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                           ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container,
                                                           int slotId) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.slotId = slotId;
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);

        this.inputNbt = new ButtonCheckbox(guiLeft + getX() + 2, guiTop + getY() + 2, 20, 10,
                Component.translatable(L10NValues.GUI_RECIPE_STRICTNBT), (entry) ->  {
            // Only allow one checkbox to be true at the same time
            if (this.inputNbt.isChecked()) {
                this.inputTags.setChecked(false);
            }
            saveGuiToState();
            loadStateToGui();
        });
        this.inputReusable = new ButtonCheckbox(guiLeft + getX() + 2, guiTop + getY() + 12, 20, 10,
                Component.translatable(L10NValues.GUI_RECIPE_REUSABLE), (entry) -> {
            saveGuiToState();
            loadStateToGui();
        });
        this.inputTags = new ButtonCheckbox(guiLeft + getX() + 2, guiTop + getY() + 22, 20, 10,
                Component.translatable(L10NValues.GUI_RECIPE_TAGVARIANTS), (entry) -> {
            // Only allow one checkbox to be true at the same time
            if (this.inputTags.isChecked()) {
                this.inputNbt.setChecked(false);
            }
            saveGuiToState();
            loadStateToGui();
            if (this.inputTags.isChecked()) {
                this.inputTagsDropdown.setFocused(true);
            }
        });
        this.inputTagsDropdown = new WidgetTextFieldDropdown<>(Minecraft.getInstance().font,
                guiLeft + getX() + 2, guiTop + getY() + 33,
                134, 14,
                Component.translatable("gui.cyclopscore.search"), true,
                Sets.newHashSet());
        this.inputTagsDropdown.setDropdownEntryListener((entry) -> saveGuiToState());
        this.inputTagsDropdown.setMaxLength(64);
        this.inputTagsDropdown.setDropdownSize(4);
        this.inputTagsDropdown.setBordered(false);
        this.inputTagsDropdown.setTextColor(ARGB.opaque(16777215));
        this.inputTagsDropdown.setCanLoseFocus(true);
        this.inputSave = new ButtonImage(guiLeft + getX() + 116, guiTop + getY() + 72,
                Component.translatable("gui.integrateddynamics.button.save"),
                (button) -> {
            // If tag checkbox is checked, only allow exiting if a valid tag has been set
            if (!this.inputTags.isChecked() || this.inputTagsDropdown.getSelectedDropdownPossibility() != null) {
                returnToMainGui();
            } else {
                this.inputTagsDropdown.setFocused(true);
            }
                }, Images.OK);

        // Load button states
        loadStateToGui();
        // Show dropdown if a tag was already set
        if (this.inputTags.isChecked()) {
            this.inputTagsDropdown.setFocused(true);
        }
    }

    protected abstract void returnToMainGui();

    public abstract ItemStack getSlotContents();

    public abstract ItemMatchProperties getSlotProperties();

    private Set<IDropdownEntry<Identifier>> getDropdownEntries() {
        LinkedHashSet<IDropdownEntry<Identifier>> set = Sets.newLinkedHashSet();
        if (getSlotContents().isEmpty()) {
            BuiltInRegistries.ITEM.listTagIds()
                    .forEach(registeredTag -> set.add(new DropdownEntry(registeredTag.location())));

        } else {
            getSlotContents().getItem().builtInRegistryHolder().tags()
                    .forEach(registeredTag -> set.add(new DropdownEntry(registeredTag.location())));
        }
        return set;
    }

    public void loadStateToGui() {
        ItemMatchProperties props = getSlotProperties();
        this.inputNbt.setChecked(props.isNbt());
        this.inputTags.setChecked(props.getItemTag() != null);
        this.inputReusable.setChecked(props.isReusable());
        this.inputTagsDropdown.setVisible(this.inputTags.isChecked());

        if (this.inputTags.isChecked()) {
            Set<IDropdownEntry<Identifier>> dropdownEntries = getDropdownEntries();
            this.inputTagsDropdown.setPossibilities(dropdownEntries);
            if (props.getItemTag() != null) {
                this.inputTagsDropdown.selectPossibility(dropdownEntries.stream()
                        .filter(e -> e.getMatchString().equals(props.getItemTag()))
                        .findFirst()
                        .orElse(null));
            } else {
                if (!dropdownEntries.isEmpty()) {
                    this.inputTagsDropdown.selectPossibility(dropdownEntries.iterator().next());
                } else {
                    this.inputTagsDropdown.selectPossibility(null);
                }
            }
        } else {
            this.inputTagsDropdown.setValue("");
            this.inputTagsDropdown.setPossibilities(Collections.emptySet());
        }
    }

    public void saveGuiToState() {
        boolean nbt = this.inputNbt.isChecked();
        String tag = this.inputTags.isChecked() ? this.inputTagsDropdown.getValue() : null;
        getSlotProperties().setNbt(nbt);
        getSlotProperties().setItemTag(tag);
        getSlotProperties().setReusable(this.inputReusable.isChecked());
    }

    @Override
    protected boolean drawRenderPattern() {
        return false;
    }

    @Override
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

        drawSlot(guiGraphics, getX() + guiLeft + 116, getY() + guiTop + 2);

        this.inputNbt.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.text(fontRenderer, IModHelpers.get().getL10NHelpers().localize(L10NValues.GUI_RECIPE_STRICTNBT), guiLeft + getX() + 24, guiTop + getY() + 3, ARGB.opaque(0), false);
        this.inputReusable.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.text(fontRenderer, IModHelpers.get().getL10NHelpers().localize(L10NValues.GUI_RECIPE_REUSABLE), guiLeft + getX() + 24, guiTop + getY() + 13, ARGB.opaque(0), false);
        this.inputTags.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.text(fontRenderer, IModHelpers.get().getL10NHelpers().localize(L10NValues.GUI_RECIPE_TAGVARIANTS), guiLeft + getX() + 24, guiTop + getY() + 23, ARGB.opaque(0), false);
        this.inputSave.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        this.inputTagsDropdown.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

        if (this.inputTagsDropdown.isFocused()) {
            int i = this.inputTagsDropdown.getHoveredVisiblePossibility(mouseX, mouseY);
            if (i >= 0) {
                IDropdownEntry<Identifier> hoveredPossibility = this.inputTagsDropdown.getVisiblePossibility(i);
                drawTagsTooltip(guiGraphics, hoveredPossibility, guiLeft, guiTop, mouseX + 10, mouseY - 20, 6, IModHelpers.get().getGuiHelpers().getSlotSize());
            }
        }
    }

    protected void drawTagsTooltip(GuiGraphicsExtractor guiGraphics, IDropdownEntry<Identifier> hoveredPossibility, int guiLeft, int guiTop,
                                   int mouseX, int mouseY, int columns, int offset) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;
        List<Item> items = BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, hoveredPossibility.getValue()))
                .stream()
                .flatMap(HolderSet.ListBacked::stream)
                .map(Holder::value)
                .toList();

        // Draw background
        TooltipRenderUtil.extractTooltipBackground(guiGraphics, x, y, Math.min(items.size(), columns) * offset,
                ((items.size() % columns == 0 ? 0 : 1) + (items.size() / columns)) * offset, null);

        // Draw item grid
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0, 0);
        int passed = 0;
        for (Item item : items) {
            guiGraphics.item(new ItemStack(item), x, y);
            x += offset;
            if (passed++ % columns == columns - 1) {
                y += offset;
                x = mouseX - guiLeft;
            }
        }
        guiGraphics.pose().popMatrix();
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (inputTagsDropdown.isFocused()) {
            if (inputTagsDropdown.charTyped(evt)) {
                return true;
            }
        }
        return super.charTyped(evt);
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (inputTagsDropdown.isFocused()) {
            inputTagsDropdown.keyPressed(evt);
            return true;
        }
        return super.keyPressed(evt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        return inputNbt.mouseClicked(evt, isDoubleClick)
                || inputReusable.mouseClicked(evt, isDoubleClick)
                || inputTags.mouseClicked(evt, isDoubleClick)
                || inputTagsDropdown.mouseClicked(evt, isDoubleClick)
                || inputSave.mouseClicked(evt, isDoubleClick)
                || super.mouseClicked(evt, isDoubleClick);
    }

    public static class DropdownEntry implements IDropdownEntry<Identifier> {
        private final Identifier tag;

        public DropdownEntry(Identifier tag) {
            this.tag = tag;
        }

        @Override
        public String getMatchString() {
            return this.tag.toString();
        }

        @Override
        public MutableComponent getDisplayString() {
            return Component.literal(this.tag.toString());
        }

        @Override
        public List<MutableComponent> getTooltip() {
            return Collections.emptyList();
        }

        @Override
        public Identifier getValue() {
            return this.tag;
        }
    }
}
