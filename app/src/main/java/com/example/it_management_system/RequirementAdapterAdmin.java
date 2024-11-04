package com.example.it_management_system;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RequirementAdapterAdmin extends ArrayAdapter<RequirementModel> {
    public RequirementAdapterAdmin(Context context, List<RequirementModel> requirements) {
        super(context, 0, requirements);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RequirementModel requirement = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView itemTextView = convertView.findViewById(android.R.id.text1);
        TextView detailsTextView = convertView.findViewById(android.R.id.text2);

        itemTextView.setText(requirement.getItem());
        detailsTextView.setText("Count: " + requirement.getItemCount() + "\nLocation: " + requirement.getLocation() +
                "\nFrom: " + requirement.getFromDate() + " To: " + requirement.getToDate());

        return convertView;
    }

}
