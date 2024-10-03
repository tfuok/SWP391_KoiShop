package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Voucher;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.VoucherRequest;
import com.example.demo.repository.VoucherRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoucherService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    VoucherRepository voucherRepository;

    public Voucher createVoucher(VoucherRequest voucherRequest) {
        try {
            Voucher voucher = modelMapper.map(voucherRequest, Voucher.class);
            Account accountRequest = authenticationService.getCurrentAccount();
            voucher.setAccount(accountRequest);
            Voucher newVoucher = voucherRepository.save(voucher);
            return newVoucher;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Voucher deleteVoucher(long id) {
        Voucher voucher = voucherRepository.findVoucherById(id);
        if (voucher == null) throw new NotFoundException("Voucher not found!");
        voucher.setDeleted(true);
        return voucherRepository.save(voucher);
    }

    public List<Voucher> getAllVoucher() {
        List<Voucher> vouchers = voucherRepository.findVoucherByIsDeletedFalse();
        return vouchers;
    }

    public Voucher updateVoucher(VoucherRequest voucherRequest, long id){
        Voucher voucher = voucherRepository.findVoucherById(id);
        if (voucher == null) throw new NotFoundException("Voucher not found!");
        voucher.setDiscountValue(voucherRequest.getDiscountValue());
        voucher.setExpiredDate(voucherRequest.getExpiredDate());
        voucher.setQuantity(voucherRequest.getQuantity());
        voucher.setDescription(voucherRequest.getDescription());
        voucher.setMinimumPoints(voucherRequest.getMinimumPoints());
        voucher.setMinimumPrice(voucherRequest.getMinimumPrice());
        return voucherRepository.save(voucher);
    }
}
