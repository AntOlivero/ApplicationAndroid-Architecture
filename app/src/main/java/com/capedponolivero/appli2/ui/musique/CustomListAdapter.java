package com.capedponolivero.appli2.ui.musique;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.capedponolivero.appli2.R;

import java.util.List;


public class CustomListAdapter extends BaseAdapter {

    private List<ItemMusique> listeMusique;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomListAdapter(Context context, List<ItemMusique> listeMusique) {
        this.context = context;
        this.listeMusique = listeMusique;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listeMusique.size();
    }

    @Override
    public Object getItem(int position) {
        return listeMusique.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_fragment_musique, null);
            holder = new ViewHolder();
            holder.nomArtiste = (TextView) convertView.findViewById(R.id.nomArtiste1);
            holder.nomMusique = (TextView) convertView.findViewById(R.id.nomMusique1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemMusique itemMusique = this.listeMusique.get(position);
        holder.nomArtiste.setText(itemMusique.getNomArtiste());
        holder.nomMusique.setText(itemMusique.getNomMusique());

        return convertView;
    }

    static class ViewHolder {
        TextView nomArtiste;
        TextView nomMusique;
    }
}
