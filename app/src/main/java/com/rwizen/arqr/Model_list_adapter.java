package com.rwizen.arqr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Model_list_adapter extends RecyclerView.Adapter<Model_list_adapter.Model_View_Holder>
{

    public RecyclerViewClickListner mlistner;

    public String[] Data;
    public Model_list_adapter(String[] data)
    {
        this.Data = data;
    }

    @Override
    public Model_View_Holder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.model_view,parent,false);
        return new Model_View_Holder(view,mlistner);
    }

    @Override
    public void onBindViewHolder(@NonNull Model_View_Holder holder, int position)
    {
          String title = Data[position];
          holder.txtTitle.setText(title);
    }

    @Override
    public int getItemCount() {

        return Data.length;
    }

    public  class Model_View_Holder extends RecyclerView.ViewHolder implements View.OnClickListener
    {   TextView txtTitle;

        public Model_View_Holder(@NonNull View itemView , View.OnClickListener listener) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }
            );
        }

        @Override
        public void onClick(View v) {
            mlistner.onClick(itemView,getAdapterPosition());

        }
    }

    public interface RecyclerViewClickListner
    {
        void onClick(View v , int position);
    }

    public void setOnItemClickListner(RecyclerViewClickListner listner)
    {
        mlistner=listner;
    }
}
