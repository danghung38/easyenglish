package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.PermissionRequest;
import com.dxh.Elearning.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse create(PermissionRequest request);

    List<PermissionResponse> getAll();

    void delete(String permission);
}
