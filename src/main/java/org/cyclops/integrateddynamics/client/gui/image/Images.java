package org.cyclops.integrateddynamics.client.gui.image;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.client.gui.image.Image;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Default images provided by this mod.
 * @author rubensworks
 */
public class Images {

    public static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(IntegratedDynamics._instance.getModId(),
            IntegratedDynamics._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI) + "icons.png");

    public static final Image BUTTON_BACKGROUND_INACTIVE = new Image(ICONS, 0, 0, 18, 18);
    public static final Image BUTTON_BACKGROUND_ACTIVE = new Image(ICONS, 18, 0, 18, 18);

    public static final Image BUTTON_MIDDLE_OFFSET = new Image(ICONS, 0, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_SETTINGS = new Image(ICONS, 18, 18, 18, 18);
    public static final Image BUTTON_MIDDLE_CHECK_ALL = new Image(ICONS, 0, 36, 18, 18);
    public static final Image BUTTON_MIDDLE_ENCODE = new Image(ICONS, 18, 36, 18, 18);
    public static final Image BUTTON_MIDDLE_ENCODE_COMPRESSED = new Image(ICONS, 36, 36, 18, 18);

    // White, so they are drawn in the color of the text of buttons
    public static final Image ICON_UNDO = new Image(ICONS, 54, 0, 7, 7);
    public static final Image ICON_REDO = new Image(ICONS, 62, 0, 7, 7);
    public static final Image ICON_COPY = new Image(ICONS, 70, 0, 7, 7);
    public static final Image ICON_CHECK = new Image(ICONS, 78, 0, 7, 7);
    public static final Image ICON_FORMAT = new Image(ICONS, 86, 0, 7, 7);

}
