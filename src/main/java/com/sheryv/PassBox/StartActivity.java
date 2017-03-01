package com.sheryv.PassBox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
    private static final int STATUS_ENC = 1;
    private static final int STATUS_DEC = 2;
    private static final int STATUS_HASH = 3;
    private static final int STATUS_JSON = 4;
    private static final int SCREEN_PROFILES = 1;
    private static final int SCREEN_UNLOCK = 2;
    private static boolean deleteMode = false;
    public byte statusFlag = 0;
    public byte screenflag = 0;
    boolean visble = false;
    private ArrayList<GroupItems> groupItems;
    private static ArrayList<String> profiles;
    private static ArrayAdapter<String> adapter;
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
        TextView textView = ((TextView) findViewById(R.id.version));
        textView.setText(textView.getText() + " " + Sh.version);
        passUnlock = (EditText) findViewById(R.id.unlockET);
        passSetup = (EditText) findViewById(R.id.setup_pass);
        unlockL = (LinearLayout) findViewById(R.id.unlock_layout);
        setupL = (RelativeLayout) findViewById(R.id.setup_layout);
        profileL = (RelativeLayout) findViewById(R.id.profile_lauout);
        profileList = (ListView) findViewById(R.id.prof_list);
        passUnlock.setText("");
        profiles = new ArrayList<>();
        profiles.clear();

        if (savedInstanceState != null)
        {
            statusFlag = savedInstanceState.getByte("stateflag");
            screenflag = savedInstanceState.getByte("screenflag");
            switch (screenflag)
            {
                case SCREEN_UNLOCK:
                    prepUnlock();
                    break;
                case SCREEN_PROFILES:
                default:
                    prepProfileChoose();
                    break;
            }
            showLoadingBar(this, statusFlag > 0);
        }
        else
        {
            showLoadingBar(this, false);
            prepProfileChoose();
        }
        if (Sh.getPref().getBoolean(getString(R.string.pref_setup_done), false)) // true set
        {
            //todo show tutorial info i zmienic te wartosc uzywajac ponizej
        /*  SharedPreferences.Editor editor = Sh.getPref().edit();
            editor.putBoolean(getString(R.string.pref_setup_done), true);
            editor.commit();*/
        }
        adapter = new ArrayAdapter<String>(this, R.layout.activity_list_profile, R.id.prof_item_text, profiles);
        profileList.setAdapter(adapter);
        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                ContinueStart((String) adapterView.getItemAtPosition(i));
            }
        });
        CheckBox cb = (CheckBox) findViewById(R.id.prof_delete_cb);
        for (int i = 0; i < profileList.getChildCount(); i++)
        {
            ImageButton btnDel = (ImageButton) profileList.getChildAt(i).findViewById(R.id.prof_delete);
             btnDel.setFocusable(false);
        }

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                deleteMode = b;
            }
        });
        updateProfileList();
    }


    private void ContinueStart(String prof)
    {
        File myFile = new File(Sh.path.getAbsolutePath() + Sh.namePath + prof);
        if (!myFile.exists())
        {
            Sh.msgl(getString(R.string.data_file_not_exist));
            return;
        }
        Sh.profile = prof;
        prepUnlock();
        //Sh.d("Shared prefs: " + Sh.getPref().contains(getString(R.string.pref_setup_done)) + " | " + String.valueOf(Sh.getPref().getBoolean(getString(R.string.pref_setup_done), false)));
/*
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
        }*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putByte("stateflag", (byte) statusFlag);
        outState.putByte("screenflag", screenflag);
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
        screenflag = SCREEN_UNLOCK;
        profileL.setVisibility(View.GONE);
        passUnlock.setFocusable(true);
        passUnlock.setFocusableInTouchMode(true);
        passUnlock.setText("");
        unlockL.setVisibility(View.VISIBLE);
        setupL.setVisibility(View.GONE);
    }

    private void prepProfileChoose()
    {
        screenflag = SCREEN_PROFILES;
        passUnlock.setFocusable(false);
        passUnlock.setFocusableInTouchMode(false);
        passUnlock.setText("");
        unlockL.setVisibility(View.GONE);
        setupL.setVisibility(View.GONE);
        profileL.setVisibility(View.VISIBLE);
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
        if (statusFlag > 0)
            return;
        if (passUnlock.getText().toString().length() < Sh.passwordLenght)
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

    @Override
    public void onBackPressed()
    {
        if (screenflag == SCREEN_UNLOCK)
        {
            prepProfileChoose();
        }
        else
            super.onBackPressed();
    }

    public void btnloadingtest(View view)
    {
        showLoadingBar(this, visble);
        visble = !visble;
    }


    public static void updateProfileList()
    {
        String path = Sh.path.getAbsolutePath() + Sh.namePath;
        File f = new File(path);
        File file[] = f.listFiles();
        String n = "";
        profiles.clear();
        for (int i = 0; i < file.length; i++)
        {
            if (file[i].getName().length() > 2)
            {
                n = file[i].getName();
                if (profiles == null)
                {
                    Sh.msg("Error! List not initialized yet");
                    return;
                }
                profiles.add(n);
                Sh.d("FileName: " + n);
            }
        }
        if (adapter == null)
        {
            Sh.msg("Error! Adapter not initialized yet");
            return;
        }
        adapter.notifyDataSetChanged();
    }

    Dialog dialog2;

    public void profileCreate(View view)
    {
        dialog2 = new Dialog(view.getContext());
        dialog2.setContentView(R.layout.dialog_edit);
        dialog2.setTitle(getResources().getText(R.string.add_profile));

        final EditText name = (EditText) dialog2.findViewById(R.id.d_name);
        final EditText web = (EditText) dialog2.findViewById(R.id.d_web);

        Button editButton = (Button) dialog2.findViewById(R.id.d_btnEdit);
        Button delButton = (Button) dialog2.findViewById(R.id.d_btnDelete);
        delButton.setVisibility(View.GONE);
         InputFilter filter =  new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           Spanned dest, int dstart, int dend) {

                    if (source instanceof SpannableStringBuilder) {
                        SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                        for (int i = end - 1; i >= start; i--) {
                            char currentChar = source.charAt(i);
                            if (!Character.isLetterOrDigit(currentChar) && !Character.isSpaceChar(currentChar)) {
                                sourceAsSpannableBuilder.delete(i, i + 1);
                            }
                        }
                        return source;
                    } else {
                        StringBuilder filteredStringBuilder = new StringBuilder();
                        for (int i = start; i < end; i++) {
                            char currentChar = source.charAt(i);
                            if (Character.isLetterOrDigit(currentChar) || Character.isSpaceChar(currentChar)) {
                                filteredStringBuilder.append(currentChar);
                            }
                        }
                        return filteredStringBuilder.toString();
                    }
                }
            };
        name.setFilters(new InputFilter[] {filter});
        web.setHint(R.string.pass);
        web.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editButton.setText(getResources().getText(R.string.accept).toString());
        editButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (name.getText().toString().isEmpty())
                {
                    Sh.msg(getResources().getText(R.string.no_null_pass).toString());
                }
                else
                {
                    if (web.getText().toString().length() < Sh.passwordLenght)
                    {
                        Sh.msgl(getString(R.string.setup_pass_lenght));
                        dialog2.dismiss();
                        return;
                    }
                    String n = name.getText().toString();
                    if (profiles.contains(n))
                    {
                        Sh.msg("Profile already exist");
                        dialog2.dismiss();
                        return;
                    }
                    if (n.length() < 3 || n.length() > 51)
                    {
                        Sh.msg(getString(R.string.profile_name));
                        return;
                    }
                    ArrayList<Items> itemses = new ArrayList<>();
                    ArrayList<GroupItems> groupItemses = new ArrayList<GroupItems>();
                    itemses.add(new Items("Login", "Example password", "01:00 01/01/2000", "Description"));
                    groupItemses.add(new GroupItems("Example group", itemses, 0, false, 1, "google.com"));
                    Sh.profile = name.getText().toString();
                    Sh.setShpb(web.getText().toString());
                    new SaveDataAsync().execute(new DataPattern(null, groupItemses, Sh.saveTypeStartActivity));
                    dialog2.dismiss();
                }
            }
        });
        dialog2.show();

    }

    void dialogC(String s)
    {
        Sh.showDialog(getString(R.string.data_altered), R.string.got, R.string.d_try_again, R.string.d_info, false, s, this, PassCryptApp.eventReadFailed, null);
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
            statusFlag = 0;
            //   btn.getBackground().setColorFilter(filter);
            view.setVisibility(View.GONE);
        }
    }

    private void onLoadedGroup(DataPattern groupItems)
    {
        showLoadingBar(this, false);
        if (groupItems != null)
        {
            Sh.msg(getString(R.string.success));
            Intent intent = new Intent(this, PActivity.class);
            Sh.unlock(this);
            passUnlock.setText("");
            intent.putExtra("starter", groupItems.g);
            prepProfileChoose();
            startActivity(intent);
        }
    }

    public void deleteProfile(View btn)
    {
        if (deleteMode)
        {
            View view = (View) btn.getParent();
            String t = ((TextView) view.findViewById(R.id.prof_item_text)).getText().toString();
            if (t.isEmpty())
            {
                Sh.msg("Error! Empty profile name! Operation canceled.");
                return;
            }
            adapter.remove(t);
            deleteProfileFile(t);
        }
        else
            Sh.msg(getString(R.string.delete_mode_required));
    }

    private void deleteProfileFile(String name)
    {
        File myFile = new File(Sh.path.getAbsolutePath() + Sh.namePath+name);
        if (!myFile.exists())
        {
            Sh.msg("File with that name does not exist");
        }
        else
        {
           boolean f = myFile.delete();
            if (!f)
                Sh.msg("Unknown error occurred outside application. File was not deleted properly.");
            else
            {
                updateProfileList();
                Sh.d(getString(R.string.success));
            }
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
            statusFlag = 0;
        }
    }


}
