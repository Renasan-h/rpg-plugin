package com.example.rpg.listener;

import com.example.rpg.service.ShopService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {

    private final String REP_COMMAND_PLAYER = "%player%";
    private final ShopService shopService;

    public ShopListener(ShopService shopService) {
        this.shopService = shopService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        shopService.handleClick(event);
    }
}