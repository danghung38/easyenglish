package com.dxh.Elearning.service.interfac;

import com.dxh.Elearning.dto.request.AddressCreateRequest;
import com.dxh.Elearning.dto.request.AddressUpdateRequest;
import com.dxh.Elearning.dto.response.AddressResponse;


import java.util.Set;

public interface AddressService {
    AddressResponse create(AddressCreateRequest addr);

    Set<AddressResponse> getMyAddressList();

    AddressResponse update(AddressUpdateRequest addr, Long id);

    void delete(Long addrId);

    AddressResponse changeDefault(Long id);

    AddressResponse getMyAddressById(Long id);
}
