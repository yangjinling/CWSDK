package cwbj.cwsdk2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cwbj.cwsdk2.R;

/**
 * Created by c&w on 2017/8/15.
 */

public class MyAdapter extends BaseAdapter {
    private List<String> lstDevices;
    private Context mContext;
    private LayoutInflater mInflate;

    public MyAdapter(Context context, List<String> lstDevices) {
        this.mContext = context;
        this.lstDevices = lstDevices;
        this.mInflate = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lstDevices .size()==0 ? 0 : lstDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return lstDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (null == view) {
            viewHolder = new ViewHolder();
            view = mInflate.inflate(R.layout.listview_item, null, false);
            viewHolder.name= ((TextView) view.findViewById(R.id.name));
            viewHolder.address = ((TextView) view.findViewById(R.id.address));
            viewHolder.state = ((TextView) view.findViewById(R.id.state));
            view.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) view.getTag());
        }
        String[] bean = lstDevices.get(i).split("\\|");
        if (null != bean) {
            Log.e("YJL","name=="+bean[1]+"-----addresss==="+bean[2]+"----state==="+bean[0]);
            viewHolder.name.setText(bean[1]);
            viewHolder.address.setText(bean[2]);
            viewHolder.state.setText(bean[0]);
        }
        return view;
    }

    class ViewHolder {
        TextView name;
        TextView address;
        TextView state;
    }
}
