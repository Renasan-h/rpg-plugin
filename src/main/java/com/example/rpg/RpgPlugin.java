package com.example.rpg;

import com.example.rpg.command.*;
import com.example.rpg.facade.ShopGuiFacade;
import com.example.rpg.listener.BlockBreakListener;
import com.example.rpg.listener.EntityKillListener;
import com.example.rpg.listener.ServerPingListener;
import com.example.rpg.listener.ShopListener;
import com.example.rpg.menu.ShopMenu;
import com.example.rpg.repository.MoneyRepository;
import com.example.rpg.repository.ShopPurchaseRepository;
import com.example.rpg.repository.ShopRepository;
import com.example.rpg.repository.interfaces.IMoneyRepository;
import com.example.rpg.repository.interfaces.IShopPurchaseRepository;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.service.ExpService;
import com.example.rpg.service.ShopService;
import com.example.rpg.util.MessageUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class RpgPlugin extends JavaPlugin implements Listener {

    private IShopPurchaseRepository shopPurchaseRepository;
    private IMoneyRepository moneyRepository;


    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new UnsupportedOperationException();
        }
        saveDefaultConfig();

        // ------------------------------
        // Resource
        // ------------------------------
        copyResourceIfAbsent("config.yml");
        copyResourceIfAbsent("money.yml");
        copyResourceIfAbsent("shop-purchases.yml");
        copyResourceIfAbsent("shop.yml");

        // ------------------------------
        // Repository
        // ------------------------------
        // TODO PostgreSQL対応時はRepository生成をFactoryへ移動する。
        this.moneyRepository = new MoneyRepository(this, new File(getDataFolder(), "money.yml"));
        this.shopPurchaseRepository = new ShopPurchaseRepository(this, new File(getDataFolder(), "shop-purchases.yml"));
        IShopRepository shopRepository = new ShopRepository(
                YamlConfiguration.loadConfiguration(new File(getDataFolder(), "shop.yml"))
        );

        // ------------------------------
        // Service
        // ------------------------------
        ShopService shopService = new ShopService(
                shopRepository,
                moneyRepository,
                shopPurchaseRepository
        );
        ExpService expService = new ExpService();

        // ------------------------------
        // Menu
        // ------------------------------
        ShopMenu shopMenu = new ShopMenu(shopRepository);

        // ------------------------------
        // Facade
        // ------------------------------
        ShopGuiFacade shopGuiFacade = new ShopGuiFacade(
                shopRepository,
                shopMenu,
                shopService
        );

        // ------------------------------
        // Command
        // ------------------------------
        Objects.requireNonNull(getCommand("rpg")).setExecutor(new RpgCommand(this));
        MoneyCommand moneyCommand = new MoneyCommand(moneyRepository);
        Objects.requireNonNull(getCommand("money")).setExecutor(moneyCommand);
        Objects.requireNonNull(getCommand("money")).setTabCompleter(moneyCommand);
        AdminCommand adminCommand = new AdminCommand(shopPurchaseRepository);
        Objects.requireNonNull(getCommand("admin")).setExecutor(adminCommand);
        Objects.requireNonNull(getCommand("admin")).setTabCompleter(adminCommand);
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand(moneyRepository));
        Objects.requireNonNull(getCommand("exp")).setExecutor(new ExpCommand(expService));
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand(shopGuiFacade, shopService));

        // ------------------------------
        // Listener
        // ------------------------------
        getServer().getPluginManager().registerEvents(new ShopListener(shopGuiFacade), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new EntityKillListener(moneyRepository, expService), this);

        getLogger().info("RpgPlugin enabled.");
    }

    @Override
    public void onDisable() {
        if (moneyRepository != null) {
            moneyRepository.save();
        }
        if (shopPurchaseRepository != null) {
            shopPurchaseRepository.save();
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

        player.sendMessage(MessageUtil.mm(
                "<gradient:#00ffff:#ff00ff>==============================</gradient>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<gold>ようこそ、</gold><aqua>" + player.getName() + "</aqua><rainbow>（" + player.getUniqueId() + "）</rainbow><gold> さん！</gold>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<yellow>Lv." + player.getLevel() + "</yellow> <red>HP 100</red> <blue>MP 50</blue> <green>Money 0G</green>"
        ));

        player.sendMessage(MessageUtil.mm(
                "<gradient:#00ffff:#ff00ff>==============================</gradient>"
        ));
    }

    /**
     * デフォルト設定ファイルをコピーする。
     *
     * <p>
     * saveResource()はJAR内のresourcesからpluginDataへコピーする。
     * 既存ファイルは上書きしたくないため存在確認を行う。
     * </p>
     *
     * @param fileName Resource名
     */
    private void copyResourceIfAbsent(String fileName) {
        File file = new File(getDataFolder(), fileName);

        if (file.exists()) {
            return;
        }

        saveResource(fileName, false);
    }
}