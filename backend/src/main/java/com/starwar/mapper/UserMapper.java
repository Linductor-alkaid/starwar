package com.starwar.mapper;

import com.starwar.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectByUsername(String username);
    
    User selectByEmail(String email);
    
    User selectById(Long id);
    
    int insert(User user);
    
    int update(User user);
}


