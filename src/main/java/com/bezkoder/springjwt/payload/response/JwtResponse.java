package com.bezkoder.springjwt.payload.response;

import java.util.List;
import com.bezkoder.springjwt.models.EUserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String lastName;
    private String email;
    private List<String> roles;
    private EUserType userType;

    public JwtResponse(String accessToken, Long id, String name, String lastName, String email, List<String> roles, EUserType userType) {
        this.token = accessToken;
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
        this.userType = userType;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }


}
