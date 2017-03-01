package com.sheryv.PassBox;

import android.os.AsyncTask;

import java.util.ArrayList;

/**
 * Created by Sheryv on 16.10.2015.
 */
public class SaveDataAsync extends AsyncTask<DataPattern, String, SaveDataAsync.DataResultSaveProfile>
{
    @Override
    protected final DataResultSaveProfile doInBackground(DataPattern... activities)
    {
        ArrayList<GroupItems> items = activities[0].g;
        String d = Sh.saveFile(items);
        if (d != null && d.equals("s"))
        {
            //  success
            // publishProgress(d);
            return new DataResultSaveProfile(null, activities[0].type);
        }
        else
            return new DataResultSaveProfile(d, activities[0].type);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        if (PActivity.instance != null)
            PActivity.instance.showLoadingBar(PActivity.instance, true);
    }

    @Override
    protected void onProgressUpdate(String... values)
    {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(DataResultSaveProfile res)
    {
        super.onPostExecute(res);
        if (res.type == Sh.saveTypePActivity)
            PActivity.instance.showLoadingBar(PActivity.instance, false);

        if (res.error == null) // success
        {
            if (res.type == Sh.saveTypePActivity)
                Sh.msg(Sh.ins.getString(R.string.data_saved));
            else if (res.type == Sh.saveTypeStartActivity)
                StartActivity.updateProfileList();
        }
        else
        {
            if (PActivity.instance != null)
                PActivity.instance.dialogC(Sh.ins.getString(R.string.data_save_failed), res.error);
            Sh.msg(Sh.ins.getString(R.string.data_save_fail_short));
        }
    }

    public class DataResultSaveProfile
    {
        public String error;
        public byte type;

        public DataResultSaveProfile(String error, byte type)
        {
            this.error = error;
            this.type = type;
        }
    }

}

