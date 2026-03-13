package com.dxh.Elearning.mapper;


import com.dxh.Elearning.dto.request.RoleRequest;
import com.dxh.Elearning.dto.response.RoleResponse;
import com.dxh.Elearning.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    //bỏ qua k map Set<permission> vì list nhận vào là String
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}