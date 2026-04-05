package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;
import org.cyclops.integrateddynamics.network.packet.ItemStackRenamePacket;
import org.lwjgl.glfw.GLFW;

/**
 * Gui for the labeller.
 * @author rubensworks
 */
public class ContainerScreenLabeller extends ContainerScreenExtended<ContainerLabeller> {

    private WidgetTextFieldExtended searchField;

    public ContainerScreenLabeller(ContainerLabeller container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        SimpleInventory temporaryInputSlots = container.getTemporaryInputSlots();
        temporaryInputSlots.addDirtyMarkListener(() -> {
            ItemStack itemStack = temporaryInputSlots.getItem(0);
            IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
            IVariableFacade variableFacade = registry.handle(ValueDeseralizationContext.of(container.getPlayerIInventory().player.level()), itemStack);
            String label = LabelsWorldStorage.Access.getInstance(IntegratedDynamics._instance).get().getLabel(variableFacade.getId());
            if(label == null && !itemStack.isEmpty() && itemStack.has(DataComponents.CUSTOM_NAME)) {
                label = itemStack.getHoverName().getString();
            }
            if(label != null) {
                this.setText(label);
            }
        });
    }

    @Override
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/labeller.png");
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new ButtonText(this.leftPos + 133,  this.topPos + 8,
                Component.translatable("item.integrateddynamics.labeller.button.write"),
                Component.translatable("item.integrateddynamics.labeller.button.write"), button -> {
            ItemStack itemStack = getMenu().getItemStack();
            IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
            IVariableFacade variableFacade = registry.handle(ValueDeseralizationContext.of(container.getPlayerIInventory().player.level()), itemStack);
            if(variableFacade.isValid()) {
                int variableId = variableFacade.getId();
                String label = StringUtils.isBlank(searchField.getValue()) ? "" : searchField.getValue();
                LabelsWorldStorage.Access.getInstance(IntegratedDynamics._instance).get().put(variableId, label);
            } else if(!itemStack.isEmpty()) {
                String name = searchField.getValue();
                IntegratedDynamics._instance.getPacketHandler().sendToServer(new ItemStackRenamePacket(name));
                getMenu().setItemStackName(name);
            }
        }));

        int searchWidth = 87;
        int searchX = 36;
        int searchY = 11;
        this.searchField = new WidgetTextFieldExtended(font, this.leftPos + searchX, this.topPos + searchY, searchWidth, font.lineHeight, Component.translatable("gui.cyclopscore.search"));
        this.searchField.setMaxLength(64);
        this.searchField.setBordered(false);
        this.searchField.setVisible(true);
        this.searchField.setFocused(true);
        this.searchField.setTextColor(ARGB.opaque(16777215));
        this.searchField.setCanLoseFocus(false);
        this.searchField.setValue("");
        this.searchField.setX(this.leftPos + (searchX + searchWidth) - this.searchField.getWidth());
    }

    @Override
    protected int getBaseYSize() {
        return 113;
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (!this.searchField.charTyped(evt)) {
            return super.charTyped(evt);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        if (evt.input() != GLFW.GLFW_KEY_ESCAPE) {
            this.searchField.keyPressed(evt);
            return true;
        }
        return super.keyPressed(evt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        return this.searchField.mouseClicked(evt, isDoubleClick)
                || super.mouseClicked(evt, isDoubleClick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // super
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        this.searchField.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void setText(String text) {
        this.searchField.setValue(text);
    }

}
