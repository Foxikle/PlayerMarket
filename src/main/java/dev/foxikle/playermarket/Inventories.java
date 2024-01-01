package dev.foxikle.playermarket;

import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Inventories {
    //TODO: Use packets to show prices?

    private PlayerMarket plugin;

    public Map<String, Inventory> inventories = new HashMap<>();
    private Set<String> pages;

    public Inventories (PlayerMarket plugin){
        this.plugin = plugin;
        pages = plugin.getPages();
        setupMenus();
    }

    private void setupMenus(){
        NamespacedKey key = new NamespacedKey(PlayerMarket.getInstance(), "MenuButton");
        for (String str : pages) {
            Inventory inv = getOuterPart();
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("pages").getConfigurationSection(str).getConfigurationSection("product_families");
            Set<String> keys = section.getKeys(false);
            for (String s : keys) {
                ConfigurationSection familySection = section.getConfigurationSection(s);
                ItemStack item = new ItemStack(Material.valueOf(familySection.getString("icon")));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', familySection.getString("title")));
                meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&eClick to view")));
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, s);
                item.setItemMeta(meta);
                inv.addItem(item);
            }
            inventories.put(str, inv);
        }
    }

    public Inventory getOuterPart(){
        NamespacedKey key = new NamespacedKey(PlayerMarket.getInstance(), "MenuButton");
        Inventory inv = Bukkit.createInventory(null, 9*(pages.size()+1), "%Title%");
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("pages");
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "MenuGlass");
        meta.setDisplayName(" ");

        ItemStack searchItem = new ItemStack(Material.OAK_HANGING_SIGN);
        ItemMeta searchMeta = searchItem.getItemMeta();
        searchMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eSearch for a product"));
        searchMeta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&c&lNOT IMPLEMENTED YET!")));
        searchItem.setItemMeta(searchMeta);
        inv.setItem(pages.size()*9, searchItem);

        for (int i = 0; i < pages.size(); i++) {
            String str = Iterables.get(pages, i);
            ConfigurationSection s = section.getConfigurationSection(str);
            ItemStack itemStack = new ItemStack(Material.valueOf(s.getString("icon")));
            ItemMeta m = itemStack.getItemMeta();
            m.setDisplayName(ChatColor.translateAlternateColorCodes('&', s.getString("title")));
            m.setLore(Collections.singletonList(ChatColor.YELLOW + "Click to view"));
            m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS);
            m.getPersistentDataContainer().set(key, PersistentDataType.STRING, str);
            itemStack.setItemMeta(m);
            inv.setItem(i*9, itemStack);
        }

        item.setItemMeta(meta);
        //TODO: Make this work and stuff with dynamic page numbers
        inv.setItem(1, item);
        inv.setItem(2, item);
        inv.setItem(3, item);
        inv.setItem(4, item);
        inv.setItem(5, item);
        inv.setItem(6, item);
        inv.setItem(7, item);
        inv.setItem(8, item);
        inv.setItem(10, item);
        inv.setItem(17, item);
        inv.setItem(19, item);
        inv.setItem(26, item);
        inv.setItem(28, item);
        inv.setItem(35, item);
        inv.setItem(37, item);
        inv.setItem(44, item);
        inv.setItem(46, item);
        inv.setItem(47, item);
        inv.setItem(48, item);
        inv.setItem(49, item);
        inv.setItem(50, item);
        inv.setItem(51, item);
        inv.setItem(52, item);
        inv.setItem(53, item);

        return inv;
    }

    public Inventory getPlayerOrdersMenu(UUID uuid){
        return null;
    }

    public Inventory getProductMenu(Product p, Player player){
        NamespacedKey buttonId = new NamespacedKey(plugin, "MenuButton");
        NamespacedKey key = new NamespacedKey(plugin, "OrderButton");
        Inventory inv = addBorder(Bukkit.createInventory(null, 27, "%family_title% -> %product%"));
        List<String> lore = new ArrayList<>();
        ItemStack instantSell = new ItemStack(Material.HOPPER);
        ItemMeta isMeta = instantSell.getItemMeta();
        isMeta.setDisplayName(ChatColor.GOLD + "Sell Instantly");
        lore.add(ChatColor.DARK_GRAY + p.item().getI18NDisplayName());
        lore.add("");
        double price = plugin.getDb().getSellOrderPrice(p, true);
        lore.add(ChatColor.YELLOW + "Inventory: " + plugin.itemCount(player, p.id()));
        lore.add("");
        lore.add(ChatColor.GRAY + "Price per unit: " + ChatColor.GOLD + (price == -1.0 ? ChatColor.GRAY + "N/A" : ChatColor.GOLD + String.valueOf(price)));
        lore.add(ChatColor.YELLOW + "Total: " + ChatColor.GOLD + (price == -1.0 ? ChatColor.GRAY + "N/A" : ChatColor.GOLD + String.valueOf(price*plugin.itemCount(player, p.id()))));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to proceed!");

        isMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, p.id());
        isMeta.getPersistentDataContainer().set(buttonId, PersistentDataType.STRING, "INSTANT_SELL");

        isMeta.setLore(lore);
        instantSell.setItemMeta(isMeta);
        lore.clear();


        ItemStack sellOffer = new ItemStack(Material.MAP);
        ItemMeta soMeta = sellOffer.getItemMeta();
        soMeta.setDisplayName(ChatColor.GREEN + "Sell Offer");
        lore.add(ChatColor.DARK_GRAY + p.item().getI18NDisplayName());
        lore.add("");
        lore.add(ChatColor.YELLOW + "Inventory: " + ChatColor.GRAY +  ChatColor.ITALIC + "LOADING");
        lore.add("");
        lore.add(ChatColor.GOLD + "Top Offers: " + ChatColor.GRAY +  ChatColor.ITALIC + "LOADING");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to pick amount!");

        soMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, p.id());
        soMeta.getPersistentDataContainer().set(buttonId, PersistentDataType.STRING, "SELL_OFFER");

        soMeta.setLore(lore);
        sellOffer.setItemMeta(soMeta);
        lore.clear();

        ItemStack instantBuy = new ItemStack(Material.GOLDEN_HORSE_ARMOR);
        ItemMeta ibMeta = instantBuy.getItemMeta();
        ibMeta.setDisplayName(ChatColor.GOLD + "Buy Instantly");
        lore.add(ChatColor.DARK_GRAY + p.item().getI18NDisplayName());
        lore.add("");
        lore.add(ChatColor.GOLD + "Price per unit: " + ChatColor.GRAY +  ChatColor.ITALIC + "LOADING");
        lore.add(ChatColor.GOLD + "Stack Price: " + ChatColor.GRAY +  ChatColor.ITALIC + "LOADING");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to proceed!");

        ibMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, p.id());
        ibMeta.getPersistentDataContainer().set(buttonId, PersistentDataType.STRING, "INSTANT_BUY");

        ibMeta.setLore(lore);
        instantBuy.setItemMeta(ibMeta);
        lore.clear();

        ItemStack buyOrder = new ItemStack(Material.FILLED_MAP);
        ItemMeta boMeta = buyOrder.getItemMeta();
        boMeta.setDisplayName(ChatColor.GREEN + "Buy Order");
        lore.add(ChatColor.DARK_GRAY + p.item().getI18NDisplayName());
        lore.add("");
        lore.add(ChatColor.GOLD + "Top Orders: " + ChatColor.GRAY + ChatColor.ITALIC + "LOADING");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to setup an order!");

        boMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, p.id());
        boMeta.getPersistentDataContainer().set(buttonId, PersistentDataType.STRING, "BUY_ORDER");

        boMeta.setLore(lore);
        buyOrder.setItemMeta(boMeta);

        inv.setItem(10, instantBuy);
        inv.setItem(11, buyOrder);

        inv.setItem(13, p.item());

        inv.setItem(15, instantSell);
        inv.setItem(16, sellOffer);
        return inv;
    }

    public Inventory getFamilyMenu(ProductFamily family) { // ie. Fish
        NamespacedKey key = new NamespacedKey(plugin, "MenuButton");
        Inventory inv = Bukkit.createInventory(null, 9,  "");
        Set<Product> products = family.getProductSet();
        List<ItemStack> items = new ArrayList<>();
        for (Product p : products ) {
            ItemStack item = p.item();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, p.id());
            item.setItemMeta(meta);
            items.add(p.item());
        }
        switch (items.size()){
            case 1 -> {
                inv = addBorder(Bukkit.createInventory(null, 27, "%family_title%"));
                inv.setItem(13, Iterables.getOnlyElement(items));
            }
            case 2 -> {
                inv = addBorder(Bukkit.createInventory(null, 27, "%family_title%"));
                inv.setItem(12, items.get(0));
                items.remove(0);
                inv.setItem(14, items.get(0));
            }
            case 3 ->{
                inv = addBorder(Bukkit.createInventory(null, 27, "%family_title%"));
                inv.setItem(12, items.get(0));
                items.remove(0);
                inv.setItem(13, items.get(0));
                items.remove(0);
                inv.setItem(14, items.get(0));
            }
            case 4 ->{
                inv = addBorder(Bukkit.createInventory(null, 27, "%family_title%"));
                inv.setItem(11, items.get(0));
                items.remove(0);

                inv.setItem(12, items.get(0));
                items.remove(0);

                inv.setItem(14, items.get(0));
                items.remove(0);

                inv.setItem(15, items.get(0));
            }

            case 5 -> {
                inv = addBorder(Bukkit.createInventory(null, 27, "%family_title%"));
                inv.setItem(11, items.get(0));
                items.remove(0);

                inv.setItem(12, items.get(0));
                items.remove(0);

                inv.setItem(13, items.get(0));
                items.remove(0);

                inv.setItem(14, items.get(0));
                items.remove(0);

                inv.setItem(15, items.get(0));
            }

            case 6 -> {
                inv = addBorder(Bukkit.createInventory(null, 27, "%family_title%"));
                inv.setItem(10, items.get(0));
                items.remove(0);

                inv.setItem(11, items.get(0));
                items.remove(0);

                inv.setItem(12, items.get(0));
                items.remove(0);

                inv.setItem(14, items.get(0));
                items.remove(0);

                inv.setItem(15, items.get(0));
                items.remove(0);

                inv.setItem(16, items.get(0));
            }

            case 7 -> {
                inv = addBorder(Bukkit.createInventory(null, 27, "%family_title%"));
                items.forEach(inv::addItem);
            }
            case 8, 9, 10, 11, 12, 13, 14 -> {
                inv = addBorder(Bukkit.createInventory(null, 36, "%family_title%"));
                items.forEach(inv::addItem);
            }
            case 15, 16, 17, 18, 19, 20, 21 -> {
                inv = addBorder(Bukkit.createInventory(null, 45, "%family_title%"));
                items.forEach(inv::addItem);
            }
            case 22, 23, 24, 25, 26, 27, 28 -> {
                inv = addBorder(Bukkit.createInventory(null, 54, "%family_title%"));
                items.forEach(inv::addItem);
            }
        }

        return inv;
    }

    private Inventory addBorder(Inventory inv) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "MenuButton");
        meta.getCustomTagContainer().setCustomTag(key, ItemTagType.STRING, "MenuGlass");
        meta.setDisplayName(" ");
        item.setItemMeta(meta);

        for (int x = 0; x < inv.getSize(); x++) {
            if ((x < 9 || x > inv.getSize() - 9 || x % 9 == 0 || (x + 1) % 9 == 0) && inv.getItem(x) == null) {
                inv.setItem(x, item);
            }
        }
        return inv;
    }

    public Inventory getInstantBuyQuantityMenu(Product p, Player player) {
        NamespacedKey key = new NamespacedKey(plugin, "MenuButton");
        NamespacedKey productKey = new NamespacedKey(plugin, "Product");
        int openSlots = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if(item == null || item.getType() == Material.AIR) openSlots++;
        }
        // TODO: Make this support unstackables...
        int possibleAmmount = openSlots*p.item().getMaxStackSize();
        Inventory inv = addBorder(Bukkit.createInventory(null, 27, "%price_selection_menu_title%"));

        ItemStack demoItem = new ItemStack(p.item());
        ItemMeta demoMeta = demoItem.getItemMeta();
        demoMeta.setLore(Collections.singletonList(ChatColor.YELLOW + "This is the item you are buying"));
        demoItem.setItemMeta(demoMeta);
        demoMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "MenuGlass");
        inv.setItem(4, demoItem);

        List<String> lore = new ArrayList<>();

        //TODO: item tags
        ItemStack one = new ItemStack(p.item().getType());
        ItemMeta oneMeta = one.getItemMeta();
        oneMeta.setDisplayName(ChatColor.GREEN + "Buy one!");
        lore.add(ChatColor.DARK_GRAY + ChatColor.stripColor(p.item().getItemMeta().getDisplayName()));
        lore.add("");
        lore.add(ChatColor.GRAY + "Amount: "  + ChatColor.GREEN + "1" + ChatColor.GRAY + "x");
        lore.add("");
        lore.add(ChatColor.GRAY + "Price: " + "LOADING");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to buy now!");
        oneMeta.setLore(lore);
        oneMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "BUY_ONE");
        oneMeta.getPersistentDataContainer().set(productKey, PersistentDataType.STRING, p.id());
        lore.clear();
        one.setItemMeta(oneMeta);
        inv.setItem(10, one);

        ItemStack stack = new ItemStack(p.item().getType());
        ItemMeta stackMeta = stack.getItemMeta();
        stack.setAmount(p.item().getMaxStackSize());
        stackMeta.setDisplayName(ChatColor.GREEN + "Buy a stack!");
        lore.add(ChatColor.DARK_GRAY + ChatColor.stripColor(p.item().getItemMeta().getDisplayName()));
        lore.add("");
        lore.add(ChatColor.GRAY + "Amount: "  + ChatColor.GREEN + "64" + ChatColor.GRAY + "x");
        lore.add("");
        lore.add(ChatColor.GRAY + "Unit Price: " + "LOADING");
        lore.add(ChatColor.GRAY + "Total: " + "LOADING");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to buy now!");
        stackMeta.setLore(lore);
        stackMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "BUY_STACK");
        stackMeta.getPersistentDataContainer().set(productKey, PersistentDataType.STRING, p.id());
        lore.clear();
        stack.setItemMeta(stackMeta);
        inv.setItem(12, stack);

        ItemStack fill = new ItemStack(Material.CHEST);
        ItemMeta fillMeta = fill.getItemMeta();
        fillMeta.setDisplayName(ChatColor.GREEN + "Fill my inventory!");
        lore.add(ChatColor.DARK_GRAY + ChatColor.stripColor(p.item().getItemMeta().getDisplayName()));
        lore.add("");
        lore.add(ChatColor.GRAY + "Amount: "  + ChatColor.GREEN + possibleAmmount + ChatColor.GRAY + "x");
        lore.add("");
        lore.add(ChatColor.GRAY + "Unit Price: " + "LOADING");
        lore.add(ChatColor.GRAY + "Total: " + "LOADING");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to buy now!");
        fillMeta.setLore(lore);
        fillMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "BUY_INVENTORY");
        fillMeta.getPersistentDataContainer().set(productKey, PersistentDataType.STRING, p.id());
        lore.clear();
        fill.setItemMeta(fillMeta);
        inv.setItem(14, fill);

        ItemStack custom = new ItemStack(Material.OAK_HANGING_SIGN);
        ItemMeta customMeta = custom.getItemMeta();
        customMeta.setDisplayName(ChatColor.GREEN + "Custom Amount");
        lore.add(ChatColor.DARK_GRAY + ChatColor.stripColor(p.item().getItemMeta().getDisplayName()));
        lore.add("");
        lore.add(ChatColor.GRAY + "Buy up to: "  + ChatColor.GREEN + possibleAmmount + ChatColor.GRAY + "x");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to specify amount!");
        customMeta.setLore(lore);
        lore.clear();
        customMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "BUY_CUSTOM");
        customMeta.getPersistentDataContainer().set(productKey, PersistentDataType.STRING, p.id());
        custom.setItemMeta(customMeta);
        inv.setItem(16, custom);

        return inv;
    }
}
