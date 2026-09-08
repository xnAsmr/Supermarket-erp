<template>
  <n-modal
    v-model:show="visible"
    :title="productId ? '编辑商品' : '新增商品'"
    :mask-closable="false"
    preset="card"
    style="width: 650px"
    :segmented="{ content: true, footer: true }"
  >
    <n-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-placement="left"
      label-width="80"
    >
      <n-grid :cols="2" :x-gap="16">
        <n-grid-item :span="2">
          <n-form-item label="商品名称" path="name">
            <n-input v-model:value="formData.name" placeholder="请输入商品名称" />
          </n-form-item>
        </n-grid-item>
        <n-grid-item :span="2">
          <n-form-item label="商品图片" path="image">
            <div class="image-upload-section">
              <div v-if="formData.image" class="image-preview">
                <img :src="formData.image" alt="商品图片" />
                <n-button
                  size="tiny"
                  type="error"
                  circle
                  class="delete-btn"
                  @click="formData.image = ''"
                >
                  <template #icon><n-icon :component="CloseOutline" /></template>
                </n-button>
              </div>
              <div v-else class="image-upload" @click="showImagePicker = true">
                <n-icon size="32" color="#ccc"><ImageOutline /></n-icon>
                <n-text depth="3" style="font-size: 12px; margin-top: 8px">点击添加图片</n-text>
              </div>
            </div>
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="条形码" path="barcode">
            <n-input v-model:value="formData.barcode" placeholder="请输入条形码" />
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="商品分类" path="category">
            <n-select
              v-model:value="formData.categoryId"
              :options="categoryOptions"
              placeholder="请选择分类"
            />
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="售价" path="sellingPrice">
            <n-input-number
              v-model:value="formData.sellingPrice"
              :min="0"
              :precision="2"
              placeholder="0.00"
              style="width: 100%"
            />
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="会员价" path="memberPrice">
            <n-input-number
              v-model:value="formData.memberPrice"
              :min="0"
              :precision="2"
              placeholder="0.00"
              style="width: 100%"
            />
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="进货价" path="purchasePrice">
            <n-input-number
              v-model:value="formData.purchasePrice"
              :min="0"
              :precision="2"
              placeholder="0.00"
              style="width: 100%"
            />
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="单位" path="unit">
            <n-select
              v-model:value="formData.unit"
              :options="unitOptions"
              placeholder="请选择单位"
            />
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="规格" path="specification">
            <n-input v-model:value="formData.specification" placeholder="如：500g/袋" />
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="品牌" path="brandId">
            <n-select
              v-model:value="formData.brandId"
              :options="brandOptions"
              :loading="loadingBrands"
              placeholder="请选择品牌"
              clearable
            />
          </n-form-item>
        </n-grid-item>
        <n-grid-item>
          <n-form-item label="状态" path="status">
            <n-select
              v-model:value="formData.status"
              :options="statusOptions"
            />
          </n-form-item>
        </n-grid-item>
      </n-grid>
    </n-form>

    <!-- 图片选择弹窗 -->
    <n-modal
      v-model:show="showImagePicker"
      preset="card"
      title="上传商品图片"
      style="width: 450px"
    >
      <n-alert type="info" style="margin-bottom: 16px">
        <template #header>图片上传说明</template>
        <n-space vertical>
          <n-text>1. 支持 JPG、PNG、GIF 格式，建议尺寸 400x400</n-text>
          <n-text>2. 文件大小不超过 2MB</n-text>
          <n-text>3. 上传后图片将保存在服务器 /upload 目录下</n-text>
        </n-space>
      </n-alert>

      <div class="upload-area">
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleFileChange"
        />
        <div v-if="!uploadPreview" class="upload-box" @click="fileInputRef?.click()">
          <n-icon size="48" color="#ccc"><CloudUploadOutline /></n-icon>
          <n-text depth="3" style="margin-top: 12px; font-size: 14px">点击选择图片</n-text>
          <n-text depth="3" style="font-size: 12px; margin-top: 4px">
            支持 JPG、PNG、GIF，最大 2MB
          </n-text>
        </div>
        <div v-else class="upload-preview">
          <img :src="uploadPreview" alt="预览" />
          <n-space justify="center" style="margin-top: 12px">
            <n-button size="small" @click="fileInputRef?.click()">重新选择</n-button>
            <n-button size="small" type="error" @click="clearUploadPreview">删除</n-button>
          </n-space>
        </div>
      </div>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showImagePicker = false">取消</n-button>
          <n-button
            type="primary"
            :loading="uploading"
            :disabled="!selectedFile"
            @click="handleConfirmImage"
          >
            确认使用
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <template #footer>
      <n-space justify="end">
        <n-button @click="visible = false">取消</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import {
  NModal, NForm, NFormItem, NInput, NInputNumber, NSelect,
  NButton, NSpace, NGrid, NGridItem, NIcon, NText, NAlert, useMessage
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { CloseOutline, ImageOutline, CloudUploadOutline } from '@vicons/ionicons5'
import { getProduct, createProduct, updateProduct, getCategoryTree, uploadImage } from '../../../api/product'
import type { BackendCategory } from '../../../api/product'
import { getBrands } from '../../../api/brand'
import type { BackendBrand } from '../../../api/brand'

const props = defineProps<{
  show: boolean
  productId?: number
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'success'): void
}>()

const message = useMessage()
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const visible = ref(props.show)

watch(() => props.show, (val) => {
  visible.value = val
  if (val && props.productId) {
    loadProduct()
  } else if (val) {
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:show', val)
})

const formData = reactive({
  name: '',
  barcode: '',
  categoryId: undefined as number | undefined,
  category: '',
  sellingPrice: 0,
  memberPrice: 0,
  purchasePrice: 0,
  unit: '件',
  specification: '',
  brandId: undefined as number | undefined,
  status: 1,
  image: ''
})

// 图片相关
const showImagePicker = ref(false)
const uploadPreview = ref('')
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const originalImage = ref('')

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  barcode: [{ required: true, message: '请输入条形码', trigger: 'blur' }],
  categoryId: [{ type: 'number', required: true, message: '请选择商品分类', trigger: 'change' }],
  sellingPrice: [{ type: 'number', required: true, message: '请输入售价', trigger: 'blur' }]
}

const categoryOptions = ref<{ label: string; value: number }[]>([])
const unitOptions = [
  { label: '件', value: '件' },
  { label: '袋', value: '袋' },
  { label: '瓶', value: '瓶' },
  { label: '罐', value: '罐' },
  { label: '盒', value: '盒' },
  { label: '桶', value: '桶' },
  { label: '份', value: '份' },
  { label: '条', value: '条' },
  { label: '个', value: '个' }
]
const statusOptions = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 }
]

const brandOptions = ref<{ label: string; value: number }[]>([])
const loadingBrands = ref(false)

async function loadBrands() {
  loadingBrands.value = true
  try {
    const res = await getBrands({ page: 1, pageSize: 500 })
    brandOptions.value = res.list.map((b: BackendBrand) => ({ label: b.name, value: b.id }))
  } catch {
    brandOptions.value = []
  } finally {
    loadingBrands.value = false
  }
}

function resetForm() {
  formData.name = ''
  formData.barcode = ''
  formData.categoryId = undefined
  formData.category = ''
  formData.sellingPrice = 0
  formData.memberPrice = 0
  formData.purchasePrice = 0
  formData.unit = '件'
  formData.specification = ''
  formData.brandId = undefined
  formData.status = 1
  formData.image = ''
  originalImage.value = ''
}

async function loadProduct() {
  if (!props.productId) return
  const res = await getProduct(props.productId)
  Object.assign(formData, {
    name: res.name,
    barcode: res.barcode,
    categoryId: res.categoryId,
    category: res.categoryName,
    sellingPrice: res.salePrice,
    memberPrice: res.vipPrice ?? 0,
    purchasePrice: res.purchasePrice,
    unit: res.unit,
    specification: res.spec ?? '',
    brandId: res.brandId ?? undefined,
    status: res.status,
    image: res.image || ''
  })
  originalImage.value = res.image || ''
}

async function loadCategories() {
  const res = await getCategoryTree()
  const options: { label: string; value: number }[] = []
  res.forEach((cat: BackendCategory) => {
    if (cat.children) {
      cat.children.forEach((child: BackendCategory) => {
        options.push({ label: child.name, value: child.id })
      })
    }
  })
  categoryOptions.value = options
}

function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    message.warning('图片大小不能超过2MB')
    return
  }

  if (!file.type.startsWith('image/')) {
    message.warning('请选择图片文件')
    return
  }

  selectedFile.value = file
  const reader = new FileReader()
  reader.onload = (event) => {
    uploadPreview.value = event.target?.result as string
  }
  reader.readAsDataURL(file)
}

function clearUploadPreview() {
  uploadPreview.value = ''
  selectedFile.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

async function handleConfirmImage() {
  if (!selectedFile.value) return
  uploading.value = true
  try {
    const url = await uploadImage(selectedFile.value)
    formData.image = url
    showImagePicker.value = false
    uploadPreview.value = ''
    selectedFile.value = null
  } catch (err: any) {
    message.error(err?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const payload: Record<string, unknown> = {
      name: formData.name,
      barcode: formData.barcode,
      categoryId: formData.categoryId,
      brandId: formData.brandId || null,
      salePrice: formData.sellingPrice,
      vipPrice: formData.memberPrice || null,
      purchasePrice: formData.purchasePrice,
      unit: formData.unit,
      spec: formData.specification || null,
      image: formData.image || null
    }

    if (props.productId) {
      payload.oldImage = originalImage.value || null
      await updateProduct(props.productId, payload)
      message.success('更新成功')
    } else {
      await createProduct(payload)
      message.success('创建成功')
    }
    visible.value = false
    emit('success')
  } catch (err: any) {
    message.error(err?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

watch(() => props.show, (val) => {
  if (val) {
    loadCategories()
    loadBrands()
  }
}, { immediate: true })
</script>

<style scoped>
.image-upload-section {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.image-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #eee;
  flex-shrink: 0;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview .delete-btn {
  position: absolute;
  top: 4px;
  right: 4px;
}

.image-upload {
  width: 120px;
  height: 120px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.image-upload:hover {
  border-color: #18a058;
  background: #f6ffed;
}

/* 上传区域 */
.upload-area {
  min-height: 200px;
}

.upload-box {
  width: 100%;
  height: 180px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-box:hover {
  border-color: #18a058;
  background: #f6ffed;
}

.upload-preview {
  text-align: center;
}

.upload-preview img {
  max-width: 100%;
  max-height: 250px;
  border-radius: 8px;
  border: 1px solid #eee;
}
</style>
