package org.cyclops.integrateddynamics.client.gui;

import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;
import org.cyclops.integrateddynamics.network.packet.ItemStackRenamePacket;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Gui for the labeller.
 * @author rubensworks
 */
public class GuiLabeller extends GuiContainerConfigurable<ContainerLabeller> {

    public static final int BUTTON_WRITE = 1;

    private GuiTextField searchField;

    /**
     * Make a new instance.
     * @param player The player.
     * @param itemIndex The index of the item in use inside the player inventory.
     */
    public GuiLabeller(EntityPlayer player, int itemIndex) {
        super(new ContainerLabeller(player, itemIndex));
        ContainerLabeller container = ((ContainerLabeller) getContainer());
        container.setGui(this);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButtonText(BUTTON_WRITE, this.guiLeft + 133,  this.guiTop + 8,
                L10NHelpers.localize("item.items.integrateddynamics.labeller.button.write")));

        Keyboard.enableRepeatEvents(true);
        int searchWidth = 87;
        int searchX = 36;
        int searchY = 11;
        this.searchField = new GuiTextField(0, this.fontRendererObj, this.guiLeft + searchX, this.guiTop + searchY, searchWidth, this.fontRendererObj.FONT_HEIGHT);
        this.searchField.setMaxStringLength(64);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setVisible(true);
        this.searchField.setTextColor(16777215);
        this.searchField.setCanLoseFocus(true);
        this.searchField.setText("");
        this.searchField.width = searchWidth;
        this.searchField.xPosition = this.guiLeft + (searchX + searchWidth) - this.searchField.width;
    }

    @Override
    protected int getBaseYSize() {
        return 113;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (!this.searchField.textboxKeyTyped(typedChar, keyCode)) {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.searchField.drawTextBox();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if(guibutton.id == BUTTON_WRITE) {
            ItemStack itemStack = getContainer().getItemStack();
            IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
            IVariableFacade variableFacade = registry.handle(itemStack);
            if(variableFacade.isValid()) {
                int variableId = variableFacade.getId();
                String label = StringUtils.isBlank(searchField.getText()) ? "" : searchField.getText();
                LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(variableId, label);
            } else if(itemStack != null) {
                this.mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("MC|ItemName", (new PacketBuffer(Unpooled.buffer())).writeString(searchField.getText())));
                String name = searchField.getText();
                IntegratedDynamics._instance.getPacketHandler().sendToServer(new ItemStackRenamePacket(name));
                getContainer().setItemStackName(name);
            }
        }
    }

    public void setText(String text) {
        this.searchField.setText(text);
    }

}
