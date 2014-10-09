package com.gabrielruizm.stalkershop;

/**
 * Created by gabriel on 09-10-14.
 */
public class Item {
    private String name;
    private String url;
    private String shopName;
    private int price;
    private int serverID;

    public Item(){}

    public Item(String name, String url, String shopName, int price, int serverID) {
        this.name = name;
        this.url = url;
        this.shopName = shopName;
        this.price = price;
        this.serverID = serverID;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getShopName() {
        return shopName;
    }

    public int getPrice() {
        return price;
    }

    public int getServerID() {
        return serverID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

}
