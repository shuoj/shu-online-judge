package cn.kastner.oj.controller;

import cn.kastner.oj.dto.AnnouncementDTO;
import cn.kastner.oj.exception.HaveSuchItemException;
import cn.kastner.oj.exception.NoSuchItemException;
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
   * @throws NoSuchItemException 没有这个公告
   */
  @GetMapping(value = "/{id}")
  public AnnouncementDTO getAnnouncement(@PathVariable String id) throws NoSuchItemException {
    return announcementService.findAnnouncementById(id);
  }

  /**
   * 创建公告
   *
   * @throws NoSuchItemException   没有此用户
   * @throws ValidateException     无权限
   * @throws HaveSuchItemException 已经有相同title的公告了 @Param AnnouncementDTO { "authorId": "title":
   *                               "content": }
   *                               <p>
   *                               <p>return 返回创建成功的公告{ "id": "authorId": "authorName": "title": "content": "modifiedDate": }
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public AnnouncementDTO createAnnouncementDTO(
      @Validated @RequestBody AnnouncementDTO announcementDTO, BindingResult bindingResult)
      throws NoSuchItemException, ValidateException, HaveSuchItemException {

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
   * @throws NoSuchItemException
   * @throws ValidateException
   * @throws HaveSuchItemException
   */
  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public AnnouncementDTO updateAnnouncement(
      @Validated @RequestBody AnnouncementDTO announcementDTO,
      BindingResult bindingResult,
      @PathVariable String id)
      throws NoSuchItemException, ValidateException, HaveSuchItemException {
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
   * @throws NoSuchItemException
   */
  @DeleteMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public AnnouncementDTO delete(@PathVariable String id) throws NoSuchItemException {
    return announcementService.delete(id);
  }
}
