package com.gabrielruizm.stalkershop;

/**
 * Created by gabriel on 09-10-14.
 */
public class Item {
    private String name;
    private String url;
    private int price;
    private int serverID;
    private String shopName;
    private boolean isNew;

    public Item(){}

    public Item(String name, String url, int price, int serverID) {
        this.name = name;
        this.url = url;
        this.price = price;
        this.serverID = serverID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        //   this.name = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url.startsWith("http://"))
            this.url = url;
        else
            this.url = ("http://".concat(url));
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
