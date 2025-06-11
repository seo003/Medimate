package com.inhatc.medimate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

public class DatePickerFragment extends DialogFragment {

    private final Consumer<Date> onDateSelected;

    public DatePickerFragment(Consumer<Date> onDateSelected) {
        this.onDateSelected = onDateSelected;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), (view, y, m, d) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(y, m, d);
            onDateSelected.accept(selectedCal.getTime());
        }, year, month, day);
    }
}
