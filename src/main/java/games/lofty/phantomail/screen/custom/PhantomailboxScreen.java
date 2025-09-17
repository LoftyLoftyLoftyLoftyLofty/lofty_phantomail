package games.lofty.phantomail.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import games.lofty.phantomail.Phantomail;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PhantomailboxScreen extends AbstractContainerScreen<PhantomailboxMenu>
{
    public static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Phantomail.MOD_ID, "textures/gui/phantomailbox/phantomailbox_gui.png");

    public PhantomailboxScreen(PhantomailboxMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE,x,y,0,0,imageWidth,imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
