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

/**
 * Class that deals with wrapping views
 */

class ViewWrapper<T, V extends View & Binder<T>> extends RecyclerView.ViewHolder {

    private V mView;

    ViewWrapper(V itemView) {
        super(itemView);
        mView = itemView;
    }

    V getMView() {
        return mView;
    }
}
