package com.hardware.shop.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hardware.shop.common.BizException;
import com.hardware.shop.common.Dict;
import com.hardware.shop.entity.Customer;
import com.hardware.shop.repository.CustomerRepository;

@Service
public class CustomerService {

    private static final Pattern PHONE = Pattern.compile(Dict.PHONE_PATTERN);

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public static class CustomerInput {
        public String name;
        public String phone;
        public String memberLevel;
    }

    public List<Customer> list(String memberLevel) {
        if (memberLevel != null) {
            return repo.findByMemberLevel(memberLevel);
        }
        return repo.findAll();
    }

    public Customer get(Long id) {
        return repo.findById(id).orElseThrow(() -> new BizException("客户不存在"));
    }

    @Transactional
    public Customer create(CustomerInput in) {
        if (in.name == null || in.name.isBlank()) {
            throw new BizException("客户名不能为空");
        }
        if (in.phone == null || !PHONE.matcher(in.phone).matches()) {
            throw new BizException("手机号格式应为 1 开头的 11 位号码");
        }
        if (repo.existsByPhone(in.phone)) {
            throw new BizException("手机号「" + in.phone + "」已存在");
        }
        if (!Dict.isValidMemberLevel(in.memberLevel)) {
            throw new BizException("客户等级必须是：普通/批发");
        }
        Customer c = new Customer();
        c.name = in.name;
        c.phone = in.phone;
        c.memberLevel = in.memberLevel;
        return repo.save(c);
    }

    @Transactional
    public Customer update(Long id, CustomerInput in) {
        Customer c = repo.findById(id).orElseThrow(() -> new BizException("客户不存在"));
        if (in.name != null) {
            if (in.name.isBlank()) {
                throw new BizException("客户名不能为空");
            }
            c.name = in.name;
        }
        if (in.phone != null) {
            if (!PHONE.matcher(in.phone).matches()) {
                throw new BizException("手机号格式应为 1 开头的 11 位号码");
            }
            if (!in.phone.equals(c.phone) && repo.existsByPhone(in.phone)) {
                throw new BizException("手机号「" + in.phone + "」已存在");
            }
            c.phone = in.phone;
        }
        if (in.memberLevel != null) {
            if (!Dict.isValidMemberLevel(in.memberLevel)) {
                throw new BizException("客户等级必须是：普通/批发");
            }
            c.memberLevel = in.memberLevel;
        }
        return repo.save(c);
    }
}
