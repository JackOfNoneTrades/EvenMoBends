package net.gobbob.mobends.client.gui;

import net.gobbob.mobends.compat.WawelAuthSkinUpload.Account;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/** Confirmation screen for replacing the active Wawel Auth account skin. */
final class GuiWawelAuthUploadConfirm extends GuiScreen {
    interface Response {
        void respond(boolean confirmed);
    }

    private final Account account;
    private final Response response;

    GuiWawelAuthUploadConfirm(Account account, Response response) {
        this.account = account;
        this.response = response;
    }

    @Override
    public void initGui() {
        int y = this.height / 2 + 24;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 105, y, 100, 20, I18n.format("mobends.gui.yes")));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 5, y, 100, 20, I18n.format("mobends.gui.no")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        this.response.respond(button.id == 0);
    }

    @Override
    protected void keyTyped(char character, int key) {
        if (key == 1) {
            this.response.respond(false);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj,
            I18n.format("mobends.upload.confirm", this.account.name),
            this.width / 2, this.height / 2 - 32, 0xffffff);
        String details = I18n.format("mobends.upload.account", this.account.uuid == null ? "?" : this.account.uuid, this.account.provider);
        this.drawCenteredString(this.fontRendererObj, details, this.width / 2, this.height / 2 - 12, 0xbfbfbf);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
