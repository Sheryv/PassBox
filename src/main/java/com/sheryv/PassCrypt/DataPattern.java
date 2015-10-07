package com.sheryv.PassCrypt;

import java.util.ArrayList;

/**
 * Created by Sheryv on 29.09.2015.
 */
public class DataPattern
{
    public ArrayList<GroupItems> g;
    public String error;

    public DataPattern(String error, ArrayList<GroupItems> g)
    {

        this.error = error;
        this.g = g;
    }
}
