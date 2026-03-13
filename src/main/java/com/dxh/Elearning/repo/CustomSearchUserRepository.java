package com.dxh.Elearning.repo;

import com.dxh.Elearning.dto.response.PageResponse;
import com.dxh.Elearning.dto.response.UserResponse;
import com.dxh.Elearning.entity.Role;
import com.dxh.Elearning.entity.User;
import com.dxh.Elearning.mapper.UserMapper;
import com.dxh.Elearning.repo.specification.SpecSearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dxh.Elearning.utils.AppConstant.SEARCH_SPEC_OPERATOR;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSearchUserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    private final UserMapper userMapper;

    /**
     * Search user join role
     *
     * @param pageable
     * @param user
     * @param role
     * @return
     */
    public PageResponse<List<UserResponse>> searchUserByCriteriaWithJoin(Pageable pageable, String[] user, String[] role) {
        log.info("-------------- searchUserByCriteriaWithJoin --------------");

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);
        Join<Role, User> roleRoot = userRoot.join("roles");

        List<Predicate> userPreList = new ArrayList<>();
        Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
        for (String u : user) {
            Matcher matcher = pattern.matcher(u);
            if (matcher.find()) {
                log.info(matcher.group(1));
                log.info(matcher.group(2));
                log.info(matcher.group(3));
                log.info(matcher.group(4));
                log.info(matcher.group(5));
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                userPreList.add(toUserPredicate(userRoot, builder, searchCriteria));
            }
        }

        List<Predicate> rolePreList = new ArrayList<>();
        for (String a : role) {
            Matcher matcher = pattern.matcher(a);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                rolePreList.add(toRolePredicate(roleRoot, builder, searchCriteria));
            }
        }

        Predicate userPre = builder.and(userPreList.toArray(new Predicate[0]));
        Predicate rolePre = builder.and(rolePreList.toArray(new Predicate[0]));
        Predicate finalPre = builder.and(userPre, rolePre);

        query.where(finalPre);

        List<User> users = entityManager.createQuery(query)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        List<UserResponse> userResponseList = users.stream().map(userMapper::toUserResponse).toList();

        long count = countUserJoinRole(user, role);

        return PageResponse.<List<UserResponse>>builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPage((int) (count/pageable.getPageSize()))
                .totalElements(count)
                .items(userResponseList)
                .build();
    }

    private Predicate toUserPredicate(Root<User> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        log.info("-------------- toUserPredicate --------------");
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }

    private Predicate toRolePredicate(Join<Role, User> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        log.info("-------------- toRolePredicate --------------");
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }

    /**
     * Count user by conditions
     *
     * @param user
     * @param role
     * @return
     */
    private long countUserJoinRole(String[] user, String[] role) {
        log.info("-------------- countUserJoinRole --------------");

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<User> userRoot = query.from(User.class);
        Join<Role, User> roleRoot = userRoot.join("roles");

        List<Predicate> userPreList = new ArrayList<>();

        Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
        for (String u : user) {
            Matcher matcher = pattern.matcher(u);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                userPreList.add(toUserPredicate(userRoot, builder, searchCriteria));
            }
        }

        List<Predicate> rolePreList = new ArrayList<>();
        for (String a : role) {
            Matcher matcher = pattern.matcher(a);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                rolePreList.add(toRolePredicate(roleRoot, builder, searchCriteria));
            }
        }

        Predicate userPre = builder.and(userPreList.toArray(new Predicate[0]));
        Predicate addPre = builder.and(rolePreList.toArray(new Predicate[0]));
        Predicate finalPre = builder.and(userPre, addPre);

        query.select(builder.count(userRoot));
        query.where(finalPre);

        return entityManager.createQuery(query).getSingleResult();
    }
}
