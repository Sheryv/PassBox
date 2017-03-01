package com.sheryv.PassBox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Sheryv on 15.09.2015.
 */
public class PActivity extends ActionBarActivity implements ICanDialog
{
    public static final int RESULT_DELETE = 2000;
    public static final int REQUEST_DEBT = 2;
    public static final int REQUEST_ADD = 3;
    public ExLoginsAdapter adapter;
    public ExpandableListView list;
    public static PActivity instance;
    public static boolean isSelecting = false;
    public static boolean isAllSelected = false;
    public int nowExpanded = -1;
    public boolean isExpanded;
    private boolean isDoneDeleting = false;
    private static boolean sortInBack = false;
    private int pos;
    private Dialog dialog;
    private Dialog dialogFailed;
    private String profile;
    private Dialog dialog2;
    private int posChild;
    private boolean saveDisabled = false;
    private TextView statusT;
    private ArrayList<GroupItems> groupItems;
    private ArrayList<GroupItems> selectedItems;
    int f = 1;

    public SwipeDetector getSwipeDetector()
    {
        return swipeDetector;
    }

    public void setSwipeDetector(SwipeDetector swipeDetector)
    {
        this.swipeDetector = swipeDetector;
    }

    SwipeDetector swipeDetector;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playout);
        saveDisabled = false;
        Sh.addActiv(this);
        instance = this;
        boolean loaded = false;
        groupItems = new ArrayList<>();
        if (savedInstanceState != null)
        {
            isSelecting = savedInstanceState.getBoolean("select");
            Serializable saved = savedInstanceState.getSerializable("data");
            if ((groupItems = (ArrayList<GroupItems>) saved) != null)
            {
                loaded = true;

            }
            else
            {
                groupItems = new ArrayList<>();

                Sh.msg("Error while loading data after change (rotation). Restart!");
/*                if(list != null && adapter != null)
                {
                    adapter.notifyDataSetChanged();
                    Sh.msg("zaladowane juz lista");
                    return;
                }*/
            }

        }
        else
        {
            Sh.d("==========> null bundle, start");

            profile = getIntent().getStringExtra("profile");
            Serializable serializable = getIntent().getSerializableExtra("starter");
            if (serializable == null)
            {
                Sh.msg("Error while transferring data from file. Restart!");
                groupItems = new ArrayList<>();
            }
            else
            {
                groupItems = (ArrayList<GroupItems>) serializable;
                if (groupItems == null)
                {
                    Sh.msg("Error while loading data from file. Restart!");
                    groupItems = new ArrayList<>();
                }
            }
        }
        Sh.d("==========> create list " + String.valueOf(groupItems.size()));
        adapter = new ExLoginsAdapter(groupItems, this, list);
        list = (ExpandableListView) findViewById(R.id.listView);
        list.setAdapter(adapter);

        statusT = ((TextView) findViewById(R.id.text_status));
        statusT.setText("Loaded: " + adapter.getGroupCount());
        if (isSelecting)
        {
            if (nowExpanded >= 0)
                list.collapseGroup(nowExpanded);
            for (int i = 0; i < list.getCount(); i++)
            {
                View v=list.getChildAt(i);
                if ( v!= null)
                {
                    CheckBox cb = (CheckBox) v.findViewById(R.id.g_selectedCb);
                    if (adapter.getGroupItem(i) != null)
                    {
                        Sh.d("selec> "+i+" > "+adapter.getGroupItem(i).isSelected);
                        cb.setChecked(adapter.getGroupItem(i).isSelected);
                    }

                }
            }
        }

        list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener()
        {
            @Override
            public void onGroupExpand(int groupPosition)
            {
                //setButtonsVisibility(groupPosition, true);

                // Sh.d("onGroupExpand: " + groupPosition);
                for (int i = 0; i < adapter.getGroupCount(); i++)
                {
                    if (i != groupPosition)
                        list.collapseGroup(i);
                }
                nowExpanded = groupPosition;
            }
        });
        list.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener()
        {
            @Override
            public void onGroupCollapse(int groupPosition)
            {

                nowExpanded = -1;
                // setButtonsVisibility(groupPosition, false);
                // Sh.d("onGroupCollapse: " + groupPosition);

            }
        });
        list.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int group, int child, long l)
            {
                onListClick(adapter.getGroupItem(group), group, child);
                return false;
            }
        });
/*        CheckBox cb;
        for (int i = 0; i < list.getChildCount(); i++)
        {
            pos = i;
            cb = (CheckBox) list.getChildAt(i).findViewById(R.id.g_selectedCb);
            Sh.d("added listener "+i+cb);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                {
                    GroupItems gi = adapter.getGroupItem(pos);
                    gi.isSelected = b;
                    adapter.setGroup(gi, pos);
                    Sh.d("changed: "+b);
                }
            });
        }*/
/*        list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener()
        {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)
            {
                d("onGroupClick: " + groupPosition);
                nowExpanded = groupPosition;
                // parent.expandGroup(groupPosition);
                return false;
            }
        });*/

/*        setSwipeDetector(new SwipeDetector());
        list.setOnTouchListener(swipeDetector);
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3)
            {
                if (getSwipeDetector().swipeDetected())
                {
                    if (getSwipeDetector().getAction() == Action.RL)
                    {
                        Sh.d("left");
                    }
                    else
                    {
                        Sh.d("right");
                    }
                }
            }
        };
        list.setOnItemClickListener(listener);*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private static Comparator<GroupItems> ALPHABETICAL_ORDER = new Comparator<GroupItems>()
    {
        public int compare(GroupItems str1, GroupItems str2)
        {
            int res = 0;
            if (sortInBack)
                res = String.CASE_INSENSITIVE_ORDER.compare(str2.name, str1.name);
            else
                res = String.CASE_INSENSITIVE_ORDER.compare(str1.name, str2.name);
            if (res == 0)
            {
                res = str1.name.compareTo(str2.name);
            }
            return res;
        }
    };

    private static ArrayList<GroupItems> sortFavo(ArrayList<GroupItems> items)
    {
        int i = 0, j = items.size() - 1;
        while (i < j)
        {
            while (items.get(i).favourite) i++;
            while (!items.get(j).favourite) j--;
            if (j > i)
            {
                GroupItems t = items.get(i);
                items.set(i, items.get(j));
                items.set(j, t);
            }
            i++;
            j--;
        }
        return items;
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
                case R.id.create_new:
                    newGroup(this);
                    return true;
                case R.id.save:
                    SaveItems();
                    return true;
                case R.id.sort_alfa:
                    groupItems = adapter.getList();
                    Collections.sort(groupItems, ALPHABETICAL_ORDER);
                    adapter.setList(groupItems);
                    sortInBack = !sortInBack;
                    return true;
                case R.id.sort_favo:
                    groupItems = adapter.getList();
                    sortFavo(groupItems);
                    adapter.setList(groupItems);
                case R.id.delete_menu:
                    toolDelete(item.getActionView());
                    return true;
                case R.id.fill:

                    ArrayList<Items> itemses = new ArrayList<>();
                    itemses.add(new Items("Login", "Example password", "01:00 01/01/2000", "Description"));
                    adapter.addToList(new GroupItems("Example group", itemses, 0, false, 1, "google.com"));

                    return true;
                default:
                    super.onOptionsItemSelected(item);
            }
            return false;
        }
    }

    private void newGroup(Context context)
    {
        dialog2 = new Dialog(context);
        dialog2.setContentView(R.layout.dialog_edit);
        dialog2.setTitle(getResources().getText(R.string.add));

        final EditText name = (EditText) dialog2.findViewById(R.id.d_name);
        final EditText web = (EditText) dialog2.findViewById(R.id.d_web);

        Button editButton = (Button) dialog2.findViewById(R.id.d_btnEdit);
        Button delButton = (Button) dialog2.findViewById(R.id.d_btnDelete);
        delButton.setVisibility(View.GONE);
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
                    GroupItems groupItems = new GroupItems(name.getText().toString(), new ArrayList<Items>(), 0, false, 1, web.getText().toString());
                    adapter.addToList(groupItems);
                    dialog2.dismiss();
                }
            }
        });
        dialog2.show();
    }


    public void onListClick(GroupItems it, int pos, int child)
    {

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("dane", it);
        intent.putExtra("danepos", pos);
        intent.putExtra("danechild", child);
        saveDisabled = true;
        startActivityForResult(intent, REQUEST_DEBT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (requestCode == REQUEST_DEBT)
        {
            if (resultCode == RESULT_OK)
            {
                GroupItems groupItems = (GroupItems) data.getSerializableExtra("result");
                int poss = data.getIntExtra("positiongroup", -1);
                int posChild = data.getIntExtra("positionchild", -1);
                if (groupItems == null || poss == -1 || posChild == -1)
                {
                    Sh.msg("Error while loading data");
                    return;
                }
                else
                {
                    adapter.set(((Items)adapter.getChild(poss ,posChild )), pos, posChild);
                    Sh.msg(getResources().getText(R.string.success).toString());
                }
            }
            else if (resultCode == RESULT_DELETE)
            {
                int poss = data.getIntExtra("position", -1);
                posChild = data.getIntExtra("positionc", -1);
                if (poss == -1 || posChild == -1)
                {
                    Sh.msg("Error while loading data");
                    return;
                }
                adapter.removeChild(poss, posChild);
            }
        }
        else if (requestCode == REQUEST_ADD)
        {
            if (resultCode == RESULT_OK)
            {
                Items items = (Items) data.getSerializableExtra("resultadd");
                int gr = data.getIntExtra("resultgroup", -1);
                if (items == null)
                {
                    Sh.msg("Error while adding data");
                    return;
                }
                adapter.addChild(items, gr);
            }
        }
    }

    public static void changeSelected(GroupItems items, boolean add, View pos)
    {
        View par = (View) pos.getParent();
        if (add)
            par.setBackgroundColor(par.getResources().getColor(R.color.selected));
        else
            par.setBackgroundColor(par.getResources().getColor(R.color.transpar));

        if (instance.selectedItems == null)
            instance.selectedItems = new ArrayList<>();
        if (add)
            instance.selectedItems.add(items);
        else
            instance.selectedItems.remove(items);
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Sh.autoLock(this);
    }

    public void SaveItems()
    {
        if (!saveDisabled)
        {
            new SaveDataAsync().execute(new DataPattern(null, groupItems, Sh.saveTypePActivity));
        }
        saveDisabled = false;
    }

    @Override
    protected void onStop()
    {
        Sh.d("onStop");
        super.onStop();
        Sh.activStopped(this);
        SaveItems();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Sh.activStarted(this);
    }
/*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Sh.d("landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Sh.d("portrait");
        }
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        saveDisabled = true;
        super.onSaveInstanceState(outState);
        outState.putSerializable("data", groupItems);
        outState.putBoolean("select", isSelecting);
    }

    void setDoneDeleting(boolean b)
    {
        isDoneDeleting = b;
    }

    public void editGroupClick(View view)
    {
        dialog = new Dialog(view.getContext());
        dialog.setContentView(R.layout.dialog_edit);
        dialog.setTitle(getResources().getText(R.string.group_edit));

        // set the custom dialog components - text, image and button
        final EditText name = (EditText) dialog.findViewById(R.id.d_name);
        final EditText web = (EditText) dialog.findViewById(R.id.d_web);

        Button editButton = (Button) dialog.findViewById(R.id.d_btnEdit);
        Button delButton = (Button) dialog.findViewById(R.id.d_btnDelete);
        name.setText(adapter.getGroupItem(nowExpanded).name);
        web.setText(adapter.getGroupItem(nowExpanded).website);
        // if button is clicked, close the custom dialog
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
                    editGroup(name.getText().toString(), web.getText().toString());
                    dialog.dismiss();
                }
            }
        });
        delButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean done = false;
                AlertDialog.Builder builder1 = new AlertDialog.Builder(v.getContext());
                builder1.setMessage(getResources().getText(R.string.delete_conf));
                builder1.setCancelable(true);
                builder1.setPositiveButton(getResources().getText(R.string.remove),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                delGroup();
//                                setDoneDeleting(true);
                                dialog.dismiss();
                            }
                        });
                builder1.setNegativeButton(getResources().getText(R.string.no),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
//                                setDoneDeleting(false);
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
                dialog.dismiss();

            }
        });

        dialog.show();
    }

    static void setButtonsVisibility(GroupItems groupItem, int groupPosition, boolean expanded, ExpandableListView list, View v)
    {
        View view;
        if (v == null)
            view = Sh.getViewByPosition(groupPosition, list);
        else
            view = v;
        ImageButton g_add = (ImageButton) view.findViewById(R.id.g_add);
        ImageButton g_edit = (ImageButton) view.findViewById(R.id.g_edit);
        ImageButton g_link = (ImageButton) view.findViewById(R.id.g_link);
        ImageView g_line = (ImageView) view.findViewById(R.id.g_line);
        CheckBox g_sel = (CheckBox) view.findViewById(R.id.g_selectedCb);
        ImageButton g_favourite = (ImageButton) view.findViewById(R.id.g_favourite);
        TextView g_count = (TextView) view.findViewById(R.id.g_count);
        if (g_favourite == null || g_add == null || g_edit == null || g_link == null || g_count == null)
        {
            Sh.d("null at " + groupPosition + " | " + view.getId());
        }
        else
        {

            if (expanded)
            {
                g_count.setVisibility(View.INVISIBLE);
                g_favourite.setVisibility(View.INVISIBLE);
                g_add.setVisibility(View.VISIBLE);
                g_edit.setVisibility(View.VISIBLE);
                g_link.setVisibility(View.VISIBLE);
                g_line.setVisibility(View.VISIBLE);
                g_sel.setVisibility(View.GONE);
            }
            else
            {
                if (isSelecting)
                {
                  //  Sh.d("selectedT: "+groupItem.isSelected);
                    updateSelected(groupItem, view, true);
                    g_sel.setVisibility(View.VISIBLE);
                    g_count.setVisibility(View.INVISIBLE);
                    g_favourite.setVisibility(View.INVISIBLE);
                }
                else
                {
                  //  Sh.d("selectedF: "+groupItem.isSelected);
                    updateSelected(groupItem, view, false);
                    g_sel.setVisibility(View.GONE);
                    g_count.setVisibility(View.VISIBLE);
                    g_favourite.setVisibility(View.VISIBLE);
                }
                g_add.setVisibility(View.INVISIBLE);
                g_edit.setVisibility(View.INVISIBLE);
                g_link.setVisibility(View.GONE);
                g_line.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void updateSelected( GroupItems item, View view, boolean enable)
    {
        if (!enable)
            view.setBackgroundColor(view.getResources().getColor(R.color.transpar));
        else if (item.isSelected)
            view.setBackgroundColor(view.getResources().getColor(R.color.selected));
        else
        {
            view.setBackgroundColor(view.getResources().getColor(R.color.transpar));

        }
//        CheckBox checkBox =((CheckBox) view.findViewById(R.id.g_selectedCb));
//        if (checkBox.isChecked() != item.isSelected)
//            checkBox.setChecked(item.isSelected);
        //  Sh.msgd("In list: " + instance.selectedItems.size());

    }

    private void reset()
    {
        if (nowExpanded >= 0)
            list.collapseGroup(nowExpanded);
        for (int i = 0; i < list.getCount(); i++)
        {
            CheckBox cb = (CheckBox) list.getChildAt(i).findViewById(R.id.g_selectedCb);
            if (cb != null)
                cb.setChecked(false);
        }
    }

    public void addChildItemClick(View view)
    {
        Intent intent = new Intent(this, AddActivity.class);
        intent.putExtra("group", getGroupPosByChildView(view));
        startActivityForResult(intent, REQUEST_ADD);
    }

    public void setFavouriteClick(View view)
    {
/*
        String hash = "$P$B1qoLcd7Jv5CmzHLvqRf4/WN3TNh8A1";
        String p = "pass";
        String h = phPass.HashPassword(p);
        Sh.d("origin: "+hash);
        Sh.d("hashed: " + h);
        Sh.d("same: " + phPass.CheckPassword(p, hash));

        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hh = messageDigest.digest(p.getBytes("UTF-8"));
            Sh.d(">>>>>>>>>>>>   | "+Base64.encodeToString(hh, Base64.NO_PADDING));
            String e = Base64.encodeToString(hh, Base64.NO_PADDING).substring(0,16);
            Sh.d(">>>>>>>>>>>>  "+e.length()+" | "+e);

        } catch (NoSuchAlgorithmException e)
        {
            Sh.d(e.getMessage());
        } catch (UnsupportedEncodingException e)
        {
            Sh.d(e.getMessage());
        }*/
        int pos = getGroupPosByChildView(view);
        GroupItems gi = adapter.getGroupItem(pos);
        gi.favourite = !gi.favourite;
        adapter.setGroup(gi, pos);

    }

    public static int getGroupPosByChildView(View child)
    {
        View rel = ((View) child.getParent()); //item group // relativelay
        for (int i = 0; i < instance.adapter.getGroupCount(); i++)
        {
            if (rel == Sh.getViewByPosition(i, instance.list))
            {
                return i;
            }
        }
        return 0;
    }

    public void openBrowser(View view)
    {
        try
        {
            String url = adapter.getGroupItem(nowExpanded).website;
            String u = url;
            if (url.startsWith("http://") || url.startsWith("https://"))
            {
                if (url.startsWith("http://"))
                    url = url.substring(8);
                else
                    url = url.substring(9);
            }
            url = "http:" + url;
            Sh.msg(getResources().getText(R.string.opening) + " " + u);
            Uri uri = Uri.parse(url);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(browserIntent);
        }
        catch (Exception e)
        {
            Sh.d(e.getMessage());
            Sh.msg(getResources().getText(R.string.wrong_url).toString());
        }
    }

    private void editGroup(String g, String web)
    {
        if (nowExpanded >= 0)
        {
            GroupItems gi = adapter.getGroupItem(nowExpanded);
            gi.name = g;
            gi.website = web;
            adapter.setGroup(gi, nowExpanded);
        }
    }

    private void delGroup()
    {
        if (nowExpanded >= 0)
        {
            Sh.msg(getResources().getText(R.string.success).toString());
            adapter.removeGroup(nowExpanded);
        }
    }

    public static void clearData()
    {
        if (instance != null)
        {
            instance.adapter.clear();
            instance.groupItems.clear();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (dialog != null)
            dialog.dismiss();
        if (dialog2 != null)
            dialog2.dismiss();
        if (dialogFailed != null)
            dialogFailed.dismiss();
    }


    public void dialogC(String msg, String error)
    {
        Sh.showDialog(msg, R.string.got, R.string.d_try_again, R.string.d_info, false, error, this, PassCryptApp.eventSaveFailed, null);
    }

    @Override
    public void showLoadingBar(Activity activity, boolean show)
    {
        if (show)
        {
            findViewById(R.id.text_status_saving).setVisibility(View.VISIBLE);
            findViewById(R.id.progress_saving).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.text_status_saving).setVisibility(View.GONE);
            findViewById(R.id.progress_saving).setVisibility(View.GONE);
        }
    }

    @Override
    public Dialog getDialog() {return this.dialogFailed;}

    @Override
    public void setDialog(Dialog d)
    {
        this.dialogFailed = d;
    }

    public void toolUp(View view)
    {
        if (nowExpanded < 1 || nowExpanded < 0)
            return;
       // Sh.msgd("Up");
        groupItems = adapter.getList();
        GroupItems t = groupItems.get(nowExpanded - 1);
        groupItems.set(nowExpanded - 1, groupItems.get(nowExpanded));
        groupItems.set(nowExpanded, t);
        adapter.setList(groupItems);
        list.expandGroup(nowExpanded - 1);
    }

    public void toolDown(View view)
    {
        groupItems = adapter.getList();
        if (nowExpanded > groupItems.size() - 2 || nowExpanded < 0)
            return;
      //  Sh.msgd("Down");
        GroupItems t = groupItems.get(nowExpanded + 1);
        groupItems.set(nowExpanded + 1, groupItems.get(nowExpanded));
        groupItems.set(nowExpanded, t);
        adapter.setList(groupItems);
        list.expandGroup(nowExpanded + 1);
    }

    public void toolSelect(View view)
    {

        if (isSelecting)
        {
            isSelecting = false;
        }
        else
        {
            isSelecting = true;
        }

        if (nowExpanded >= 0)
            list.collapseGroup(nowExpanded);

        groupItems = adapter.getList();

        for (int i = 0; i < groupItems.size(); i++)
        {
            setButtonsVisibility(groupItems.get(i), i, false, list, null);
        }/*        int pos = getGroupPosByChildView(view);
        GroupItems gi = adapter.getGroupItem(pos);
        gi.favourite = !gi.favourite;
        adapter.setGroup(gi, pos);*/
    }




    public void toolSelectAll(View view)
    {
        if (!isSelecting)
            return;
        if (nowExpanded >= 0)
            list.collapseGroup(nowExpanded);
        for (int i = 0; i < list.getCount(); i++)
        {
            CheckBox cb = (CheckBox) list.getChildAt(i).findViewById(R.id.g_selectedCb);
            if (cb != null)
            cb.setChecked(!isAllSelected);
        }
/*        if (isAllSelected || !isSelecting)
        {
            for (int i = 0; i < adapter.getGroupCount(); i++)
            {
                adapter.getGroupItem(i).isSelected = false;
                list.getChildAt(i).setBackgroundColor(view.getResources().getColor(R.color.transpar));
            }
            if ( !isSelecting)
            {
                isAllSelected = false;
                return;
            }
        }
        else
        {
            for (int i = 0; i < adapter.getGroupCount(); i++)
            {
                adapter.getGroupItem(i).isSelected = true;
                list.getChildAt(i).setBackgroundColor(view.getResources().getColor(R.color.selected));
            }
        }
        adapter.notifyDataSetChanged();
        */
        isAllSelected = !isAllSelected;
        statusT.setText("All: " + isAllSelected);
    }

    public void toolDelete(View view)
    {
        if (!isSelecting)
            return;
        for (int i = 0; i < adapter.getGroupCount(); i++)
        {
            if (adapter.getGroupItem(i).isSelected)
                adapter.removeGroup(i);
        }
        Sh.msg(getResources().getText(R.string.success).toString());
        reset();
    }

    public void toolLock(View view) {
        Sh.lock(this);
    }


    public static enum Action
    {
        LR, // Left to Right
        RL, // Right to Left
        TB, // Top to bottom
        BT, // Bottom to Top
        None // when no action was detected
    }

    class SwipeDetector implements View.OnTouchListener
    {


        private static final int MIN_DISTANCE = 100;
        private float downX, downY, upX, upY;
        private Action mSwipeDetected = Action.None;

        public boolean swipeDetected()
        {
            return mSwipeDetected != Action.None;
        }

        public Action getAction()
        {
            return mSwipeDetected;
        }

        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    downX = event.getX();
                    downY = event.getY();
                    mSwipeDetected = Action.None;
                    return false; // allow other events like Click to be processed
                }
                case MotionEvent.ACTION_MOVE:
                {
                    upX = event.getX();
                    upY = event.getY();

                    float deltaX = downX - upX;
                    float deltaY = downY - upY;
                    if (Math.abs(deltaY) > MIN_DISTANCE)
                    {
                        // top or down
/*                        if (deltaY < 0) {
                            Sh.d(this.getClass().getName() + " || sc : del=" + deltaY);
                            mSwipeDetected = Action.TB;
                            return false;
                        }
                        if (deltaY > 0) {
                            Sh.d(this.getClass().getName() + " || sc : del=" + deltaY);
                            mSwipeDetected = Action.BT;
                            return false;
                        }*/
                    }
                    else if (Math.abs(deltaX) > MIN_DISTANCE)
                    {
                        // left or right
                        if (deltaX < 0)
                        {
                            Sh.d(this.getClass().getName() + " || swipe right : del=" + deltaX);
                            mSwipeDetected = Action.LR;
                            return true;
                        }
                        if (deltaX > 0)
                        {
                            Sh.d(this.getClass().getName() + " || swipe left : del=" + deltaX);
                            mSwipeDetected = Action.RL;
                            return true;
                        }
                    }
                    return true;

                    // vertical swipe detection
                }
            }
            return false;
        }
    }


}
