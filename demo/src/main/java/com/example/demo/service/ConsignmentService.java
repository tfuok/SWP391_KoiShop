package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.*;
import com.example.demo.repository.*;
import com.example.demo.util.DateUtils;
import org.modelmapper.ModelMapper;
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
public class ConsignmentService {

    @Autowired
    private ConsignmentRepository consignmentRepository;


    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private KoiRepository koiRepository;

    @Autowired
    private CareTypeRepository careTypeRepository;


    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CertificateService certificateService;
    @Autowired
    private KoiService koiService;

    /**
     * Creates a new Consignment based on the provided request.
     *
     * @param consignmentRequest The request containing consignment details.
     * @return The newly created Consignment entity.
     */
    public Consignment createConsignment(ConsignmentRequest consignmentRequest) {
        // Map from ConsignmentRequest to Consignment entity
        //Consignment consignment = modelMapper.map(consignmentRequest, Consignment.class);
        Consignment consignment = new Consignment();
        consignment.setType(consignmentRequest.getType());
        consignment.setAddress(consignmentRequest.getAddress());
        consignment.setDescription(consignmentRequest.getDescription());
        consignment.setStatus(Status.PENDING);

        // Set the account to the currently authenticated user
        Account account = authenticationService.getCurrentAccount();
        consignment.setAccount(account);

        // Set creation date
        consignment.setCreateDate(new Date());

        // Normalize and set startDate and endDate
        Date normalizedStartDate = DateUtils.normalizeDate(consignmentRequest.getStartDate());
        Date normalizedEndDate = DateUtils.normalizeDate(consignmentRequest.getEndDate());
        consignment.setStartDate(normalizedStartDate);
        consignment.setEndDate(normalizedEndDate);

        // Set CareType
        if (consignment.getType() == Type.OFFLINE) {
            CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
            if (careType == null) {
                throw new NotFoundException("CareType not found with ID: " + consignmentRequest.getCareTypeId());
            }
            consignment.setCareType(careType);
        } else if (consignment.getType() == Type.ONLINE) {
            CareType careType = careTypeRepository.findByCareTypeName("Phi Ky Gui Online");
            consignment.setCareType(careType);
        }
        // Calculate and set cost if type is "OFFLINE"
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

        // Initialize ConsignmentDetails list
        if(consignmentRequest.getConsignmentDetailRequests().isEmpty()){
            throw new NotFoundException("Consignment details not found");
        }
        List<ConsignmentDetails> consignmentDetailsList = new ArrayList<>();
        for (ConsignmentDetailRequest consignmentDetailRequest : consignmentRequest.getConsignmentDetailRequests()) {

            Koi koi = koiService.createKoi(consignmentDetailRequest.getKoiRequest());

            ConsignmentDetails consignmentDetail = new ConsignmentDetails();
            consignmentDetail.setConsignment(consignment);
            consignmentDetail.setKoi(koi);

            consignmentDetailsList.add(consignmentDetail);
        }

        consignment.setConsignmentDetails(consignmentDetailsList);

        // Save consignment (cascade will save consignmentDetails)
        return consignmentRepository.save(consignment);
    }


    /**
     * Retrieves all active consignments.
     *
     * @return A list of active Consignments.
     */
    public List<Consignment> getAllConsignments() {
        return consignmentRepository.findByIsDeletedFalse();
    }

    /**
     * Deletes (soft deletes) a consignment by its ID.
     *
     * @param id The ID of the consignment to delete.
     * @return The updated Consignment entity.
     */
    public Consignment deleteConsignment(long id) {
        Consignment consignment = consignmentRepository.findConsignmentById(id);
        if (consignment == null) {
            throw new NotFoundException("Consignment not found with ID: " + id);
        }

        consignment.setIsDeleted(true);
        return consignmentRepository.save(consignment);
    }

    /**
     * Updates an existing Consignment based on the provided request.
     *
     * @param consignmentRequest The request containing updated consignment details.
     * @param id                 The ID of the consignment to update.
     * @return The updated Consignment entity.
     */
    public Consignment updateConsignment(ConsignmentRequest consignmentRequest, long id) {
        Consignment foundConsignment = consignmentRepository.findConsignmentById(id);
        if (foundConsignment == null) {
            throw new NotFoundException("Consignment not found with ID: " + id);
        }

        // Update basic fields
        foundConsignment.setType(consignmentRequest.getType());
        foundConsignment.setDescription(consignmentRequest.getDescription());

        // Normalize and set startDate and endDate
        Date normalizedStartDate = DateUtils.normalizeDate(consignmentRequest.getStartDate());
        Date normalizedEndDate = DateUtils.normalizeDate(consignmentRequest.getEndDate());
        foundConsignment.setStartDate(normalizedStartDate);
        foundConsignment.setEndDate(normalizedEndDate);

        // Update CareType
        if (foundConsignment.getType() == Type.OFFLINE) {
            CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
            if (careType == null) {
                throw new NotFoundException("CareType not found with ID: " + consignmentRequest.getCareTypeId());
            }
            foundConsignment.setCareType(careType);
        }

        // Recalculate cost if type is "OFFLINE"
        if (foundConsignment.getType() == Type.OFFLINE) {
            CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
            float estimateCost = calculateTotalCost(
                    careType.getCostPerDay(),
                    consignmentRequest.getConsignmentDetailRequests().size(),
                    normalizedStartDate,
                    normalizedEndDate
            );
            foundConsignment.setCost(estimateCost);
        }

        // Update ConsignmentDetails
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

        return consignmentRepository.save(foundConsignment);
    }


    /**
     * Retrieves consignments associated with a specific user (account) ID.
     *
     * @param accountId The ID of the account.
     * @return A list of Consignments linked to the account.
     */
    public List<Consignment> getConsignmentsByAccountId(long accountId) {
        return consignmentRepository.findByAccount_IdAndIsDeletedFalse(accountId);
    }

    /**
     * Calculates the total cost of a consignment.
     *
     * @param costPerDay The cost per day for the care type.
     * @param quantity   The number of Koi involved.
     * @param startDate  The start date of the consignment.
     * @param endDate    The end date of the consignment.
     * @return The total estimated cost.
     */
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
        /*
         COde cua minh
         1.tao order
         */
        Consignment consignment = createConsignment(consignmentRequest);
        double money = consignment.getCost() * 100;
        String amount = String.valueOf((int) money);

        String tmnCode = "VONI2DAD";
        String secretKey = "PIOSTSKRYSENPWY7NW7UG7HGWCHTT4IS";
        String vnpUrl = " https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "https://blearning.vn/guide/swp/docker-local?orderID=" + consignment.getId(); // trang thong bao thanh toan thanh cong
        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", String.valueOf(consignment.getId()));
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + consignment.getId());
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
            Consignment consignment = consignmentRepository.findById(id)
                    .orElseThrow((() -> new NotFoundException("Order not found")));

        /*
        1. tao payment
         */

            Payment payment = new Payment();
            payment.setConsignment(consignment);
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
            transaction1.setDescription("NAP TIEN VNPAY TO CUSTOMER");
            transactions.add(transaction1);

            Transactions transaction2 = new Transactions();
            //customer -> server
            Account manager = accountRepository.findAccountByRole(Role.OWNER);
            transaction2.setFrom(customer);
            transaction2.setTo(manager);
            transaction2.setPayment(payment);
            transaction2.setStatus(TransactionEnum.SUCCESS);
            transaction2.setDescription("CHUYEN TIEN KY GUI OFFLINE VE OWNER");
            double newBalance = manager.getBalance() + consignment.getCost();
            manager.setBalance(newBalance);
            transactions.add(transaction2);


            payment.setTransactions(transactions);

            accountRepository.save(manager);
            paymentRepository.save(payment);
            consignment.setStatus(Status.PENDING);
            consignmentRepository.save(consignment);

        }

    public Consignment updateStatusConsignment(long id, ConsignmentStatusRequest consignmentStatusRequest) {
        Consignment consignment = consignmentRepository.findConsignmentById(id);
        consignment.setStatus(consignmentStatusRequest.getStatus());
        if(consignment.getStatus()==Status.CONFIRMED){
            for(ConsignmentDetails consignmentDetails : consignment.getConsignmentDetails()){
                consignmentDetails.getKoi().setConsignment(true);
                if(consignment.getType()==Type.ONLINE) {
                    consignmentDetails.getKoi().setDeleted(false);
                }
            }
        }
        else if(consignment.getStatus()==Status.DECLINED){
            Payment payment = new Payment();
            payment.setConsignment(consignment);
            payment.setCreateAt(new Date());
            payment.setMethod(PaymentEnums.BANKING);
        }
        return null;
    }
}
