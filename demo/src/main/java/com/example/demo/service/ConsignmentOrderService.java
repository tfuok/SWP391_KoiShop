package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.OrderConsignmentRequest;
import com.example.demo.model.Request.OrderDetailRequest;
import com.example.demo.model.Response.OrderConsignmentResponse;
import com.example.demo.repository.*;
import com.example.demo.util.DateUtils;
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
import java.util.concurrent.TimeUnit;

@Service
public class ConsignmentOrderService {
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    KoiRepository koiRepository;
    @Autowired
    VoucherRepository voucherRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CareTypeRepository careTypeRepository;
    @Autowired
    private ConsignmentRepository consignmentRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private CertificateService certificateService;

    public OrderConsignmentResponse CreateConsignmentOrder(OrderConsignmentRequest orderConsignmentRequest) {
        Orders orders = new Orders();
        Account customer = authenticationService.getCurrentAccount();
        List<OrderDetails> orderDetails = new ArrayList<>();
        double total = 0;

        orders.setDate(new Date());
        orders.setCustomer(customer);
        orders.setStatus(Status.PENDING);
        orders.setDescription(orderConsignmentRequest.getDescription());

        for (OrderDetailRequest orderDetailRequest : orderConsignmentRequest.getDetail()) {
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
        if (orderConsignmentRequest.getVoucherCode() != null) {
            Voucher voucher = voucherRepository.findVoucherByCodeAndIsDeletedFalse(orderConsignmentRequest.getVoucherCode());
            if (voucher == null || voucher.getExpiredDate().before(new Date()) || voucher.getQuantity() <= 0) {
                throw new NotFoundException("Invalid or expired voucher.");
            }

            double discount = voucher.getDiscountValue();
            double finalAmount = total - (total * discount / 100);
            orders.setFinalAmount(finalAmount);

            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherRepository.save(voucher);
        } else {
            orders.setFinalAmount(total);
        }

        Consignment consignment = new Consignment();
        consignment.setType(Type.OFFLINE);
        consignment.setAddress("Koi Shop");
        consignment.setDescription(orderConsignmentRequest.getDescription());
        consignment.setStatus(Status.PENDING);

        Account account = authenticationService.getCurrentAccount();
        consignment.setAccount(account);
        consignment.setCreateDate(new Date());

        Date normalizedStartDate = DateUtils.normalizeDate(new Date());
        Date normalizedEndDate = DateUtils.normalizeDate(orderConsignmentRequest.getEndDate());
        consignment.setStartDate(normalizedStartDate);
        consignment.setEndDate(normalizedEndDate);

        if (consignment.getType() == Type.OFFLINE) {
            CareType careType = careTypeRepository.findByCareTypeId(orderConsignmentRequest.getCareTypeId());
            if (careType == null) {
                throw new NotFoundException("CareType not found with ID: " + orderConsignmentRequest.getCareTypeId());
            }
            consignment.setCareType(careType);

            float estimateCost = calculateTotalCost(careType.getCostPerDay(), orderConsignmentRequest.getDetail().size(), normalizedStartDate, normalizedEndDate);
            consignment.setCost(estimateCost);
        }

        if (orderConsignmentRequest.getDetail().isEmpty()) {
            throw new NotFoundException("Consignment details not found");
        }

        List<ConsignmentDetails> consignmentDetailsList = new ArrayList<>();
        for (OrderDetailRequest orderDetailRequest : orderConsignmentRequest.getDetail()) {
            ConsignmentDetails consignmentDetails = new ConsignmentDetails();
            Koi koi = koiRepository.findKoiByIdAndIsDeletedFalse(orderDetailRequest.getKoiId());
            consignmentDetails.setKoi(koi);
            consignmentDetails.setConsignment(consignment);
            consignmentDetailsList.add(consignmentDetails);
        }
        consignment.setConsignmentDetails(consignmentDetailsList);

        consignmentRepository.save(consignment);
        Orders savedOrder = orderRepository.save(orders);

        return new OrderConsignmentResponse(savedOrder, consignment);
    }


    public String createUrl(OrderConsignmentRequest orderConsignmentRequest) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);
        /*
         COde cua minh
         1.tao order
         */
        OrderConsignmentResponse orderConsignmentResponse = CreateConsignmentOrder(orderConsignmentRequest);
        Orders orders  = orderConsignmentResponse.getOrder();
        Consignment consignment = orderConsignmentResponse.getConsignment();
        double money = (orders.getFinalAmount() + consignment.getCost()) * 100;
        String amount = String.valueOf((int) money);

        String tmnCode = "VONI2DAD";
        String secretKey = "PIOSTSKRYSENPWY7NW7UG7HGWCHTT4IS";
        String vnpUrl = " https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "http://koishop.site/successful-consignment?orderID=" + orders.getId() +  "&consignmentID=" + consignment.getId();
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

    public void createTransaction(long orderId, long consignmentId) throws Exception {
        try {
            Orders orders = orderRepository.findById(orderId)
                    .orElseThrow((() -> new NotFoundException("Order not found")));
            Consignment consignment = consignmentRepository.findById(consignmentId)
                    .orElseThrow((() -> new NotFoundException("Consignment not found")));
            double totalCost = orders.getFinalAmount() + consignment.getCost();
        /*
        1. tao payment
         */

            Payment payment = new Payment();
            payment.setOrders(orders);
            payment.setConsignment(consignment);
            payment.setCreateAt(new Date());
            payment.setMethod(PaymentEnums.BANKING);
            payment.setCreateAt(new Date());
            payment.setTotal(totalCost);

            List<Transactions> transactions = new ArrayList<>();

            //tao transaction
            Transactions transaction1 = new Transactions();
            //vnpay -> customer
            transaction1.setFrom(null);
            Account customer = authenticationService.getCurrentAccount();
            transaction1.setTo(customer);
            transaction1.setPayment(payment);
            transaction1.setCreateAt(new Date());
            transaction1.setStatus(TransactionEnum.SUCCESS);
            transaction1.setDescription("VNPAY TO CUSTOMER");
            transactions.add(transaction1);

            double MoneySendToVendor = 0;
            Account manager = accountRepository.findAccountByRole(Role.MANAGER);
            for (OrderDetails orderDetails : orders.getOrderDetails()) {
                if (orderDetails.getKoi().getAccount().getRole() == Role.CUSTOMER) {
                    double orderAmount = orderDetails.getPrice() * 0.9;
                    MoneySendToVendor += orderAmount;
                }
            }
            double MoneySendtoManager = orders.getFinalAmount() - MoneySendToVendor;

            //customer -> server
//            Account manager = orders.getOrderDetails().get(0).getKoi().getAccount();
            Transactions transaction2 = new Transactions();
            transaction2.setFrom(customer);
            transaction2.setTo(manager);
            transaction2.setPayment(payment);
            transaction2.setStatus(TransactionEnum.SUCCESS);
            transaction2.setAmount(MoneySendtoManager);
            transaction2.setCreateAt(new Date());
            transaction2.setDescription("CUSTOMER TO MANAGER");
            double newBalance = manager.getBalance() + MoneySendtoManager ;
            manager.setBalance(newBalance);
            transactions.add(transaction2);

            //server -> user (if koi is consignment online)
            for (OrderDetails orderDetails : orders.getOrderDetails()) {
                if (orderDetails.getKoi().getAccount().getRole() == Role.CUSTOMER) {
                    Transactions transaction3 = new Transactions();
                    transaction3.setFrom(manager);
                    transaction3.setTo(orderDetails.getKoi().getAccount());
                    transaction3.setPayment(payment);
                    transaction3.setStatus(TransactionEnum.SUCCESS);
                    transaction3.setDescription("MANAGER TO CONSIGNMENT VENDOR");
                    transaction3.setCreateAt(new Date());
                    double orderAmount = orderDetails.getPrice() * 0.9;
                    transaction3.setAmount(orderAmount);
                    Account vendor = orderDetails.getKoi().getAccount();
                    vendor.setBalance(vendor.getBalance() + orderAmount);
                    accountRepository.save(vendor);
                    transactions.add(transaction3);
                }
            }
            Koi koi = null;
            for (OrderDetails orderDetails : orders.getOrderDetails()) {
                OrderDetails details = new OrderDetails();
                koi = koiRepository.findKoiByIdAndIsDeletedFalse(orderDetails.getKoi().getId());
                koi.setSold(true);
                koi.setAccount(customer);
                koiRepository.save(koi);
                if (koi.getQuantity() == 1) {
                     certificateService.sendCertificateEmail(customer, koi.getCertificate().getCertificateId());
                }
            }
            //tao transaction
            Transactions transaction5 = new Transactions();
            //customer -> server

            transaction5.setFrom(customer);
            transaction5.setTo(manager);
            transaction5.setPayment(payment);
            transaction5.setStatus(TransactionEnum.SUCCESS);
            transaction5.setCreateAt(new Date());
            transaction5.setAmount(consignment.getCost());
            if(consignment.getType() == Type.OFFLINE){
                transaction5.setDescription("CHUYEN PHI KY GUI OFFLINE VE MANAGER");
            }else{
                transaction5.setDescription("CHUYEN PHI KY GUI ONLINE VE MANAGER");
            }
            transactions.add(transaction5);
            payment.setTransactions(transactions);
            accountRepository.save(manager);
            paymentRepository.save(payment);
            orders.setStatus(Status.PAID);
            consignment.setStatus(Status.PAID);
            orderRepository.save(orders);
            consignmentRepository.save(consignment);
            emailService.sendConsignmentBillEmail(consignment,consignment.getAccount().getEmail());
            emailService.sendOrderBillEmail(orders, orders.getCustomer().getEmail()); // Ensure to get customer's email
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static float calculateTotalCost(float costPerDay, int quantity, Date startDate, Date endDate) {
        // Calculate the difference in milliseconds
        long diffInMillies = endDate.getTime() - startDate.getTime();

        // Calculate the number of days
        long daysBetween = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        // Return total cost
        return daysBetween * costPerDay * quantity;
    }
}
