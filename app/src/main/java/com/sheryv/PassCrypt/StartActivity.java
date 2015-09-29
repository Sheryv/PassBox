package com.sheryv.PassCrypt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class StartActivity extends ActionBarActivity implements ICanDialog
{
    private ArrayList<GroupItems> groupItems;
    private EditText passUnlock;
    private EditText passSetup;
    private LinearLayout unlockL;
    private RelativeLayout setupL;
    private Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Sh.addActiv(this);
        TextView textView =((TextView) findViewById(R.id.version));
        textView.setText(textView.getText()+" "+Sh.version);
        passUnlock = (EditText) findViewById(R.id.unlockET);
        passSetup = (EditText) findViewById(R.id.setup_pass);
        unlockL = (LinearLayout) findViewById(R.id.unlock_layout);
        setupL = (RelativeLayout) findViewById(R.id.setup_layout);
        Sh.d("Shared prefs: " + Sh.getPref().contains(getString(R.string.pref_setup_done)) + " | " + String.valueOf(
                Sh.getPref().getBoolean(getString(R.string.pref_setup_done), false)));



            if (Sh.getPref().getBoolean(getString(R.string.pref_setup_done), false)) // true set
            {
                File myFile = new File(Sh.path.getAbsolutePath()+Sh.namePath);
                if (!myFile.exists())
                {
                    Sh.msgl(getString(R.string.data_file_not_exist));
                    prepSetup();
                }
                else
                {
                    prepUnlock();
                }
            }
            else
            {   // first setup
                prepSetup();
                File myFile = new File(Sh.path.getAbsolutePath()+Sh.namePath);
                if (myFile.exists())
                {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage(getString(R.string.data_old_file_exist));
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(getString(R.string.data_use_old),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    SharedPreferences.Editor editor = Sh.getPref().edit();
                                    editor.putBoolean(getString(R.string.pref_setup_done), true);
                                    editor.commit();
                                    recreate();
                                    dialog.dismiss();
                                }
                            });
                    builder1.setNegativeButton(getString(R.string.data_create_new),
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {

                                    prepSetup();
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }

            }



    }

    private void prepSetup()
    {
        Sh.msgd("New setup");
        unlockL.setVisibility(View.GONE);
        setupL.setVisibility(View.VISIBLE);
    }
    private void prepUnlock()
    {
        unlockL.setVisibility(View.VISIBLE);
        setupL.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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

    public void unlockClick(View view)
    {
        if (passUnlock.getText().toString().length() < 8)
        {
            Sh.msgl(getString(R.string.setup_pass_lenght));
            return;
        }
        Sh.setShpb(passUnlock.getText().toString());
        groupItems = Sh.readFile(StartActivity.this);
        if(groupItems == null)
        {
        }
        else
        {
            SharedPreferences.Editor editor = Sh.getPref().edit();
            editor.putBoolean(getString(R.string.pref_setup_done), true);
            editor.commit();
            Intent intent = new Intent(this, PActivity.class);
            Sh.unlock(this);
            // String json = Sh.toJson(groupItems);
            //  Sh.fromJson(json);
            intent.putExtra("starter", groupItems);
            startActivity(intent);
        }

    }
    @Override
    protected void onPause()
    {
        super.onPause();
        if (dialog != null)
            dialog.dismiss();
    }

    public void setupClick(View view)
    {
        if (passSetup.getText().toString().length() < 8)
        {
            Sh.msgl(getString(R.string.setup_pass_lenght));
            return;
        }
        Sh.setShpb(passSetup.getText().toString());
        SharedPreferences.Editor editor = Sh.getPref().edit();
        editor.putBoolean(getString(R.string.pref_setup_done), true);
        editor.commit();
        groupItems = new ArrayList<>();
        Intent intent = new Intent(this, PActivity.class);
        Sh.unlock(this);
        for (int i =0; i<8;i++)
        {
            ArrayList<Items> itemses = new ArrayList<>();
            itemses.add(new Items("Login 1", "Example password", "01:00 01/01/2000", "Description"));
            itemses.add(new Items("Login 2", "Example password 2", "01:00 01/01/2000", "Description 2"));
            groupItems.add(new GroupItems("Example group", itemses, 0, false, 1,
                    "google.com || you should delete this group"));
        }

        intent.putExtra("starter", groupItems);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    public Dialog getDialog()
    {
        return this.dialog;
    }

    @Override
    public void setDialog(Dialog d)
    {
        this.dialog = d;
    }
}
