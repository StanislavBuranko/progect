package com.askerweb.autoclickerreplay.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.ktExt.UtilsApp;

import java.util.Objects;


public class FaqFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.faq, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean all_permission = UtilsApp.checkAllPermission(getContext());
        if(!all_permission){
            NavHostFragment.findNavController(this).navigate(R.id.to_permission_and_accessibility);
        }
    }
}
