package com.assistant.ui.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.assistant.App;
import com.assistant.R;
import com.assistant.ui.fragment.AlarmFragment;
import com.assistant.ui.fragment.MyFragmentPageAdapter;
import com.assistant.ui.fragment.NoteFragment;
import com.assistant.utils.ConstUtils;
import com.assistant.utils.PasswordUtils;
import com.assistant.utils.ThemeUtil;
import com.assistant.utils.ToolbarUtils;
import com.orhanobut.logger.Logger;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tab_layout)
    TabLayout tabLayout;
    @Bind(R.id.view_pager)
    ViewPager viewPager;
    @Bind(R.id.nav_view)
    NavigationView navView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        // 调用父类方法设置TabLayout主题颜色
        initTabLayout(tabLayout);
        initDrawer();
        initHeaderLayout();
        initTabWithViewPager();

    }

    /**
     * 初始化TabLayout和ViewPager
     */
    private void initTabWithViewPager() {
        MyFragmentPageAdapter pageAdapter = new MyFragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * 初始化抽屉栏
     */
    private void initDrawer() {
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        navView.setNavigationItemSelectedListener(this);
    }

    /**
     * 初始化抽屉栏里面的HeaderLayout，并绑定里面的控件
     */
    private void initHeaderLayout() {
        View drawView = navView.inflateHeaderView(R.layout.nav_header_home);
        TextView tvUserName = (TextView) drawView.findViewById(R.id.tv_userName);
        if (user != null) {
            tvUserName.setText(user.getUserName());
        }
    }

    @Override
    protected void initToolbar() {
        ToolbarUtils.initToolbar(toolbar, this);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_home;
    }


    private void openOrCloseDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * 接收更新主题的事件
     *
     * @param event
     */
    public void onEventMainThread(MainEvent event) {
        switch (event) {
            case CHANGE_THEME:
                // 重新初始化界面
                this.recreate();
                break;
            case ENTER_PRIVATE_NOTE:
                // 进入加密笔记页面
                enterPrivateActivity();
                //this.finish();
                break;
        }
    }

    public enum MainEvent {
        CHANGE_THEME,
        ENTER_PRIVATE_NOTE
    }


/*******************************************华丽的分割线*********************************************/


    /**
     * 当按下返回键的时候的操作
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (AlarmFragment.isMenuOn) {
                Logger.d("isMenuOn = " + AlarmFragment.isMenuOn);
                EventBus.getDefault().post(AlarmFragment.AlarmEvent.CLOSE_MENU);
            } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                // 将任务移至后台
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 当界面完全初始化后执行此函数
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
        if (toolbar != null) {
            // 点击导航菜单，打开或关闭抽屉
            toolbar.setNavigationOnClickListener(v -> openOrCloseDrawer());
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        ComponentName componentName = getComponentName();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        searchView.setQueryHint(getString(R.string.search_note));
        // 搜索监听器
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                EventBus.getDefault().post(newText);
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                EventBus.getDefault().post(NoteFragment.NoteEvent.HIDE_FAB);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                EventBus.getDefault().post(NoteFragment.NoteEvent.DISPLAY_FAB);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                // 进入设置页面
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                break;
            case R.id.nav_private_note:
                // 进入私人笔记页面
                PasswordUtils passwordUtils = new PasswordUtils(this);
                passwordUtils.checkSetting();
                break;
            case R.id.nav_change_Theme:
                ThemeUtil.showThemeChooseDialog(this);
                break;
            case R.id.nav_setting:
                // 进入设置页面
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_exit:
                finish();
                Intent exit = new Intent(this, LoginActivity.class);
                boolean isAutoLogin = preferenceUtils.getBooleanParam(ConstUtils.AUTO_LOGIN, false);
                if (isAutoLogin) {
                    preferenceUtils.saveParam(ConstUtils.AUTO_LOGIN, false);
                }
                App.setUserState(ConstUtils.UNLOGIN);
                startActivity(exit);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 进入到加密笔记的页面
     */
    private void enterPrivateActivity() {
        Intent intent = new Intent(this, PrivateNoteActivity.class);
        App.setNoteType(ConstUtils.PRIVATE_NOTE);
        startActivity(intent);
        finish();
    }

}
