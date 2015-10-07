package com.sheryv.PassCrypt;

import android.app.Activity;
import android.app.Dialog;

public interface ICanDialog
{
    Dialog getDialog();
    void setDialog(Dialog d);
    void showLoadingBar(Activity activity, boolean show);
}