package cn.kastner.oj.service;

import cn.kastner.oj.dto.AnnouncementDTO;
import cn.kastner.oj.exception.AnnouncementException;

import java.util.List;

public interface AnnouncementService {

  List<AnnouncementDTO> findAnnouncement(int page, int size);

  AnnouncementDTO findAnnouncementById(String id) throws AnnouncementException;

  AnnouncementDTO create(AnnouncementDTO announcementDTO) throws AnnouncementException;

  AnnouncementDTO update(AnnouncementDTO announcementDTO) throws AnnouncementException;

  AnnouncementDTO delete(String id) throws AnnouncementException;
}
