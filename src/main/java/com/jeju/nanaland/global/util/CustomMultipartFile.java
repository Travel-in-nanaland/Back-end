package com.jeju.nanaland.global.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
public class CustomMultipartFile implements MultipartFile {

  private final File file;
  private final String name;
  private final String originalFilename;
  private final String contentType;
  private static final int BUFFER_SIZE = 8192;

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
    return new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    try (InputStream in = getInputStream();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(dest), BUFFER_SIZE)) {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
    }
  }
}
