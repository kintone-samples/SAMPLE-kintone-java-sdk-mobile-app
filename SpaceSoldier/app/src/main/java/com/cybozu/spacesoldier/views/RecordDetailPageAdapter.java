package com.cybozu.spacesoldier.views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cybozu.spacesoldier.AppCommon;

import java.util.ArrayList;
import java.util.List;

public class RecordDetailPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList = new ArrayList<>();

    public RecordDetailPageAdapter(FragmentManager fm, AppCommon app, Integer recordId) {
        super(fm);

        RecordDetailDataFragment dataFagment = new RecordDetailDataFragment();
        dataFagment.setApp(app);
        dataFagment.setRecordId(recordId);
        RecordDetailCommentFragment commentFagment = new RecordDetailCommentFragment();
        this.fragmentList.add(dataFagment);
        commentFagment.setApp(app);
        commentFagment.setRecordId(recordId);
        this.fragmentList.add(commentFagment);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

}
