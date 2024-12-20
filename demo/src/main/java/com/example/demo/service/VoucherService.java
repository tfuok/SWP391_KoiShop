
package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Voucher;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.VoucherRequest;
import com.example.demo.repository.VoucherRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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
            return voucherRepository.save(voucher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Voucher deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findVoucherById(id);
        if (voucher == null) throw new NotFoundException("Voucher not found!");
        voucher.setDeleted(true);
        return voucherRepository.save(voucher);
    }

    public List<Voucher> getAllVoucher() {
        Date currentDate = new Date();
        return voucherRepository.findActiveVouchers(currentDate);
    }

    public Voucher updateVoucher(VoucherRequest voucherRequest, long id){
        Voucher voucher = voucherRepository.findVoucherById(id);
        if (voucher == null) throw new NotFoundException("Voucher not found!");
        voucher.setDiscountValue(voucherRequest.getDiscountValue());
        voucher.setExpiredDate(voucherRequest.getExpiredDate());
        voucher.setQuantity(voucherRequest.getQuantity());
        voucher.setCode(voucher.getCode());
        voucher.setName(voucherRequest.getName());
        return voucherRepository.save(voucher);
    }
}
