package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.RoleRequest;
import com.dxh.Elearning.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse create(RoleRequest request);

    List<RoleResponse> getAll();

    void delete(Long role);
}
