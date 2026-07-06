package com.example.rpg;

import com.example.rpg.command.*;
import com.example.rpg.listener.BlockBreakListener;
import com.example.rpg.listener.EntityKillListener;
import com.example.rpg.listener.ServerPingListener;
import com.example.rpg.listener.ShopListener;
import com.example.rpg.menu.ShopMenu;
import com.example.rpg.repository.ShopRepository;
import com.example.rpg.service.ExpService;
import com.example.rpg.service.MoneyService;
import com.example.rpg.service.ShopPurchaseService;
import com.example.rpg.service.ShopService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RpgPlugin extends JavaPlugin implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private ShopRepository shopRepository;
    private ShopPurchaseService shopPurchaseService;
    private MoneyService moneyService;
    private ExpService expService;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("config.yml", true); // true = 上書き
        this.moneyService = new MoneyService(this);
        this.expService = new ExpService();
        this.shopPurchaseService = new ShopPurchaseService(this);
        shopRepository = new ShopRepository();
        shopRepository.load(getConfig());
        ShopMenu shopMenu = new ShopMenu(shopRepository);

        ShopService shopService = new ShopService(shopRepository, shopPurchaseService, moneyService);


        // 実行Lister・コマンドの登録
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new EntityKillListener(moneyService, expService), this);

        getCommand("shop").setExecutor(new ShopCommand(shopMenu));
        getServer().getPluginManager().registerEvents(new ShopListener(shopService), this);

        MoneyCommand moneyCommand = new MoneyCommand(moneyService);
        getCommand("money").setExecutor(new MoneyCommand(moneyService));
        getCommand("money").setTabCompleter(new MoneyCommand(moneyService));
        getCommand("pay").setExecutor(new PayCommand(moneyService));
        getCommand("exp").setExecutor(new ExpCommand(expService));

        getCommand("rpg").setExecutor(new RpgCommand(this));
        getLogger().info("RpgPlugin enabled.");
    }

    @Override
    public void onDisable() {
        if (moneyService != null) {
            moneyService.save();
        }
        if (shopPurchaseService != null) {
            shopPurchaseService.save();
        }


        getLogger().info("RpgPlugin disabled.");
    }

    /**
     * サーバー接続時に実行される参加イベント
     *
     * @param event 参加イベント
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        player.sendMessage(mm.deserialize(
                "<gradient:#00ffff:#ff00ff>==============================</gradient>"
        ));

        player.sendMessage(mm.deserialize(
                "<gold>ようこそ、</gold><aqua>" + player.getName() + "</aqua><rainbow>（" + player.getUniqueId() + "）</rainbow><gold> さん！</gold>"
        ));

        player.sendMessage(mm.deserialize(
                "<yellow>Lv." + player.getLevel() + "</yellow> <red>HP 100</red> <blue>MP 50</blue> <green>Money 0G</green>"
        ));

        player.sendMessage(mm.deserialize(
                "<gradient:#00ffff:#ff00ff>==============================</gradient>"
        ));
    }

    public ShopRepository getShopRepository() {
        return shopRepository;
    }
}