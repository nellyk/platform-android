/*
 * Copyright (c) 2014 Ushahidi.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program in the file LICENSE-AGPL. If not, see
 * https://www.gnu.org/licenses/agpl-3.0.html
 */

package com.ushahidi.android.ui.activity;


import com.squareup.otto.Subscribe;
import com.ushahidi.android.R;
import com.ushahidi.android.model.DeploymentModel;
import com.ushahidi.android.model.PostModel;
import com.ushahidi.android.model.UserModel;
import com.ushahidi.android.module.PostUiModule;
import com.ushahidi.android.presenter.ActivateDeploymentPresenter;
import com.ushahidi.android.presenter.DeploymentNavPresenter;
import com.ushahidi.android.presenter.MainPresenter;
import com.ushahidi.android.state.ApplicationState;
import com.ushahidi.android.state.IDeploymentState;
import com.ushahidi.android.state.IUserState;
import com.ushahidi.android.ui.fragment.ListPostFragment;
import com.ushahidi.android.ui.prefs.Prefs;
import com.ushahidi.android.ui.widget.NavDrawerItem;
import com.ushahidi.android.ui.widget.SlidingTabLayout;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

/**
 * Post Activity
 *
 * @author Ushahidi Team <team@ushahidi.com>
 */
public class PostActivity extends BaseActivity implements NavDrawerItem.NavDrawerItemListener,
        NavDrawerItem.NavDeploymentItemListener,
        MainPresenter.View,
        DeploymentNavPresenter.View,
        ListPostFragment.PostListListener,
        ActivateDeploymentPresenter.View  {

    private static final String SELECTED_TAB = "selected_tab";

    @Inject
    ActivateDeploymentPresenter mActivateDeploymentPresenter;

    @Inject
    Prefs mPrefs;

    @Inject
    DeploymentNavPresenter mDeploymentNavPresenter;

    @Inject
    MainPresenter mMainPresenter;

    private SlidingTabLayout mSlidingTabStrip;

    private ViewPager mViewPager;

    private TabPagerAdapter mAdapter;

    private int mCurrentItem;

    private List<String> mTabTitle;

    private SearchView mSearchView = null;

    private String mQuery = "";

    private ListPostFragment mListPostFragment;

    public PostActivity() {
        super(R.layout.activity_post, R.menu.post, R.id.drawer_layout, R.id.navdrawer_items_list);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainPresenter.setView(this);
        mActivateDeploymentPresenter.setView(this);
        mDeploymentNavPresenter.setView(this);
        mTabTitle = new ArrayList<>();
        mTabTitle.add(getString(R.string.list));
        mTabTitle.add(getString(R.string.map));

        mSlidingTabStrip = (SlidingTabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mAdapter = new TabPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(
                getResources().getDimensionPixelSize(R.dimen.element_spacing_normal));

        mSlidingTabStrip.setViewPager(mViewPager);
        mSlidingTabStrip.setTabListener(new SlidingTabLayout.TabListener() {
            @Override
            public void onTabSelected(int pos) {
                // NO-OP
            }

            @Override
            public void onTabReSelected(int pos) {
                mAdapter.getItem(pos);

            }
        });

        if (savedInstanceState != null) {
            mCurrentItem = savedInstanceState.getInt(SELECTED_TAB);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleSearchIntent(intent);
    }

    private void handleSearchIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            query = query == null ? "" : query;
            mQuery = query;
            performQuery(query);
            if (mSearchView != null) {
                mSearchView.setQuery(query, false);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        mSearchView =
                (SearchView) menu.findItem(R.id.menu_search_post).getActionView();
        initSearchView();
        return true;
    }

    private void initSearchView() {
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mSearchView.clearFocus();
                performQuery(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    refresh();
                } else {
                    performQuery(s);
                }

                return true;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //Reload list upon closing search view.
                return false;
            }
        });

        if (!TextUtils.isEmpty(mQuery)) {
            mSearchView.setQuery(mQuery, false);
        }
        SearchView.SearchAutoComplete searchAutoComplete
                = (SearchView.SearchAutoComplete) mSearchView
                .findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(getResources().getColor(android.R.color.white));
        searchAutoComplete.setTextSize(14);
    }

    private void performQuery(String query) {
        if(mListPostFragment != null) {
            mListPostFragment.search(query);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivateDeploymentPresenter.resume();
        mDeploymentNavPresenter.resume();
        mMainPresenter.resume();
        mSlidingTabStrip.getBackground().setAlpha(255);
    }

    @Override
    public void onPause() {
        mMainPresenter.onPause();
        super.onPause();
        mCurrentItem = mViewPager.getCurrentItem();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_TAB, mCurrentItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected List<Object> getModules() {
        List<Object> modules = new LinkedList<>();
        modules.add(new PostUiModule());
        return modules;
    }

    @Override
    protected void initNavDrawerItems() {

        mNavDrawerItemViews
                .add(setNavDrawerItem(R.string.manage_deployments, R.drawable.ic_action_map, 1,
                        DeploymentActivity.getIntent(this)));

        mNavDrawerItemViews
                .add(setNavDrawerItem(R.string.settings, R.drawable.ic_action_settings, 2,
                        SettingsActivity.getIntent(this)));

        mNavDrawerItemViews.add(setNavDrawerItem(R.string.about, R.drawable.ic_action_info, 3,
                AboutActivity.getIntent(this)));

        mNavDrawerItemViews
                .add(setNavDrawerItem(R.string.send_feedback, R.drawable.ic_action_help, 4,
                        SendFeedbackActivity.getIntent(this)));
        setFragments();

    }

    private void setFragments() {

        List<Fragment> fragments = new ArrayList<>();

        if(mListPostFragment == null) {
            mListPostFragment = new ListPostFragment();
        }

        fragments.add(mListPostFragment);
        fragments.add(createTabFragments(PostTab.MAP));

        mAdapter.setFragments(fragments);
        mSlidingTabStrip.notifyDataSetChanged();
        mViewPager.setCurrentItem(mCurrentItem);
    }

    private Fragment createTabFragments(PostTab tab) {
        switch (tab) {
            case LIST:
                return new ListPostFragment();
            case MAP:
                return new ListPostFragment();
        }
        return null;
    }

    private NavDrawerItem setNavDrawerItem(int titleResId, int iconId, int position) {
        NavDrawerItem navDrawerItem = new NavDrawerItem(this, getResources().getString(titleResId),
                iconId, position);
        navDrawerItem.setOnClickListener(this);
        return navDrawerItem;
    }

    private NavDrawerItem setNavDrawerItem(int titleResId, int iconId, int position,
            Intent intent) {
        return setNavDrawerItem(getResources().getString(titleResId), iconId, position, intent);
    }

    private NavDrawerItem setNavDrawerItem(String title, int iconId, int position, Intent intent) {
        NavDrawerItem navDrawerItem = new NavDrawerItem(this, title,
                iconId, position, intent);
        navDrawerItem.setOnClickListener(this);
        return navDrawerItem;
    }

    @Override
    public void onNavDrawerItemClick(NavDrawerItem navDrawerItem) {
        final int position = navDrawerItem.getPosition();
        for (NavDrawerItem item : mNavDrawerItemViews) {
            if (position != item.getPosition()) {
                item.setSelected(false);
            }
        }
        navDrawerItem.launchActivityOrFragment();
        closeNavDrawer();
    }

    @Override
    public void onNavDeploymentItemClick(NavDrawerItem navDrawerItem) {
        List<DeploymentModel> deploymentModels = mDeploymentNavPresenter.getDeployments();
        final int position = navDrawerItem.getPosition();

        if (deploymentModels.get(position).getStatus() == DeploymentModel.Status.DEACTIVATED) {
            mActivateDeploymentPresenter.activateDeployment(deploymentModels,
                    navDrawerItem.getPosition());
        }
    }

    @Override
    public void renderDeploymentList(List<DeploymentModel> listDeploymentModel) {
        mNavDrawerItemViews.clear();
        for (int i = 0; i < listDeploymentModel.size(); i++) {
            NavDrawerItem navDrawerItem = setNavDrawerItem(listDeploymentModel.get(i).getTitle(),
                    R.drawable.ic_action_globe, i, null);
            navDrawerItem.setNavDrawerItemId(listDeploymentModel.get(i).getId());
            navDrawerItem.setStatus(listDeploymentModel.get(i).getStatus());
            navDrawerItem.setOnDeploymentClickListener(this);
            if (listDeploymentModel.get(i).getStatus() == DeploymentModel.Status.ACTIVATED) {
                navDrawerItem.setSelected(true);
            } else {
                navDrawerItem.setSelected(false);
            }
            navDrawerItem.markStatus();
            mNavDrawerItemViews.add(navDrawerItem);
        }
    }

    @Subscribe
    public void onActiveDeploymentChanged(IDeploymentState.ActivatedDeploymentChangedEvent event) {
        setActionBarTitle(mDeploymentState.getActiveDeployment().getTitle());
    }

    @Override
    public void showError(String message) {
        showToast(message);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void createNav() {
        initNavDrawerItems();
        createNavDrawer();
    }

    @Override
    public void markStatus() {
        mDeploymentNavPresenter.resume();
    }

    @Override
    public void getActiveDeployment(DeploymentModel deploymentModel) {
        mPrefs.getActiveDeploymentUrl().set(deploymentModel.getUrl());
        mDeploymentState.setActiveDeployment(deploymentModel);
    }

    @Override
    public void hideSwipeRefresh() {
        toggleSwipeRefreshing(false);
    }

    @Override
    public void enableSwipeRefresh() {
        toggleSwipeRefreshing(true);
    }

    @Override
    public void onPostClicked(final PostModel postModel) {

    }

    @Override
    public void setUserProfiles(List<UserModel> userProfiles) {
        setUpUserAccountList(userProfiles);
    }

    @Override
    public void setActiveUserProfile(UserModel userProfile) {
        setupAndShowLoginOrUserProfile(userProfile);
    }

    @Subscribe
    public void onUnthorizied(IUserState.UnauthorizedAccessEvent event) {
        launcher.launchLogin();
    }

    @Subscribe
    public void onSwipe(ApplicationState.SwipeRefreshEvent event) {
        refresh();
    }

    private void refresh() {
        if(mListPostFragment !=null) {
            mListPostFragment.refreshLists();
        }
    }

    private static enum PostTab {
        LIST, MAP
    }

    private class TabPagerAdapter extends FragmentPagerAdapter {

        private final ArrayList<Fragment> mFragments;

        private TabPagerAdapter(FragmentManager fm) {
            super(fm);

            mFragments = new ArrayList<>();
        }

        public void setFragments(List<Fragment> fragments) {
            mFragments.clear();
            mFragments.addAll(fragments);
            notifyDataSetChanged();
        }

        @Override
        public final Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public final int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitle.get(position);
        }
    }
}
