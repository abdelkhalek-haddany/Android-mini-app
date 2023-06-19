package com.example.haddany.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.haddany.R;
import com.example.haddany.model.Activity;

import java.util.List;

public class AdapterActivity extends RecyclerView.Adapter<AdapterActivity.HolderActivity>{

    private Context context;
    private List<Activity> activites;

    public AdapterActivity(Context context, List<Activity> activites) {
        this.context = context;
        this.activites = activites;
    }

    @NonNull
    @Override
    public HolderActivity onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_row, parent,false);

        return new HolderActivity(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderActivity holder, int position) {
        Activity activity = activites.get(position);

        String type = activity.getTypeActivity();
        String date = activity.getDate();
        double longitude = activity.getLongitude();
        double latitude = activity.getLatitude();
        double speed = activity.getSpeed();

        holder.typeActivity.setText(type);
        holder.date.setText(date);
        holder.latitude.setText(String.valueOf(latitude));
        holder.longitude.setText(String.valueOf(longitude));
        holder.speed.setText(speed+" m/s");
    }

    @Override
    public int getItemCount() {
        return activites.size();
    }

    class HolderActivity extends RecyclerView.ViewHolder{

        TextView typeActivity,date,latitude,longitude,speed;

        public HolderActivity(@NonNull View itemView){
            super(itemView);

            typeActivity = itemView.findViewById(R.id.type);
            date = itemView.findViewById(R.id.date);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            speed = itemView.findViewById(R.id.speed);
        }

    }
}
