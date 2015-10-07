package com.sheryv.PassCrypt;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Sheryv on 20.09.2015.
 */
public class ExLoginsAdapter extends BaseExpandableListAdapter
{
    private Activity activity;
    private LayoutInflater inflater;
   // private ArrayList<Items> childItems;
   ExpandableListView list;
   private ArrayList<GroupItems> groupItems;

    public ExLoginsAdapter(ArrayList<GroupItems> groupItems, Activity activity, ExpandableListView list) {
        this.groupItems = groupItems;
        this.list = list;
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
    }

    @Override
    public View getChildView( int groupPosition,  int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

      //  ViewHolder viewHolder = null;
            GroupItems group = groupItems.get(groupPosition);
            Items item = group.items.get(childPosition);
        ViewHolder.ItemView view = null;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }
            view = new ViewHolder().new ItemView();
            view.login = (TextView) convertView.findViewById(R.id.textView2);
            view.desc = (TextView) convertView.findViewById(R.id.textView4);
            view.date = (TextView) convertView.findViewById(R.id.textView3);
 /*            convertView.setTag(view);
       }
        else
        {
            view = (ViewHolder.ItemView) convertView.getTag();
        }*/
        view.login.setText(item.login);
        view.desc.setText(item.desc);
        view.date.setText(item.date);


        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

          ViewHolder viewHolder = null;
        GroupItems group = groupItems.get(groupPosition);
       // Log.d("group", "this " + group.name + " | " + group.items.size());

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.g_name);
            viewHolder.website = (TextView) convertView.findViewById(R.id.g_website);
            viewHolder.count = (TextView) convertView.findViewById(R.id.g_count);
            viewHolder.number = (TextView) convertView.findViewById(R.id.g_number);
            viewHolder.favourite = (ImageButton) convertView.findViewById(R.id.g_favourite);
            viewHolder.add = (ImageButton) convertView.findViewById(R.id.g_add);
            viewHolder.link = (ImageButton) convertView.findViewById(R.id.g_link);
            viewHolder.edit = (ImageButton) convertView.findViewById(R.id.g_edit);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        PActivity.setButtonsVisibility(groupPosition, isExpanded, list, convertView);
        viewHolder.edit.setFocusable(false);
        viewHolder.add.setFocusable(false);
        viewHolder.link.setFocusable(false);
        viewHolder.favourite.setFocusable(false);
/*        viewHolder.edit.setVisibility(View.INVISIBLE);
        viewHolder.add.setVisibility(View.INVISIBLE);
        viewHolder.link.setVisibility(View.INVISIBLE);
        viewHolder.favourite.setVisibility(View.VISIBLE);*/
        group.isExpanded = isExpanded;
        viewHolder.name.setText(group.name);
        Bitmap bm;
        if (group.favourite)
        {
            bm = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.ic_favourite_on);
        }
        else
        {
            bm = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.ic_favourite_off);
        }
        viewHolder.favourite.setImageBitmap(bm);
        viewHolder.website.setText(group.website);
        //nie$ viewHolder.website.setMovementMethod(LinkMovementMethod.getInstance());
        String s= "";
        if (group.items.size() > 0)
            s = String.valueOf(group.items.size());
        viewHolder.count.setText(s);
        s=(groupPosition-1)+".";
        if(groupPosition < 9)
            s=" "+s;
        viewHolder.number.setText(s);

        return convertView;
    }

    public void addToList(GroupItems item)
    {
        groupItems.add(item);
        update();
    }
    public void fill(ArrayList<GroupItems> items)
    {
        groupItems.addAll(items);
        update();
    }
    public void clear()
    {
        groupItems.clear();
        update();
    }
    public void addChild(Items item, int group)
    {
        groupItems.get(group).items.add(item);
        update();
    }
    public void set(Items item, int pos, int posChild)
    {
        groupItems.get(pos).items.set(posChild, item);
        update();
    }
    public void setGroup(GroupItems item, int pos)
    {
        groupItems.set(pos, item);
        update();
    }
    public void removeGroup( int pos)
    {
        groupItems.remove(pos);
        update();
    }
    public void removeChild( int pos, int posChild)
    {
        groupItems.get(pos).items.remove(posChild);
        update();
    }
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groupItems.get(groupPosition).items.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*10000+childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groupItems.get(groupPosition).items.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupItems.get(groupPosition);
    }

    public GroupItems getGroupItem(int groupPosition)
    {
        return groupItems.get(groupPosition);
    }
    @Override
    public int getGroupCount() {
        return groupItems.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void update()
    {
        if (isEmpty())
            notifyDataSetInvalidated();
        else
            notifyDataSetChanged();
    }
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    protected class ViewHolder
    {
        public TextView name;
        public TextView website;
        public TextView count;
        public TextView number;
        public ImageButton favourite;
        public ImageButton link;
        public ImageButton add;
        public ImageButton edit;
        public ArrayList<ItemView> views;
        protected class ItemView
        {
        public TextView login;
        public TextView date;
        public TextView desc;

        }
    }

}
