package com.sheryv.PassCrypt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class LoginsAdapter extends ArrayAdapter<Items>
{

   // private List<Items> objects = new ArrayList<Items>();

    private Context context;
    private LayoutInflater layoutInflater;

    private int res;


    public LoginsAdapter(Context context, int resource)
    {
        super(context, resource);
        this.res = resource;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void fill(Items[] item)
    {
        clear();
        for (int i = 0; i < item.length; i++)
        {
            add(item[i]);
        }
        if (isEmpty())
            notifyDataSetInvalidated();
        else
            notifyDataSetChanged();
    }

    public void addToList(Items item)
    {
        add(item);
        if (isEmpty())
            notifyDataSetInvalidated();
        else
            notifyDataSetChanged();
    }

    public void removeFromList(Items it)
    {
        remove(it);
        if (isEmpty())
            notifyDataSetInvalidated();
        else
            notifyDataSetChanged();
    }

/*    @Override
    public long getItemId(int position)
    {
        return position;
    }*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
            ViewHolder viewHolder;
        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(res, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView2);
            viewHolder.login = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.desc = (TextView) convertView.findViewById(R.id.textView4);
            viewHolder.date = (TextView) convertView.findViewById(R.id.textView3);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initializeViews(getItem(position), viewHolder);
        return convertView;
    }

    private void initializeViews(Items object, ViewHolder holder)
    {
       // holder.name.setText(object.name);
        holder.login.setText(object.login);
        holder.date.setText(object.date);
        holder.desc.setText(object.desc);

    }
    public void set(Items items, int pos)
    {
        remove(getItem(pos));
        insert(items, pos);
/*                Items i = getItem(pos);
        i.name = items.name;
        i.login = items.login;
        i.passSetup = items.passSetup;
        i.desc = items.desc;
        i.date = items.date;
        i.modifDate = items.modifDate;*/

    }
    protected class ViewHolder
    {
       public TextView name;
       public TextView login;
       public TextView date;
       public TextView desc;
    }
}
