package com.huzhu.controller;

import com.huzhu.common.ApiResponse;
import com.huzhu.entity.Inventory;
import com.huzhu.entity.Order;
import com.huzhu.entity.ProductionProcess;
import com.huzhu.entity.Purchase;
import com.huzhu.entity.QualityControl;
import com.huzhu.service.InventoryService;
import com.huzhu.service.OrderService;
import com.huzhu.service.ProductionProcessService;
import com.huzhu.service.PurchaseService;
import com.huzhu.service.QualityControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private ProductionProcessService productionProcessService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PurchaseService purchaseService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private QualityControlService qualityControlService;

    @GetMapping("/production")
    public ApiResponse<Map<String, Object>> productionAnalysis() {
        List<ProductionProcess> records = productionProcessService.list();
        long total = records.size();
        long finished = records.stream().filter(item -> "已完成".equals(item.getStatus())).count();
        long processing = records.stream().filter(item -> "处理中".equals(item.getStatus())).count();

        double finishedRate = total == 0 ? 0D : (double) finished / total;
        Map<String, Object> result = new HashMap<>();
        result.put("totalTasks", total);
        result.put("finishedTasks", finished);
        result.put("processingTasks", processing);
        result.put("finishedRate", BigDecimal.valueOf(finishedRate).setScale(4, RoundingMode.HALF_UP));
        result.put("byProduct", records.stream().collect(Collectors.groupingBy(ProductionProcess::getProductName, Collectors.counting())));
        return ApiResponse.success(result);
    }

    @GetMapping("/sales")
    public ApiResponse<Map<String, Object>> salesAnalysis() {
        List<Order> records = orderService.list();
        BigDecimal totalAmount = records.stream()
                .map(Order::getTotalAmount)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> byStatus = records.stream()
                .collect(Collectors.groupingBy(item -> item.getStatus() == null ? "未知" : item.getStatus(), Collectors.counting()));
        Map<String, BigDecimal> byCustomer = records.stream()
                .filter(item -> item.getCustomerName() != null && item.getTotalAmount() != null)
                .collect(Collectors.groupingBy(Order::getCustomerName,
                        Collectors.mapping(Order::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        Map<String, Object> result = new HashMap<>();
        result.put("orderCount", records.size());
        result.put("salesAmount", totalAmount);
        result.put("byStatus", byStatus);
        result.put("byCustomer", byCustomer);
        return ApiResponse.success(result);
    }

    @GetMapping("/supply-chain")
    public ApiResponse<Map<String, Object>> supplyChainAnalysis() {
        List<Purchase> purchases = purchaseService.list();
        List<Inventory> inventories = inventoryService.list();

        BigDecimal purchaseAmount = purchases.stream()
                .map(Purchase::getTotalAmount)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal inventoryValue = inventories.stream()
                .map(Inventory::getTotalValue)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double turnover = inventoryValue.compareTo(BigDecimal.ZERO) == 0 ? 0D
                : purchaseAmount.divide(inventoryValue, 4, RoundingMode.HALF_UP).doubleValue();

        Map<String, Object> result = new HashMap<>();
        result.put("purchaseCount", purchases.size());
        result.put("purchaseAmount", purchaseAmount);
        result.put("inventoryCount", inventories.size());
        result.put("inventoryValue", inventoryValue);
        result.put("inventoryTurnoverRate", turnover);
        result.put("inventoryByCategory", inventories.stream().collect(Collectors.groupingBy(Inventory::getCategory, Collectors.counting())));
        return ApiResponse.success(result);
    }

    @GetMapping("/monthly-trend")
    public ApiResponse<Map<String, Object>> monthlyTrend(@RequestParam(defaultValue = "12") Integer months) {
        int monthCount = months == null ? 12 : Math.max(3, Math.min(24, months));

        List<Order> orders = orderService.list();
        List<Purchase> purchases = purchaseService.list();
        List<ProductionProcess> productions = productionProcessService.list();
        List<QualityControl> qualityControls = qualityControlService.list();

        YearMonth endMonth = detectLatestMonth(orders, purchases, productions, qualityControls);
        YearMonth startMonth = endMonth.minusMonths(monthCount - 1L);

        Map<YearMonth, BigDecimal> purchaseMonthly = initDecimalSeries(startMonth, monthCount);
        Map<YearMonth, BigDecimal> salesMonthly = initDecimalSeries(startMonth, monthCount);
        Map<YearMonth, Double> productionMonthly = initDoubleSeries(startMonth, monthCount);
        Map<YearMonth, Long> orderMonthly = initLongSeries(startMonth, monthCount);
        Map<YearMonth, Long> qualityTotalMonthly = initLongSeries(startMonth, monthCount);
        Map<YearMonth, Long> qualityQualifiedMonthly = initLongSeries(startMonth, monthCount);

        for (Purchase purchase : purchases) {
            YearMonth ym = parseYearMonth(purchase.getPurchaseDate());
            if (ym != null && purchaseMonthly.containsKey(ym) && purchase.getTotalAmount() != null) {
                purchaseMonthly.put(ym, purchaseMonthly.get(ym).add(purchase.getTotalAmount()));
            }
        }

        for (Order order : orders) {
            YearMonth ym = parseYearMonth(order.getOrderDate());
            if (ym != null && salesMonthly.containsKey(ym)) {
                if (order.getTotalAmount() != null) {
                    salesMonthly.put(ym, salesMonthly.get(ym).add(order.getTotalAmount()));
                }
                orderMonthly.put(ym, orderMonthly.get(ym) + 1);
            }
        }

        for (ProductionProcess process : productions) {
            String date = process.getEndDate() != null ? process.getEndDate() : process.getStartDate();
            YearMonth ym = parseYearMonth(date);
            if (ym != null && productionMonthly.containsKey(ym) && process.getQuantity() != null) {
                productionMonthly.put(ym, productionMonthly.get(ym) + process.getQuantity());
            }
        }

        for (QualityControl qualityControl : qualityControls) {
            YearMonth ym = parseYearMonth(qualityControl.getTestDate());
            if (ym != null && qualityTotalMonthly.containsKey(ym)) {
                qualityTotalMonthly.put(ym, qualityTotalMonthly.get(ym) + 1);
                String status = qualityControl.getStatus() == null ? "" : qualityControl.getStatus();
                if (status.contains("合格") || status.equalsIgnoreCase("qualified")) {
                    qualityQualifiedMonthly.put(ym, qualityQualifiedMonthly.get(ym) + 1);
                }
            }
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> purchaseAmountTrend = new ArrayList<>();
        List<BigDecimal> salesAmountTrend = new ArrayList<>();
        List<Double> productionQtyTrend = new ArrayList<>();
        List<Long> orderCountTrend = new ArrayList<>();
        List<BigDecimal> qualityPassRateTrend = new ArrayList<>();

        YearMonth cursor = startMonth;
        for (int i = 0; i < monthCount; i++) {
            labels.add(cursor.toString());
            purchaseAmountTrend.add(purchaseMonthly.get(cursor));
            salesAmountTrend.add(salesMonthly.get(cursor));
            productionQtyTrend.add(BigDecimal.valueOf(productionMonthly.get(cursor)).setScale(2, RoundingMode.HALF_UP).doubleValue());
            orderCountTrend.add(orderMonthly.get(cursor));

            long total = qualityTotalMonthly.get(cursor);
            long qualified = qualityQualifiedMonthly.get(cursor);
            BigDecimal passRate = total == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(qualified * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
            qualityPassRateTrend.add(passRate);

            cursor = cursor.plusMonths(1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("purchaseAmountTrend", purchaseAmountTrend);
        result.put("salesAmountTrend", salesAmountTrend);
        result.put("productionQtyTrend", productionQtyTrend);
        result.put("orderCountTrend", orderCountTrend);
        result.put("qualityPassRateTrend", qualityPassRateTrend);
        return ApiResponse.success(result);
    }

    private YearMonth detectLatestMonth(List<Order> orders,
                                        List<Purchase> purchases,
                                        List<ProductionProcess> productions,
                                        List<QualityControl> qualityControls) {
        YearMonth latest = YearMonth.now();

        for (Order order : orders) {
            YearMonth ym = parseYearMonth(order.getOrderDate());
            if (ym != null && ym.isAfter(latest)) {
                latest = ym;
            }
        }
        for (Purchase purchase : purchases) {
            YearMonth ym = parseYearMonth(purchase.getPurchaseDate());
            if (ym != null && ym.isAfter(latest)) {
                latest = ym;
            }
        }
        for (ProductionProcess process : productions) {
            YearMonth ym = parseYearMonth(process.getEndDate() != null ? process.getEndDate() : process.getStartDate());
            if (ym != null && ym.isAfter(latest)) {
                latest = ym;
            }
        }
        for (QualityControl qualityControl : qualityControls) {
            YearMonth ym = parseYearMonth(qualityControl.getTestDate());
            if (ym != null && ym.isAfter(latest)) {
                latest = ym;
            }
        }
        return latest;
    }

    private Map<YearMonth, BigDecimal> initDecimalSeries(YearMonth start, int months) {
        Map<YearMonth, BigDecimal> result = new LinkedHashMap<>();
        YearMonth cursor = start;
        for (int i = 0; i < months; i++) {
            result.put(cursor, BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    private Map<YearMonth, Double> initDoubleSeries(YearMonth start, int months) {
        Map<YearMonth, Double> result = new LinkedHashMap<>();
        YearMonth cursor = start;
        for (int i = 0; i < months; i++) {
            result.put(cursor, 0D);
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    private Map<YearMonth, Long> initLongSeries(YearMonth start, int months) {
        Map<YearMonth, Long> result = new LinkedHashMap<>();
        YearMonth cursor = start;
        for (int i = 0; i < months; i++) {
            result.put(cursor, 0L);
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    private YearMonth parseYearMonth(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        String value = date.trim();
        try {
            if (value.length() == 7) {
                return YearMonth.parse(value, DateTimeFormatter.ofPattern("yyyy-MM"));
            }
            return YearMonth.from(LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } catch (DateTimeParseException e) {
            try {
                return YearMonth.from(LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy/M/d")));
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
