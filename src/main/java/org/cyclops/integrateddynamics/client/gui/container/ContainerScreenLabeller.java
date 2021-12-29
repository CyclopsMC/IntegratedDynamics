package org.cyclops.integrateddynamics.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
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
        container.setGui(this);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/labeller.png");
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new ButtonText(this.leftPos + 133,  this.topPos + 8,
                new TranslatableComponent("item.integrateddynamics.labeller.button.write"),
                new TranslatableComponent("item.integrateddynamics.labeller.button.write"), button -> {
            ItemStack itemStack = getMenu().getItemStack();
            IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
            IVariableFacade variableFacade = registry.handle(itemStack);
            if(variableFacade.isValid()) {
                int variableId = variableFacade.getId();
                String label = StringUtils.isBlank(searchField.getValue()) ? "" : searchField.getValue();
                LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(variableId, label);
            } else if(!itemStack.isEmpty()) {
                String name = searchField.getValue();
                IntegratedDynamics._instance.getPacketHandler().sendToServer(new ItemStackRenamePacket(name));
                getMenu().setItemStackName(name);
            }
        }));

        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int searchWidth = 87;
        int searchX = 36;
        int searchY = 11;
        this.searchField = new WidgetTextFieldExtended(font, this.leftPos + searchX, this.topPos + searchY, searchWidth, font.lineHeight, new TranslatableComponent("gui.cyclopscore.search"));
        this.searchField.setMaxLength(64);
        this.searchField.setBordered(false);
        this.searchField.setVisible(true);
        this.searchField.changeFocus(true);
        this.searchField.setTextColor(16777215);
        this.searchField.setCanLoseFocus(false);
        this.searchField.setValue("");
        this.searchField.x = this.leftPos + (searchX + searchWidth) - this.searchField.getWidth();
    }

    @Override
    protected int getBaseYSize() {
        return 113;
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (!this.searchField.charTyped(typedChar, keyCode)) {
            return super.charTyped(typedChar, keyCode);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        if (typedChar != GLFW.GLFW_KEY_ESCAPE) {
            this.searchField.keyPressed(typedChar, keyCode, modifiers);
            return true;
        }
        return super.keyPressed(typedChar, keyCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return this.searchField.mouseClicked(mouseX, mouseY, mouseButton)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        // super
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        this.searchField.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void setText(String text) {
        this.searchField.setValue(text);
    }

}
