package com.sheryv.PassCrypt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;


public class StartActivity extends ActionBarActivity implements ICanDialog
{
    public static final String READ_FLAG = "READ_FLAG";
    public static final String WRITE_FLAG = "WRITE_FLAG";
    private static final int STATUS_ENC = 0;
    private static final int STATUS_DEC = 1;
    private static final int STATUS_HASH = 2;
    private static final int STATUS_JSON = 3;
    public int statusFlag = -1;
    boolean visble = false;
    private ArrayList<GroupItems> groupItems;
    private ArrayList<String> profiles;
    private ArrayAdapter<String> adapter;
    private EditText passUnlock;
    private EditText passSetup;
    private ListView profileList;
    private LinearLayout unlockL;
    private RelativeLayout setupL;
    private RelativeLayout profileL;
    private Dialog dialog;
    private String status;
    private String profileName;
    private ColorFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Sh.addActiv(this);
        if (savedInstanceState != null)
        {
            statusFlag = savedInstanceState.getByte("stateflag");
            showLoadingBar(this, statusFlag >= 0);
        }
        else
            showLoadingBar(this, false);
        TextView textView = ((TextView) findViewById(R.id.version));
        textView.setText(textView.getText() + " " + Sh.version);
        passUnlock = (EditText) findViewById(R.id.unlockET);
        passSetup = (EditText) findViewById(R.id.setup_pass);
        unlockL = (LinearLayout) findViewById(R.id.unlock_layout);
        setupL = (RelativeLayout) findViewById(R.id.setup_layout);
        profileL = (RelativeLayout) findViewById(R.id.profile_lauout);
        profileList = (ListView) findViewById(R.id.prof_list);
        profiles = new ArrayList<>();
        profiles.clear();
        String path = Sh.path.getAbsolutePath() + Sh.namePath;
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i = 0; i < file.length; i++)
        {
            profiles.add(file[i].getName());
            Sh.d("FileName: " + file[i].getName());
        }
        adapter = new ArrayAdapter<String>(this, R.layout.activity_list_profile, profiles);
        profileList.setAdapter(adapter);
        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Sh.d("item click: " + (String) adapterView.getItemAtPosition(i));
                //ContinueStart(adapterView.getItemAtPosition(i));
            }
        });
    }

    private void ContinueStart(String prof)
    {
        Sh.d("Shared prefs: " + Sh.getPref().contains(getString(R.string.pref_setup_done)) + " | " + String.valueOf(
                Sh.getPref().getBoolean(getString(R.string.pref_setup_done), false)));

        // filter = ((Button) findViewById(R.id.btnUnlock)).getBackground().getColorFilter();
        if (Sh.getPref().getBoolean(getString(R.string.pref_setup_done), false)) // true set
        {
            File myFile = new File(Sh.path.getAbsolutePath() + Sh.namePath + prof);
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
            File myFile = new File(Sh.path.getAbsolutePath() + Sh.namePath + prof);
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

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putByte("stateflag", (byte) statusFlag);
    }

    private void prepSetup()
    {
        Sh.msgd("New setup");
        profileL.setVisibility(View.GONE);
        unlockL.setVisibility(View.GONE);
        setupL.setVisibility(View.VISIBLE);
    }

    private void prepUnlock()
    {
        profileL.setVisibility(View.GONE);
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
        if (statusFlag >= 0)
            return;
        if (passUnlock.getText().toString().length() < 8)
        {
            Sh.msgl(getString(R.string.setup_pass_lenght));
            return;
        }
        Sh.setShpb(passUnlock.getText().toString());
        showLoadingBar(this, true);
        //groupItems = Sh.readFile(StartActivity.this);
        new LoadDataAsync().execute(this);
        groupItems = new ArrayList<>();
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
        for (int i = 0; i < 8; i++)
        {
            ArrayList<Items> itemses = new ArrayList<>();
            itemses.add(new Items("Login 1", "Example password", "01:00 01/01/2000", "Description"));
            itemses.add(new Items("Login 2", "Example password 2", "01:00 01/01/2000", "Description 2"));
            groupItems.add(new GroupItems("Example group", itemses, 0, false, 1,
                    "google.com || you should delete this group"));
        }

        intent.putExtra("starter", groupItems);
        intent.putExtra("profile", profileName);
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

    public String getStatus()
    {
        String s = getString(R.string.loading);
        switch (statusFlag)
        {
            case STATUS_DEC:
                s += getString(R.string.loading_decrypting);
                break;
            case STATUS_ENC:
                s += getString(R.string.loading_encrypting);
                break;
            case STATUS_HASH:
                s += getString(R.string.loading_hash);
                break;
            case STATUS_JSON:
                s += getString(R.string.loading_json);
                break;
        }
        return s;
    }

    public void btnloadingtest(View view)
    {
        showLoadingBar(this, visble);
        visble = !visble;
    }

    public void profileCreate(View view) {}

    void dialogC(String s)
    {
        Sh.showDialog(getString(R.string.data_altered), R.string.got, R.string.d_try_again, R.string.d_info, true, s, this, null);
    }

    public void showLoadingBar(Activity activity, boolean show)
    {
        View view = activity.findViewById(R.id.loading_bar);
        View btn = activity.findViewById(R.id.btnUnlock);
        TextView text = (TextView) activity.findViewById(R.id.text_status);
        if (show)
        {
            btn.setEnabled(false);
            //   btn.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            text.setText(getStatus());
            view.setVisibility(View.VISIBLE);
        }
        else
        {
            btn.setEnabled(true);
            statusFlag = -1;
            //   btn.getBackground().setColorFilter(filter);
            view.setVisibility(View.GONE);
        }
    }

    private void onLoadedGroup(DataPattern groupItems)
    {
        if (groupItems != null)
        {
            Sh.msg(getString(R.string.success));
            SharedPreferences.Editor editor = Sh.getPref().edit();
            editor.putBoolean(getString(R.string.pref_setup_done), true);
            editor.commit();
            Intent intent = new Intent(this, PActivity.class);
            Sh.unlock(this);
            passUnlock.setText("");
            // String json = Sh.toJson(groupItems);
            //  Sh.fromJson(json);
            intent.putExtra("starter", groupItems.g);
            startActivity(intent);
        }
    }

    public class LoadDataAsync extends AsyncTask<StartActivity, String, DataPattern>
    {
        @Override
        protected DataPattern doInBackground(StartActivity... activities)
        {
            StartActivity activity = activities[0];
            DataPattern d = Sh.readFile(activity);
            ArrayList<GroupItems> groupItemses = d.g;
            if (groupItemses == null)
            {
                publishProgress(d.error);
                return null;
            }
            return d;
        }

        @Override
        protected void onPreExecute()
        {
            statusFlag = 0;
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            super.onProgressUpdate(values);
            dialogC(values[0]);
        }


        @Override
        protected void onPostExecute(DataPattern integer)
        {
            super.onPostExecute(integer);
            onLoadedGroup(integer);
        }
    }


}
