package ru.myitschool.normalplayer.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.myitschool.normalplayer.api.vk.VkService;
import ru.myitschool.normalplayer.api.vk.model.response.VkTokenResponse;
import ru.myitschool.normalplayer.databinding.FragmentVkAuthBinding;


public class VkAuthFragment extends DialogFragment {

    private static final String TAG = VkAuthFragment.class.getSimpleName();

    private FragmentVkAuthBinding binding;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VkService.getInstance().getVkApi().getToken(binding.phone.getEditText().getText().toString(), binding.password.getEditText().getText().toString()).enqueue(new Callback<VkTokenResponse>() {
                    @Override
                    public void onResponse(Call<VkTokenResponse> call, Response<VkTokenResponse> response) {
                        String token = response.body().getToken();
                        Log.d(TAG, "onResponse: " + token);
                    }

                    @Override
                    public void onFailure(Call<VkTokenResponse> call, Throwable t) {
                        Toast.makeText(requireContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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