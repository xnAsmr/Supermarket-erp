package com.supermarket.erp.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.product.dto.ProductCreateDTO;
import com.supermarket.erp.module.product.dto.ProductQueryDTO;
import com.supermarket.erp.module.product.entity.Category;
import com.supermarket.erp.module.product.entity.Product;
import com.supermarket.erp.module.product.mapper.CategoryMapper;
import com.supermarket.erp.module.product.mapper.ProductMapper;
import com.supermarket.erp.module.product.service.IProductService;
import com.supermarket.erp.module.product.vo.ProductVO;
import com.supermarket.erp.module.stock.entity.Stock;
import com.supermarket.erp.module.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final StockMapper stockMapper;

    @Value("${file.upload-dir:../uploads}")
    private String uploadDir;

    @Override
    public ProductVO getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(2001, "商品不存在");
        }
        return toVO(product);
    }

    @Override
    public PageResult<ProductVO> pageList(ProductQueryDTO query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0);

        if (query.getTenantId() != null) {
            wrapper.eq(Product::getTenantId, query.getTenantId());
        }

        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w
                    .like(Product::getName, kw)
                    .or().like(Product::getPinyin, kw)
                    .or().eq(Product::getBarcode, kw)
            );
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, query.getCategoryId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Product::getStatus, query.getStatus());
        }
        if (query.getIsPromotion() != null) {
            wrapper.eq(Product::getIsPromotion, query.getIsPromotion());
        }

        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> page = new Page<>(query.getPage(), query.getPageSize());
        Page<Product> result = productMapper.selectPage(page, wrapper);

        List<Long> categoryIds = result.getRecords().stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> categoryNameMap = Map.of();
        if (!categoryIds.isEmpty()) {
            categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
        }

        Map<Long, String> finalCategoryNameMap = categoryNameMap;
        List<ProductVO> voList = result.getRecords().stream()
                .map(p -> {
                    ProductVO vo = toVO(p);
                    if (vo.getCategoryId() != null) {
                        vo.setCategoryName(finalCategoryNameMap.getOrDefault(vo.getCategoryId(), ""));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public List<ProductVO> searchForPos(String keyword, Long tenantId) {
        List<Product> products = productMapper.searchByKeyword(keyword, tenantId);
        return products.stream().map(p -> {
            ProductVO vo = toVO(p);
            setStockInfo(vo, tenantId, p.getId());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getHotProducts(Long tenantId, Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0)
                .eq(Product::getTenantId, tenantId)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSaleCount)
                .last("LIMIT " + limit);
        return productMapper.selectList(wrapper).stream().map(p -> {
            ProductVO vo = toVO(p);
            setStockInfo(vo, tenantId, p.getId());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Long create(ProductCreateDTO dto, Long tenantId, Long userId) {
        Product existing = productMapper.selectByBarcode(dto.getBarcode(), tenantId);
        if (existing != null) {
            throw new BusinessException(2002, "商品条码已存在");
        }

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setTenantId(tenantId);
        product.setProductNo("P" + System.currentTimeMillis());
        product.setPinyin(toPinyin(dto.getName()));
        product.setStatus(1);
        product.setSaleCount(0L);
        product.setCreateBy(userId);
        product.setUpdateBy(userId);
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    public void update(Long id, ProductCreateDTO dto, Long tenantId, Long userId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(2001, "商品不存在");
        }

        Product check = productMapper.selectByBarcode(dto.getBarcode(), tenantId);
        if (check != null && !check.getId().equals(id)) {
            throw new BusinessException(2002, "商品条码已存在");
        }

        String oldImage = product.getImage();
        String newImage = dto.getImage();

        BeanUtils.copyProperties(dto, product);
        product.setPinyin(toPinyin(dto.getName()));
        product.setUpdateBy(userId);
        productMapper.updateById(product);

        if (newImage == null || newImage.isBlank()) {
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            wrapper.eq(Product::getId, id).set(Product::getImage, null);
            productMapper.update(null, wrapper);
        }

        deleteOldFileIfChanged(oldImage, newImage);
    }

    @Override
    public void delete(Long id, Long tenantId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(2001, "商品不存在");
        }
        productMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status, Long tenantId) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(2001, "商品不存在");
        }
        product.setStatus(status);
        productMapper.updateById(product);
    }

    private ProductVO toVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    private void setStockInfo(ProductVO vo, Long tenantId, Long productId) {
        try {
            LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Stock::getTenantId, tenantId)
                    .eq(Stock::getProductId, productId);
            List<Stock> stocks = stockMapper.selectList(wrapper);
            int totalWarehouse = stocks.stream()
                    .mapToInt(s -> s.getWarehouseQuantity() != null ? s.getWarehouseQuantity().intValue() : 0)
                    .sum();
            int totalShelf = stocks.stream()
                    .mapToInt(s -> s.getShelfQuantity() != null ? s.getShelfQuantity().intValue() : 0)
                    .sum();
            vo.setWarehouseQuantity(totalWarehouse);
            vo.setShelfQuantity(totalShelf);
        } catch (Exception e) {
            log.warn("查询库存失败, productId={}: {}", productId, e.getMessage());
            vo.setWarehouseQuantity(0);
            vo.setShelfQuantity(0);
        }
    }

    private String toPinyin(String name) {
        if (name == null || name.isBlank()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                sb.append(Character.toLowerCase(c));
            } else if (c >= '\u4e00' && c <= '\u9fff') {
                sb.append(pinYinOf(c));
            }
        }
        return sb.toString();
    }

    private String pinYinOf(char c) {
        String[] array = new String[]{
                "a","ai","an","ang","ao","ba","bai","ban","bang","bao","bei","ben","beng","bi","bian","biao",
                "bie","bin","bing","bo","bu","ca","cai","can","cang","cao","ce","cen","ceng","cha","chai","chan",
                "chang","chao","che","chen","cheng","chi","chong","chou","chu","chua","chuai","chuan","chuang",
                "chui","chun","chuo","ci","cong","cou","cu","cuan","cui","cun","cuo","da","dai","dan","dang",
                "dao","de","deng","di","dian","diao","die","ding","diu","dong","dou","du","duan","dui","dun","duo",
                "e","en","er","fa","fan","fang","fei","fen","feng","fo","fou","fu","ga","gai","gan","gang","gao",
                "ge","gei","gen","geng","gong","gou","gu","gua","guai","guan","guang","gui","gun","guo","ha","hai",
                "han","hang","hao","he","hei","hen","heng","hong","hou","hu","hua","huai","huan","huang","hui",
                "hun","huo","ji","jia","jian","jiang","jiao","jie","jin","jing","jiong","jiu","ju","juan","jue",
                "jun","ka","kai","kan","kang","kao","ke","ken","keng","kong","kou","ku","kua","kuai","kuan",
                "kuang","kui","kun","kuo","la","lai","lan","lang","lao","le","lei","leng","li","lian","liang",
                "liao","lie","lin","ling","liu","long","lou","lu","luan","lun","luo","lv","ma","mai","man","mang",
                "mao","me","mei","men","meng","mi","mian","miao","mie","min","ming","miu","mo","mou","mu","na",
                "nai","nan","nang","nao","ne","nei","nen","neng","ni","nian","niang","niao","nie","nin","ning",
                "niu","nong","nou","nu","nuan","nuo","nv","o","ou","pa","pai","pan","pang","pao","pei","pen",
                "peng","pi","pian","piao","pie","pin","ping","po","pu","qi","qia","qian","qiang","qiao","qie",
                "qin","qing","qiong","qiu","qu","quan","que","qun","ran","rang","rao","re","ren","reng","ri",
                "rong","rou","ru","ruan","rui","run","ruo","sa","sai","san","sang","sao","se","sen","seng","sha",
                "shai","shan","shang","shao","she","shei","shen","sheng","shi","shou","shu","shua","shuai","shuan",
                "shuang","shui","shun","shuo","si","song","sou","su","suan","sui","sun","suo","ta","tai","tan",
                "tang","tao","te","teng","ti","tian","tiao","tie","ting","tong","tou","tu","tuan","tui","tun",
                "tuo","wa","wai","wan","wang","wei","wen","weng","wo","wu","xi","xia","xian","xiang","xiao",
                "xie","xin","xing","xiong","xiu","xu","xuan","xue","xun","ya","yan","yang","yao","ye","yi",
                "yin","ying","yong","you","yu","yuan","yue","yun","za","zai","zan","zang","zao","ze","zei","zen",
                "zeng","zha","zhai","zhan","zhang","zhao","zhe","zhen","zheng","zhi","zhong","zhou","zhu","zhua",
                "zhuai","zhuan","zhuang","zhui","zhun","zhuo","zi","zong","zou","zu","zuan","zui","zun","zuo"
        };
        int idx = c - '\u4e00';
        if (idx >= 0 && idx < array.length) return array[idx];
        return String.valueOf(c);
    }

    private void deleteOldFileIfChanged(String oldImage, String newImage) {
        if (oldImage == null || oldImage.isBlank()) return;
        if (oldImage.equals(newImage)) return;

        try {
            String relativePath = oldImage.replaceFirst("^/api/v1/upload/file/", "");
            java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, relativePath).toAbsolutePath().normalize();
            if (java.nio.file.Files.exists(filePath)) {
                java.nio.file.Files.delete(filePath);
                log.info("已删除旧图片: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("删除旧图片失败: {}", oldImage, e);
        }
    }
}
