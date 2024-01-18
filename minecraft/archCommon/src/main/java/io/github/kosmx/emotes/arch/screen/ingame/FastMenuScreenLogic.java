package io.github.kosmx.emotes.arch.screen.ingame;

import dev.kosmx.playerAnim.core.util.MathHelper;
import io.github.kosmx.emotes.arch.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.arch.screen.IScreenSlave;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseWheel;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Stuff to override
 * isPauseScreen return false
 * render
 */
public abstract class FastMenuScreenLogic extends AbstractScreenLogic {
    private FastMenuWidget widget;
    private static final Component warn_no_emotecraft = TmpGetters.getDefaults().newTranslationText("emotecraft.no_server");
    private static final Component warn_only_proxy = TmpGetters.getDefaults().newTranslationText("emotecraft.only_proxy");

    protected FastMenuScreenLogic(IScreenSlave screen) {
        super(screen);
    }


    @Override
    public void emotes_initScreen(){
        int x = (int) Math.min(screen.getWidth() * 0.8, screen.getHeight() * 0.8);
        this.widget = newFastMenuWidget((screen.getWidth() - x) / 2, (screen.getHeight() - x) / 2, x);
        screen.addToChildren(widget);
        //this.buttons.add(new ButtonWidget(this.width - 120, this.height - 30, 96, 20, new TranslatableText("emotecraft.config"), (button -> this.client.openScreen(new EmoteMenu(this)))));
        screen.addToButtons(newButton(screen.getWidth() - 120, screen.getHeight() - 30, 96, 20, TmpGetters.getDefaults().newTranslationText("emotecraft.emotelist"), (button->screen.openScreen(newFullScreenMenu()))));
        screen.addButtonsToChildren();
    }

    protected abstract IScreenSlave newFullScreenMenu();


    @Override
    public void emotes_renderScreen(GuiGraphics matrices, int mouseX, int mouseY, float delta){
        screen.renderBackground(matrices);
        widget.render(matrices, mouseX, mouseY, delta);
        if(!((ClientConfig)EmoteInstance.config).hideWarningMessage.get()) {
            int remoteVer = ClientPacketManager.isRemoteAvailable() ? 2 : ClientPacketManager.isAvailableProxy() ? 1 : 0;
            if (remoteVer != 2) {
                drawCenteredText(matrices, remoteVer == 0 ? warn_no_emotecraft : warn_only_proxy, screen.getWidth() / 2, screen.getHeight() / 24 - 1, MathHelper.colorHelper(255, 255, 255, 255));
            }
        }
    }

    @Override
    public boolean emotes_isThisPauseScreen() {
        return false;
    }

    abstract protected FastMenuWidget newFastMenuWidget(int x, int y, int size);

    protected abstract class FastMenuWidget extends AbstractFastChooseWidget {

        public FastMenuWidget(int x, int y, int size){
            super(x, y, size);
        }

        @Override
        protected boolean doHoverPart(IChooseWheel.IChooseElement part){
            return part.hasEmote();
        }

        @Override
        protected boolean isValidClickButton(int button){
            return button == 0;
        }

        @Override
        protected boolean EmotesOnClick(IChooseWheel.IChooseElement element, int button){
            if(element.getEmote() != null){
                boolean bl = element.getEmote().playEmote(TmpGetters.getClientMethods().getMainPlayer());
                screen.openScreen(null);
                return bl;
            }
            return false;
        }

        @Override
        protected boolean doesShowInvalid() {
            return false;
        }
    }
}
