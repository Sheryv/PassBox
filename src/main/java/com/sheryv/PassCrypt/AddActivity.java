package com.sheryv.PassCrypt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class AddActivity extends ActionBarActivity
{

    private Items item;
    private int group;
    private EditText log;
    private EditText pass;
    private EditText desc;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Sh.addActiv(this);
        group = getIntent().getIntExtra("group", -1);
        if (group == -1)
        {
            Sh.msg("Error while loading data");
        }
        log=((EditText)findViewById(R.id.log));
        pass=((EditText)findViewById(R.id.pass));
        desc=((EditText)findViewById(R.id.desc));
    }


    public void addClick(View view)
    {
        if(log.getText().toString().isEmpty() || pass.getText().toString().isEmpty())
        {
            Sh.msg(getResources().getText(R.string.no_null_pass).toString());
            return;
        }
        item = new Items(log.getText().toString(), pass.getText().toString(), Sh.getTime(), desc.getText().toString());
        Intent intent = new Intent();
        intent.putExtra("resultadd", item);
        intent.putExtra("resultgroup", group);
        setResult(RESULT_OK,intent);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_add, menu);
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
                    addClick(item.getActionView());
                    return true;
                default:
                    super.onOptionsItemSelected(item);
            }
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
