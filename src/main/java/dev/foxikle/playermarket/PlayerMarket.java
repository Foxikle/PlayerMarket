package dev.foxikle.playermarket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.foxikle.playermarket.data.Database;
import dev.foxikle.playermarket.data.Escrow;
import dev.foxikle.playermarket.listeners.InventoryClickListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PlayerMarket extends JavaPlugin {
    private Gson gson;
    private static PlayerMarket instance;
    private Economy econ = null;
    private Database db;
    private Map<String, Product> products = new HashMap<>();
    private FileConfiguration config;
    private Set<String> pages;
    private Map<String, ProductFamily> families = new HashMap<>();
    public Inventories invs;
    private Escrow escrow;
    private NamespacedKey itemIdKey;
    //TODO: Add cross filling
    //TODO: Add a search feature

    @Override
    public void onEnable() {
        if (!new File("plugins/PlayerMarket/config.yml").exists()) {
            this.saveResource("config.yml", false);
        }
        if (!setupEconomy() ) {
            this.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", this.getPluginMeta().getName()));
            getServer().getPluginManager().disablePlugin(this);
        }
        instance = this;
         gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ConfigurationSerializableAdapter())
                .create();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            db = new Database();
            try {
                db.connect();
                db.createOrderTables();
                Bukkit.getLogger().info("Database connected.");
                setupProducts();
            } catch (ClassNotFoundException | SQLException e) {
                Bukkit.getLogger().severe("Invalid Database credentails. Disabling plugin.");
                instance.getServer().getPluginManager().disablePlugin(instance);
            }
        }, 40);
        File file = new File("plugins/PlayerMarket/config.yml");
        config = YamlConfiguration.loadConfiguration(file);
        pages = getConfig().getConfigurationSection("pages").getKeys(false);
        invs = new Inventories(this);
        escrow = new Escrow(this);
        itemIdKey = new NamespacedKey(this, config.getString("ItemIdKey"));
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
    }

    @Override
    public void onDisable() {
        db.disconnect();
    }

    //TODO: remove since its a testing command
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player player) {
            if (label.equalsIgnoreCase("inv")) {
                    Bukkit.getScheduler().runTaskAsynchronously(this, () -> player.sendMessage(db.getGlobalBuyOrders().toString()));
                return true;
            } else if (label.equalsIgnoreCase("invs")) {
                //escrow.takeItem(temp, 160, player);
                player.openInventory(invs.inventories.get("MINING"));
            }
        }
        return false;
    }

    public Gson getGson(){
        return gson;
    }

    public static PlayerMarket getInstance() {
        return instance;
    }

    private void setupProducts(){
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            products.clear();
            ConfigurationSection mainSection = instance.getConfig().getConfigurationSection("pages");
            Set<String> pageKeys = mainSection.getKeys(false);
            for (String str : pageKeys) { // All the "pages"
                ConfigurationSection pageSection = mainSection.getConfigurationSection(str); // MINING, etc
                ConfigurationSection familiesSection = pageSection.getConfigurationSection("product_families"); // STONES, etc
                Set<String> familyKeys = familiesSection.getKeys(false);
                for (String familyKey : familyKeys) { // all the families
                    Set<Product> temp = new HashSet<>();
                    ConfigurationSection s = familiesSection.getConfigurationSection(familyKey);
                    ConfigurationSection productsSection = s.getConfigurationSection("products");
                    Set<String> productKeys = productsSection.getKeys(false);
                    for (String productKey : productKeys) {
                        products.put(productKey, new Product(productsSection.getItemStack(productKey), productKey));
                        temp.add(new Product(productsSection.getItemStack(productKey), productKey));
                    }
                    families.put(familyKey, new ProductFamily(familyKey, temp, Material.valueOf(s.getString("icon"))));
                }
            }
        });
    }

    @Override
    public FileConfiguration getConfig(){
        return config;
    }

    public Set<String> getPages() {
        return pages;
    }

    public Inventories getInvs() {
        return invs;
    }

    public Map<String, Product> getProducts() {
        return products;
    }

    public Map<String, ProductFamily> getFamilies() {
        return families;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public Economy getEcon() {
        return econ;
    }

    public Database getDb() {
        return db;
    }

    public Escrow getEscrow() {
        return escrow;
    }

    public NamespacedKey getItemIdKey() {
        return itemIdKey;
    }

    public int itemCount(Player player, String id) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getItemMeta() != null) {
                if(stack.getItemMeta().getPersistentDataContainer().getKeys().contains(getItemIdKey())){
                    count += stack.getAmount();
                } else if(id.equalsIgnoreCase(stack.getType().name())) {
                    count += stack.getAmount();
                }
            }
        }
        return count;
    }
}
