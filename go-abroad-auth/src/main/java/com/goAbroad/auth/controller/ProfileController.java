package com.goAbroad.auth.controller;

import com.goAbroad.auth.dto.UserInfoResponse;
import com.goAbroad.auth.dto.UserInfoUpdateRequest;
import com.goAbroad.auth.entity.User;
import com.goAbroad.auth.mapper.UserMapper;
import com.goAbroad.auth.repository.UserRepository;
import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.common.result.R;
import com.goAbroad.common.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息 Controller
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<UserInfoResponse> getUserInfo() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        return R.ok(userMapper.toResponse(user));
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/update")
    public R<UserInfoResponse> updateUserInfo(@RequestBody UserInfoUpdateRequest request) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户名是否被占用
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            boolean exists = userRepository.existsByUsernameAndIdNot(request.getUsername(), userId);
            if (exists) {
                throw new BusinessException("标识名已被使用");
            }
        }

        userMapper.updateFromRequest(request, user);
        user = userRepository.save(user);

        return R.ok(userMapper.toResponse(user));
    }
}
