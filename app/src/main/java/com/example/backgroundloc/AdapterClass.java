package com.example.backgroundloc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterClass extends RecyclerView.Adapter<com.example.backgroundloc.AdapterClass.MyViewHolder> {

    private OnClickItemListener mListener;
    public interface OnClickItemListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnClickItemListener listener) {
        mListener = listener;
    }

    ArrayList<String> list;
    public AdapterClass(ArrayList<String> list){
        this.list = list;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_holder, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.disease.setText(list.get(position));
        //holder.medicine.setText(list.get(position).getMedication());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView disease, medicine;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            disease = itemView.findViewById(R.id.disease);
            //medicine =  itemView.findViewById(R.id.medication);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

}
