package cn.kastner.oj.service;

import cn.kastner.oj.dto.AnnouncementDTO;
import cn.kastner.oj.exception.HaveSuchItemException;
import cn.kastner.oj.exception.NoSuchItemException;

import java.util.List;

public interface AnnouncementService {

  List<AnnouncementDTO> findAnnouncement(int page, int size);

  AnnouncementDTO findAnnouncementById(String id) throws NoSuchItemException;

  AnnouncementDTO create(AnnouncementDTO announcementDTO)
      throws NoSuchItemException, HaveSuchItemException;

  AnnouncementDTO update(AnnouncementDTO announcementDTO)
      throws NoSuchItemException, HaveSuchItemException;

  AnnouncementDTO delete(String id) throws NoSuchItemException;
}
