package com.community.property.config;

import com.community.property.entity.FeeType;
import com.community.property.entity.User;
import com.community.property.service.FeeTypeService;
import com.community.property.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;
    private final FeeTypeService feeTypeService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始初始化数据...");
        
        initAdminUser();
        initPropertyUsers();
        initResidentUsers();
        initFeeTypes();
        
        log.info("数据初始化完成！");
    }

    private void initAdminUser() {
        String adminUsername = "admin";
        User existAdmin = userService.getByUsername(adminUsername);
        
        if (existAdmin == null) {
            log.info("创建管理员账号...");
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setPhone("13800138000");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userService.save(admin);
            log.info("管理员账号创建成功：admin / admin123");
        } else {
            log.info("管理员账号已存在，更新密码为: admin123");
            existAdmin.setPassword(passwordEncoder.encode("admin123"));
            userService.updateById(existAdmin);
        }
    }

    private void initPropertyUsers() {
        List<String> propertyUsers = Arrays.asList("property01", "property02");
        List<String> realNames = Arrays.asList("张物业", "李维修");
        List<String> phones = Arrays.asList("13900139001", "13900139002");

        for (int i = 0; i < propertyUsers.size(); i++) {
            String username = propertyUsers.get(i);
            User existUser = userService.getByUsername(username);

            if (existUser == null) {
                log.info("创建物业人员账号: {}", username);
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRealName(realNames.get(i));
                user.setPhone(phones.get(i));
                user.setRole("PROPERTY");
                user.setStatus(1);
                userService.save(user);
                log.info("物业人员账号创建成功：{} / 123456", username);
            } else {
                log.info("物业人员账号已存在，更新密码: {}", username);
                existUser.setPassword(passwordEncoder.encode("123456"));
                userService.updateById(existUser);
            }
        }
    }

    private void initResidentUsers() {
        List<String> residentUsers = Arrays.asList("resident01", "resident02");
        List<String> realNames = Arrays.asList("王居民", "赵居民");
        List<String> phones = Arrays.asList("13800138001", "13800138002");
        List<String> idCards = Arrays.asList("110101199001011234", "110101199002022345");

        for (int i = 0; i < residentUsers.size(); i++) {
            String username = residentUsers.get(i);
            User existUser = userService.getByUsername(username);

            if (existUser == null) {
                log.info("创建居民账号: {}", username);
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRealName(realNames.get(i));
                user.setPhone(phones.get(i));
                user.setIdCard(idCards.get(i));
                user.setRole("RESIDENT");
                user.setStatus(1);
                userService.save(user);
                log.info("居民账号创建成功：{} / 123456", username);
            } else {
                log.info("居民账号已存在，更新密码: {}", username);
                existUser.setPassword(passwordEncoder.encode("123456"));
                userService.updateById(existUser);
            }
        }
    }

    private void initFeeTypes() {
        long count = feeTypeService.count();
        if (count > 0) {
            log.info("费用类型已存在，跳过初始化");
            return;
        }

        log.info("初始化费用类型...");
        
        FeeType[] feeTypes = {
            createFeeType("物业费", "元/平方米/月", new BigDecimal("2.5"), "物业管理服务费，按房屋面积收取"),
            createFeeType("水费", "元/吨", new BigDecimal("3.5"), "居民生活用水费"),
            createFeeType("电费", "元/度", new BigDecimal("0.6"), "居民生活用电费"),
            createFeeType("停车费", "元/月", new BigDecimal("200.00"), "小区停车月租费"),
            createFeeType("维修费", "元/次", null, "上门维修服务费，按实际情况收取")
        };

        for (FeeType feeType : feeTypes) {
            feeTypeService.save(feeType);
        }
        log.info("费用类型初始化完成");
    }

    private FeeType createFeeType(String name, String unit, BigDecimal price, String description) {
        FeeType feeType = new FeeType();
        feeType.setTypeName(name);
        feeType.setUnit(unit);
        feeType.setPrice(price);
        feeType.setDescription(description);
        feeType.setStatus(1);
        return feeType;
    }
}