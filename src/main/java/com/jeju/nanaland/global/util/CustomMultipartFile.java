package com.jeju.nanaland.global.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
public class CustomMultipartFile implements MultipartFile {

  private final File file;
  private final String name;
  private final String originalFilename;
  private final String contentType;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOriginalFilename() {
    return originalFilename;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean isEmpty() {
    return file.length() == 0;
  }

  @Override
  public long getSize() {
    return file.length();
  }

  @Override
  public byte[] getBytes() throws IOException {
    return Files.readAllBytes(file.toPath());
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(file);
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    Files.copy(file.toPath(), dest.toPath());
  }
}
