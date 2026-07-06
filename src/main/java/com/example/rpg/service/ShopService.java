package com.example.rpg.service;

import com.example.rpg.constants.ShopServiceConst;
import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.menu.ShopMenu;
import com.example.rpg.repository.ShopRepository;
import com.example.rpg.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopPurchaseService shopPurchaseService;
    private final MoneyService moneyService;
    private final ShopMenu shopMenu;

    public ShopService(
            ShopRepository shopRepository,
            ShopPurchaseService shopPurchaseService,
            MoneyService moneyService,
            ShopMenu shopMenu
    ) {
        this.shopRepository = shopRepository;
        this.shopPurchaseService = shopPurchaseService;
        this.moneyService = moneyService;
        this.shopMenu = shopMenu;
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (isCategoryMenu(event)) {
            event.setCancelled(true);
            handleCategoryClick(player, event);
            return;
        }

        ShopCategoryDto category = findCategoryByTitle(event);

        if (category != null) {
            event.setCancelled(true);
            handleItemClick(player, event, category);
        }
    }

    private boolean isCategoryMenu(InventoryClickEvent event) {
        return event.getView().title().equals(MessageUtil.mm(ShopMenu.categoryTitle()));
    }

    private void handleCategoryClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        int clickedSlot = event.getRawSlot();

        ShopCategoryDto category = findCategoryBySlot(clickedSlot);

        if (category == null) {
            return;
        }

        shopMenu.openCategory(player, category);
    }

    private void handleItemClick(Player player, InventoryClickEvent event, ShopCategoryDto category) {
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        ShopItemDto shopItem = findItemBySlot(category, event.getRawSlot());

        if (shopItem == null) {
            return;
        }

        buy(player, shopItem);
    }

    private void buy(Player player, ShopItemDto shopItem) {
        if (player.getInventory().firstEmpty() == -1 && !shopItem.isCommandItem()) {
            player.sendMessage(MessageUtil.red("インベントリに空きがありません。"));
            return;
        }

        if (shopItem.getLimit() >= 0) {
            int currentCount = shopPurchaseService.getPurchaseCount(player.getUniqueId(), shopItem.getId());

            if (currentCount >= shopItem.getLimit()) {
                player.sendMessage(MessageUtil.red("この商品は購入上限に達しています。"));
                player.sendMessage(MessageUtil.mm(
                        "<gray>購入済み: </gray><yellow>" + currentCount + "</yellow>" +
                                "<gray> / 上限: </gray><yellow>" + shopItem.getLimit() + "</yellow>"
                ));
                return;
            }
        }

        if (!moneyService.removeMoney(player.getUniqueId(), shopItem.getPrice())) {
            int currentMoney = moneyService.getMoney(player.getUniqueId());

            player.sendMessage(MessageUtil.red("所持金が足りません。"));
            player.sendMessage(MessageUtil.mm(
                    "<gray>必要: </gray><gold>" + shopItem.getPrice() + "G</gold>" +
                            "<gray> / 現在: </gray><gold>" + currentMoney + "G</gold>"
            ));
            return;
        }

        if (shopItem.isCommandItem()) {
            buyCommands(player, shopItem);
        } else {
            buyItem(player, shopItem);
        }

        shopPurchaseService.addPurchaseCount(player.getUniqueId(), shopItem.getId());

        player.sendMessage(MessageUtil.mm(
                "<yellow>購入しました: </yellow>" + shopItem.getName() +
                        " <gray>x" + shopItem.getAmount() + "</gray>" +
                        " <gray>-" + shopItem.getPrice() + "G</gray>"
        ));
    }

    private ShopCategoryDto findCategoryBySlot(int slot) {
        return shopRepository.getShop()
                .getCategories()
                .values()
                .stream()
                .filter(category -> category.getSlot() == slot)
                .findFirst()
                .orElse(null);
    }

    private ShopCategoryDto findCategoryByTitle(InventoryClickEvent event) {
        for (ShopCategoryDto category : shopRepository.getShop().getCategories().values()) {
            if (event.getView().title().equals(MessageUtil.mm(ShopMenu.itemTitlePrefix() + category.getName()))) {
                return category;
            }
        }

        return null;
    }

    private ShopItemDto findItemBySlot(ShopCategoryDto category, int slot) {
        return category.getItems()
                .values()
                .stream()
                .filter(item -> item.getSlot() == slot)
                .findFirst()
                .orElse(null);
    }

    private ShopItemDto findSellableItem(Material material) {
        ShopDto shop = shopRepository.getShop();

        return shop.getCategories()
                .values()
                .stream()
                .flatMap(category -> category.getItems().values().stream())
                .filter(ShopItemDto::isSellable)
                .filter(item -> item.getMaterial() == material)
                .findFirst()
                .orElse(null);
    }

    public boolean sellHeldItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(MessageUtil.red("売却するアイテムを手に持ってください。"));
            return true;
        }

        ShopItemDto shopItem = findSellableItem(itemInHand.getType());

        if (shopItem == null) {
            player.sendMessage(MessageUtil.red("このアイテムは売却できません。"));
            return true;
        }

        int sellAmount = 1;
        int totalPrice = shopItem.getSellPrice() * sellAmount;

        itemInHand.setAmount(itemInHand.getAmount() - sellAmount);
        moneyService.addMoney(player.getUniqueId(), totalPrice);

        player.sendMessage(MessageUtil.mm(
                "<yellow>売却しました: </yellow>" +
                        shopItem.getName() +
                        " <gray>x" + sellAmount + "</gray>" +
                        " <gold>+" + totalPrice + "G</gold>"
        ));

        return true;
    }

    private void buyItem(Player player, ShopItemDto shopItem) {
        ItemStack itemStack = new ItemStack(
                shopItem.getMaterial(),
                shopItem.getAmount()
        );

        player.getInventory().addItem(itemStack);
    }

    private void buyCommands(Player player, ShopItemDto shopItem) {
        for (String command : shopItem.getCommands()) {
            String parsedCommand = command.replace(ShopServiceConst.REP_COMMAND_PLAYER, player.getName());

            player.getServer().dispatchCommand(
                    player.getServer().getConsoleSender(),
                    parsedCommand
            );
        }
    }

    public boolean sellHandItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(MessageUtil.red("売却するアイテムを手に持ってください。"));
            return false;
        }

        ShopItemDto shopItem = findSellableItem(itemInHand.getType());

        if (shopItem == null) {
            player.sendMessage(MessageUtil.red("このアイテムは売却できません。"));
            return false;
        }

        int sellAmount = 1;
        int totalPrice = shopItem.getSellPrice() * sellAmount;

        itemInHand.setAmount(itemInHand.getAmount() - sellAmount);
        moneyService.addMoney(player.getUniqueId(), totalPrice);

        player.sendMessage(MessageUtil.mm("<yellow>売却しました：</yellow>" + " <gray>x" + sellAmount + "</gray>" +
                " <gold>+" + totalPrice + "G</gold>"));

        return true;
    }
}