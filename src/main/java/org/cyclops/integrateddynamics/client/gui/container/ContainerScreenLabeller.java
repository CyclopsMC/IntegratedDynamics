package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;
import org.cyclops.integrateddynamics.network.packet.ItemStackRenamePacket;

/**
 * Gui for the labeller.
 * @author rubensworks
 */
public class ContainerScreenLabeller extends ContainerScreenExtended<ContainerLabeller> {

    private WidgetTextFieldExtended searchField;

    public ContainerScreenLabeller(ContainerLabeller container, PlayerInventory playerInventory, ITextComponent title) {
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
        addButton(new ButtonText(this.guiLeft + 133,  this.guiTop + 8,
                L10NHelpers.localize("item.items.integrateddynamics.labeller.button.write"),
                L10NHelpers.localize("item.items.integrateddynamics.labeller.button.write"), button -> {
            ItemStack itemStack = getContainer().getItemStack();
            IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
            IVariableFacade variableFacade = registry.handle(itemStack);
            if(variableFacade.isValid()) {
                int variableId = variableFacade.getId();
                String label = StringUtils.isBlank(searchField.getText()) ? "" : searchField.getText();
                LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(variableId, label);
            } else if(!itemStack.isEmpty()) {
                String name = searchField.getText();
                IntegratedDynamics._instance.getPacketHandler().sendToServer(new ItemStackRenamePacket(name));
                getContainer().setItemStackName(name);
            }
        }));

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        int searchWidth = 87;
        int searchX = 36;
        int searchY = 11;
        this.searchField = new WidgetTextFieldExtended(font, this.guiLeft + searchX, this.guiTop + searchY, searchWidth, font.FONT_HEIGHT, L10NHelpers.localize("gui.cyclopscore.search"));
        this.searchField.setMaxStringLength(64);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setVisible(true);
        this.searchField.focused = true;
        this.searchField.setTextColor(16777215);
        this.searchField.setCanLoseFocus(false);
        this.searchField.setText("");
        this.searchField.x = this.guiLeft + (searchX + searchWidth) - this.searchField.getWidth();
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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return this.searchField.mouseClicked(mouseX, mouseY, mouseButton)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.searchField.renderButton(mouseX, mouseY, partialTicks);
    }

    public void setText(String text) {
        this.searchField.setText(text);
    }

}
