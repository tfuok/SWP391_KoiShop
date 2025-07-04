package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.OrderDetailRequest;
import com.example.demo.model.Request.OrderRequest;
import com.example.demo.model.Response.*;
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

    @Autowired
    VoucherRepository voucherRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    ConsignmentRepository consignmentRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public Orders create(OrderRequest orderRequest) {
        Orders orders = new Orders();
        Account customer = authenticationService.getCurrentAccount();
        List<OrderDetails> orderDetails = new ArrayList<>();
        double total = 0;

        orders.setDate(new Date());
        orders.setCustomer(customer);
        orders.setStatus(Status.PENDING);
        orders.setDescription(orderRequest.getDescription());
        orders.setAddress(orderRequest.getAddress());

        for (OrderDetailRequest orderDetailRequest : orderRequest.getDetail()) {
            OrderDetails details = new OrderDetails();
            Koi koi = koiRepository.findKoiByIdAndIsDeletedFalse(orderDetailRequest.getKoiId());
            double price = (koi.getSalePrice() > 0) ? koi.getSalePrice() : koi.getPrice();
            details.setKoi(koi);
            details.setOrder(orders);
            details.setPrice(koi.getPrice());
            orderDetails.add(details);
            total += price;
        }
        orders.setOrderDetails(orderDetails);
        orders.setTotal(total);
        if (orderRequest.getVoucherCode() != null) {
            Voucher voucher = voucherRepository.findVoucherByCodeAndIsDeletedFalse(orderRequest.getVoucherCode());
            if (voucher == null || voucher.getExpiredDate().before(new Date()) || voucher.getQuantity() <= 0) {
                throw new NotFoundException("Invalid or expired voucher.");
            }

            // Apply discount
            double discount = voucher.getDiscountValue();
            double finalAmount = total - (total * discount / 100);
            orders.setFinalAmount(finalAmount);

            // Decrease voucher quantity
            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherRepository.save(voucher);
        } else {
            orders.setFinalAmount(total);
        }
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
        double money = orders.getFinalAmount() * 100;
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
            Payment existingPayment = paymentRepository.findByOrders(orders);
            if (existingPayment != null) {
                throw new IllegalStateException("Payment for this order already exists.");
            }
            Account customer = authenticationService.getCurrentAccount();
            Payment payment = new Payment();
            payment.setOrders(orders);
            payment.setCreateAt(new Date());
            payment.setMethod(PaymentEnums.BANKING);
            payment.setTotal(orders.getFinalAmount());
            payment.setCustomer(customer);

            List<Transactions> transactions = new ArrayList<>();

            //tao transaction
            Transactions transaction1 = new Transactions();
            //vnpay -> customer
            transaction1.setFrom(null);
            System.out.println(customer);
            transaction1.setTo(customer);
            transaction1.setPayment(payment);
            transaction1.setStatus(TransactionEnum.SUCCESS);
            transaction1.setDescription("CUSTOMER TO VNPAY");
            transaction1.setCreateAt(new Date());
            transactions.add(transaction1);

//            double MoneySendToVendor = 0;
//            for (OrderDetails orderDetails : orders.getOrderDetails()) {
//                if (orderDetails.getKoi().getAccount().getRole() == Role.CUSTOMER) {
//                    double orderAmount = orderDetails.getPrice() * 0.9;
//                    MoneySendToVendor += orderAmount;
//                }
//            }
//            double MoneySendtoManager = orders.getFinalAmount() - MoneySendToVendor;

            Transactions transaction2 = new Transactions();
            //customer -> server
            Account manager = accountRepository.findAccountByRole(Role.MANAGER);
            transaction2.setFrom(customer);
            transaction2.setTo(manager);
            transaction2.setPayment(payment);
            transaction2.setStatus(TransactionEnum.SUCCESS);
            transaction2.setDescription("VNPAY TO SERVER");
            double newBalance = manager.getBalance() + orders.getFinalAmount();
            transaction2.setAmount(orders.getFinalAmount());
            transaction2.setCreateAt(new Date());
            manager.setBalance(newBalance);
            transactions.add(transaction2);

            //server -> user (if koi is consignment online)
            //Account manager = accountRepository.findAccountByRole(Role.MANAGER);
//            for (OrderDetails orderDetails : orders.getOrderDetails()) {
//                if (orderDetails.getKoi().getAccount().getRole() == Role.CUSTOMER) {
//                    Transactions transaction3 = new Transactions();
//                    transaction3.setFrom(manager);
//                    transaction3.setTo(orderDetails.getKoi().getAccount());
//                    transaction3.setPayment(payment);
//                    transaction3.setStatus(TransactionEnum.SUCCESS);
//                    transaction3.setDescription("MANAGER TO CONSIGNMENT VENDOR");
//                    transaction3.setCreateAt(new Date());
//                    double orderAmount = orderDetails.getPrice() * 0.9;
//                    transaction3.setAmount(orderAmount);
//                    Account vendor = orderDetails.getKoi().getAccount();
//                    vendor.setBalance(vendor.getBalance() + orderAmount);
//                    accountRepository.save(vendor);
//                    transactions.add(transaction3);
//                }
//            }
            payment.setTransactions(transactions);
            Koi koi = null;
            for (OrderDetails orderDetails : orders.getOrderDetails()) {
                OrderDetails details = new OrderDetails();
                koi = koiRepository.findKoiByIdAndIsDeletedFalse(orderDetails.getKoi().getId());
                koi.setSold(true);
//                koi.setAccount(customer);
                koiRepository.save(koi);
//                if (koi.getQuantity() == 1) {
//                    certificateService.sendCertificateEmail(orders.getCustomer(), koi.getCertificate().getCertificateId());
//                }
            }
            accountRepository.save(manager);
            paymentRepository.save(payment);
            orders.setStatus(Status.PAID);
            orderRepository.save(orders);
            // Send email with the order bill
            emailService.sendOrderBillEmail(orders, orders.getCustomer().getEmail()); // Ensure to get customer's email
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Orders cancelOrderByCustomer(Long orderId) {
        Account customer = authenticationService.getCurrentAccount();
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getCustomer().getId() != customer.getId()) {
            throw new IllegalStateException("You can only cancel your own orders.");
        }

        if (order.getStatus() == Status.SHIPPING) {
            throw new IllegalStateException("Order has already been shipped and cannot be cancelled.");
        }

        order.setStatus(Status.CANCELLED);
        double refundAmount = order.getFinalAmount();
        double newBalance = customer.getBalance() + refundAmount;
        customer.setBalance(newBalance);
        Transactions refundTransactions = new Transactions();
        refundTransactions.setAmount(refundAmount);
        refundTransactions.setStatus(TransactionEnum.SUCCESS);
        refundTransactions.setDescription("REFUND FOR CUSTOMER");
        Account manager = accountRepository.findAccountByRole(Role.MANAGER);
        refundTransactions.setFrom(manager);
        refundTransactions.setTo(customer);
        refundTransactions.setCreateAt(new Date());
        manager.setBalance(manager.getBalance() - refundAmount);
        accountRepository.save(manager);
        accountRepository.save(customer);
        transactionRepository.save(refundTransactions);
        for (OrderDetails orderDetails : order.getOrderDetails()) {
            Koi koi = koiRepository.findById(orderDetails.getKoi().getId());
            koi.setSold(false);
            koi.setDeleted(false);
            //koi.setAccount(accountRepository.findAccountByRole(Role.MANAGER));
            koiRepository.save(koi);
        }

        accountRepository.save(customer);
        return orderRepository.save(order);
    }

    public void assignStaff(long staffId, Long orderId, Long consignmentId) {
        Account staff = accountRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff not found"));

        if (orderId == null && consignmentId == null) {
            throw new IllegalArgumentException("At least one of orderId or consignmentId must be provided");
        }

        if (orderId != null) {
            Orders orders = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException("Order not found"));
            orders.setStaff(staff);
            orderRepository.save(orders);
        }

        if (consignmentId != null) {
            Consignment consignment = consignmentRepository.findById(consignmentId)
                    .orElseThrow(() -> new NotFoundException("Consignment not found"));
            consignment.setStaff(staff);
            consignmentRepository.save(consignment);
        }
    }


    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersForCurrentUser() {
        // Get the currently authenticated user
        Account account = authenticationService.getCurrentAccount();
        List<Orders> ordersList = null;
        if (account.getRole().equals(Role.STAFF)) {
            ordersList = orderRepository.findByStaff(account);
        } else {
            ordersList = orderRepository.findOrdersByCustomer(account);
        }
        // Convert the list of Orders entities to OrderResponse objects
        return ordersList.stream()
                .map(this::mapToOrderResponse)  // Reuse the existing mapToOrderResponse method
                .collect(Collectors.toList());
    }

    public Orders updateOrderStatusByStaff(Long orderId, Status newStatus) {
        Account currentStaff = authenticationService.getCurrentAccount();
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        Account customer = order.getCustomer();
        Account manager = accountRepository.findAccountByRole(Role.MANAGER);
        if (newStatus == Status.DECLINED) {
            double refundAmount = order.getFinalAmount();
            double newBalance = customer.getBalance() + refundAmount;
            customer.setBalance(newBalance);
            Transactions refundTransactions = new Transactions();
            refundTransactions.setAmount(refundAmount);
            refundTransactions.setStatus(TransactionEnum.SUCCESS);
            refundTransactions.setDescription("REFUND FOR CUSTOMER");
            refundTransactions.setFrom(manager);
            refundTransactions.setTo(customer);
            refundTransactions.setCreateAt(new Date());
            manager.setBalance(manager.getBalance() - refundAmount);
            accountRepository.save(manager);
            transactionRepository.save(refundTransactions);
            for (OrderDetails orderDetails : order.getOrderDetails()) {
                Koi orderKoi = koiRepository.findById(orderDetails.getKoi().getId());
                orderKoi.setAccount(manager);
                orderKoi.setSold(false);
                koiRepository.save(orderKoi);
            }
        } else if (newStatus == Status.COMPLETED) {
            Payment orderPayment = paymentRepository.findByOrderId(orderId);
            for (OrderDetails orderDetails : order.getOrderDetails()) {
                if (orderDetails.getKoi().getAccount().getRole() == Role.CUSTOMER) {
                    Transactions transaction3 = new Transactions();
                    transaction3.setFrom(manager);
                    transaction3.setTo(orderDetails.getKoi().getAccount());
                    transaction3.setPayment(orderPayment);
                    transaction3.setStatus(TransactionEnum.SUCCESS);
                    transaction3.setDescription("MANAGER TO CONSIGNMENT VENDOR");
                    transaction3.setCreateAt(new Date());
                    double orderAmount = orderDetails.getPrice() * 0.9;
                    transaction3.setAmount(orderAmount);
                    Account vendor = orderDetails.getKoi().getAccount();
                    vendor.setBalance(vendor.getBalance() + orderAmount);
                    accountRepository.save(vendor);
                    orderPayment.getTransactions().add(transaction3);
                    paymentRepository.save(orderPayment);
                    }
                Koi koi = orderDetails.getKoi();
                koi.setSold(true);
                koi.setAccount(order.getCustomer());
                koiRepository.save(koi);
                if (koi.getQuantity() == 1) {
                    certificateService.sendCertificateEmail(order.getCustomer(), koi.getCertificate().getCertificateId());
                }
            }
        }
        accountRepository.save(customer);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public Orders staffConfirmOrdersByImage(Long orderId, String image) {
        Account currentStaff = authenticationService.getCurrentAccount();
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setImage(image);
        return orderRepository.save(order);
    }

    private OrderResponse mapToOrderResponse(Orders order) {
        List<OrderDetailResponse> details = order.getOrderDetails().stream()
                .map(detail -> new OrderDetailResponse(
                        detail.getKoi().getId(),
                        detail.getPrice(),
                        detail.getKoi().getName(),
                        detail.getKoi().getImages()
                ))
                .collect(Collectors.toList());

        Feedback feedback = order.getFeedback();
        FeedbackResponse feedbackResponse = null;
        if (feedback != null) {
            feedbackResponse = new FeedbackResponse(
                    feedback.getId(),
                    feedback.getContent(),
                    feedback.getRating(),
                    feedback.getCustomer() != null ? feedback.getCustomer().getUsername() : null,
                    order.getId()
            );
        }
        return OrderResponse.builder()
                .id(order.getId())
                .date(order.getDate())
                .total(order.getTotal())
                .description(order.getDescription())
                .status(order.getStatus())
                .finalAmount(order.getFinalAmount())
                .staffId(order.getStaff() != null ? order.getStaff().getId() : null)
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .orderDetails(details)
                .feedback(feedbackResponse)
                .image(order.getImage())
                .address(order.getAddress())
                .build();
    }

    private ConsignmentResponse mapToConsignmentResponse(Consignment consignment) {
        ConsignmentResponse response = new ConsignmentResponse();

        response.setConsignmentID(consignment.getId());
        response.setType(consignment.getType() != null ? consignment.getType().name() : null);
        response.setAddress(consignment.getAddress());
        response.setDescription(consignment.getDescription());
        response.setCost(String.valueOf(consignment.getCost()));
        response.setStartDate(consignment.getStartDate());
        response.setEndDate(consignment.getEndDate());
        response.setCreateDate(consignment.getCreateDate());
        response.setStatus(consignment.getStatus() != null ? consignment.getStatus().name() : null);
        response.setCareTypeName(consignment.getCareType() != null ? consignment.getCareType().getCareTypeName() : null);
        response.setStaffid(consignment.getStaff() != null ? consignment.getStaff().getId() : 0);

        response.setDetails(consignment.getConsignmentDetails().stream()
                .map(detail -> new ConsignmentDetailResponse(
                        detail.getKoi().getId(),
                        detail.getConsignment().getCost(),
                        detail.getKoi().getName(),
                        detail.getKoi().getImages()
                ))
                .collect(Collectors.toList()));

        return response;
    }


    public List<PaymentResponse> getAllPaymentsForCurrentUser() {
        Account currentAccount = authenticationService.getCurrentAccount();
        List<Payment> payments = paymentRepository.findByCustomerWithOrders(currentAccount);
        return payments.stream()
                .map(payment -> new PaymentResponse(
                        payment.getId(),
                        payment.getCreateAt(),
                        payment.getTotal(),
                        payment.getMethod(),
                        payment.getOrders() != null ? mapToOrderResponse(payment.getOrders()) : null,
                        payment.getConsignment() != null ? mapToConsignmentResponse(payment.getConsignment()) : null
                ))
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getAllPayment() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(payment -> new PaymentResponse(
                        payment.getId(),
                        payment.getCreateAt(),
                        payment.getTotal(),
                        payment.getMethod(),
                        payment.getOrders() != null ? mapToOrderResponse(payment.getOrders()) : null,
                        payment.getConsignment() != null ? mapToConsignmentResponse(payment.getConsignment()) : null
                ))
                .collect(Collectors.toList());
    }

}


