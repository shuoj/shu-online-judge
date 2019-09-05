package cn.kastner.oj.service;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.dto.ChangePasswordDTO;
import cn.kastner.oj.exception.HaveSuchItemException;
import cn.kastner.oj.exception.ValidateException;

public interface MeService {

  User update(User user) throws HaveSuchItemException;

  User changePassword(ChangePasswordDTO changePasswordDTO) throws ValidateException;
}
