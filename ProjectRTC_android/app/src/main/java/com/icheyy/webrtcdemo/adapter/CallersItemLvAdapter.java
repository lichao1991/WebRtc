package com.icheyy.webrtcdemo.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icheyy.webrtcdemo.R;
import com.icheyy.webrtcdemo.bean.Caller;

import java.util.List;

public class CallersItemLvAdapter extends BaseAdapter {

    private Activity mActivity;
    private List<Caller> mContentList;

    public CallersItemLvAdapter(Activity activity, List<Caller> contentList) {
        this.mActivity = activity;
        this.mContentList = contentList;
    }

    @Override
    public int getCount() {
        return mContentList == null ? 0 : mContentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mContentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CallerItemHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_callers_list, null);
            holder = new CallerItemHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (CallerItemHolder) convertView.getTag();
        }

        Caller caller = mContentList.get(position);
        holder.tv_caller_name.setText(caller.getName());
        if (caller.getStatus()) {
            holder.tv_caller_status.setText("在线");
            holder.tv_caller_status.setTextColor(0xff7cfc00);

        } else {

            holder.tv_caller_status.setText("正在通话");
            holder.tv_caller_status.setTextColor(0xffff0000);
        }

        return convertView;
    }


    class CallerItemHolder {
        private TextView tv_caller_name;
        private TextView tv_caller_status;

        public CallerItemHolder(View itemView) {
            tv_caller_name = (TextView) itemView.findViewById(R.id.tv_caller_name);
            tv_caller_status = (TextView) itemView.findViewById(R.id.tv_caller_status);
        }
    }
}
