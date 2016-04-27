/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Popdeem
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.popdeem.sdk.uikit.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.popdeem.sdk.R;
import com.popdeem.sdk.uikit.activity.PDUIInboxActivity;
import com.popdeem.sdk.uikit.adapter.PDUIHomeFlowPagerAdapter;
import com.popdeem.sdk.uikit.utils.PDUIColorUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class PDUIHomeFlowFragment extends Fragment {

    public PDUIHomeFlowFragment() {
    }

    public static PDUIHomeFlowFragment newInstance() {
        return new PDUIHomeFlowFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pd_home_flow, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.pd_home_inbox_fab);
        fab.setImageDrawable(PDUIColorUtils.getInboxButtonIcon(getActivity()));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PDUIInboxActivity.class));
            }
        });

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pd_home_view_pager);
        viewPager.setAdapter(new PDUIHomeFlowPagerAdapter(getChildFragmentManager(), getActivity()));

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.pd_home_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

}
