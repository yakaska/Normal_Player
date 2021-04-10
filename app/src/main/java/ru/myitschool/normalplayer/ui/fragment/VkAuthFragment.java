package ru.myitschool.normalplayer.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.myitschool.normalplayer.api.vk.model.response.VkTokenResponse;
import ru.myitschool.normalplayer.api.vk.token.VkTokenService;
import ru.myitschool.normalplayer.common.playback.MusicService;
import ru.myitschool.normalplayer.databinding.FragmentVkAuthBinding;
import ru.myitschool.normalplayer.ui.activity.MainActivity;
import ru.myitschool.normalplayer.ui.viewmodel.MainActivityViewModel;
import ru.myitschool.normalplayer.utils.ProviderUtil;
import ru.myitschool.normalplayer.utils.VkUtils;


public class VkAuthFragment extends DialogFragment {

    private static final String TAG = VkAuthFragment.class.getSimpleName();

    private FragmentVkAuthBinding binding;

    private MainActivityViewModel mainActivityViewModel;

    public VkAuthFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVkAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivityViewModel = new ViewModelProvider(getActivity(), ProviderUtil.provideMainActivityViewModel(requireActivity())).get(MainActivityViewModel.class);
        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VkTokenService.getInstance().getVkTokenApi().getToken(binding.phone.getEditText().getText().toString(), binding.password.getEditText().getText().toString()).enqueue(new Callback<VkTokenResponse>() {
                    @Override
                    public void onResponse(Call<VkTokenResponse> call, Response<VkTokenResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            VkUtils.saveToken(getContext(), response.body().getToken());
                            Log.d(TAG, "onResponse: " + response.body().getToken());
                            mainActivityViewModel.setMediaSource(MusicService.SOURCE_VK);
                            startActivity(new Intent(getContext().getApplicationContext(), MainActivity.class));
                            getDialog().cancel();
                        }
                    }

                    @Override
                    public void onFailure(Call<VkTokenResponse> call, Throwable t) {
                        Log.e(TAG, "onFailure: ", t);
                    }
                });

            }
        });
        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
    }
}