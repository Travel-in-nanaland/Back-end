package com.jeju.nanaland.domain.nana.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

public class NanaRequest {

  @Data
  @Builder
  public static class NanaUploadDto {

    private Long postId;
    private MultipartFile nanaTitleImage;
    private String language;
    private String subHeading;
    private String heading;
    private String notice;
    private List<NanaContentDto> nanaContents;

    @Data
    @Builder
    public static class NanaContentDto {

      private int number;
      private MultipartFile nanaContentImage;
      private String subHeading;
      private String heading;
      private String content;
      private List<String> additionalInfo;
      private List<String> answer;
      private String hashtag;
    }
  }

}
