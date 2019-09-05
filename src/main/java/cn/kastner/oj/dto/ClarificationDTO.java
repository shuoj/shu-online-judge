package cn.kastner.oj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClarificationDTO {

    private String id;

    private String authorName;

    private String contestId;

    private String question;

    private String answer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;

    private Boolean isPublic;

    private Boolean isRead;
}
