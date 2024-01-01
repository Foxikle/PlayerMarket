package dev.foxikle.playermarket.listeners;

import dev.foxikle.playermarket.Order;
import dev.foxikle.playermarket.PlayerMarket;
import dev.foxikle.playermarket.Product;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Objects;

public class InventoryClickListener implements Listener {

    private PlayerMarket plugin;

    public InventoryClickListener(PlayerMarket plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getAction() == InventoryAction.COLLECT_TO_CURSOR) // prevents double clicks
            return;
        NamespacedKey key = new NamespacedKey(plugin, "MenuButton");
        NamespacedKey idKey = new NamespacedKey(plugin, "OrderButton");
        NamespacedKey pKey = new NamespacedKey(plugin, "Product");
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getItemMeta() == null) return;
        if (!e.getCurrentItem().getItemMeta().getPersistentDataContainer().getKeys().contains(key)) return;

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        String data = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        String product = "ERROR!";
        if(meta.getPersistentDataContainer().get(pKey, PersistentDataType.STRING) != null) {
            product = meta.getPersistentDataContainer().get(pKey, PersistentDataType.STRING);
        }
        Player player = (Player) e.getWhoClicked();
        /*
        Page navigation buttons
         */
        if(Objects.equals(data, "BUY_ONE")) {
            if(Objects.equals(product, "ERROR!")) throw new NullPointerException("The product tag within the item cannot be null!");
            Product p = plugin.getProducts().get(product);
            if(p == null) throw new NullPointerException("The product " + product + " does not exist!");
            plugin.getEscrow().processInstantBuy(p, 1, player);
        } else if(Objects.equals(data, "BUY_STACK")) {
            if(Objects.equals(product, "ERROR!")) throw new NullPointerException("The product tag within the item cannot be null!");
            Product p = plugin.getProducts().get(product);
            if(p == null) throw new NullPointerException("The product " + product + " does not exist!");
            plugin.getEscrow().processInstantBuy(p, p.item().getMaxStackSize(), player);
        } else if(Objects.equals(data, "BUY_INVENTORY")) {
            if(Objects.equals(product, "ERROR!")) throw new NullPointerException("The product tag within the item cannot be null!");
            Product p = plugin.getProducts().get(product);
            if(p == null) throw new NullPointerException("The product " + product + " does not exist!");
            int openSlots = 0;
            for (ItemStack i : player.getInventory().getContents()) {
                if(i == null || i.getType() == Material.AIR) openSlots++;
            }
            plugin.getEscrow().processInstantBuy(p, openSlots*p.item().getMaxStackSize(), player);
        } else if(Objects.equals(data, "BUY_CUSTOM")) {
            if(Objects.equals(product, "ERROR!")) throw new NullPointerException("The product tag within the item cannot be null!");
            Product p = plugin.getProducts().get(product);
            if(p == null) throw new NullPointerException("The product " + product + " does not exist!");
            // todo: collect input probably with a sign or anvil
        } else if (plugin.getPages().contains(data)) { // there is a valid page
            player.openInventory(plugin.getInvs().inventories.get(data));
        } else if (plugin.getProducts().keySet().contains(data)) { // there is a valid product
            player.openInventory(plugin.invs.getProductMenu(plugin.getProducts().get(data), player));
        } else if (plugin.getFamilies().keySet().contains(data)) { // there is a valid product family
            player.openInventory(plugin.getInvs().getFamilyMenu(plugin.getFamilies().get(data)));
        } else if (meta.getPersistentDataContainer().getKeys().contains(idKey)) {
            String id = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
            Product p = PlayerMarket.getInstance().getProducts().get(id);
            if (data.equalsIgnoreCase("INSTANT_BUY")) {
                player.openInventory(plugin.getInvs().getInstantBuyQuantityMenu(p, player));
                /*
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    if(plugin.getDb().getSellOrders(id).isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Couldn't find any sell offers to fill!");
                        return;
                    }
                    int amount = plugin.itemCount(player, id);
                    plugin.getEscrow().processInstantBuy(product, amount, player);
                });
                 */
            } else if (data.equalsIgnoreCase("INSTANT_SELL")) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    if(plugin.getDb().getBuyOrders(id).isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Couldn't find any buy orders to fill!");
                        return;
                    }
                    int amount = plugin.itemCount(player, id);
                    plugin.getEscrow().processInstantSell(p, amount, player);
                });

                // update the order too.
            } else if (data.equalsIgnoreCase("BUY_ORDER")) {
                int price = 10;
                int quantity = 100;
                plugin.getEscrow().takeCoins(price * quantity, player);
                Order order = new Order(Order.OrderType.BUY, player.getUniqueId(), Collections.emptySet(), price, (short) quantity, p, (short) 0, price*quantity);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDb().addOrder(order));
            } else if (data.equalsIgnoreCase("SELL_OFFER")) {
                int price = 10;
                int quantity = 100;
                plugin.getEscrow().processSellOrder(p, price, quantity, player);
                Order order = new Order(Order.OrderType.BUY, player.getUniqueId(), Collections.emptySet(), price, (short) quantity, p, (short) 0, price*quantity);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getDb().addOrder(order));
            }
        } else {
            return;
        }
        e.setCancelled(true);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
    }
}
