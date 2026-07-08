package com.example.rpg.listener;

import com.example.rpg.facade.ShopGuiFacade;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {

    /**
     * SHOP機能の入口。
     */
    private final ShopGuiFacade shopGuiFacade;

    /**
     * SHOP Listenerを生成する。
     *
     * @param shopGuiFacade SHOP GUI Facade
     */
    public ShopListener(ShopGuiFacade shopGuiFacade) {
        this.shopGuiFacade = shopGuiFacade;
    }

    /**
     * SHOP GUIクリック時の処理。
     *
     * @param event インベントリクリックイベント
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        shopGuiFacade.handleClick(event);
    }
}