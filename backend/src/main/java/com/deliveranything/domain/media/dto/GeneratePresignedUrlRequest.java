package com.deliveranything.domain.media.dto;

import com.deliveranything.domain.media.enums.UploadDomain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GeneratePresignedUrlRequest(
    @NotBlank(message = "파일 이름은 필수입니다.")
    String fileName,

    @NotNull(message = "도메인은 필수입니다.")
    UploadDomain domain
) {

}
