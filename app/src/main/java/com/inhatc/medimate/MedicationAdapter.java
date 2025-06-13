package com.inhatc.medimate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>{
    private List<MedicationItem> medicationList;

    public MedicationAdapter(List<MedicationItem> medicationList) {
        this.medicationList = medicationList;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        MedicationItem item = medicationList.get(position);
        holder.drugName.setText(item.getDrugName());
        holder.medicationPeriod.setText("복용 기간: " + item.getMedicationPeriod());
        holder.schedules.setText(item.getSchedules());
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView drugName;
        TextView medicationPeriod;
        TextView schedules;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            drugName = itemView.findViewById(R.id.tvDrugName);
            medicationPeriod = itemView.findViewById(R.id.tvMedicationPeriod);
            schedules = itemView.findViewById(R.id.tvSchedules);
        }
    }
}
