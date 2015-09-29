package com.sheryv.PassCrypt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sheryv on 15.09.2015.
 */
public class PActivity extends ActionBarActivity
{
    public static final int RESULT_DELETE = 2000;
    public static final int REQUEST_DEBT = 2;
    public static final int REQUEST_ADD = 3;
    public  ExLoginsAdapter adapter;
    public  ExpandableListView list;
    public  static PActivity instance;
    public int nowExpanded =-1;
    public boolean isExpanded;
    private boolean isDoneDeleting = false;
    private int pos    ;
    private Dialog dialog;
    private Dialog dialog2;
    private int posChild;
    private TextView statusT;
    private  ArrayList<GroupItems> groupItems;
    int f = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playout);
        Sh.addActiv(this);
        instance = this;
        boolean loaded = false;
        groupItems = new ArrayList<>();
        if(savedInstanceState != null )
        {
            Serializable saved =  savedInstanceState.getSerializable("data");
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

        statusT=((TextView)findViewById(R.id.text_status));
        statusT.setText("Loaded: " + adapter.getGroupCount());


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
        list.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int group, int child, long l)
            {
                onListClick(adapter.getGroupItem(group), group, child);
                try
                {
                    Sh.msg("child: " + adapter.getGroupItem(group).name + " | " + adapter.getGroupItem(
                            group).items.get(child).login);
                } catch (Exception e)
                {
                    Sh.d(e.getMessage());
                }
                return false;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
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
                case R.id.create_new:
                    newGroup(this);
                    return true;
                case R.id.save:
                    Sh.saveFile(groupItems);
                    return true;
                case R.id.fill:
                    for (int i =0; i<10;i++)
                    {
                        ArrayList<Items> itemses = new ArrayList<>();
                        itemses.add(new Items("Login 1:"+i, "Example password", "01:00 01/01/2000", "Description"));
                        itemses.add(new Items("Login 2:"+i, "Example password 2", "01:00 01/01/2000", "Description 2"));
                        adapter.addToList(new GroupItems("Example group "+i, itemses, 0, false, 1,"google.com"));
                    }
                    return true;
                default:
                super.onOptionsItemSelected(item);
            }
            return false;
        }
    }

    private void newGroup(Context context)
    {
        dialog2= new Dialog(context);
        dialog2.setContentView(R.layout.dialog_edit);
        dialog2.setTitle(getResources().getText(R.string.add));

        final EditText name = (EditText) dialog2.findViewById(R.id.d_name);
        final EditText web = (EditText) dialog2.findViewById(R.id.d_web);

        Button editButton = (Button) dialog2.findViewById(R.id.d_btnEdit);
        Button delButton = (Button) dialog2.findViewById(R.id.d_btnDelete);
        delButton.setVisibility(View.GONE);
        editButton.setText(getResources().getText(R.string.accept).toString());
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().isEmpty())
                {
                    Sh.msg(getResources().getText(R.string.no_null_pass).toString());
                }
                else
                {
                    GroupItems groupItems = new GroupItems(name.getText().toString(),new ArrayList<Items>(), 0, false, 1, web.getText().toString());
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
        startActivityForResult(intent, REQUEST_DEBT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_DEBT) {
            if(resultCode == RESULT_OK){
                GroupItems groupItems= (GroupItems) data.getSerializableExtra("result");
                int pos = data.getIntExtra("positiongroup", -1);
                int posChild = data.getIntExtra("positionchild", -1);
                if (groupItems == null || pos == -1 || posChild == -1)
                {
                    Sh.msg("Error while loading data");
                    return;
                }
                else
                {
                    adapter.set(groupItems.items.get(posChild), pos, posChild);
                    Sh.msg(getResources().getText(R.string.success).toString());
                }
            }
            else if (resultCode == RESULT_DELETE) {
                pos       = data.getIntExtra("position", -1);
                posChild = data.getIntExtra("positionc", -1);
                if (pos == -1 || posChild ==-1)
                {
                    Sh.msg("Error while loading data");
                    return;
                }
                adapter.removeChild(pos, posChild);
            }
        }
        else if(requestCode == REQUEST_ADD)
        {
            if(resultCode == RESULT_OK)
            {
                Items items= (Items) data.getSerializableExtra("resultadd");
                int gr= data.getIntExtra("resultgroup",-1);
                if (items == null || pos == -1)
                {
                    Sh.msg("Error while loading data");
                    return;
                }
                adapter.addChild(items, gr);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Sh.autoLock(this);
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

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable("data", groupItems);
    }

    void setDoneDeleting(boolean b)
    {
        isDoneDeleting = b;
    }
    public void editGroupClick( View view)
    {
        dialog= new Dialog(view.getContext());
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
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    static void setButtonsVisibility(int groupPosition, boolean expanded, ExpandableListView list, View v)
    {
        View view;
        if (v == null)
            view = Sh.getViewByPosition(groupPosition, list);
        else
            view = v;
        ImageButton g_favourite = (ImageButton) view.findViewById(R.id.g_favourite);
        ImageButton g_add       = (ImageButton) view.findViewById(R.id.g_add);
        ImageButton g_edit      = (ImageButton) view.findViewById(R.id.g_edit);
        ImageButton g_link      = (ImageButton) view.findViewById(R.id.g_link);
        ImageView g_line      = (ImageView) view.findViewById(R.id.g_line);
        TextView g_count = (TextView) view.findViewById(R.id.g_count);
        if (g_favourite == null || g_add == null || g_edit == null || g_link == null || g_count == null )
        {
            Sh.msg("null at " + groupPosition + " | " + view.getId());
        }
        else
        {
            if (expanded)
            {
                g_count.setVisibility(View.INVISIBLE);
                g_favourite.setVisibility(View.INVISIBLE);
                g_add      .setVisibility(View.VISIBLE);
                g_edit     .setVisibility(View.VISIBLE);
                g_link     .setVisibility(View.VISIBLE);
                g_line     .setVisibility(View.VISIBLE);
            }
            else
            {
                g_count.setVisibility(View.VISIBLE);
                g_favourite.setVisibility(View.VISIBLE);
                g_add      .setVisibility(View.INVISIBLE);
                g_edit     .setVisibility(View.INVISIBLE);
                g_link     .setVisibility(View.GONE);
                g_line     .setVisibility(View.INVISIBLE);
            }
        }
    }
    public void addChildItemClick(View view)
    {
        Intent intent= new Intent(this, AddActivity.class);
        intent.putExtra("group", nowExpanded);
        startActivityForResult(intent, REQUEST_ADD);
    }

    public void setFavouriteClick(View view)
    {
/*
        String hash = "$P$B1qoLcd7Jv5CmzHLvqRf4/WN3TNh8A1";
        String p = "22ford22";
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

    private int getGroupPosByChildView(View child)
    {
        View rel = ((View) child.getParent()); //item group // relativelay
        for (int i = 0; i < adapter.getGroupCount(); i++)
        {
            if (rel == Sh.getViewByPosition(i, list))
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
            String url=adapter.getGroupItem(nowExpanded).website;
            String u = url;
            if (url.startsWith("http://") || url.startsWith("https://"))
            {
                if (url.startsWith("http://"))
                    url = url.substring(8);
                else
                    url = url.substring(9);
            }
            url = "http:"+url;
            Sh.msg(getResources().getText(R.string.opening) + " " + u);
            Uri uri=Uri.parse(url);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(browserIntent);
        } catch (Exception e)
        {
            Sh.d(e.getMessage());
            Sh.msg(getResources().getText(R.string.wrong_url).toString());
        }
    }
    private void editGroup(String g, String web )
    {
        if(nowExpanded >= 0)
        {
            GroupItems gi = adapter.getGroupItem(nowExpanded);
            gi.name = g;
            gi.website = web;
            adapter.setGroup(gi, nowExpanded);
        }
    }
    private void delGroup()
    {
        if(nowExpanded >= 0)
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
    }

    private void delChild()
    {
        adapter.removeChild(pos, posChild);
    }

}
