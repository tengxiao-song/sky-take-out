package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    // cron： 秒 分 时 日 月 周 年(可选）
    // 处理超时订单
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void processTimeOutOrder() {
        LocalDateTime now = LocalDateTime.now();
        List<Orders> orders = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, now.minusMinutes(15));
        for (Orders order : orders) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelReason("订单超时，自动取消");
            order.setCancelTime(now);
            orderMapper.update(order);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void processDeliveryOrder() {
        List<Orders> orders = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
        for (Orders order : orders) {
            order.setStatus(Orders.COMPLETED);
            orderMapper.update(order);
        }
    }
}
