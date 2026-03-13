package com.dxh.Elearning.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> implements Serializable {

    int pageNo;
    int pageSize;
    int totalPage;
    long totalElements;
    T items;
}