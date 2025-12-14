package com.starwar.service;

import com.starwar.dto.LoginDTO;
import com.starwar.dto.RegisterDTO;
import com.starwar.vo.UserVO;

public interface UserService {
    void register(RegisterDTO registerDTO);
    
    String login(LoginDTO loginDTO);
    
    UserVO getUserInfo(Long userId);
}


