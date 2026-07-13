package com.example.rpg.shop.listener;

import com.example.rpg.shop.facade.ShopFacade;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {

    /**
     * SHOP機能の入口。
     */
    private final ShopFacade shopFacade;

    /**
     * SHOP Listenerを生成する。
     *
     * @param shopFacade SHOP GUI Facade
     */
    public ShopListener(ShopFacade shopFacade) {
        this.shopFacade = shopFacade;
    }

    /**
     * SHOP GUIクリック時の処理。
     *
     * @param event インベントリクリックイベント
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        shopFacade.handleClick(event);
    }
}