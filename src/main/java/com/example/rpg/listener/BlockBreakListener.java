package com.example.rpg.listener;

import com.example.rpg.common.message.MessageUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var player = event.getPlayer();

        Material material = event.getBlock().getType();

        player.sendMessage(MessageUtil.green("ブロックを破壊しました。: " + material.name()));
    }
}
