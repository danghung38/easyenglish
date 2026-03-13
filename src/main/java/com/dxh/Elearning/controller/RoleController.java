package com.dxh.Elearning.controller;


import com.dxh.Elearning.dto.request.RoleRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.RoleResponse;
import com.dxh.Elearning.service.interfac.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @Operation(method = "POST", summary = "Create role",
            description = "Create new role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @Operation(method = "GET", summary = "Get all role",
            description = "Get all role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    ApiResponse<List<RoleResponse>> getAll(){
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @Operation(method = "DELETE", summary = "Delete role",
            description = "Delete role by id")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{roleId}")
    ApiResponse<Void> delete(@PathVariable Long roleId){
        roleService.delete(roleId);
        return ApiResponse.<Void>builder().build();
    }
}
