package com.example.rpg.service;

import com.example.rpg.constants.ShopServiceConst;
import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.repository.ShopRepository;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopPurchaseService shopPurchaseService;
    private final MoneyService moneyService;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ShopService(ShopRepository shopRepository, ShopPurchaseService shopPurchaseService, MoneyService moneyService) {
        this.shopRepository = shopRepository;
        this.shopPurchaseService = shopPurchaseService;
        this.moneyService = moneyService;
    }

    public void handleClick(InventoryClickEvent event) {
        ShopDto shop = shopRepository.getShop();

        if (!event.getView().title().equals(mm.deserialize(shop.getTitle()))) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(mm.deserialize(
                    "<red>インベントリに空きがありません。</red>"
            ));
            return;
        }

        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        int clickedSlot = event.getRawSlot();

        ShopItemDto shopItem = findBySlot(clickedSlot);

        if (shopItem == null) {
            return;
        }

        if (shopItem.getLimit() >= 0) {
            int currentCount = shopPurchaseService.getPurchaseCount(player.getUniqueId(), shopItem.getId());

            if (currentCount >= shopItem.getLimit()) {
                player.sendMessage(mm.deserialize(
                        "<red>この商品は購入上限に達しています。</red>"
                ));

                player.sendMessage(mm.deserialize(
                        "<gray>購入済み: </gray><yellow>" + currentCount + "</yellow>" +
                                "<gray> / 上限: </gray><yellow>" + shopItem.getLimit() + "</yellow>"
                ));
                return;
            }
        }

        if (!moneyService.removeMoney(player.getUniqueId(), shopItem.getPrice())) {
            int currentMoney = moneyService.getMoney(player.getUniqueId());

            player.sendMessage(mm.deserialize("<red>所持金が足りません。</red>"));
            player.sendMessage(mm.deserialize("<gray>必要： </gray><gold>" + shopItem.getPrice() + "G</gold>" +
                    "<gray>/ 現在： </gray><gold>" + currentMoney + "G</gold>"));
            return;
        }

        if (shopItem.isCommandItem()) {
            buyCommands(player, shopItem);
        } else {
            buyItem(player, shopItem);
        }

        shopPurchaseService.addPurchaseCount(player.getUniqueId(), shopItem.getId());
        player.sendMessage(mm.deserialize(
                "<yellow>購入しました：</yellow>" + shopItem.getName() +
                        " <gray>x" + shopItem.getAmount() + "</gray> <gray>-" + shopItem.getPrice() + "G</gray>"
        ));
    }

    private ShopItemDto findBySlot(int slot) {
        return shopRepository.getShop()
                .getItems()
                .values()
                .stream()
                .filter(item -> item.getSlot() == slot)
                .findFirst()
                .orElse(null);
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
}