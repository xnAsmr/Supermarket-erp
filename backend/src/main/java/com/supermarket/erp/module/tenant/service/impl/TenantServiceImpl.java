package com.supermarket.erp.module.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.auth.mapper.UserMapper;
import com.supermarket.erp.module.tenant.dto.TenantCreateDTO;
import com.supermarket.erp.module.tenant.entity.Store;
import com.supermarket.erp.module.tenant.entity.Tenant;
import com.supermarket.erp.module.tenant.mapper.StoreMapper;
import com.supermarket.erp.module.tenant.mapper.TenantMapper;
import com.supermarket.erp.module.tenant.service.ITenantService;
import com.supermarket.erp.module.tenant.vo.TenantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements ITenantService {

    private final TenantMapper tenantMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<TenantVO> pageList(Integer page, Integer pageSize) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getDeleted, 0);
        wrapper.orderByDesc(Tenant::getCreateTime);

        Page<Tenant> tenantPage = new Page<>(page, pageSize);
        Page<Tenant> result = tenantMapper.selectPage(tenantPage, wrapper);

        List<Long> tenantIds = result.getRecords().stream()
                .map(Tenant::getId)
                .collect(Collectors.toList());

        Map<Long, Long> storeCountMap = Map.of();
        if (!tenantIds.isEmpty()) {
            LambdaQueryWrapper<Store> storeWrapper = new LambdaQueryWrapper<>();
            storeWrapper.in(Store::getTenantId, tenantIds);
            storeWrapper.eq(Store::getDeleted, 0);
            List<Store> stores = storeMapper.selectList(storeWrapper);
            storeCountMap = stores.stream()
                    .collect(Collectors.groupingBy(Store::getTenantId, Collectors.counting()));
        }

        final Map<Long, Long> countMap = storeCountMap;
        List<TenantVO> voList = result.getRecords().stream()
                .map(tenant -> {
                    TenantVO vo = toVO(tenant);
                    vo.setStoreCount(countMap.getOrDefault(tenant.getId(), 0L).intValue());
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public TenantVO getById(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(6001, "租户不存在");
        }
        TenantVO vo = toVO(tenant);

        LambdaQueryWrapper<Store> storeWrapper = new LambdaQueryWrapper<>();
        storeWrapper.eq(Store::getTenantId, id);
        storeWrapper.eq(Store::getDeleted, 0);
        Long storeCount = storeMapper.selectCount(storeWrapper);
        vo.setStoreCount(storeCount.intValue());

        return vo;
    }

    @Override
    public Long create(TenantCreateDTO dto) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(dto.getTenantName());
        tenant.setContactName(dto.getContactName());
        tenant.setContactPhone(dto.getContactPhone());
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setAddress(dto.getAddress());
        tenant.setLicenseNo(dto.getLicenseNo());
        tenant.setPlanType(dto.getPlanType());
        tenant.setMaxStores(dto.getMaxStores());
        tenant.setMaxUsers(dto.getMaxUsers());
        tenant.setRemark(dto.getRemark());
        tenant.setExpireTime(dto.getExpireTime() != null && !dto.getExpireTime().isBlank()
                ? parseDateTime(dto.getExpireTime())
                : LocalDateTime.now().plusYears(1));
        tenant.setTenantNo("T" + System.currentTimeMillis());
        tenant.setStatus(1);
        tenantMapper.insert(tenant);
        return tenant.getId();
    }

    @Override
    public void update(Long id, TenantCreateDTO dto) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(6001, "租户不存在");
        }
        tenant.setTenantName(dto.getTenantName());
        tenant.setContactName(dto.getContactName());
        tenant.setContactPhone(dto.getContactPhone());
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setAddress(dto.getAddress());
        tenant.setLicenseNo(dto.getLicenseNo());
        tenant.setPlanType(dto.getPlanType());
        tenant.setMaxStores(dto.getMaxStores());
        tenant.setMaxUsers(dto.getMaxUsers());
        tenant.setRemark(dto.getRemark());
        if (dto.getExpireTime() != null) {
            tenant.setExpireTime(parseDateTime(dto.getExpireTime()));
        }
        tenantMapper.updateById(tenant);
    }

    @Override
    public void updateCurrent(Long id, TenantCreateDTO dto) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(6001, "租户不存在");
        }
        // 租户端仅允许修改基本信息，套餐/额度/到期时间由系统管理员维护
        tenant.setTenantName(dto.getTenantName());
        tenant.setContactName(dto.getContactName());
        tenant.setContactPhone(dto.getContactPhone());
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setAddress(dto.getAddress());
        tenant.setRemark(dto.getRemark());
        tenantMapper.updateById(tenant);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(6001, "租户不存在");
        }
        tenant.setStatus(status);
        tenantMapper.updateById(tenant);
    }

    @Override
    public void delete(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new BusinessException(6001, "租户不存在");
        }
        tenantMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 租户总数
        Long totalTenants = tenantMapper.selectCount(
                new LambdaQueryWrapper<Tenant>().eq(Tenant::getDeleted, 0));
        // 活跃租户
        Long activeTenants = tenantMapper.selectCount(
                new LambdaQueryWrapper<Tenant>().eq(Tenant::getDeleted, 0).eq(Tenant::getStatus, 1));
        // 门店总数
        Long totalStores = storeMapper.selectCount(
                new LambdaQueryWrapper<Store>().eq(Store::getDeleted, 0));
        // 系统用户总数
        Long totalUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));

        // 套餐类型分布
        List<Tenant> allTenants = tenantMapper.selectList(
                new LambdaQueryWrapper<Tenant>().eq(Tenant::getDeleted, 0));
        Map<Integer, Long> planDist = allTenants.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPlanType() == null ? 0 : t.getPlanType(),
                        Collectors.counting()));
        List<Map<String, Object>> planTypeDistribution = new ArrayList<>();
        planDist.forEach((type, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("planType", type);
            item.put("count", count);
            planTypeDistribution.add(item);
        });

        // 租户状态分布
        Map<Integer, Long> statusDist = allTenants.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus() == null ? 0 : t.getStatus(),
                        Collectors.counting()));
        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        statusDist.forEach((status, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", status);
            item.put("count", count);
            statusDistribution.add(item);
        });

        // 近6个月租户开通趋势
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            Long count = tenantMapper.selectCount(
                    new LambdaQueryWrapper<Tenant>()
                            .eq(Tenant::getDeleted, 0)
                            .ge(Tenant::getCreateTime, monthStart)
                            .lt(Tenant::getCreateTime, monthEnd));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            item.put("count", count);
            monthlyTrend.add(item);
        }

        // 即将到期租户（30天内）
        LocalDateTime expireLimit = now.plusDays(30);
        List<Map<String, Object>> expiringTenants = new ArrayList<>();
        List<Tenant> expiring = tenantMapper.selectList(
                new LambdaQueryWrapper<Tenant>()
                        .eq(Tenant::getDeleted, 0)
                        .eq(Tenant::getStatus, 1)
                        .isNotNull(Tenant::getExpireTime)
                        .between(Tenant::getExpireTime, now, expireLimit)
                        .orderByAsc(Tenant::getExpireTime));
        for (Tenant t : expiring) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", t.getId());
            item.put("tenantName", t.getTenantName());
            item.put("expireTime", t.getExpireTime());
            item.put("planType", t.getPlanType());
            expiringTenants.add(item);
        }

        result.put("totalTenants", totalTenants);
        result.put("activeTenants", activeTenants);
        result.put("totalStores", totalStores);
        result.put("totalUsers", totalUsers);
        result.put("planTypeDistribution", planTypeDistribution);
        result.put("statusDistribution", statusDistribution);
        result.put("monthlyTrend", monthlyTrend);
        result.put("expiringTenants", expiringTenants);
        return result;
    }

    private TenantVO toVO(Tenant tenant) {
        TenantVO vo = new TenantVO();
        BeanUtils.copyProperties(tenant, vo);
        return vo;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
