/*
 * Copyright 2018.  Mihaela Madalina Mladin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package website.madalina.bakingapp.util;

import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RecyclerViewMarginDecoration extends RecyclerView.ItemDecoration {
    public static final int ORIENTATION_VERTICAL = 1;
    private final int columns;
    private int margin;
    private int orientation;

    public RecyclerViewMarginDecoration(int orientation,
                                        @IntRange(from = 0) int margin,
                                        @IntRange(from = 0) int columns) {
        this.margin = margin;
        this.columns = columns;
        this.orientation = orientation;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view);
        if (orientation == ORIENTATION_VERTICAL) {
            outRect.bottom = margin;
            outRect.right = margin;
            if (position < columns) {
                outRect.top = margin;
            }
            if (position % columns == 0) {
                outRect.left = margin;
            }
        }
    }
}