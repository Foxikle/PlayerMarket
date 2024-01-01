package dev.foxikle.playermarket;

import org.bukkit.Material;

import java.util.Set;

public class ProductFamily {
    private String id;
    private Set<Product> productSet;
    private Material icon;

    public ProductFamily(String id, Set<Product> productSet, Material icon) {
        this.id = id;
        this.productSet = productSet;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public Set<Product> getProductSet() {
        return productSet;
    }

    public Material getIcon() {
        return icon;
    }
}
