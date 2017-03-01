package com.sheryv.PassBox;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sheryv on 19.09.2015.
 */
public class PassCryptApp extends Application
{
    public static final byte HmacLenght = 28;
    public static final String namePath = "/data/passcrypt/";
    public static final byte eventReadFailed = 1;
    public static final byte eventSaveFailed = 2;
    public static final byte saveTypeStartActivity = 1;
    public static final byte saveTypePActivity = 2;
    public static final byte passwordLenght = 6;
    public static Context instance;
    public static PassCryptApp ins;
    public static String version = "";
    public static int versionCode = 0;
    public static boolean autoLock;
    public static File path;
    public static String profile;
    private static ArrayList<Activity> openedActivities;
    private static int activs = 0;
    private static boolean locked = false;
    private static String enc = "";
    private static boolean started = false;
    private byte[] pass;
    private SharedPreferences sharedPref;

    public static void exitClick(Activity activity)
    {
        int pid = android.os.Process.myPid();
        // finish();
        msg("PassBox Exit");
        if(activity instanceof PActivity)
        {
            PActivity a = (PActivity) activity;
            a.SaveItems();

        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        for (int i = openedActivities.size() - 1; i >= 0; i--)
        {
            openedActivities.get(i).finish();
        }
        activity.startActivity(intent);
        android.os.Process.killProcess(pid);
    }

    public static void mAboutClick(Context con)
    {
        Intent intent = new Intent(con, Options.class);
        con.startActivity(intent);
    }


    public static void addActiv(Activity activity)
    {
        if (!openedActivities.contains(activity))
        {
            openedActivities.add(activity);
        }
    }

    public static void removeActiv(Activity activity)
    {
        if (openedActivities.contains(activity))
        {
            openedActivities.remove(activity);
        }
    }

    public static SharedPreferences getPref()
    {
        if (ins != null)
        {
            ins.sharedPref = ins.getSharedPreferences(ins.getString(R.string.preference_file_key_sheryv),
                    Context.MODE_PRIVATE);
            if (ins.sharedPref != null)
                return ins.sharedPref;
            else
                msg("Shared prefs are still null");
        }
        msg("App instance or shared prefs are null");
        throw new NullPointerException("App instance or shared prefs are null");
    }

    private static String toJson(ArrayList<GroupItems> items) throws Exception
    {
        Gson gson = new Gson();
        String en = null;
        ApiCrypter crypter = new ApiCrypter(getShpb(), HmacLenght);
        try
        {
            String txt = gson.toJson(items);
            en = crypter.encryptNoIv(txt); // z jsona
        }
        catch (Exception e)
        {
            de(e);
            throw new Exception("Encrypting failed [ID:4]=> " + e.getMessage());

        }
        return en;
    }

    public static String saveFile(ArrayList<GroupItems> items)
    {
        String text = null;
        try
        {
            text = toJson(items);
        }
        catch (Exception e)
        {
            de(e);
            return e.getMessage();
        }

        if (text == null)
        {
            return "Empty encrypted string [ID:99]";
        }
        try
        {
            enc = text;
            File dir = new File(path.getAbsolutePath() + namePath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            File myFile = new File(path.getAbsolutePath() + namePath+profile);
            if (!myFile.exists())
            {
                myFile.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.write(text);
            myOutWriter.close();
            fOut.close();
        }
        catch (Exception e)
        {
            de(e);
            return "Could not save file: [ID:5]=> " + e.getMessage();
        }
        return "s";
    }

    public static DataPattern readFile(Activity con)
    {
        StringBuilder text = new StringBuilder();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File(path.getAbsolutePath() + namePath+profile)));
            String line;

            while ((line = br.readLine()) != null)
            {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e)
        {
            de(e);
            if ( Sh.getPref().contains(ins.getString(R.string.pref_setup_done)))
            {
                return new DataPattern( e.getMessage(), null);
                //msgl(ins.getString(R.string.data_file_not_exist) +" (" + e.getMessage()+")");
            }
            return null;
        }
        enc = text.toString();
        ApiCrypter crypter = new ApiCrypter(getShpb(), HmacLenght);
        String dec = null;
        try
        {
            dec = crypter.decryptNoIv(text.toString());
           // d("encrypting complete");
        }
        catch (Exception e)
        {
            //msgl("Exception occured while decrypting data: " + e.getMessage());
            de(e);
            return new DataPattern("Password | Hash wrong [ID:2]=> " + e.getMessage(), null);
        }
        ArrayList<GroupItems> groupItemses = null;
        try
        {
            groupItemses = fromJson(dec, con);
        }
        catch (JsonSyntaxException e)
        {
            return new DataPattern(e.getMessage(), null);
        }
        if (groupItemses != null)
        {
            //msg(ins.getString(R.string.success));
            //d("from json complete");
        }
        return new DataPattern("", groupItemses);
    }

    private static ArrayList<GroupItems> fromJson(String txt,Activity con) throws JsonSyntaxException
    {

        Type listType = new TypeToken<ArrayList<GroupItems>>() {}.getType();
        Gson gson = new Gson();
      //  d("dec json: "+txt);
        ArrayList<GroupItems> p = null;
        try
        {
            p = gson.fromJson(txt, listType);
        }
        catch (JsonSyntaxException e)
        {
           // msgl("Could not load data, incorrect format: " + e.getMessage());
            de(e);
            throw new JsonSyntaxException("Format [ID:3]=> " + e.getMessage());
        }
        return p;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        openedActivities = new ArrayList<>();
        ins = this;
        if (instance != null)
        {
            msg("App already running");
        }
        else
        {

            instance = getApplicationContext();
            msg(getResources().getText(R.string.app_name).toString() + " " + getResources().getText(R.string.started));
        }

        sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key_sheryv), Context.MODE_PRIVATE);
        PackageInfo pInfo = null;
        try
        {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            d(e.getMessage());
        }

        try
        {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        }
        catch (Exception e)
        {
            Sh.msg("Error: Could not find directory path");
        }
    }


    public static void autoLock(Context con)
    {
        // locked = true;
        Sh.msg(con.getResources().getText(R.string.locked).toString() + "!");
        openedActivities.clear();
    }

    public static void lock(Activity a)
    {
        if(a instanceof PActivity)
        {
            PActivity ac = (PActivity) a;
            ac.SaveItems();
        }
        openedActivities.clear();
        PActivity.clearData();
        locked = true;
        Intent intent = new Intent(a, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        a.startActivity(intent);
        Sh.msgl(a.getResources().getText(R.string.locked).toString());

    }

    public static void unlock(Context con)
    {
        locked = false;
    }

    public static void activStarted(Context con)
    {
//        msg("Activ "+ started +" | now: " + activs+1);
        if (!started)
        {
            started = true;
            activs = 0;
        }
        else if (locked)
        {
            Intent intent = new Intent(con, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            con.startActivity(intent);
            activs = 0;
        }
        activs++;

    }

    public static void activStopped(Context con)
    {
        activs--;
//        msg("Activ stopped: "+activs);
        if (activs <= 0 && started)
        {
            autoLock(con);
        }
    }

    /*    public static String getShp()
        {
            String s = null;
            try
            {
                s= new String(ins.pass, "UTF-8");
            } catch (UnsupportedEncodingException e)
            {
                d(e.getMessage());
            }
            return s;
        }*/
    public static byte[] getShpb()
    {
        return ins.pass;
    }

    public static void setShpb(String p)
    {
        try
        {
            ins.pass = ApiCrypter.hashSha256(p.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            d(e.getMessage());
        }
    }

    public String printd(byte[] s)
    {
        String string = "byte[]: ";
        for (int i = 0; i < s.length; i++)
        {
            string += s[i] + ", ";
        }
        return string;
    }

    public static void d(String s, String method)
    {
        Log.d(method, s);
    }
    static  long times = 0;
       static Calendar cal;
    public static void d(String s)
    {
        if(times == 0)
            times = System.currentTimeMillis();

        if (cal == null)
            cal = Calendar.getInstance();
        long tt = System.currentTimeMillis() -times;
/*        String m = String.valueOf(cal.get(Calendar.MINUTE));
        String c = String.valueOf(cal.get(Calendar.SECOND));
        String l = String.valueOf(cal.get(Calendar.MILLISECOND));*/
       // String b = String.valueOf(System.currentTimeMillis() / 1000D);
        //b = b.substring(b.length()-7,b.length()-1);
        double b = System.currentTimeMillis() / 1000D;
        Log.w("= " + System.currentTimeMillis() + " | " + tt + " =>", s);
        times = System.currentTimeMillis();
    }

    public static void de(Exception s)
    {
        Log.w(" =====Exception=====>  ", s.getMessage() + " " + s.getStackTrace()[0].toString());
    }

    public static void msg(String t)
    {
        Toast.makeText(instance, t,
                Toast.LENGTH_SHORT).show();
        d(t, "=---- MESSAGE ----=");
    }

    public static void msgl(String t)
    {
        Toast.makeText(instance, t,
                Toast.LENGTH_LONG).show();
        d(t, "=---- MESSAGE_L ----=");
    }

    public static void msgd(String t)
    {
        Toast.makeText(instance, t,
                Toast.LENGTH_SHORT).show();
        d(t, "=---- MSG_D ----=");
    }

    public static String getTime()
    {
        Calendar calendar = Calendar.getInstance();
        String h = String.valueOf(calendar.get(Calendar.HOUR));
        if (h.length() == 1)
            h = "0" + h;
        String m = String.valueOf(calendar.get(Calendar.MINUTE));
        if (m.length() == 1)
            m = "0" + m;
        return h + ":" + m + " " + calendar.get(Calendar.DAY_OF_MONTH)
                + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR);
    }

    public static View getViewByPosition(int pos, ExpandableListView listView)
    {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition)
        {
            return listView.getAdapter().getView(pos, null, listView);
        }
        else
        {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public static void showDialog(String txt, int btnT,int btn2T, int titleT, boolean areBtnVisible, String error, final Activity con, final byte event, final Method m)
    {
        String btn = con.getString(btnT);
        String btn2 = con.getString(btn2T);
        String title  = con.getString(titleT);
        if(!(con instanceof ICanDialog))
        {
            Sh.msgd("showDialog() is used from wrong class");
            return;
        }
        final ICanDialog activity = (ICanDialog) con;
        activity.setDialog(new Dialog(con));
        activity.getDialog().setContentView(R.layout.dialog_info);
        activity.getDialog().setTitle(title);
        TextView t = (TextView) activity.getDialog().findViewById(R.id.d_info_text);
        TextView er = (TextView) activity.getDialog().findViewById(R.id.d_info_tapclose);
        t.setText(txt+"\n\n"+con.getString(R.string.d_info_hide));
        er.setText(con.getString(R.string.error_details)+error);

        Button okButton = (Button) activity.getDialog().findViewById(R.id.d_info_btncon);
        Button cancelButton = (Button) activity.getDialog().findViewById(R.id.d_info_btn);
        if (areBtnVisible)
        {
            okButton    .setText(btn);
            cancelButton.setText(btn2);
            okButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    if (m != null)
                    {
                        try
                        {
                            m.invoke(this);
                        }
                        catch (IllegalAccessException e)
                        {
                            e.printStackTrace();
                        }
                        catch (InvocationTargetException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    switch (event)
                    {
                        case eventReadFailed:
                            activity.showLoadingBar(con, false);
                            openListActivFromDialog(con);
                            break;
                        case eventSaveFailed:

                            break;
                    }
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    activity.showLoadingBar(con, false);
                    activity.getDialog().cancel();
                }
            });
        }
        else
        {
            okButton    .setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        }
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                activity.showLoadingBar(con, false);
                activity.getDialog().cancel();
            }
        });


        activity.getDialog().show();
    }

    private static void openListActivFromDialog(Activity con)
    {
        SharedPreferences.Editor editor = Sh.getPref().edit();
        editor.putBoolean(con.getString(R.string.pref_setup_done), false);
        editor.commit();

        Intent intent = new Intent(con, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //Sh.unlock(con);
        // String json = Sh.toJson(groupItems);
        //  Sh.fromJson(json);
        //  intent.putExtra("starter", new ArrayList<GroupItems>());
        con.startActivity(intent);
    }

    public static boolean setMenuHadlers(int id, Activity activity)
    {
        switch (id)
        {

            case R.id.exit:
                Sh.exitClick(activity);
                return true;
            case R.id.settings:
                Sh.mAboutClick(activity);
                return true;
            case R.id.mLock:
                Sh.lock(activity);
               // Sh.activStarted(activity);
                return true;
            default:
                return false;
        }
    }



}
