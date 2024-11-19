package c8y.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import com.cumulocity.rest.representation.ResourceRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginMode implements ResourceRepresentation {
    private String userManagementSource;
    private String type;
    private String id;
    @Builder.Default
    private String  providerName = "Cumulocity";
    private String visibleOnLoginPage;
    private String grantType;
    
}
