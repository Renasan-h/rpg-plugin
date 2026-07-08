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

        // config.ymlの読み込み
        saveDefaultConfig();
        saveResource("config.yml", true); // true = 上書き

        // moneyRepositoryの生成
        copyDefaultResourceIfMissing("money.yaml");
        this.moneyRepository = new MoneyRepository(this, new File(getDataFolder(), "money.yml"));

        // shopPurchaseRepositoryの生成
        copyDefaultResourceIfMissing("shop-purchases.yml");
        this.shopPurchaseRepository = new ShopPurchaseRepository(this, new File(getDataFolder(), "shop-purchases.yml"));

        // shopRepositoryの生成
        copyDefaultResourceIfMissing("shop.yml");
        IShopRepository shopRepository = new ShopRepository(
                YamlConfiguration.loadConfiguration(new File(getDataFolder(), "shop.yml"))
        );

        ShopMenu shopMenu = new ShopMenu(shopRepository);

        ShopService shopService = new ShopService(
                shopRepository,
                moneyRepository,
                shopPurchaseRepository
        );

        ShopGuiFacade shopGuiFacade = new ShopGuiFacade(
                shopRepository,
                shopMenu,
                shopService
        );

        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand(shopGuiFacade, shopService));
        getServer().getPluginManager().registerEvents(new ShopListener(shopGuiFacade), this);

        ExpService expService = new ExpService();

        // 実行Lister・コマンドの登録
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new EntityKillListener(moneyRepository, expService), this);

        MoneyCommand moneyCommand = new MoneyCommand(moneyRepository);
        Objects.requireNonNull(getCommand("money")).setExecutor(moneyCommand);
        Objects.requireNonNull(getCommand("money")).setTabCompleter(moneyCommand);
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand(moneyRepository));
        Objects.requireNonNull(getCommand("exp")).setExecutor(new ExpCommand(expService));

        Objects.requireNonNull(getCommand("rpg")).setExecutor(new RpgCommand(this));

        AdminCommand adminCommand = new AdminCommand(shopPurchaseRepository);
        Objects.requireNonNull(getCommand("admin")).setExecutor(adminCommand);
        Objects.requireNonNull(getCommand("admin")).setTabCompleter(adminCommand);

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
     * 指定されたconfigファイルを確認し、存在しない場合はテンプレートファイルをデータフォルダにコピーする
     *
     * @param fileName config file name
     */
    public void copyDefaultResourceIfMissing(String fileName) {
        File file = new File(getDataFolder(), fileName);

        if (file.exists()) {
            return;
        }

        saveResource(fileName, false);
    }
}