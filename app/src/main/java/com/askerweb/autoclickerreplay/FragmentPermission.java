package com.askerweb.autoclickerreplay;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.askerweb.autoclickerreplay.services.SimulateTouchAccessibilityService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentPermission extends Fragment {

    private Unbinder unbinder;

    @BindView(R.id.block_permission_overlay)
    LinearLayout block_overlay_permission;

    @BindView(R.id.block_accessibility)
    LinearLayout block_accessibility;

    @BindView(R.id.btn_get_permission_overlay)
    Button btnAllowOverlay;

    @BindView(R.id.btn_add_accessibility_service)
    Button btnAddServiceAccessibility;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission, container, false);
        unbinder = ButterKnife.bind(this, view);
        checkAllows();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(UtilsApp.checkAllPermission(getContext()))
            NavHostFragment.findNavController(this).navigate(R.id.to_faq);
        else
            checkAllows();
    }

    void checkAllows(){
        int overlay = !UtilsApp.checkPermissionOverlay(getContext()) ? View.VISIBLE : View.GONE;
        int accessibility = !UtilsApp.checkAccessibilityPermission(getContext(), SimulateTouchAccessibilityService.class) ?
                View.VISIBLE : View.GONE;
        block_overlay_permission.setVisibility(overlay);
        block_accessibility.setVisibility(accessibility);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btn_get_permission_overlay)
    void getPermissionOverlay(){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getContext().getPackageName()));
        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.btn_add_accessibility_service)
    void onClickButtonAddService(){
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
