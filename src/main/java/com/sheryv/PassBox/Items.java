package com.sheryv.PassBox;

import java.io.Serializable;

/**
 * Created by Sheryv on 18.09.2015.
 */

public class Items implements Serializable
{

    public Items(String login,String pass,  String date, String desc)
    {

        this.login = login;
        this.pass = pass;
        this.date = date;
        this.desc = desc;
        this.modifDate = date;
        this.extra = "e";
    }
    public Items()
    {

        this.login = "";
        this.pass = "";
        this.date = "";
        this.desc = "";
        this.extra = "e";
        this.modifDate = "";
    }

    public String login;
    public String pass;
    public String date;
    public String modifDate;
    public String desc;
    public String extra;

}
