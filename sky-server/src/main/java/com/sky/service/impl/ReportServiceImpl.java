package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        // 存放从begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateString = StringUtils.join(dateList, ",");
        turnoverReportVO.setDateList(dateString);

        // 存放从begin到end范围内每天的营业额(状态为已完成的订单合计)
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", 5);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0 : turnover; // 营业额为空时，设置为0
            turnoverList.add(turnover);
        }
        String turnoverString = StringUtils.join(turnoverList, ",");
        turnoverReportVO.setTurnoverList(turnoverString);
        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        UserReportVO userReportVO = new UserReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateString = StringUtils.join(dateList, ",");
        userReportVO.setDateList(dateString);

        // 存放从begin到end范围内每天的新增用户数(createTime在当天的用户数)
        List<Integer> newUserList = new ArrayList<>();
        // 存放从begin到end范围内每天的总用户数(就是createTime在end前的用户数)
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUser = totalUser == null ? 0 : totalUser;
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList, ","));
        return userReportVO;
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        OrderReportVO orderReportVO = new OrderReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateString = StringUtils.join(dateList, ",");
        orderReportVO.setDateList(dateString);

        // 存放从begin到end范围内每天的订单数
        List<Integer> totalOrderList = new ArrayList<>();

        // 存放从begin到end范围内每天的有效订单数(状态为已完成的订单数)
        List<Integer> completedOrderList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer order = getOrderCount(beginTime, endTime, null);
            order = order == null ? 0 : order;
            totalOrderList.add(order);
            Integer completedOrder = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            completedOrder = completedOrder == null ? 0 : completedOrder;
            completedOrderList.add(completedOrder);
        }
        orderReportVO.setOrderCountList(StringUtils.join(totalOrderList, ","));
        orderReportVO.setValidOrderCountList(StringUtils.join(completedOrderList, ","));

        // 使用stream流计算总订单数和有效订单数
        Integer totalOrders = totalOrderList.stream().reduce(Integer::sum).get();
        Integer completedOrders = completedOrderList.stream().reduce(Integer::sum).get();
        orderReportVO.setTotalOrderCount(totalOrders);
        orderReportVO.setValidOrderCount(completedOrders);
        Double completionRate = 0.0;
        if (totalOrders != 0) {
            completionRate = completedOrders.doubleValue() / totalOrders;
        }
        orderReportVO.setOrderCompletionRate(completionRate);
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        // 获取销量前10的商品
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);

        // 存放销量前10的商品名称, 用stream流调用map方法获取所有商品名称并加入集合
        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        // 存放销量前10的商品销量
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        salesTop10ReportVO.setNameList(StringUtils.join(names, ","));
        salesTop10ReportVO.setNumberList(StringUtils.join(numbers, ","));
        return salesTop10ReportVO;
    }

    private Integer getOrderCount(LocalDateTime begin , LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库，获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);
        // 通过POI将数据写入Excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData1 = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData1.getTurnover());
                row.getCell(3).setCellValue(businessData1.getValidOrderCount());
                row.getCell(4).setCellValue(businessData1.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData1.getUnitPrice());
                row.getCell(6).setCellValue(businessData1.getNewUsers());
            }
            excel.write(response.getOutputStream());
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // 通过response将Excel文件下载到客户端浏览器
    }
}
