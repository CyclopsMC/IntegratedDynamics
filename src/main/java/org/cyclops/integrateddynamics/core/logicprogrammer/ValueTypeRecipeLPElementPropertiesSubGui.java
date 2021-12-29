package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonCheckbox;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.client.gui.IDropdownEntry;
import org.cyclops.integrateddynamics.core.client.gui.WidgetTextFieldDropdown;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.ingredient.ItemMatchProperties;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Selection panel for the list element value type.
 */
@OnlyIn(Dist.CLIENT)
class ValueTypeRecipeLPElementPropertiesSubGui extends RenderPattern<ValueTypeRecipeLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private final int slotId;
    private ButtonCheckbox inputNbt;
    private ButtonCheckbox inputTags;
    private WidgetTextFieldDropdown<ResourceLocation> inputTagsDropdown;
    private ButtonImage inputSave;

    public ValueTypeRecipeLPElementPropertiesSubGui(ValueTypeRecipeLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                                    ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container,
                                                    int slotId) {
        super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        this.slotId = slotId;
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        super.init(guiLeft, guiTop);

        this.inputNbt = new ButtonCheckbox(guiLeft + getX() + 2, guiTop + getY() + 2, 110, 10,
                new TranslatableComponent(L10NValues.GUI_RECIPE_STRICTNBT), (entry) ->  {
            // Only allow one checkbox to be true at the same time
            if (this.inputNbt.isChecked()) {
                this.inputTags.setChecked(false);
            }
            saveGuiToState();
            loadStateToGui();
        });
        this.inputTags = new ButtonCheckbox(guiLeft + getX() + 2, guiTop + getY() + 12, 110, 10,
                new TranslatableComponent(L10NValues.GUI_RECIPE_TAGVARIANTS), (entry) -> {
            // Only allow one checkbox to be true at the same time
            if (this.inputTags.isChecked()) {
                this.inputNbt.setChecked(false);
            }
            saveGuiToState();
            loadStateToGui();
            if (this.inputTags.isChecked()) {
                this.inputTagsDropdown.setFocus(true);
            }
        });
        this.inputTagsDropdown = new WidgetTextFieldDropdown<>(Minecraft.getInstance().font,
                guiLeft + getX() + 2, guiTop + getY() + 23,
                134, 14,
                new TranslatableComponent("gui.cyclopscore.search"), true,
                Sets.newHashSet());
        this.inputTagsDropdown.setDropdownEntryListener((entry) -> saveGuiToState());
        this.inputTagsDropdown.setMaxLength(64);
        this.inputTagsDropdown.setDropdownSize(4);
        this.inputTagsDropdown.setBordered(false);
        this.inputTagsDropdown.setTextColor(16777215);
        this.inputTagsDropdown.setCanLoseFocus(true);
        this.inputSave = new ButtonImage(guiLeft + getX() + 116, guiTop + getY() + 72,
                new TranslatableComponent("gui.integrateddynamics.button.save"),
                (button) -> {
            // If tag checkbox is checked, only allow exiting if a valid tag has been set
            if (!this.inputTags.isChecked() || this.inputTagsDropdown.getSelectedDropdownPossibility() != null) {
                element.lastGui.setRecipeSubGui();
            } else {
                this.inputTagsDropdown.changeFocus(true);
            }
                }, Images.OK);

        // Load button states
        loadStateToGui();
        // Show dropdown if a tag was already set
        if (this.inputTags.isChecked()) {
            this.inputTagsDropdown.changeFocus(true);
        }
    }

    public ItemStack getSlotContents() {
        return container.slots.get(slotId + ValueTypeRecipeLPElement.SLOT_OFFSET).getItem();
    }

    public ItemMatchProperties getSlotProperties() {
        return getElement().getInputStacks().get(slotId);
    }

    private Set<IDropdownEntry<ResourceLocation>> getDropdownEntries() {
        LinkedHashSet<IDropdownEntry<ResourceLocation>> set = Sets.newLinkedHashSet();
        if (getSlotContents().isEmpty()) {
            for (ResourceLocation registeredTag : ItemTags.getAllTags().getAvailableTags()) {
                set.add(new DropdownEntry(registeredTag));
            }

        } else {
            for (ResourceLocation registeredTag : ItemTags.getAllTags().getMatchingTags(getSlotContents().getItem())) {
                set.add(new DropdownEntry(registeredTag));
            }
        }
        return set;
    }

    public void loadStateToGui() {
        ItemMatchProperties props = getSlotProperties();
        this.inputNbt.setChecked(props.isNbt());
        this.inputTags.setChecked(props.getItemTag() != null);
        this.inputTagsDropdown.setVisible(this.inputTags.isChecked());

        if (this.inputTags.isChecked()) {
            Set<IDropdownEntry<ResourceLocation>> dropdownEntries = getDropdownEntries();
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
        element.sendSlotPropertiesToServer(slotId, getSlotProperties());
    }

    @Override
    protected boolean drawRenderPattern() {
        return false;
    }

    @Override
    public void renderBg(PoseStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

        drawSlot(matrixStack, getX() + guiLeft + 116, getY() + guiTop + 2);

        this.inputNbt.render(matrixStack, mouseX, mouseY, partialTicks);
        fontRenderer.draw(matrixStack, L10NHelpers.localize(L10NValues.GUI_RECIPE_STRICTNBT), guiLeft + getX() + 24, guiTop + getY() + 3, 0);
        this.inputTags.render(matrixStack, mouseX, mouseY, partialTicks);
        fontRenderer.draw(matrixStack, L10NHelpers.localize(L10NValues.GUI_RECIPE_TAGVARIANTS), guiLeft + getX() + 24, guiTop + getY() + 13, 0);
        this.inputSave.render(matrixStack, mouseX, mouseY, partialTicks);
        this.inputTagsDropdown.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawGuiContainerForegroundLayer(PoseStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

        if (this.inputTagsDropdown.isFocused()) {
            int i = this.inputTagsDropdown.getHoveredVisiblePossibility(mouseX, mouseY);
            if (i >= 0) {
                IDropdownEntry<ResourceLocation> hoveredPossibility = this.inputTagsDropdown.getVisiblePossibility(i);
                drawTagsTooltip(matrixStack, hoveredPossibility, guiLeft, guiTop, mouseX + 10, mouseY - 20, 6, GuiHelpers.SLOT_SIZE);
            }
        }
    }

    protected void drawTagsTooltip(PoseStack poseStack, IDropdownEntry<ResourceLocation> hoveredPossibility, int guiLeft, int guiTop,
                                   int mouseX, int mouseY, int columns, int offset) {
        int x = mouseX - guiLeft;
        int y = mouseY - guiTop;
        List<Item> items = ItemTags.getAllTags().getTag(hoveredPossibility.getValue()).getValues();

        // Draw background
        GuiHelpers.drawTooltipBackground(poseStack, x, y, Math.min(items.size(), columns) * offset,
                ((items.size() % columns == 0 ? 0 : 1) + (items.size() / columns)) * offset);

        // Draw item grid
        int passed = 0;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.blitOffset = 300F;
        for (Item item : items) {
            itemRenderer.renderGuiItem(new ItemStack(item), x, y);
            x += offset;
            if (passed++ % columns == columns - 1) {
                y += offset;
                x = mouseX - guiLeft;
            }
        }
        itemRenderer.blitOffset = 0F;
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (inputTagsDropdown.isFocused()) {
            if (inputTagsDropdown.charTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (inputTagsDropdown.isFocused()) {
            inputTagsDropdown.keyPressed(typedChar, keyCode, modifiers);
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return inputNbt.mouseClicked(mouseX, mouseY, mouseButton)
                || inputTags.mouseClicked(mouseX, mouseY, mouseButton)
                || inputTagsDropdown.mouseClicked(mouseX, mouseY, mouseButton)
                || inputSave.mouseClicked(mouseX, mouseY, mouseButton)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public static class DropdownEntry implements IDropdownEntry<ResourceLocation> {
        private final ResourceLocation tag;

        public DropdownEntry(ResourceLocation tag) {
            this.tag = tag;
        }

        @Override
        public String getMatchString() {
            return this.tag.toString();
        }

        @Override
        public MutableComponent getDisplayString() {
            return new TextComponent(this.tag.toString());
        }

        @Override
        public List<MutableComponent> getTooltip() {
            return Collections.emptyList();
        }

        @Override
        public ResourceLocation getValue() {
            return this.tag;
        }
    }
}
