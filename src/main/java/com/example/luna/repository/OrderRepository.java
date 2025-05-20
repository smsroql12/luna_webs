package com.example.luna.repository;

import com.example.luna.dto.AdminOrderDTO;
import com.example.luna.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("""
    SELECT o FROM Order o
    JOIN OrderItem oi ON o.id = oi.orderid
    JOIN Product p ON oi.productid = p.no
    WHERE o.userid = :userid
    AND (:search IS NULL OR p.name LIKE %:search%)
    AND (:startDate IS NULL OR o.created >= :startDate)
    AND (:endDate IS NULL OR o.created <= :endDate)
    GROUP BY o.id
""")
    Page<Order> searchByFilters(
            @Param("userid") Long userid,
            @Param("search") String search,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);


    @Query(
            value = "SELECT new com.example.luna.dto.AdminOrderDTO(" +
                    "o.id, o.status, u.email, o.created, o.cancel, o.total, o.address1, o.address2, o.returnitem, o.returnmsg, o.returntrackingnum, o.returncomplete) " +
                    "FROM Order o JOIN SiteUser u ON o.userid = u.id " +
                    "WHERE (:orderId IS NULL OR STR(o.id) LIKE %:orderId%) " +
                    "AND (:userIds IS NULL OR o.userid IN :userIds) " +
                    "AND (:startDateTime IS NULL OR o.created >= :startDateTime) " +
                    "AND (:endDateTime IS NULL OR o.created <= :endDateTime) " +
                    "AND (:status IS NULL OR o.status = :status) " +
                    "AND ((:cancelOnly = true AND o.cancel = 1) " +
                    "OR (:includeCanceled = true) " +
                    "OR (:cancelOnly = false AND :includeCanceled = false AND o.cancel = 0)) " +
                    // 반품 조건 추가
                    "AND (:returnOnly = false OR o.returnitem = 1) " +
                    "AND (:returnStatus IS NULL OR o.returncomplete = :returnStatus)",
            countQuery = "SELECT COUNT(o) FROM Order o JOIN SiteUser u ON o.userid = u.id " +
                    "WHERE (:orderId IS NULL OR STR(o.id) LIKE %:orderId%) " +
                    "AND (:userIds IS NULL OR o.userid IN :userIds) " +
                    "AND (:startDateTime IS NULL OR o.created >= :startDateTime) " +
                    "AND (:endDateTime IS NULL OR o.created <= :endDateTime) " +
                    "AND (:status IS NULL OR o.status = :status) " +
                    "AND ((:cancelOnly = true AND o.cancel = 1) " +
                    "OR (:includeCanceled = true) " +
                    "OR (:cancelOnly = false AND :includeCanceled = false AND o.cancel = 0)) " +
                    "AND (:returnOnly = false OR o.returnitem = 1) " +
                    "AND (:returnStatus IS NULL OR o.returncomplete = :returnStatus)"
    )
    Page<AdminOrderDTO> findAdminOrdersWithCancelAndReturn(
            @Param("orderId") String orderId,
            @Param("userIds") List<Long> userIds,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("status") Integer status,
            @Param("cancelOnly") boolean cancelOnly,
            @Param("includeCanceled") boolean includeCanceled,
            @Param("returnOnly") boolean returnOnly,
            @Param("returnStatus") Integer returnStatus,
            Pageable pageable
    );



}