package dev.foxikle.playermarket;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Order implements Serializable {
    /**
     * The type of order it is
     */
    private final OrderType type;
    /**
     * The unique identifier of the order (Database key!)
     */
    private final String orderID;
    /**
     * The UUID of the player who owns the order
     */
    private final UUID owner;
    /**
     * The amount of items the order is for
     */
    private final short quantity;
    /**
     * The type of item the order is for
     */
    private final Product product;
    /**
     * The people who have contributed to filling the order
     */
    private Set<OrderFiller> fillers;
    /**
     * The price the order is selling at
     */
    private double price;
    /**
     * The amount of product that is filled
     */
    private short filled;
    /**
     * The amount of item/coins that are in escrow
     */
    private long escrowed;

    /**
     * The Item ID to use in searching
     */
    private String itemID;
    /**
     *
     * @param type
     * @param owner
     * @param fillers
     * @param price
     * @param quantity
     * @param filled
     * @param escrowed
     */

    public Order(OrderType type, UUID owner, Set<OrderFiller> fillers, double price, short quantity, Product product, short filled, long escrowed) {
        this.type = type;
        this.orderID = generateID();
        this.owner = owner;
        this.fillers = fillers;
        this.price = price;
        this.quantity = quantity;
        this.product = product;
        this.filled = filled;
        this.escrowed = escrowed;
        this.itemID = product.id();
    }

    public long getEscrowed() {
        return escrowed;
    }

    public void setEscrowed(long escrowed) {
        this.escrowed = escrowed;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public OrderType getType() {
        return type;
    }

    public Product getProduct() {
        return product;
    }

    public String getOrderID() {
        return orderID;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<OrderFiller> getFillers() {
        return fillers;
    }

    public void setFillers(Set<OrderFiller> fillers) {
        this.fillers = fillers;
    }

    public short getFilled() {
        return filled;
    }

    public void setFilled(short filled) {
        this.filled = filled;
    }

    public short getQuantity() {
        return quantity;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String serialize(){
        return PlayerMarket.getInstance().getGson().toJson(this);
    }

    public static Order deserialize(String str){
        return PlayerMarket.getInstance().getGson().fromJson(str, Order.class);
    }

    public enum OrderType {
        BUY,
        SELL
    }

    private String generateID(){
        String characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = characterSet.length();
        StringBuilder uniqueId = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(length);
            char character = characterSet.charAt(index);
            uniqueId.append(character);
        }

        return uniqueId.toString();
    }
}
