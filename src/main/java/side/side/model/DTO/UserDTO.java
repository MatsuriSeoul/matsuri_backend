package side.side.model.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private Long userId;
    private String userName;
    private String profileImage;

    public UserDTO(Long userId, String userName, String profileImage) {
        this.userId = userId;
        this.userName = userName;
        this.profileImage = profileImage;
    }
}
