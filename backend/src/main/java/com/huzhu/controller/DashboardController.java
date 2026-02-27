package com.huzhu.controller;

import com.huzhu.common.ApiResponse;
import com.huzhu.entity.Inventory;
import com.huzhu.entity.Order;
import com.huzhu.entity.PlantingBase;
import com.huzhu.entity.Purchase;
import com.huzhu.service.DeliveryService;
import com.huzhu.service.DocumentFileService;
import com.huzhu.service.NoticeService;
import com.huzhu.service.CustomerService;
import com.huzhu.service.InventoryService;
import com.huzhu.service.OrderService;
import com.huzhu.service.PlantingBaseService;
import com.huzhu.service.ProductionProcessService;
import com.huzhu.service.PurchaseService;
import com.huzhu.service.QualityControlService;
import com.huzhu.service.TaskItemService;
import com.huzhu.service.TraceRecordService;
import com.huzhu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private PlantingBaseService plantingBaseService;
    @Autowired
    private PurchaseService purchaseService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private ProductionProcessService productionProcessService;
    @Autowired
    private QualityControlService qualityControlService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private UserService userService;
    @Autowired
    private DeliveryService deliveryService;
    @Autowired
    private TaskItemService taskItemService;
    @Autowired
    private DocumentFileService documentFileService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private TraceRecordService traceRecordService;

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        Map<String, Object> result = new HashMap<>();
        result.put("baseCount", plantingBaseService.count());
        result.put("purchaseCount", purchaseService.count());
        result.put("inventoryCount", inventoryService.count());
        result.put("productionCount", productionProcessService.count());
        result.put("qualityCount", qualityControlService.count());
        result.put("orderCount", orderService.count());
        result.put("customerCount", customerService.count());
        result.put("userCount", userService.count());
        result.put("deliveryCount", deliveryService.count());
        result.put("taskCount", taskItemService.count());
        result.put("documentCount", documentFileService.count());
        result.put("noticeCount", noticeService.count());
        result.put("traceRecordCount", traceRecordService.count());

        List<Purchase> purchases = purchaseService.list();
        BigDecimal purchaseAmount = purchases.stream()
                .map(Purchase::getTotalAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("purchaseAmount", purchaseAmount);

        List<Order> orders = orderService.list();
        BigDecimal salesAmount = orders.stream()
                .map(Order::getTotalAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("salesAmount", salesAmount);

        List<Inventory> inventories = inventoryService.list();
        BigDecimal inventoryValue = inventories.stream()
                .map(Inventory::getTotalValue)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("inventoryValue", inventoryValue);

        List<PlantingBase> bases = plantingBaseService.list();
        Double plantingArea = bases.stream()
                .map(PlantingBase::getArea)
                .filter(v -> v != null)
                .reduce(0D, Double::sum);
        result.put("plantingArea", plantingArea);

        return ApiResponse.success(result);
    }
}
