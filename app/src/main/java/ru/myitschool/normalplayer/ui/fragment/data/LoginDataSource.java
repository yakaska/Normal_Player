package ru.myitschool.normalplayer.ui.fragment.data;

import java.io.IOException;

import retrofit2.Response;
import ru.myitschool.normalplayer.api.vk.model.response.VkTokenResponse;
import ru.myitschool.normalplayer.api.vk.token.VkTokenService;
import ru.myitschool.normalplayer.ui.fragment.data.model.LoggedInUser;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            Response<VkTokenResponse> tokenResponse = VkTokenService.getInstance().getVkTokenApi().getToken(username, password).execute();
            if (tokenResponse.body() != null) {
                if (tokenResponse.body().getToken() != null) {
                    return new Result.Success<>(new LoggedInUser(tokenResponse.body().getToken(), "fuck my ass"));
                } else {
                    throw new Exception("Error");
                }
            } else {
                throw new Exception("Token error");
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}