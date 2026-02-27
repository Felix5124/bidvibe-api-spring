package com.bidvibe.bidvibeapispring.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho PUT /api/users/profile.
 * Chỉ cho phép cập nhật các trường cho phép; email và role không thay đổi ở đây.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 50, message = "Nickname phải từ 2 đến 50 ký tự")
    private String nickname;

    @Size(max = 512, message = "URL avatar không được quá 512 ký tự")
    private String avatarUrl;

    @Pattern(regexp = "^(\\+?[0-9]{9,15})?$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Size(max = 500, message = "Địa chỉ không được quá 500 ký tự")
    private String address;
}
