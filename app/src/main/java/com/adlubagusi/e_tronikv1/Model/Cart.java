package com.adlubagusi.e_tronikv1.Model;

public class Cart {
    private String pid, pname, price, quantity, discout;

    public Cart(){

    }
    public Cart(String pid, String pname, String price, String quantity, String discout) {
        this.pid = pid;
        this.pname = pname;
        this.price = price;
        this.quantity = quantity;
        this.discout = discout;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDiscout() {
        return discout;
    }

    public void setDiscout(String discout) {
        this.discout = discout;
    }
}
