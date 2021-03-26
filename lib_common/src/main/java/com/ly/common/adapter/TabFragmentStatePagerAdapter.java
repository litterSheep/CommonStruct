package com.ly.common.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 会自动彻底销毁缓存范围之外的fragment的PagerAdapter
 * 减少内存开销
 *
 * @author ly
 * @date 2018/3/27 10:34
 */
public class TabFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private List<? extends Fragment> mList;

    public TabFragmentStatePagerAdapter(FragmentManager fm, List<? extends Fragment> list) {
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
    public Object instantiateItem(@NotNull ViewGroup vg, int position) {
        return super.instantiateItem(vg, position);
    }

    @Override
    public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getItemPosition(@NotNull Object object) {
        return POSITION_NONE;
    }
}
