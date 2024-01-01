package dev.foxikle.playermarket.data;

import dev.foxikle.playermarket.Order;
import dev.foxikle.playermarket.PlayerMarket;
import dev.foxikle.playermarket.Product;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class Database {

    //TODO: Friendly reminder to call it async!

    private final PlayerMarket plugin = PlayerMarket.getInstance();

    private final String host = plugin.getConfig().getString("sqlHost");
    private final String port = plugin.getConfig().getString("sqlPort");
    private final String database = plugin.getConfig().getString("sqlName");
    private final String username = plugin.getConfig().getString("sqlUsername");
    private final String password = plugin.getConfig().getString("sqlPassword");
    private Connection connection;
    
    protected boolean isConnected() {
        return (connection != null);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if (!isConnected())
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected Connection getConnection() {
        return connection;
    }

    public void createOrderTables() {
        PreparedStatement ps;
        try {
            ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS sellorders (id VARCHAR(16), item VARCHAR(100), owner VARCHAR(50), content TEXT, PRIMARY KEY (id))");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PreparedStatement ps1;
        try {
            ps1 = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS buyorders (id VARCHAR(16), item VARCHAR(100), owner VARCHAR(50), content TEXT, PRIMARY KEY (id))");
            ps1.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<Order> getPlayerBuyOrders(UUID uuid){
        PreparedStatement ps;
        Set<Order> returnme = new HashSet<>();
        try {
            ps = getConnection().prepareStatement("SELECT * FROM buyorders WHERE uuid=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String str = rs.getString("content");
                if (!str.equals("")) {
                    returnme.add(Order.deserialize(str));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnme;
    }

    public Set<Order> getPlayerSellOrders(UUID uuid){
        PreparedStatement ps;
        Set<Order> returnme = new HashSet<>();
        try {
            ps = getConnection().prepareStatement("SELECT * FROM sellorders WHERE uuid=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String str = rs.getString("content");
                if (!str.equals("")) {
                    returnme.add(Order.deserialize(str));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnme;
    }

    public Set<Order> getGlobalBuyOrders(){
        PreparedStatement ps;
        Set<Order> returnme = new HashSet<>();
        try {
            ps = getConnection().prepareStatement("SELECT * FROM buyorders");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String str = rs.getString("content");
                if (!str.equals("")) {
                    returnme.add(Order.deserialize(str));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnme;
    }

    public Set<Order> getGlobalSellOrders(){
        PreparedStatement ps;
        Set<Order> returnme = new HashSet<>();
        try {
            ps = getConnection().prepareStatement("SELECT * FROM sellorders");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String str = rs.getString("content");
                if (!str.equals("")) {
                    returnme.add(Order.deserialize(str));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnme;
    }

    public Set<Order> getSellOrders(String item_id){
        PreparedStatement ps;
        Set<Order> returnme = new HashSet<>();
        try {
            ps = getConnection().prepareStatement("SELECT * FROM sellorders where item=?");
            ps.setString(1, item_id);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String str = rs.getString("content");
                if (!str.equals("")) {
                    returnme.add(Order.deserialize(str));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnme;
    }

    public Set<Order> getBuyOrders(String item_id){
        PreparedStatement ps;
        Set<Order> returnme = new HashSet<>();
        try {
            ps = getConnection().prepareStatement("SELECT * FROM buyorders where item=?");
            ps.setString(1, item_id);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String str = rs.getString("content");
                returnme.add(Order.deserialize(str));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnme;
    }

    public void updateOrder(String id, Order newOrder) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("UPDATE sellorders SET content=? WHERE id=?");
            ps.setString(1, newOrder.serialize());
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            PreparedStatement ps = getConnection().prepareStatement("UPDATE buyorders SET content=? WHERE id=?");
            ps.setString(1, newOrder.serialize());
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteOrder(String id) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("DELETE FROM sellorders WHERE id=?");
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            PreparedStatement ps = getConnection().prepareStatement("DELETE FROM buyorders WHERE id=?");
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addOrder(Order order){
        if(order.getType() == Order.OrderType.BUY){
            try {
                PreparedStatement ps = getConnection().prepareStatement("INSERT INTO buyorders (id, item, owner, content) VALUES (?,?,?,?)");
                ps.setString(1, order.getOrderID());
                ps.setString(2, order.getProduct().id());
                ps.setString(3, order.getOwner().toString());
                ps.setString(4, order.serialize());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (order.getType() == Order.OrderType.SELL) {
            try {
                PreparedStatement ps = getConnection().prepareStatement("INSERT INTO sellorders (id, item, owner, content) VALUES (?,?,?,?)");
                ps.setString(1, order.getOrderID());
                ps.setString(2, order.getProduct().id());
                ps.setString(3, order.getOwner().toString());
                ps.setString(4, order.serialize());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public double getSellOrderPrice(Product p, boolean lowest){
        List<Order> order = new ArrayList<>(getSellOrders(p.id()).stream().toList());
        if(order.isEmpty())
            return -1.0;
        order.sort(Comparator.comparingDouble(Order::getPrice));
        if(lowest) {
            return order.get(0).getPrice();
        } else {
            return order.get(order.size()-1).getPrice();
        }
    }
}
