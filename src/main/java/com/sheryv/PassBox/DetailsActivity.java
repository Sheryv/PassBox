package com.sheryv.PassBox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class DetailsActivity extends ActionBarActivity
{
    private GroupItems groupItems;
    private Items item;
    private int groupPos;
    private int childPos;
    private EditText log;
    private EditText pass;
    private TextView logT;
    private TextView passT;
    private EditText desc;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Sh.addActiv(this);



        groupItems = (GroupItems) getIntent().getSerializableExtra("dane");
        groupPos =  getIntent().getIntExtra("danepos", -1);
        childPos =  getIntent().getIntExtra("danechild", -1);
        item = groupItems.items.get(childPos);
        if (groupItems == null || groupPos == -1|| childPos == -1)
        {
            Sh.msg("Error while loading data");
            finish();
        }
        else
        {
            log=((EditText)findViewById(R.id.log));
            pass=((EditText)findViewById(R.id.pass));
            desc=((EditText)findViewById(R.id.desc));
            passT=((TextView)findViewById(R.id.passText));
            logT=((TextView)findViewById(R.id.logText));
            passT.setText(changePassString(item.pass));
            logT.setText(item.login);
            getSupportActionBar().setTitle(getResources().getText(R.string.details) + " - " + groupItems.name);
            log.setText(item.login);
            pass.setText(item.pass);
            desc.setText(item.desc);
            ((TextView)findViewById(R.id.modifiedText)).setText(getResources().getText(R.string.modified)+": "+ item.modifDate);
        }
        ((ImageButton) findViewById(R.id.show)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        passT.setText(item.pass);
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                        passT.setText(changePassString(item.pass));
                    default:
                        break;

                }
                return false;
            }
        });
        ((ImageButton) findViewById(R.id.show)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                passT.setText(changePassString(item.pass));
            }
        });
    }

    private String changePassString(String pass)
    {
        String p = "";
        for (int i = 0; i < pass.length(); i++)
        {
            p += "*";
        }
        return p;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (Sh.setMenuHadlers(item.getItemId(), this))
            return true;
        else
        {
            switch (item.getItemId())
            {
                case R.id.accept_add:
                    editClick(item.getActionView());
                    return true;
                case R.id.m_delete:
                    delete(this);
                    return true;
                default:
                    super.onOptionsItemSelected(item);
            }
            return false;
        }

    }

    public void editClick(View view)
    {
        String l = log.getText().toString();
        String p = pass.getText().toString();
        String d = desc.getText().toString();
        if(l.isEmpty() || p.isEmpty())
        {
            Sh.msg(getResources().getText(R.string.no_null_pass).toString());
            return;
        }
        item.login = l;
        item.pass = p;
        item.desc = d;
        item.modifDate= Sh.getTime();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", groupItems);
        returnIntent.putExtra("positiongroup", groupPos);
        returnIntent.putExtra("positionchild", childPos);
        setResult(RESULT_OK, returnIntent);
        finish();
        
    }

    public void removeClick(View view)
    {
        delete(view.getContext());
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
    void delete(Context con)
    {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(con);
        builder1.setMessage(getResources().getText(R.string.delete_conf));
        builder1.setCancelable(true);
        builder1.setPositiveButton(getResources().getText(R.string.remove),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                        deleteEnd();
                        //Sh.msg("deleting | to be done");
                    }
                });
        builder1.setNegativeButton(getResources().getText(R.string.no),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    void deleteEnd()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("position", groupPos);
        returnIntent.putExtra("positionc", childPos);
        setResult(PActivity.RESULT_DELETE,returnIntent);
        finish();
    }

    public void showPassClick(boolean show)
    {

    }
}
