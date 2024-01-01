package dev.foxikle.playermarket.data;

import dev.foxikle.playermarket.Order;
import dev.foxikle.playermarket.OrderFiller;
import dev.foxikle.playermarket.PlayerMarket;
import dev.foxikle.playermarket.Product;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.xml.crypto.Data;
import java.util.*;

public class Escrow {

    private final PlayerMarket plugin;
    private Economy econ;

    //TODO: Do something about concurrency

    public Escrow (PlayerMarket plugin) {
        this.plugin = plugin;
        econ = plugin.getEcon();
    }

    public void refundOrder(Order order) {
        Player player = Bukkit.getPlayer(order.getOwner());
        if(player == null) return;
        if(order.getType() == Order.OrderType.BUY){
            econ.bankDeposit(player.getName(), order.getEscrowed());
        } else if (order.getType() == Order.OrderType.SELL) {
            refundItem(order.getProduct(), (int) order.getEscrowed(), player);
        }
    }

    public void processInstantSell(Product product, int quantity, Player player){
        if(takeItem(product, quantity, player)){
            fillBuyOrders(product, quantity, player);
        }
    }

    public void processSellOrder(Product product, double price, int quantity, Player player){
        if(takeItem(product, quantity, player)){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Order order = new Order(Order.OrderType.SELL, player.getUniqueId(), Collections.emptySet(), price, (short) quantity, product, (short) 0, quantity);
                plugin.getDb().addOrder(order);
            });
        }
    }

    private boolean takeItem (Product product, int quantity, Player player) {
        int toRemove = 0;
        if(plugin.itemCount(player, product.id()) >= quantity) {
            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack != null) {
                    if (stack.getAmount() <= toRemove) { // go ahead to remove the entire stack
                        if (stack.getItemMeta() != null) {
                            if (stack.getItemMeta().getPersistentDataContainer().getKeys().contains(plugin.getItemIdKey())) {
                                toRemove -= stack.getAmount();
                                stack.setAmount(0);
                            } else if (product.id().equalsIgnoreCase(stack.getType().name())) {
                                toRemove -= stack.getAmount();
                                stack.setAmount(0);
                            }
                            if (toRemove == 0) {
                                return true;
                            }
                        }
                    } else { // only remove some
                        if (stack.getItemMeta() != null) {
                            if (stack.getItemMeta().getPersistentDataContainer().getKeys().contains(plugin.getItemIdKey())) {
                                toRemove -= stack.getAmount() - toRemove;
                                stack.setAmount(stack.getAmount() - toRemove);
                                return true;
                            } else if (product.id().equalsIgnoreCase(stack.getType().name())) {
                                toRemove -= stack.getAmount() - toRemove;
                                stack.setAmount(stack.getAmount() - toRemove);
                                return true;
                            }
                        }
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }

    public void processInstantBuy(Product product, int quantity, Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if(quantity/product.item().getMaxStackSize() <= getEmptySlots(player)) { // enough space
                double cost = plugin.getDb().getSellOrderPrice(product, true);
                if(cost == -1.0) {
                    player.sendMessage(ChatColor.RED + "Could not find any orders!");
                    return;
                }
                cost  *= quantity;
                if(plugin.getEcon().getBalance(player) >= cost) {
                    plugin.getEcon().withdrawPlayer(player, cost);
                    fillSellOffers(product, quantity, player);
                    ItemStack item = new ItemStack(product.item());
                    item.setAmount(quantity);
                    player.getInventory().addItem(item);
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have enough currency!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have enough space for this!");
            }
        });
    }

    private void fillBuyOrders(Product product, int amount, Player filler){
        filler.playSound(filler.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int leftTofill = amount;
            Database db = plugin.getDb();
            List<Order> sortedList = new LinkedList<>();
            Set<Order> orders = db.getBuyOrders(product.id());
            sortedList.addAll(orders);
            sortedList.sort((o1, o2) -> (int) (o2.getPrice() - o1.getPrice())); // decending order.

            for (Order o : sortedList) {
                short missing = (short) (o.getQuantity() - o.getFilled());
                if(leftTofill == 0) break;
                if(leftTofill >= missing){
                    o.setFilled((short) (o.getFilled() + leftTofill)); // fills the order.
                    Set<OrderFiller> fillers = o.getFillers();
                    fillers.add(new OrderFiller(filler.getUniqueId(), missing, System.currentTimeMillis()));
                    o.setFillers(fillers);
                    db.deleteOrder(o.getOrderID());
                    //TODO: Deal with escrowed coins

                    OfflinePlayer op = Bukkit.getOfflinePlayer(o.getOwner());
                    if(op.isOnline()){
                        op.getPlayer().sendMessage("Bink bonk buy order filled");
                    }
                    leftTofill -= missing;
                } else {
                    o.setFilled((short) (o.getFilled() + leftTofill));
                    Set<OrderFiller> fillers = o.getFillers();
                    fillers.add(new OrderFiller(filler.getUniqueId(), (short) leftTofill, System.currentTimeMillis()));
                    o.setFillers(fillers);
                    db.updateOrder(o.getOrderID(), o);
                    leftTofill = 0;
                }
            }
            if(leftTofill > 0){
                refundItem(product, leftTofill, filler);
            }
        });
    }

    private void fillSellOffers(Product product, int amount, Player filler){
        filler.playSound(filler.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1); // maybe a different sound, dunno
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int leftTofill = amount;
            Database db = plugin.getDb();
            List<Order> sortedList = new LinkedList<>();
            Set<Order> orders = db.getSellOrders(product.id());
            sortedList.addAll(orders);
            sortedList.sort((o1, o2) -> (int) (o1.getPrice() - o2.getPrice())); // ascending order.
            //TODO: Deal with escrowed coins

            for (Order o : sortedList) {
                short missing = (short) (o.getQuantity() - o.getFilled());
                if(leftTofill == 0) break;
                if(leftTofill >= missing){
                    o.setFilled((short) (o.getFilled() + leftTofill)); // fills the order.
                    Set<OrderFiller> fillers = o.getFillers();
                    fillers.add(new OrderFiller(filler.getUniqueId(), missing, System.currentTimeMillis()));
                    o.setFillers(fillers);
                    db.deleteOrder(o.getOrderID());

                    OfflinePlayer op = Bukkit.getOfflinePlayer(o.getOwner());
                    if(op.isOnline()){
                        op.getPlayer().sendMessage("Bink bonk sell order filled");
                    }
                    leftTofill -= missing;
                } else {
                    o.setFilled((short) (o.getFilled() + leftTofill));
                    Set<OrderFiller> fillers = o.getFillers();
                    fillers.add(new OrderFiller(filler.getUniqueId(), (short) leftTofill, System.currentTimeMillis()));
                    o.setFillers(fillers);
                    db.updateOrder(o.getOrderID(), o);
                    leftTofill = 0;
                }
            }
            if(leftTofill > 0){
                refundItem(product, leftTofill, filler);
            }
        });
    }

    public void refundItem(Product product, int quantity, Player player){
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Escrow refunded " + quantity + " " + product.item().getI18NDisplayName());
        ItemStack item = product.item().clone();
        item.setAmount(quantity);
        player.getInventory().addItem(item);
    }

    public boolean takeCoins(double coinsToTake, Player player){
        if(econ.getBalance(player) >= coinsToTake) {
            econ.bankWithdraw(player.getUniqueId().toString(), coinsToTake);
            return true;
        }
        //TODO: Move all messages to a messages.yml or somethign
        player.sendMessage(ChatColor.RED + "You do not have enough money to do this!");
        return false;
    }

    private int getEmptySlots(Player p) {
        int i = 0;
        for (ItemStack item : p.getInventory().getContents())
            if (item == null || item.getType() == Material.AIR) {
                i++;
            }
        return i;
    }
}
