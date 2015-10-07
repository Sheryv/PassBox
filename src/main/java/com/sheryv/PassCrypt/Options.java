package com.sheryv.PassCrypt;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;


public class Options extends ActionBarActivity
{
    Context con;
    boolean autoLock;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Sh.addActiv(this);
        Switch s=(Switch) findViewById(R.id.o_autolock);
        autoLock= Sh.getPref().getBoolean(getString(R.string.pref_autolock), false) ;

        con = this;
        s.setChecked(autoLock);
        s.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                    {
                        compoundButton.getContext();
                        //int s = b ? 1 : 0;
                        SharedPreferences.Editor editor = Sh.getPref().edit();
                        editor.putBoolean(getString(R.string.pref_autolock), b);
                        autoLock = b;
                        editor.commit();
                        Sh.msgd("switch change");
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (Sh.setMenuHadlers(item.getItemId(), this))
            return true;
        else
        {
            super.onOptionsItemSelected(item);
            return false;
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Sh.activStopped(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Sh.activStarted(this);
    }
}
