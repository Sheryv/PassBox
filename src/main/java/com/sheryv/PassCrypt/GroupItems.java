package com.sheryv.PassCrypt;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sheryv on 20.09.2015.
 */
public class GroupItems implements Serializable
{
    public String name;
    public String website;
    public String profile;
    public ArrayList<Items> items;
    public boolean favourite;
    public int color = 0;
    public int bgColor = 1;
    public String extra;
    public transient boolean isExpanded = false;

    public GroupItems(String name, ArrayList<Items> items, int color, boolean favourite, int bgColor, String website)
    {
        this.name = name;
        this.items = items;
        this.color = color;
        this.favourite = favourite;
        this.bgColor = bgColor;
        this.website = website;
        this.extra = "e";
        this.profile = "p";
    }
    public GroupItems()
    {
        this.name = "";
        this.items = new ArrayList<Items>();
        this.color = 0;
        this.favourite = false;
        this.bgColor = 1;
        this.website = "";
        this.extra = "e";
        this.profile = "p";
    }
}
