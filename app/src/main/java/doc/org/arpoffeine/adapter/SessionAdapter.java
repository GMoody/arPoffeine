/*
 * Copyright (C) 2017 Grigory Tureev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package doc.org.arpoffeine.adapter;

import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import doc.org.arpoffeine.domain.Session;
import doc.org.arpoffeine.util.ItemTouchHelperAdapter;
import doc.org.arpoffeine.view.SessionItem;
import doc.org.arpoffeine.view.SessionItem_;

/**
 * Class which acts as an adapter for sessions and also deals with the filtration
 */

@EBean(scope = EBean.Scope.Singleton)
public class SessionAdapter extends RecyclerViewAdapterBase<Session, SessionItem> implements Filterable, ItemTouchHelperAdapter {

    @Override
    protected SessionItem onCreateItemView(ViewGroup parent, int viewType) {
        return SessionItem_.build(parent.getContext());
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                String query = charSequence.toString().toLowerCase();
                if (query.isEmpty())
                    filterResults.values = getMItemsToStore();
                else {
                    List<Session> filteredList = new ArrayList<>();
                    for (Session session : getMItemsToStore())
                        if (session.getMUrl().toLowerCase().contains(query) ||
                            session.getMName().toLowerCase().contains(query))
                            filteredList.add(session);
                    filterResults.values = filteredList;
                }
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                setMItemsToShow((List<Session>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onItemDismiss(int position) {
        if (getMItemsToShow().size() != getMItemsToStore().size())
            removeItem(getMItemsToShow().get(position));
        else removeItem(position);
    }
}
