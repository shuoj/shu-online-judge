package cn.kastner.oj.controller;

import cn.kastner.oj.dto.AnnouncementDTO;
import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.exception.ValidateException;
import cn.kastner.oj.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/announcements")
public class AnnouncementRestController {

  private final AnnouncementService announcementService;

  @Autowired
  public AnnouncementRestController(AnnouncementService announcementService) {
    this.announcementService = announcementService;
  }

  @GetMapping
  public List<AnnouncementDTO> getAnnouncements(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "5") Integer size) {
    return announcementService.findAnnouncement(page, size);
  }

  /**
   * 获取指定公告
   *
   * @param id announcementId
   */
  @GetMapping(value = "/{id}")
  public AnnouncementDTO getAnnouncement(@PathVariable String id) throws AppException {
    return announcementService.findAnnouncementById(id);
  }

  /**
   * 创建公告
   *
   * @throws ValidateException     无权限
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public AnnouncementDTO createAnnouncementDTO(
      @Validated @RequestBody AnnouncementDTO announcementDTO, BindingResult bindingResult)
      throws AppException {

    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      return announcementService.create(announcementDTO);
    }
  }

  /**
   * 更新公告信息
   *
   * @param announcementDTO { 和创建公告信息的参数相同 }
   * @param id              return{ 更新后的题目信息 }
   * @throws ValidateException
   */
  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public AnnouncementDTO updateAnnouncement(
      @Validated @RequestBody AnnouncementDTO announcementDTO,
      BindingResult bindingResult,
      @PathVariable String id)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      announcementDTO.setId(id);
      return announcementService.update(announcementDTO);
    }
  }

  /**
   * 删除指定公告
   *
   * @param id return 删除的题目
   */
  @DeleteMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public AnnouncementDTO delete(@PathVariable String id) throws AppException {
    return announcementService.delete(id);
  }
}
