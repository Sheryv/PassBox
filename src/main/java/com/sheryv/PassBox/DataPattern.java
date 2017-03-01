package com.sheryv.PassBox;

import java.util.ArrayList;

/**
 * Created by Sheryv on 29.09.2015.
 */
public class DataPattern
{
    public ArrayList<GroupItems> g;
    public String error;
    public byte type;
    public DataPattern(String error, ArrayList<GroupItems> g)
    {

        this.error = error;
        this.g = g;
        type = Sh.saveTypePActivity;
    }
    public DataPattern(String error, ArrayList<GroupItems> g, byte type)
    {

        this.error = error;
        this.g = g;
        this.type = type;
    }
}
