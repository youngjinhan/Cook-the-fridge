package org.pytorch.demo.objectdetection;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private ArrayList<Recipe> arrayList;
    private Context context;

    public CustomAdapter(ArrayList<Recipe> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Glide.with(holder.itemView)
                .load(arrayList.get(position).getPicture())
                .into(holder.iv_picture);
        holder.tv_name.setText(arrayList.get(position).getName());
        holder.tv_ingredient.setText(arrayList.get(position).getIngredient());
        holder.tv_link.setText(arrayList.get(position).getLink());
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0 );
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_picture;
        TextView tv_name;
        TextView tv_ingredient;
        TextView tv_link;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iv_picture = itemView.findViewById(R.id.iv_picture);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_ingredient = itemView.findViewById(R.id.tv_ingredient);
            this.tv_link = itemView.findViewById(R.id.tv_link);

            itemView.setClickable(true);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        Intent intent = new Intent(context, WebActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("LINK", arrayList.get(pos).getLink());
                        context.startActivity(intent);

                    }
                }
            });

        }
    }
}
