package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.*;
import com.example.demo.model.Response.*;
import com.example.demo.repository.*;
import com.example.demo.util.DateUtils;
import jakarta.persistence.criteria.Order;
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
import java.util.stream.Collectors;

@Service
public class ConsignmentService {

    @Autowired
    private ConsignmentRepository consignmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private KoiRepository koiLotRepository;

    @Autowired
    private CareTypeRepository careTypeRepository;

    @Autowired
    private BreedRepository breedRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CertificateService certificateService;
    @Autowired
    private EmailService emailService;

    public Consignment createConsignment(ConsignmentRequest consignmentRequest) throws Exception {
        Consignment consignment = new Consignment();
        consignment.setType(consignmentRequest.getType());
        consignment.setAddress(consignmentRequest.getAddress());
        consignment.setDescription(consignmentRequest.getDescription());
        consignment.setStatus(ConsignmentStatus.PENDING);
        Account account = authenticationService.getCurrentAccount();
        consignment.setAccount(account);
        consignment.setCreateDate(new Date());
        Date normalizedStartDate = DateUtils.normalizeDate(consignmentRequest.getStartDate());
        Date normalizedEndDate = DateUtils.normalizeDate(consignmentRequest.getEndDate());
        consignment.setStartDate(normalizedStartDate);
        consignment.setEndDate(normalizedEndDate);
        if (consignment.getType() == Type.OFFLINE) {
            CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
            if (careType == null) {
                throw new NotFoundException("CareType not found with ID: " + consignmentRequest.getCareTypeId());
            }
            consignment.setCareType(careType);
        } else if (consignment.getType() == Type.ONLINE) {
            if(careTypeRepository.findByCareTypeName("Phi Ky Gui Online") == null){
                throw new NotFoundException("Chua Co Phi Ky Gui Online O CareType");
            }else {
                CareType careType = careTypeRepository.findByCareTypeName("Phi Ky Gui Online");
                consignment.setCareType(careType);
            }
        }
        if (consignment.getType() == Type.OFFLINE) {
            CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
            float estimateCost = calculateTotalCost(
                    careType.getCostPerDay(),
                    consignmentRequest.getConsignmentDetailRequests().size(),
                    normalizedStartDate,
                    normalizedEndDate
            );
            consignment.setCost(estimateCost);
        } else if (consignment.getType() == Type.ONLINE) {
            CareType careType = careTypeRepository.findByCareTypeId(consignment.getCareType().getCareTypeId());
            float estimateCost = careType.getCostPerDay();
            consignment.setCost(estimateCost);
        }
        if(consignmentRequest.getConsignmentDetailRequests().isEmpty()){
            throw new NotFoundException("Consignment details not found");
        }
        List<ConsignmentDetails> consignmentDetailsList = new ArrayList<>();
        for (ConsignmentDetailRequest consignmentDetailRequest : consignmentRequest.getConsignmentDetailRequests()) {
            Koi koi = createConsignmentKoi(consignmentDetailRequest.getKoiRequest());
            ConsignmentDetails consignmentDetail = new ConsignmentDetails();
            consignmentDetail.setConsignment(consignment);
            consignmentDetail.setKoi(koi);
            consignmentDetailsList.add(consignmentDetail);
        }

        consignment.setConsignmentDetails(consignmentDetailsList);
        return consignmentRepository.save(consignment);
    }

    public List<ConsignmentResponse> getAllConsignments() {

        List<Consignment> consignmentsList = consignmentRepository.findByIsDeletedFalse();
        List<ConsignmentResponse> consignmentResponseList = new ArrayList<>();
        for(Consignment consignment : consignmentsList){
            ConsignmentResponse consignmentResponse = mapToConsignmentResponse(consignment);
            consignmentResponseList.add(consignmentResponse);
        }
        return consignmentResponseList;
    }
    public Consignment deleteConsignment(long id) {
        Consignment consignment = consignmentRepository.findConsignmentById(id);
        if (consignment == null) {
            throw new NotFoundException("Consignment not found with ID: " + id);
        }

        consignment.setIsDeleted(true);
        return consignmentRepository.save(consignment);
    }


//    public Consignment updateConsignment(ConsignmentRequest consignmentRequest, long id) {
//        Consignment foundConsignment = consignmentRepository.findConsignmentById(id);
//        if (foundConsignment == null) {
//            throw new NotFoundException("Consignment not found with ID: " + id);
//        }
//
//        // Update basic fields
//        foundConsignment.setType(consignmentRequest.getType());
//        foundConsignment.setDescription(consignmentRequest.getDescription());
//
//        // Normalize and set startDate and endDate
//        Date normalizedStartDate = DateUtils.normalizeDate(consignmentRequest.getStartDate());
//        Date normalizedEndDate = DateUtils.normalizeDate(consignmentRequest.getEndDate());
//        foundConsignment.setStartDate(normalizedStartDate);
//        foundConsignment.setEndDate(normalizedEndDate);
//
//        // Update CareType
//        if (foundConsignment.getType() == Type.OFFLINE) {
//            CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
//            if (careType == null) {
//                throw new NotFoundException("CareType not found with ID: " + consignmentRequest.getCareTypeId());
//            }
//            foundConsignment.setCareType(careType);
//        }
//
//        // Recalculate cost if type is "OFFLINE"
//        if (foundConsignment.getType() == Type.OFFLINE) {
//            CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
//            float estimateCost = calculateTotalCost(
//                    careType.getCostPerDay(),
//                    consignmentRequest.getConsignmentDetailRequests().size(),
//                    normalizedStartDate,
//                    normalizedEndDate
//            );
//            foundConsignment.setCost(estimateCost);
//        }
//
//        // Update ConsignmentDetails
//        List<ConsignmentDetails> updatedDetails = new ArrayList<>();
//        for (ConsignmentDetailRequest detailRequest : consignmentRequest.getConsignmentDetailRequests()) {
//
//            if (koi == null) {
//                throw new NotFoundException("Koi not found with ID: " + detailRequest.getId());
//            }
//            Koi koi = detailRequest.getKoiRequest().
//            ConsignmentDetails consignmentDetail = new ConsignmentDetails();
//            consignmentDetail.setConsignment(foundConsignment);
//            consignmentDetail.setKoi(koi);
//            updatedDetails.add(consignmentDetail);
//        }
//
//        // Replace existing details with updated ones
//        foundConsignment.getConsignmentDetails().clear();
//        foundConsignment.getConsignmentDetails().addAll(updatedDetails);
//
//        return consignmentRepository.save(foundConsignment);//   }



    public List<ConsignmentResponse> getStaffConsignments() {
        List<Consignment> consignmentList = consignmentRepository.findByStaff_Id(authenticationService.getCurrentAccount().getId());
        List<ConsignmentResponse> consignmentResponseList = new ArrayList<>();
        for(Consignment consignment : consignmentList){
            ConsignmentResponse consignmentResponse = mapToConsignmentResponse(consignment);
            consignmentResponseList.add(consignmentResponse);
        }
        return consignmentResponseList;
    }

    public static float calculateTotalCost(float costPerDay, int quantity, Date startDate, Date endDate) {
        // Calculate the difference in milliseconds
        long diffInMillies = endDate.getTime() - startDate.getTime();

        // Calculate the number of days
        long daysBetween = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        // Return total cost
        return daysBetween * costPerDay * quantity;
    }

    public String createUrl(ConsignmentRequest consignmentRequest) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);

        Consignment consignment = createConsignment(consignmentRequest);
        double money = consignment.getCost() * 100;
        String amount = String.valueOf((int) money);

        String tmnCode = "VONI2DAD";
        String secretKey = "PIOSTSKRYSENPWY7NW7UG7HGWCHTT4IS";
        String vnpUrl = " https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "http://koishop.site/successful-consign?consignmentID=" + consignment.getId(); // trang thong bao thanh toan thanh cong
        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", String.valueOf(consignment.getId()+1000));
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + consignment.getId()+1000);
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


        public void createConsignmentTransaction(long id){
            Consignment consignment = consignmentRepository.findConsignmentById(id);
            if(consignment == null){
                throw new NotFoundException("Consignment not found");
            }

        /*
        1. tao payment
         */
            Payment existingPayment = paymentRepository.findByConsignment(consignment);
            if (existingPayment != null) {
                throw new IllegalStateException("Payment for this consignment already exists.");
            }

            Payment payment = new Payment();
            payment.setConsignment(consignment);
            payment.setCreateAt(new Date());
            payment.setMethod(PaymentEnums.BANKING);
            payment.setTotal(consignment.getCost());
            payment.setCreateAt(new Date());
            List<Transactions> transactions = new ArrayList<>();
            Account account =authenticationService.getCurrentAccount();
            payment.setCustomer(account);

            //tao transaction
            Transactions transaction1 = new Transactions();
            //vnpay -> customer
            transaction1.setFrom(null);
            Account customer = authenticationService.getCurrentAccount();
            transaction1.setTo(customer);
            transaction1.setPayment(payment);
            transaction1.setStatus(TransactionEnum.SUCCESS);
            transaction1.setDescription("VNPAY TO CUSTOMER");
            transaction1.setCreateAt(new Date());
            transactions.add(transaction1);

            Transactions transaction2 = new Transactions();
            //customer -> server
            Account manager = accountRepository.findAccountByRole(Role.MANAGER);
            transaction2.setFrom(customer);
            transaction2.setTo(manager);
            transaction2.setPayment(payment);
            transaction2.setStatus(TransactionEnum.SUCCESS);
            transaction2.setAmount(consignment.getCost());
            transaction2.setCreateAt(new Date());
            if(consignment.getType() == Type.OFFLINE){
                transaction2.setDescription("OFFLINE CONSIGNMENT COST TO MANAGER");
            }else{
                transaction2.setDescription("ONLINE CONSIGNMENT COST TO MANAGER");
            }
            double newBalance = manager.getBalance() + consignment.getCost();
            manager.setBalance(newBalance);
            transactions.add(transaction2);
            payment.setTransactions(transactions);

            accountRepository.save(manager);
            paymentRepository.save(payment);
            consignment.setStatus(ConsignmentStatus.PAID);
            consignmentRepository.save(consignment);
            emailService.sendConsignmentBillEmail(consignment,consignment.getAccount().getEmail());
        }
    public Consignment cancelConsignmentByCustomer(Long consignmentId) {
        Account customer = authenticationService.getCurrentAccount();
        Consignment consignment = consignmentRepository.findById(consignmentId)
                .orElseThrow(() -> new NotFoundException("Consignment not found"));

        if (consignment.getAccount().getId() != customer.getId()) {
            throw new IllegalStateException("You can only cancel your own orders.");
        }


        consignment.setStatus(ConsignmentStatus.CANCELLED);


        for(ConsignmentDetails consignmentDetails : consignment.getConsignmentDetails()){
            Koi koi = koiLotRepository.findById(consignmentDetails.getKoi().getId());
            koi.setSold(false);
            koi.setDeleted(true);

            koiLotRepository.save(koi);
        }

       return consignmentRepository.save(consignment);
    }
    public Koi createConsignmentKoi(KoiRequest koiLotRequest) {
        try {

            Koi koiLot = new Koi();
            koiLot.setName(koiLotRequest.getName());
            koiLot.setPrice(koiLotRequest.getPrice());
            koiLot.setVendor(koiLotRequest.getVendor());
            koiLot.setGender(koiLotRequest.getGender());
            koiLot.setBornYear(koiLotRequest.getBornYear());
            koiLot.setSize(koiLotRequest.getSize());
            koiLot.setOrigin(koiLotRequest.getOrigin());
            koiLot.setDescription(koiLotRequest.getDescription());
            koiLot.setQuantity(1);
            koiLot.setImages(koiLotRequest.getImageUrl());
            koiLot.setDeleted(true);
            List<Images> imagesList = koiLotRequest.getImagesList().stream().map(imageListRequest -> {
                Images image = new Images();
                image.setImages(imageListRequest.getImage());
                image.setKoi(koiLot);
                return image;
            }).collect(Collectors.toList());

            koiLot.setImagesList(imagesList);

            Set<Breed> breeds = new HashSet<>();
            for (Long breedId : koiLotRequest.getBreedId()) {
                Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
                if (breed == null) throw new NotFoundException("Breed not exist");
                breeds.add(breed);
            }
            koiLot.setBreeds(breeds);


            Account accountRequest = authenticationService.getCurrentAccount();
            koiLot.setAccount(accountRequest);
            if (accountRequest.getRole() == Role.CUSTOMER) {
                koiLot.setDeleted(true);
            }


            return koiLotRepository.save(koiLot);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create KoiLot");
        }
    }

    public Consignment updateConsignmentStatus(long id, ConsignmentStatus status) throws Exception {
        Consignment consignment = consignmentRepository.findConsignmentById(id);
        if (consignment == null) {
            throw new NotFoundException("Consignment not exist");
        }
        consignment.setStatus(status);
        if (consignment.getStatus() == ConsignmentStatus.CONFIRMED) {
            for (ConsignmentDetails consignmentDetails : consignment.getConsignmentDetails()) {
                consignmentDetails.getKoi().setConsignment(true);
                if(consignment.getType() == Type.OFFLINE) {
                    Payment payment = paymentRepository.findByConsignment(consignment);
                    if (payment != null) {
                        Orders orders = payment.getOrders();
                        if (orders != null) {
                            Koi koi = consignmentDetails.getKoi();
                            certificateService.sendCertificateEmail(consignment.getAccount(), koi.getCertificate().getCertificateId());
                            paymentRepository.findByConsignment(consignment).getOrders().setStatus(Status.CONFIRMED);

                        }
                    }
                }
                else if (consignment.getType() == Type.ONLINE) {
                    consignmentDetails.getKoi().setDeleted(false);
                    Certificate certificate = certificateService.createCertificates(consignmentDetails.getKoi());
                    Koi koi = consignmentDetails.getKoi();
                    koi.setCertificate(certificate);



                    koiLotRepository.save(koi);
                }
            }
        }else if(consignment.getStatus() == ConsignmentStatus.DECLINED){
            Payment payment = paymentRepository.findByConsignmentId(id);
            Orders orders = payment.getOrders();
            if(orders != null) {
                Orders order = payment.getOrders();
                order.setStatus(Status.DECLINED);
                orderRepository.save(order);
            }
            Transactions refundTransactions = new Transactions();
            refundTransactions.setAmount(payment.getTotal());
            refundTransactions.setStatus(TransactionEnum.SUCCESS);
            refundTransactions.setDescription("REFUND PAYMENT FOR CUSTOMER");
            Account manager = accountRepository.findAccountByRole(Role.MANAGER);
            Account customer = payment.getCustomer();
            refundTransactions.setFrom(manager);
            refundTransactions.setTo(customer);
            refundTransactions.setCreateAt(new Date());
            manager.setBalance(manager.getBalance() - payment.getTotal());
            customer.setBalance(customer.getBalance() + payment.getTotal());
            payment.getTransactions().add(refundTransactions);
            accountRepository.save(manager);
            accountRepository.save(customer);
            paymentRepository.save(payment);
            }
        return consignmentRepository.save(consignment);
    }

    public List<KoiOnlineConsignmentResponse> getAllOnlineKoi() {
        List<KoiOnlineConsignmentResponse> responses = new ArrayList<>();


        Long accountId = authenticationService.getCurrentAccount().getId();

        // Fetch all Koi consigned online by the current account
        List<Koi> koiList = koiLotRepository.findAllKoiByAccountIdAndConsignmentType(accountId, Type.ONLINE);

        for (Koi koi : koiList) {
            KoiOnlineConsignmentResponse response = new KoiOnlineConsignmentResponse();


            List<Consignment> consignments = koiLotRepository.findConsignmentsByKoiId(koi.getId());


            Consignment consignment = consignments.isEmpty() ? null : consignments.get(0);

            if (consignment != null) {

                response.setId(consignment.getId());
                response.setPrice(koi.getPrice());
                response.setName(koi.getName());
            }

            response.setImgUrl(koi.getImages());


            if (koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.CONFIRMED && koi.isSold()) {
                response.setStatus("SOLD");
            }  else if(koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.CONFIRMED) {
                response.setStatus("ON SELL");
            } else if (!koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.PAID) {
                response.setStatus("WAIT FOR ACCEPT");
            } else if (!koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.DECLINED) {
                response.setStatus("DECLINED");
            } else if (!koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.PENDING) {
                response.setStatus("PENDING");
            }else if ( consignment != null && consignment.getStatus() == ConsignmentStatus.CANCELLED) {
                response.setStatus("CANCELLED");
            }
            responses.add(response);
        }
        responses.sort((r1, r2) -> Long.compare(r2.getId(), r1.getId()));
        return responses;
    }

    public List<KoiOfflineConsignmentResponse> getAllOfflineKoi() {
        List<KoiOfflineConsignmentResponse> responses = new ArrayList<>();
        Long accountId = authenticationService.getCurrentAccount().getId();
        List<Koi> koiList = koiLotRepository.findAllKoiByAccountIdAndConsignmentType(accountId, Type.OFFLINE);
        for (Koi koi : koiList) {
            KoiOfflineConsignmentResponse response = new KoiOfflineConsignmentResponse();
            List<Consignment> consignments = koiLotRepository.findConsignmentsByKoiId(koi.getId());
            Consignment consignment = !consignments.isEmpty() ? consignments.get(consignments.size() - 1) : null;
            if (consignment != null) {
                response.setId(consignment.getId());
                response.setEndDate(consignment.getEndDate());
                response.setPrice(consignment.getCost());
            }
            response.setImgUrl(koi.getImages());
            if (koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.CONFIRMED) {
                response.setIsConsignment("CONSIGNED");
            } else if (!koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.PAID) {
                response.setIsConsignment("PAID");
            } else if (!koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.DECLINED) {
                response.setIsConsignment("DECLINED");
            } else if (!koi.isConsignment() && consignment != null && consignment.getStatus() == ConsignmentStatus.PENDING) {
                response.setIsConsignment("PENDING");
            }else if ( consignment != null && consignment.getStatus() == ConsignmentStatus.CANCELLED) {
                    response.setIsConsignment("CANCELLED");
            }
            Payment payment = paymentRepository.findByConsignment(consignment);
            if (payment != null) {
                Orders orders = payment.getOrders();
                if (orders != null) {
                    response.setOfOrder(true);
                }
            }
            responses.add(response);
        }
        responses.sort((r1, r2) -> Long.compare(r2.getId(), r1.getId()));
        return responses;
    }
    public void addAddress(Long consignmentId, String address) throws Exception {
        Orders orders = paymentRepository.findByConsignmentId(consignmentId).getOrders();
        if(orders == null) {
            throw new Exception("There no order for this consignment to add address");
            }
            orders.setAddress(address);
            orderRepository.save(orders);

        }

    public Consignment extendEndDate(Long consignmentId, Date endDate) {
        Consignment oldConsignment = consignmentRepository.findConsignmentById(consignmentId);

        Consignment newConsignment = new Consignment();
        oldConsignment.setIsDeleted(true);


        Date normalizedStartDate = DateUtils.normalizeDate(oldConsignment.getEndDate());
        Date normalizedEndDate = DateUtils.normalizeDate(endDate);
        CareType careType = careTypeRepository.findByCareTypeId(oldConsignment.getCareType().getCareTypeId());
        float estimateCost = calculateTotalCost(
                careType.getCostPerDay(),
                oldConsignment.getConsignmentDetails().size(),
                normalizedStartDate,
                normalizedEndDate

        );
        newConsignment.setCreateDate(oldConsignment.getCreateDate());
        newConsignment.setCost(estimateCost);
        newConsignment.setType(oldConsignment.getType());
        newConsignment.setStatus(oldConsignment.getStatus());

        List<ConsignmentDetails> newConsignmentDetails = new ArrayList<>();
        for (ConsignmentDetails oldDetail : oldConsignment.getConsignmentDetails()) {
            ConsignmentDetails newDetail = new ConsignmentDetails();
            newDetail.setKoi(oldDetail.getKoi());  // Set the same Koi
            newDetail.setConsignment(newConsignment);  // Link to the new consignment
            newConsignmentDetails.add(newDetail);
        }
        newConsignment.setCreateDate(oldConsignment.getCreateDate());
        newConsignment.setConsignmentDetails(newConsignmentDetails);
        newConsignment.setEndDate(normalizedEndDate);
        newConsignment.setStartDate(normalizedStartDate);
        newConsignment.setAddress(oldConsignment.getAddress());
        newConsignment.setDescription(oldConsignment.getDescription());
        newConsignment.setCareType(oldConsignment.getCareType());
        newConsignment.setAccount(oldConsignment.getAccount());
        newConsignment.setStaff(oldConsignment.getStaff());
        consignmentRepository.save(newConsignment);

        return newConsignment;
    }
    public String createExtendUrl(Long consignmentId, Date endDate) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);

        Consignment consignment = extendEndDate(consignmentId,endDate);
        double money = consignment.getCost() * 100;
        String amount = String.valueOf((int) money);

        String tmnCode = "VONI2DAD";
        String secretKey = "PIOSTSKRYSENPWY7NW7UG7HGWCHTT4IS";
        String vnpUrl = " https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "http://koishop.site/successful-consign?consignmentID=" + consignment.getId(); // trang thong bao thanh toan thanh cong
        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", String.valueOf(consignment.getId()+1000));
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + consignment.getId()+1000);
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
    public void createExtendTransaction(long id){
        Consignment consignment = consignmentRepository.findConsignmentById(id);
        if(consignment == null){
            throw new NotFoundException("Consignment not found");
        }

        /*
        1. tao payment
         */
        Payment existingPayment = paymentRepository.findByConsignment(consignment);
        if (existingPayment != null) {
            throw new IllegalStateException("Payment for this consignment already exists.");
        }

        Payment payment = new Payment();
        payment.setConsignment(consignment);
        payment.setCreateAt(new Date());
        payment.setMethod(PaymentEnums.BANKING);
        payment.setTotal(consignment.getCost());
        payment.setCreateAt(new Date());
        List<Transactions> transactions = new ArrayList<>();
        Account account =authenticationService.getCurrentAccount();
        payment.setCustomer(account);

        //tao transaction
        Transactions transaction1 = new Transactions();
        //vnpay -> customer
        transaction1.setFrom(null);
        Account customer = authenticationService.getCurrentAccount();
        transaction1.setTo(customer);
        transaction1.setPayment(payment);
        transaction1.setStatus(TransactionEnum.SUCCESS);
        transaction1.setDescription("VNPAY TO CUSTOMER");
        transaction1.setCreateAt(new Date());
        transactions.add(transaction1);

        Transactions transaction2 = new Transactions();
        //customer -> server
        Account manager = accountRepository.findAccountByRole(Role.MANAGER);
        transaction2.setFrom(customer);
        transaction2.setTo(manager);
        transaction2.setPayment(payment);
        transaction2.setStatus(TransactionEnum.SUCCESS);
        transaction2.setAmount(consignment.getCost());
        transaction2.setCreateAt(new Date());
        transaction2.setDescription("EXTEND COST TO MANAGER");
        double newBalance = manager.getBalance() + consignment.getCost();
        manager.setBalance(newBalance);
        transactions.add(transaction2);
        payment.setTransactions(transactions);

        accountRepository.save(manager);
        paymentRepository.save(payment);
        consignment.setStatus(ConsignmentStatus.PAID);
        consignmentRepository.save(consignment);

    }
    public ConsignmentResponse assignStaff(long consignmentId, long staffId) {
        Consignment consignment = consignmentRepository.findById(consignmentId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        Account staff = accountRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff not found"));
        consignment.setStaff(staff);
        consignmentRepository.save(consignment);
        return mapToConsignmentResponse(consignment);
    }
    public ConsignmentDetailResponse mapToConsignmentDetailResponse(Koi koi) {
        ConsignmentDetailResponse detailResponse = new ConsignmentDetailResponse();
        detailResponse.setKoiId(koi.getId());
        detailResponse.setPrice(koi.getPrice());
        detailResponse.setKoiName(koi.getName());
        if (koi.getImagesList() != null && !koi.getImagesList().isEmpty()) {
            detailResponse.setImageUrl(koi.getImages());
        }
        return detailResponse;
    }


    public ConsignmentResponse mapToConsignmentResponse(Consignment consignment) {
        ConsignmentResponse response = new ConsignmentResponse();
        response.setConsignmentID(consignment.getId());
        response.setType(consignment.getType().toString());
        response.setAddress(consignment.getAddress());
        response.setDescription(consignment.getDescription());
        response.setCost(String.valueOf(consignment.getCost()));
        response.setStartDate(consignment.getStartDate());
        response.setEndDate(consignment.getEndDate());
        response.setPhoneNumber(consignment.getAccount().getPhone());
        response.setCreateDate(consignment.getCreateDate());
        response.setStatus(consignment.getStatus().toString());
        response.setCareTypeName(consignment.getCareType() != null ? consignment.getCareType().getCareTypeName() : "N/A");
        response.setStaffid(consignment.getStaff() != null ? consignment.getStaff().getId() : 0); // Get staff ID
        List<ConsignmentDetailResponse> details = consignment.getConsignmentDetails().stream()
                .map(consignmentDetail -> mapToConsignmentDetailResponse(consignmentDetail.getKoi()))
                .collect(Collectors.toList());
        response.setDetails(details);
        return response;
    }
}
