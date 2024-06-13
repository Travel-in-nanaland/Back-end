package com.jeju.nanaland.domain.nana.dto;

import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

public class NanaRequest {

  @Data
  public static class NanaUploadDto {

    private Long postId;
    private int version;
    private MultipartFile nanaTitleImage;
    private String language;
    private String subHeading;
    private String heading;
    private String notice;
    private List<NanaContentDto> nanaContents;

    @Data
    public static class NanaContentDto {

      private int number;
      private MultipartFile nanaContentImage;
      private String subTitle;
      private String title;
      private String content;
      private List<String> additionalInfo;
      private List<String> infoDesc;
      private String hashtag;
    }
  }

}
