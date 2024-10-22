package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.*;
import com.example.demo.model.Response.*;
import com.example.demo.repository.*;
import com.example.demo.util.DateUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.pulsar.PulsarProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private OrderRepository orderRepository;

    @Autowired
    private CertificateService certificateService;


    @Autowired
    private KoiService koiService;
    @Autowired
    private EmailService emailService;

    /**
     * Creates a new Consignment based on the provided request.
     *
     * @param consignmentRequest The request containing consignment details.
     * @return The newly created Consignment entity.
     */
    public Consignment createConsignment(ConsignmentRequest consignmentRequest) throws Exception {
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
            if(careTypeRepository.findByCareTypeName("Phi Ky Gui Online") == null){
                throw new NotFoundException("Chua Co Phi Ky Gui Online O CareType");
            }else {
                CareType careType = careTypeRepository.findByCareTypeName("Phi Ky Gui Online");
                consignment.setCareType(careType);
            }
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


            Koi koi = createConsignmentKoi(consignmentDetailRequest.getKoiRequest());
            //Certificate certificate = certificateService.createCertificates(koi);
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
    public List<ConsignmentResponse> getAllConsignments() {

        List<Consignment> consignmentsList = consignmentRepository.findByIsDeletedFalse();
        List<ConsignmentResponse> consignmentResponseList = new ArrayList<>();
        for(Consignment consignment : consignmentsList){
            ConsignmentResponse consignmentResponse = mapToConsignmentResponse(consignment);
            consignmentResponseList.add(consignmentResponse);
        }
        return consignmentResponseList;
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



    public List<ConsignmentResponse> getStaffConsignments() {
        List<Consignment> consignmentList = consignmentRepository.findByStaff_Id(authenticationService.getCurrentAccount().getId());
        List<ConsignmentResponse> consignmentResponseList = new ArrayList<>();
        for(Consignment consignment : consignmentList){
            ConsignmentResponse consignmentResponse = mapToConsignmentResponse(consignment);
            consignmentResponseList.add(consignmentResponse);
        }
        return consignmentResponseList;
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
            Account manager = accountRepository.findAccountByRole(Role.MANAGER);
            transaction2.setFrom(customer);
            transaction2.setTo(manager);
            transaction2.setPayment(payment);
            transaction2.setStatus(TransactionEnum.SUCCESS);
            if(consignment.getType() == Type.OFFLINE){
                transaction2.setDescription("CHUYEN PHI KY GUI OFFLINE VE MANAGER");
            }else{
                transaction2.setDescription("CHUYEN PHI KY GUI ONLINE VE MANAGER");
            }
            double newBalance = manager.getBalance() + consignment.getCost();
            manager.setBalance(newBalance);
            transactions.add(transaction2);
            payment.setTransactions(transactions);

            accountRepository.save(manager);
            paymentRepository.save(payment);
            consignment.setStatus(Status.PAID);
            consignmentRepository.save(consignment);
            emailService.sendConsignmentBillEmail(consignment,consignment.getAccount().getEmail());
        }
    public Koi createConsignmentKoi(KoiRequest koiLotRequest) {
        try {
            // Create a new KoiLot manually without using ModelMapper for the List<Breed>
            Koi koiLot = new Koi();
            koiLot.setName(koiLotRequest.getName());
            koiLot.setPrice(koiLotRequest.getPrice());
            koiLot.setVendor(koiLotRequest.getVendor());
            koiLot.setGender(koiLotRequest.getGender());
            koiLot.setBornYear(koiLotRequest.getBornYear());
            koiLot.setSize(koiLotRequest.getSize());
            koiLot.setOrigin(koiLotRequest.getOrigin());
            koiLot.setDescription(koiLotRequest.getDescription());
            koiLot.setQuantity(koiLotRequest.getQuantity());
            koiLot.setImages(koiLotRequest.getImageUrl());
            koiLot.setDeleted(true);

            List<Images> imagesList = koiLotRequest.getImagesList().stream().map(imageListRequest -> {
                Images image = new Images();
                image.setImages(imageListRequest.getImage());
                image.setKoi(koiLot);  // Associate the image with the koi
                return image;
            }).collect(Collectors.toList());

            koiLot.setImagesList(imagesList);

            // Map breed IDs to Breed entities
            Set<Breed> breeds = new HashSet<>();
            for (Long breedId : koiLotRequest.getBreedId()) {
                Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
                if (breed == null) throw new NotFoundException("Breed not exist");
                breeds.add(breed);
            }
            koiLot.setBreeds(breeds);

            // Set the account of the creator (authenticated user)
            Account accountRequest = authenticationService.getCurrentAccount();
            koiLot.setAccount(accountRequest);
            if (accountRequest.getRole() == Role.CUSTOMER) {
                koiLot.setDeleted(true);
            }

            // Save the KoiLot entity
            return koiLotRepository.save(koiLot);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create KoiLot");
        }
    }

    public Consignment updateConsignmentStatusByStaff(long id, Status status) {
        Consignment consignment = consignmentRepository.findConsignmentById(id);
        consignment.setStatus(status);
        if(consignment.getStatus()==Status.CONFIRMED) {
            for (ConsignmentDetails consignmentDetails : consignment.getConsignmentDetails()) {
                consignmentDetails.getKoi().setConsignment(true);
                if (consignment.getType() == Type.ONLINE) {
                    consignmentDetails.getKoi().setDeleted(false);
                }
            }
        }
        else if(consignment.getStatus()==Status.DECLINED){
            Payment payment = new Payment();
            payment.setConsignment(consignment);
            payment.setCreateAt(new Date());
            payment.setMethod(PaymentEnums.BANKING);

            Transactions transactions1 = new Transactions();
            Account manager = accountRepository.findAccountByRole(Role.MANAGER);

            transactions1.setFrom(manager);
            transactions1.setTo(consignment.getAccount());
            transactions1.setPayment(payment);
            transactions1.setStatus(TransactionEnum.SUCCESS);
            transactions1.setDescription("Refund to the customer");

        }
        return consignmentRepository.save(consignment);
    }
public List<KoiOnlineConsignmentResponse> getAllOnlineKoi() {
    List<KoiOnlineConsignmentResponse> responses = new ArrayList<>();
    for (Koi koi : koiLotRepository.findAllKoiByAccountIdAndConsignmentType(authenticationService.getCurrentAccount().getId(),Type.ONLINE)) {
        KoiOnlineConsignmentResponse response = new KoiOnlineConsignmentResponse();
        response.setId(koiLotRepository.findConsignmentByKoiId(koi.getId()).getId());
        response.setName(koi.getName());
        response.setPrice(koi.getPrice());
        if (koi.isConsignment() == true && koi.isSold() == true) {
            response.setStatus("Sold");
        } else if (koi.isConsignment() == true && koi.isSold() == false) {
            response.setStatus("Not Sold");
        } else {
            response.setStatus("Not Accepted");
        }
        response.setImgUrl(koi.getImages());
        responses.add(response);
    }
    return responses;
}
    public List<KoiOfflineConsignmentResponse> getAllOfflineKoi(){
        List<KoiOfflineConsignmentResponse> responses = new ArrayList<>();
        for(Koi koi : koiLotRepository.findAllKoiByAccountIdAndConsignmentType(authenticationService.getCurrentAccount().getId(),Type.OFFLINE)){
            KoiOfflineConsignmentResponse response = new KoiOfflineConsignmentResponse();
            response.setId(koiLotRepository.findConsignmentByKoiId(koi.getId()).getId());
            response.setEndDate(koiLotRepository.findConsignmentByKoiId(koi.getId()).getEndDate());
            response.setImgUrl(koi.getImages());
            if(koi.isConsignment()==true) {
                response.setIsConsignment("Consigned");
            }else{
                response.setIsConsignment("Not Consigned");
            }
            responses.add(response);
        }
        return responses;
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
        // Assuming imagesList is not empty and contains the main image URL
        if (koi.getImagesList() != null && !koi.getImagesList().isEmpty()) {
            detailResponse.setImageUrl(koi.getImages()); // Adjust to get the correct URL
        }
        return detailResponse;
    }
    public ConsignmentResponse mapToConsignmentResponse(Consignment consignment) {
        ConsignmentResponse response = new ConsignmentResponse();
        response.setConsignmentID(consignment.getId());
        response.setType(consignment.getType().toString());
        response.setAddress(consignment.getAddress());
        response.setDescription(consignment.getDescription());
        response.setCost(String.valueOf(consignment.getCost())); // Adjust as per your currency formatting
        response.setStartDate(consignment.getStartDate());
        response.setEndDate(consignment.getEndDate());
        response.setCreateDate(consignment.getCreateDate());
        response.setStatus(consignment.getStatus().toString());
        response.setCareTypeName(consignment.getCareType() != null ? consignment.getCareType().getCareTypeName() : "N/A");
        response.setStaffid(consignment.getStaff() != null ? consignment.getStaff().getId() : 0); // Get staff ID
        // Map the consignment details (Koi) to the response list
        List<ConsignmentDetailResponse> details = consignment.getConsignmentDetails().stream()
                .map(consignmentDetail -> mapToConsignmentDetailResponse(consignmentDetail.getKoi()))
                .collect(Collectors.toList());

        response.setDetails(details);
        return response;
    }

}
