package com.ly.common.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * 创建过的fragment都缓存到内存的PagerAdapter
 * 适用于tab少的情况
 *
 * @author ly
 * @date 2018/3/27 10:34
 */
public class TabFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<? extends Fragment> mList;

    public TabFragmentPagerAdapter(FragmentManager fm, List<? extends Fragment> list) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mList = list;
    }

    public void setList(List<Fragment> mlist) {
        this.mList = mlist;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int arg0) {
        return mList.get(arg0);//显示第几个页面
    }

    @Override
    public int getCount() {
        return mList.size();//有几个页面
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup vg, int position) {
        return super.instantiateItem(vg, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
