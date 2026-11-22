package com.dxh.Elearning.mapper;



import com.dxh.Elearning.dto.request.PermissionRequest;
import com.dxh.Elearning.dto.response.PermissionResponse;
import com.dxh.Elearning.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
