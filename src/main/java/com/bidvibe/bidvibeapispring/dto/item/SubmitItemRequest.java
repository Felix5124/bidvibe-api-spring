package com.bidvibe.bidvibeapispring.dto.item;

import java.util.List;

import com.bidvibe.bidvibeapispring.entity.Item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body cho POST /api/items/submit – User ký gửi đồ vật.
 */
@Getter
@Setter
@NoArgsConstructor
public class SubmitItemRequest {

    @NotBlank(message = "Tên vật phẩm không được để trống")
    @Size(max = 200, message = "Tên vật phẩm không được quá 200 ký tự")
    private String name;

    @Size(max = 2000, message = "Mô tả không được quá 2000 ký tự")
    private String description;

    /** Danh sách URL ảnh đã upload lên storage của Supabase. */
    @Size(min = 1, message = "Phải có ít nhất 1 ảnh")
    private List<String> imageUrls;

    /** Độ hiếm do người gửi đề xuất; Admin có thể thay đổi khi duyệt. */
    private Item.Rarity rarity;
}
