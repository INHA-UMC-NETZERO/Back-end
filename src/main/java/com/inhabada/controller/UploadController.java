package com.inhabada.controller;

import com.inhabada.dto.UploadResponse;
import com.inhabada.exception.ValidationException;
import com.inhabada.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/uploads")
@Tag(name = "Uploads", description = "게시글 이미지 파일 업로드 API")
public class UploadController {

    private static final int MAX_FILES = 5;

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "게시글 등록에 사용할 이미지 파일을 최대 5개까지 multipart/form-data로 업로드하고, 저장된 이미지 key와 URL 정보를 반환합니다.")
    public ResponseEntity<List<UploadResponse>> upload(
            @Parameter(description = "업로드할 이미지 파일 목록. 최대 5개")
            @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("업로드할 파일이 없습니다");
        }
        if (files.size() > MAX_FILES) {
            throw new ValidationException("사진은 최대 " + MAX_FILES + "장까지 업로드할 수 있습니다");
        }

        List<UploadResponse> responses = files.stream()
                .map(uploadService::upload)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
