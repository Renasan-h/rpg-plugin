package com.example.rpg.dto;

import org.bukkit.Material;

import java.util.List;

public class ShopItemDto {

    private final String id;
    private final int slot;
    private final ShopItemType type;
    private final Material material;
    private final String name;
    private final int price;
    private final int amount;
    private final int limit;
    private final String permission;
    private final List<String> lore;
    private final List<String> commands;

    public ShopItemDto(
            String id,
            int slot,
            ShopItemType type,
            Material material,
            String name,
            int price,
            int amount,
            int limit,
            String permission,
            List<String> lore,
            List<String> commands
    ) {
        this.id = id;
        this.slot = slot;
        this.type = type;
        this.material = material;
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.limit = limit;
        this.permission = permission;
        this.lore = lore;
        this.commands = commands;
    }

    public String getId() {
        return id;
    }

    public int getSlot() {
        return slot;
    }

    public ShopItemType getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public int getLimit() {
        return limit;
    }

    public String getPermission() {
        return permission;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getCommands() {
        return commands;
    }

    public boolean isCommandItem() {
        return type == ShopItemType.COMMAND;
    }
}