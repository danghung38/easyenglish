package com.dxh.Elearning.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dxh.Elearning.utils.AppConstant.SORT_BY;

public class Utils {
    public static Pageable createPageable(Integer pageNo, Integer pageSize, String sortBy){
        int page = pageNo>0?(pageNo-1):0;
        List<Sort.Order> sorts = new ArrayList<>();
        if (StringUtils.hasLength(sortBy)) {
            // name:asc|desc
            Pattern pattern = Pattern.compile(SORT_BY); // AppConstant.SORT_BY = "(\\w+?)(:)(.*)"
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String field = matcher.group(1);
                String direction = matcher.group(3);
                if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
                    throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
                }
                if (direction.equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, field));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, field));
                }
            }
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));
        return pageable;
    }


}
