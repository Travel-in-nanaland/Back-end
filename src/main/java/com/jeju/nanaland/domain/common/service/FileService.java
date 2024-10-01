package com.jeju.nanaland.domain.common.service;

import static com.jeju.nanaland.global.exception.ErrorCode.FILE_FAIL_ERROR;

import com.jeju.nanaland.global.exception.ServerErrorException;
import com.jeju.nanaland.global.util.CustomMultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
  private final String tmpLocation = System.getProperty("java.io.tmpdir");

  public File convertMultipartFileToFile(MultipartFile multipartFile) {
    String originalFilename = multipartFile.getOriginalFilename();
    if (originalFilename == null) {
      originalFilename = "unknown_file";
    }
    String fileName = originalFilename.replaceAll("\\s+", "_");
    File convertFile = new File(
        tmpLocation + File.separator + UUID.randomUUID() + "_" + fileName);

    try (FileOutputStream fileOutputStream = new FileOutputStream(convertFile)) {
      fileOutputStream.write(multipartFile.getBytes());
    } catch (IOException e) {
      throw new ServerErrorException(FILE_FAIL_ERROR.getMessage());
    }
    return convertFile;
  }

  public MultipartFile convertFileToMultipartFile(File file) throws IOException {
    String contentType = Files.probeContentType(file.toPath());
    if (contentType == null) {
      contentType = "application/octet-stream";
    }
    return new CustomMultipartFile(file, "file", file.getName(), contentType);
  }
}
