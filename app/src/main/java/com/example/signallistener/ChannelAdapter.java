package com.example.signallistener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.VH> {

    private static final String[] ROUTES = {
            ChannelConfig.ROUTE_NONE,
            ChannelConfig.ROUTE_MT5,
            ChannelConfig.ROUTE_API
    };

    private final List<ChannelConfig> items = new ArrayList<>();

    public ChannelAdapter(List<ChannelConfig> initial) {
        items.addAll(initial);
    }

    public void refresh(List<ChannelConfig> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ChannelConfig c = items.get(position);
        h.txtName.setText(c.name);

        String when = c.lastSignalTime == 0 ? "—"
                : new SimpleDateFormat("MM-dd HH:mm", Locale.US)
                    .format(new Date(c.lastSignalTime));
        h.txtStats.setText(c.signalsReceived + " signals · last " + when);

        // Prevent listeners from firing during bind
        h.chkEnabled.setOnCheckedChangeListener(null);
        h.spinnerRoute.setOnItemSelectedListener(null);
        h.btnDelete.setOnClickListener(null);

        h.chkEnabled.setChecked(c.enabled);

        // Use custom layouts with white text (visible on dark background)
        ArrayAdapter<String> a = new ArrayAdapter<>(
                h.itemView.getContext(),
                R.layout.spinner_item, ROUTES);
        a.setDropDownViewResource(R.layout.spinner_dropdown_item);
        h.spinnerRoute.setAdapter(a);

        int idx = 0;
        for (int i = 0; i < ROUTES.length; i++)
            if (ROUTES[i].equals(c.route)) { idx = i; break; }
        h.spinnerRoute.setSelection(idx);

        h.chkEnabled.setOnCheckedChangeListener((v, checked) -> {
            c.enabled = checked;
            ChannelStore.get(v.getContext()).update(c);
        });
        h.spinnerRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (!ROUTES[pos].equals(c.route)) {
                    c.route = ROUTES[pos];
                    ChannelStore.get(v.getContext()).update(c);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
        h.btnDelete.setOnClickListener(v ->
                ChannelStore.get(v.getContext()).remove(c.name));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox chkEnabled;
        TextView txtName, txtStats;
        Spinner spinnerRoute;
        ImageButton btnDelete;
        VH(View v) {
            super(v);
            chkEnabled  = v.findViewById(R.id.chkEnabled);
            txtName     = v.findViewById(R.id.txtName);
            txtStats    = v.findViewById(R.id.txtStats);
            spinnerRoute = v.findViewById(R.id.spinnerRoute);
            btnDelete   = v.findViewById(R.id.btnDelete);
        }
    }
}
