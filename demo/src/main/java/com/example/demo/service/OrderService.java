package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.OrderDetailRequest;
import com.example.demo.model.Request.OrderRequest;
import com.example.demo.model.Response.OrderDetailResponse;
import com.example.demo.model.Response.OrderResponse;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    KoiRepository koiRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CertificateService certificateService;

    public Orders create(OrderRequest orderRequest) {
        Orders orders = new Orders();
        Account customer = authenticationService.getCurrentAccount();
        List<OrderDetails> orderDetails = new ArrayList<>();
        double total = 0;

        orders.setDate(new Date());
        orders.setCustomer(customer);
        orders.setStatus(Status.PENDING);
        orders.setDescription(orderRequest.getDescription());

        for (OrderDetailRequest orderDetailRequest : orderRequest.getDetail()) {
            OrderDetails details = new OrderDetails();
            Koi koi = koiRepository.findKoiByIdAndIsDeletedFalse(orderDetailRequest.getKoiId());
            details.setKoi(koi);
            details.setOrder(orders);
            details.setPrice(koi.getPrice());
            orderDetails.add(details);
            total += koi.getPrice();
        }
        orders.setOrderDetails(orderDetails);
        orders.setTotal(total);
        return orderRepository.save(orders);
    }

    public String createUrl(OrderRequest orderRequest) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);
        /*
         COde cua minh
         1.tao order
         */
        Orders orders = create(orderRequest);
        double money = orders.getTotal() * 100;
        String amount = String.valueOf((int) money);

        String tmnCode = "VONI2DAD";
        String secretKey = "PIOSTSKRYSENPWY7NW7UG7HGWCHTT4IS";
        String vnpUrl = " https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "http://koishop.site/successful?orderID=" + orders.getId(); // trang thong bao thanh toan thanh cong
        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", String.valueOf(orders.getId()));
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + orders.getId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount", amount);

        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_CreateDate", formattedCreateDate);
        vnpParams.put("vnp_IpAddr", "128.199.178.23");

        StringBuilder signDataBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("=");
            signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("&");
        }
        signDataBuilder.deleteCharAt(signDataBuilder.length() - 1); // Remove last '&'

        String signData = signDataBuilder.toString();
        String signed = generateHMAC(secretKey, signData);

        vnpParams.put("vnp_SecureHash", signed);

        StringBuilder urlBuilder = new StringBuilder(vnpUrl);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("&");
        }
        urlBuilder.deleteCharAt(urlBuilder.length() - 1); // Remove last '&'

        return urlBuilder.toString();
    }

    private String generateHMAC(String secretKey, String signData) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(keySpec);
        byte[] hmacBytes = hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public void createTransaction(long id) throws Exception {
        try {
            Orders orders = orderRepository.findById(id)
                    .orElseThrow((() -> new NotFoundException("Order not found")));

        /*
        1. tao payment
         */

            Payment payment = new Payment();
            payment.setOrders(orders);
            payment.setCreateAt(new Date());
            payment.setMethod(PaymentEnums.BANKING);

            List<Transactions> transactions = new ArrayList<>();

            //tao transaction
            Transactions transaction1 = new Transactions();
            //vnpay -> customer
            transaction1.setFrom(null);
            Account customer = authenticationService.getCurrentAccount();
            transaction1.setTo(customer);
            transaction1.setPayment(payment);
            transaction1.setStatus(TransactionEnum.SUCCESS);
            transaction1.setDescription("CUSTOMER TO VNPAY");
            transactions.add(transaction1);

            Transactions transaction2 = new Transactions();
            //customer -> server
//            Account manager = orders.getOrderDetails().get(0).getKoi().getAccount();
            Account manager = accountRepository.findAccountByRole(Role.MANAGER);
            transaction2.setFrom(customer);
            transaction2.setTo(manager);
            transaction2.setPayment(payment);
            transaction2.setStatus(TransactionEnum.SUCCESS);
            transaction2.setDescription("VNPAY TO SERVER");
            double newBalance = manager.getBalance() + orders.getTotal();
            manager.setBalance(newBalance);
            transactions.add(transaction2);

            payment.setTransactions(transactions);
        Koi koi = null;
        for (OrderDetails orderDetails : orders.getOrderDetails()) {
            OrderDetails details = new OrderDetails();
            koi = koiRepository.findKoiByIdAndIsDeletedFalse(orderDetails.getKoi().getId());
            koi.setSold(true);
            koi.setAccount(customer);
            koiRepository.save(koi);
            if(koi.getQuantity()==1) {
                certificateService.sendCertificateEmail(customer, koi.getCertificate());
            }
        }
            accountRepository.save(manager);
            paymentRepository.save(payment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OrderResponse assignStaff(long orderId, long staffId){
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        Account staff = accountRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff not found"));

        orders.setStaff(staff);
        orderRepository.save(orders);
        return mapToOrderResponse(orders);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Orders order) {
        List<OrderDetailResponse> details = order.getOrderDetails().stream()
                .map(detail -> new OrderDetailResponse(
                        detail.getKoi().getId(),
                        detail.getPrice(),
                        detail.getKoi().getName()
                ))
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .date(order.getDate())
                .total(order.getTotal())
                .rating(order.getRating())
                .description(order.getDescription())
                .status(order.getStatus())
                .feedback(order.getFeedback())
                .finalAmount(order.getFinalAmount())
                .staffId(order.getStaff() != null ? order.getStaff().getId() : null)
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .orderDetails(details)
                .build();
    }
}


