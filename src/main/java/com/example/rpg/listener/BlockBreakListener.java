package com.example.rpg.listener;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        var player = event.getPlayer();

        Material material = event.getBlock().getType();

        player.sendMessage(mm.deserialize("<green>ブロックを破壊しました。: </green>" + material.name()));
    }
}
