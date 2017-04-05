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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Base adapter class that deals with processing elements and binding views
 */

@SuppressWarnings("WeakerAccess")
abstract class RecyclerViewAdapterBase<T, V extends View & Binder<T>> extends RecyclerView.Adapter<ViewWrapper<T, V>> {

    @Getter @Setter
    private List<T> mItemsToShow = Collections.synchronizedList(new ArrayList<T>());
    @Getter @Setter
    private List<T> mItemsToStore = Collections.synchronizedList(new ArrayList<T>());

    protected abstract V onCreateItemView(ViewGroup parent, int viewType);

    @Override
    public int getItemCount() {
        return mItemsToShow.size();
    }

    @Override
    public final ViewWrapper<T, V> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewWrapper<>(onCreateItemView(parent, viewType));
    }

    @Override
    public void onBindViewHolder(ViewWrapper<T, V> viewHolder, int position) {
        V view = viewHolder.getMView();
        T data = getItem(position);
        view.bind(data, position);
    }

    public T getItem(int position) {
        return mItemsToShow.get(position);
    }

    public void addItem(T t, int position) {
        mItemsToShow.add(position, t);
        mItemsToStore.add(position, t);
        notifyItemInserted(position);
    }

    public void addItem(T item) {
        mItemsToShow.add(item);
        mItemsToStore.add(item);
        notifyItemInserted(mItemsToShow.size() - 1);
    }

    public void removeItem(T item) {
        mItemsToShow.remove(item);
        mItemsToStore.remove(item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mItemsToShow.remove(position);
        if (mItemsToShow.size() < mItemsToStore.size())
            mItemsToStore.remove(position);
        notifyDataSetChanged();
    }

    public void clear() {
        int size = getItemCount();
        mItemsToShow.clear();
        mItemsToStore.clear();
        notifyItemRangeRemoved(0, size);
    }
}
