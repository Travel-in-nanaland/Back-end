package com.jeju.nanaland.global.file.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.COMPLETE_PRESIGNED_URL_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.GET_PRESIGNED_URL_SUCCESS;

import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.file.dto.FileRequest;
import com.jeju.nanaland.global.file.dto.FileResponse;
import com.jeju.nanaland.global.file.service.FileUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "파일(File)", description = "파일(File) API입니다.")
public class FileUploadController {

  private final FileUploadService fileUploadService;

  @PostMapping("/upload-init")
  public BaseResponse<FileResponse.InitResultDto> uploadInit(
      @RequestBody @Valid FileRequest.InitCommandDto initCommandDto
  ){
    FileResponse.InitResultDto initResultDto = fileUploadService.uploadInit(initCommandDto);
    return BaseResponse.success(GET_PRESIGNED_URL_SUCCESS, initResultDto);
  }

  @PostMapping("/upload-complete")
  public BaseResponse<Void> uploadComplete(
      @RequestBody @Valid FileRequest.CompleteCommandDto completeCommandDto
  ) {
    fileUploadService.uploadComplete(completeCommandDto);
    return BaseResponse.success(COMPLETE_PRESIGNED_URL_SUCCESS);
  }
}
